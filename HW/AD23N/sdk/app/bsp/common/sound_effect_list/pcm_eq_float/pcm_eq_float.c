#include "pcm_eq_api.h"
#include "pcm_eq_float/pcm_eq_float.h"
#include "fix_iir_filter_api.h"
#include "decoder_api.h"
#include "config.h"
#include "sound_effect_api.h"
#include "remain_output.h"
#include "app_modules.h"
#include "mmu/malloc.h"
#include "my_malloc.h"


#define LOG_TAG_CONST       NORM
#define LOG_TAG             "[eq]"
#include "log.h"

#if PCM_EQ_EN


#define EQ_READSIZE  128 //EQ算法每次运行输入的点数
const int iir_filter_run_mode = 1; //1：16in16out   1<<1：16in32out   1<<3：32in32out

#define EQ_INBUF_SIZE  (388)
#define EQ_OBUF_SIZE   (1024)


typedef struct _PCM_EQ_IO_CONTEXT_ {
    void *priv;
    int(*output)(void *priv, void *data, int len);
} PCM_EQ_IO_CONTEXT;

typedef struct _PCM_EQ_HDL {
    EFFECT_OBJ obj;
    sound_in_obj si;
    PCM_EQ_IO_CONTEXT io;
    remain_ops eq_float_remain_ops;
    struct iir_filter_param iir_parm;
    EQ_COEFF_BUFF buff; //用于将EQ参数算出EQ系数
    u32 eq_float_workbuf[EQ_INBUF_SIZE / 4];
    u32 eq_f_obuf[EQ_OBUF_SIZE / 4];
} PCM_EQ_HDL;

void *pcm_eq_float_phy(void *obuf, PCM_EQ_HDL *p_eq_hdl, void **ppsound);
float *get_eq_coeff_tab(u32 sr, EQ_COEFF_BUFF *ptr);

#define EQ_F_MAX_MALLOC_CNT  20
static u8 s_eq_f_hdl_mcnt = 0;

static void dump_eq_f_hdl_mcnt()
{
    log_info("s_eq_f_hdl_mcnt %d\n", s_eq_f_hdl_mcnt);
}
const struct mcnt_operations eq_f_hdl_mcnt sec_used(.d_malloc_cnt) = {
    .malloc_cnt_dump = dump_eq_f_hdl_mcnt,
};



static const struct eq_seg_info eq_tab_normal[] = {
    {0, EQ_IIR_TYPE_BAND_PASS, 31,    0, 0.7f},
    {1, EQ_IIR_TYPE_BAND_PASS, 62,    0, 0.7f},
    {2, EQ_IIR_TYPE_BAND_PASS, 125,   0, 0.7f},
    {3, EQ_IIR_TYPE_BAND_PASS, 250,   0, 0.7f},
    {4, EQ_IIR_TYPE_BAND_PASS, 500,   0, 0.7f},
    {5, EQ_IIR_TYPE_BAND_PASS, 1000,  9, 0.7f},
    {6, EQ_IIR_TYPE_BAND_PASS, 2000,  0, 0.7f},
    {7, EQ_IIR_TYPE_BAND_PASS, 4000,  0, 0.7f},
    {8, EQ_IIR_TYPE_BAND_PASS, 8000,  0, 0.7f},
    {9, EQ_IIR_TYPE_BAND_PASS, 16000, 0, 0.7f},
};
static const struct eq_seg_info eq_tab_rock[] = {
    {0, EQ_IIR_TYPE_BAND_PASS, 31,    -2, 0.7f},
    {1, EQ_IIR_TYPE_BAND_PASS, 62,     0, 0.7f},
    {2, EQ_IIR_TYPE_BAND_PASS, 125,    2, 0.7f},
    {3, EQ_IIR_TYPE_BAND_PASS, 250,    4, 0.7f},
    {4, EQ_IIR_TYPE_BAND_PASS, 500,   -2, 0.7f},
    {5, EQ_IIR_TYPE_BAND_PASS, 1000,  -2, 0.7f},
    {6, EQ_IIR_TYPE_BAND_PASS, 2000,   0, 0.7f},
    {7, EQ_IIR_TYPE_BAND_PASS, 4000,   0, 0.7f},
    {8, EQ_IIR_TYPE_BAND_PASS, 8000,   4, 0.7f},
    {9, EQ_IIR_TYPE_BAND_PASS, 16000,  4, 0.7f},
};
static const struct eq_seg_info eq_tab_pop[] = {
    {0, EQ_IIR_TYPE_BAND_PASS, 31,     3, 0.7f},
    {1, EQ_IIR_TYPE_BAND_PASS, 62,     1, 0.7f},
    {2, EQ_IIR_TYPE_BAND_PASS, 125,    0, 0.7f},
    {3, EQ_IIR_TYPE_BAND_PASS, 250,   -2, 0.7f},
    {4, EQ_IIR_TYPE_BAND_PASS, 500,   -4, 0.7f},
    {5, EQ_IIR_TYPE_BAND_PASS, 1000,  -4, 0.7f},
    {6, EQ_IIR_TYPE_BAND_PASS, 2000,  -2, 0.7f},
    {7, EQ_IIR_TYPE_BAND_PASS, 4000,   0, 0.7f},
    {8, EQ_IIR_TYPE_BAND_PASS, 8000,   1, 0.7f},
    {9, EQ_IIR_TYPE_BAND_PASS, 16000,  2, 0.7f},
};
static const struct eq_seg_info eq_tab_classic[] = {
    {0, EQ_IIR_TYPE_BAND_PASS, 31,     0, 0.7f},
    {1, EQ_IIR_TYPE_BAND_PASS, 62,     8, 0.7f},
    {2, EQ_IIR_TYPE_BAND_PASS, 125,    8, 0.7f},
    {3, EQ_IIR_TYPE_BAND_PASS, 250,    4, 0.7f},
    {4, EQ_IIR_TYPE_BAND_PASS, 500,    0, 0.7f},
    {5, EQ_IIR_TYPE_BAND_PASS, 1000,   0, 0.7f},
    {6, EQ_IIR_TYPE_BAND_PASS, 2000,   0, 0.7f},
    {7, EQ_IIR_TYPE_BAND_PASS, 4000,   0, 0.7f},
    {8, EQ_IIR_TYPE_BAND_PASS, 8000,   2, 0.7f},
    {9, EQ_IIR_TYPE_BAND_PASS, 16000,  2, 0.7f},
};
static const struct eq_seg_info eq_tab_country[] = {
    {0, EQ_IIR_TYPE_BAND_PASS, 31,    -2, 0.7f},
    {1, EQ_IIR_TYPE_BAND_PASS, 62,     0, 0.7f},
    {2, EQ_IIR_TYPE_BAND_PASS, 125,    0, 0.7f},
    {3, EQ_IIR_TYPE_BAND_PASS, 250,    2, 0.7f},
    {4, EQ_IIR_TYPE_BAND_PASS, 500,    2, 0.7f},
    {5, EQ_IIR_TYPE_BAND_PASS, 1000,   0, 0.7f},
    {6, EQ_IIR_TYPE_BAND_PASS, 2000,   0, 0.7f},
    {7, EQ_IIR_TYPE_BAND_PASS, 4000,   0, 0.7f},
    {8, EQ_IIR_TYPE_BAND_PASS, 8000,   4, 0.7f},
    {9, EQ_IIR_TYPE_BAND_PASS, 16000,  4, 0.7f},
};
static const struct eq_seg_info eq_tab_jazz[] = {
    {0, EQ_IIR_TYPE_BAND_PASS, 31,     0, 0.7f},
    {1, EQ_IIR_TYPE_BAND_PASS, 62,     0, 0.7f},
    {2, EQ_IIR_TYPE_BAND_PASS, 125,    0, 0.7f},
    {3, EQ_IIR_TYPE_BAND_PASS, 250,    4, 0.7f},
    {4, EQ_IIR_TYPE_BAND_PASS, 500,    4, 0.7f},
    {5, EQ_IIR_TYPE_BAND_PASS, 1000,   4, 0.7f},
    {6, EQ_IIR_TYPE_BAND_PASS, 2000,   0, 0.7f},
    {7, EQ_IIR_TYPE_BAND_PASS, 4000,   2, 0.7f},
    {8, EQ_IIR_TYPE_BAND_PASS, 8000,   3, 0.7f},
    {9, EQ_IIR_TYPE_BAND_PASS, 16000,  4, 0.7f},
};
/* 添加需要参与切换的系数表 */
static const struct PCM_EQ_F_PARAM eq_param_tab[] = {
    /* EQ系数表          总增益 */
    {eq_tab_normal,    0},
    {eq_tab_rock,      0},
    {eq_tab_pop,       0},
    {eq_tab_classic,  -8},
    {eq_tab_country,   0},
    {eq_tab_jazz,     -2},
};

static u8 g_eq_coeff_malloc_cnt = 0;
static float *eq_coeffbuf_malloc(u32 size)
{
    D_MALLOC_CNT_INC(g_eq_coeff_malloc_cnt, __FUNCTION__, 250);
    return (float *)zalloc(size);
}
static float *eq_coeffbuf_free(void *ptr)
{
    D_MALLOC_CNT_DEC(g_eq_coeff_malloc_cnt);
    free(ptr);
    return NULL;
}
static void dump_eq_mcnt()
{
    log_info("g_eq_coeff_malloc_cnt %d\n", g_eq_coeff_malloc_cnt);
}
const struct mcnt_operations eq_coeff_mcnt sec_used(.d_malloc_cnt) = {
    .malloc_cnt_dump = dump_eq_mcnt,
};

static const struct PCM_EQ_F_PARAM *get_eq_param_tab()
{
    static u8 eq_mode = 0;
    if (eq_mode >= sizeof(eq_param_tab) / sizeof(eq_param_tab[0])) {
        eq_mode = 0;
    }
    u8 curr_mode = eq_mode;
    eq_mode++;
    log_info("sel curr_mode %d\n", curr_mode);
    return &eq_param_tab[curr_mode];
}

void eq_release(void **ppeffect)
{
    PCM_EQ_HDL *p_eq_f_data = *ppeffect;
    if (NULL != p_eq_f_data) {
        EQ_COEFF_BUFF *p_coeff_buf = &p_eq_f_data->buff;
        if (NULL != p_coeff_buf->p_eq_coeff_tab) {
            log_info("FREE1 EQ_DATA:0x%x 0x%x\n", (u32)p_eq_f_data, p_coeff_buf->p_eq_coeff_tab);
            p_coeff_buf->p_eq_coeff_tab = eq_coeffbuf_free(p_coeff_buf->p_eq_coeff_tab);
        }
        log_info("FREE1 EQ_DATA:0x%x\n", (u32)p_eq_f_data);
        /* 再释放音效_data */
        D_MALLOC_CNT_DEC(s_eq_f_hdl_mcnt);
        free(p_eq_f_data);
        *ppeffect = NULL;
    }
}
/* static void dump_eq_param(struct PCM_EQ_F_PARAM *eq_param, u32 seg_num) */
/* { */
/* log_info("dump_eq_param \n"); */
/* const struct eq_seg_info *ptr = eq_param->seg; */
/* for (u8 i = 0; i < seg_num; i++) { */
/* int *p_gain = (int *)&ptr->gain; */
/* int *p_q = (int *)&ptr->q; */
/* xprintf("0x%8x, 0x%8x, 0x%8x, 0x%8x, 0x%8x\n", ptr->index, ptr->iir_type, ptr->freq, *p_gain, *p_q); */
/* ptr += 1; */
/* } */
/* } */

/* static void dump_eq_coeff_by_int(float *p_eq_coeff_tab, u32 seg_num) */
/* { */
/* log_info("dump_eq_coeff \n"); */
/* int *ptr = (int *)p_eq_coeff_tab; */
/* for (u8 i = 0; i < seg_num; i++) { */
/* xprintf("0x%x, 0x%x, 0x%x, 0x%x, 0x%x\n", ptr[0], ptr[1], ptr[2], ptr[3], ptr[4]); */
/* ptr += 5; */
/* } */
/* } */

float *get_eq_coeff_tab(u32 sr, EQ_COEFF_BUFF *ptr)
{
    EQ_COEFF_BUFF *p_coeff_buf = ptr;
    p_coeff_buf->eq_param = (struct PCM_EQ_F_PARAM *)get_eq_param_tab();
    p_coeff_buf->sample_rate = sr;
    p_coeff_buf->max_nsection = EQ_SECTION_MAX;
    if (NULL == p_coeff_buf->p_eq_coeff_tab) {
        p_coeff_buf->p_eq_coeff_tab = (float *)eq_coeffbuf_malloc(p_coeff_buf->max_nsection * 5 * sizeof(float));
        log_info("malloc 0x%x, size %d\n", p_coeff_buf->p_eq_coeff_tab, p_coeff_buf->max_nsection * 5 * sizeof(float));
        memset((u8 *)p_coeff_buf->p_eq_coeff_tab, 0, p_coeff_buf->max_nsection * 5 * sizeof(float));
    }
    /* dump_eq_param(p_coeff_buf->eq_param, p_coeff_buf->max_nsection); */
    //把用户配置的EQ参数，换算成算法使用的滤波器系数，存到p_eq_coefftab
    bool err = calculate_eq_param(p_coeff_buf, p_coeff_buf->max_nsection);
    if (err != true) {
        log_error("calculate param_to_coeff err 0x%x\n", err);
    }
    /* dump_eq_coeff_by_int(p_coeff_buf->p_eq_coeff_tab, p_coeff_buf->max_nsection); */
    return p_coeff_buf->p_eq_coeff_tab;
}

/* void eq_release() */
/* { */
/* EQ_COEFF_BUFF *p_coeff_buf = &pcm_eq_hdl.buff; */
/* if (NULL != p_coeff_buf->p_eq_coeff_tab) { */
/* p_coeff_buf->p_eq_coeff_tab = eq_coeffbuf_free(p_coeff_buf->p_eq_coeff_tab); */
/* } */
/* } */


void *pcm_eq_api(void *obuf, u32 sr, u32 channel, void **ppsound)
{
    D_MALLOC_CNT_INC(s_eq_f_hdl_mcnt, "s_eq_f_hdl_mcnt", EQ_F_MAX_MALLOC_CNT);
    PCM_EQ_HDL *p_pcm_eq_hdl = (void *)malloc(sizeof(PCM_EQ_HDL));
    log_info("MALLOC p_pcm_eq_hdl 0x%x\n", p_pcm_eq_hdl);
    if (NULL == p_pcm_eq_hdl) {
        log_error("eq_cannot_malloc_any_ram!!\n");
        return NULL;
    }
    memset(p_pcm_eq_hdl, 0, sizeof(PCM_EQ_HDL));
    p_pcm_eq_hdl->iir_parm.channel = channel;
    p_pcm_eq_hdl->iir_parm.n_section = EQ_SECTION_MAX;
    p_pcm_eq_hdl->iir_parm.sos_matrix = get_eq_coeff_tab(sr, &p_pcm_eq_hdl->buff);

    p_pcm_eq_hdl->iir_parm.pcm_info.IndataBit = DATA_INT_16BIT;
    p_pcm_eq_hdl->iir_parm.pcm_info.OutdataBit = DATA_INT_16BIT;
    p_pcm_eq_hdl->iir_parm.pcm_info.IndataInc = 1;
    p_pcm_eq_hdl->iir_parm.pcm_info.OutdataInc = 1;
    p_pcm_eq_hdl->iir_parm.pcm_info.Qval = 15;
    u32 buf_size = need_fix_iir_filter_buf(&p_pcm_eq_hdl->iir_parm);

    if (EQ_INBUF_SIZE < buf_size) {
        log_info("PCM EQ Work Data space is not big enough:%d : %d", EQ_INBUF_SIZE, buf_size);
        eq_release((void *)&p_pcm_eq_hdl);
        return NULL;
    }
    log_info("PCM EQ Work Data space is enough:%d : %d", EQ_INBUF_SIZE, buf_size);

    return pcm_eq_float_phy(obuf, p_pcm_eq_hdl, ppsound);
}

void *link_pcm_eq_sound(void *p_sound_out, void *p_dac_cbuf, void **pp_effect, u32 sr, u32 channel)
{
    sound_out_obj *p_next_sound = 0;
    sound_out_obj *p_curr_sound = p_sound_out;
    p_curr_sound->effect = pcm_eq_api(p_curr_sound->p_obuf, sr, channel, (void **)&p_next_sound);
    if (NULL != p_curr_sound->effect) {
        if (NULL != pp_effect) {
            *pp_effect = p_curr_sound->effect;
        }
        p_curr_sound->enable |= B_DEC_EFFECT;
        p_curr_sound = p_next_sound;
        p_curr_sound->p_obuf = p_dac_cbuf;
    } else {
        log_error("pcm eq init fail\n");
    }
    log_info("pcm eq init succ\n");
    return p_curr_sound;
}

int pcm_eq_float_run(void *hld, short *inbuf, int len)
{
    u32 rlen = 0;
    sound_in_obj *p_eq_soundin = hld;
    PCM_EQ_HDL *p_eq_hdl = p_eq_soundin->priv;
    remain_ops *p_eq_remain_ops = &p_eq_hdl->eq_float_remain_ops;
    PCM_EQ_IO_CONTEXT *p_io = &p_eq_hdl->io;
    u32 sp_number = len / 2;

    /* 0. output 上次剩余的数据 */
    remain_output(p_io->priv, p_eq_remain_ops);
    if (p_eq_remain_ops->remain_len) {        //上次数据输出仍旧没输出完
        /* log_info("rlen %d\n", p_eq_remain_ops->remain_len); */
        return 0;
    }

    /* 1. 新一轮输入输出 */
    memset(p_eq_remain_ops->obuf, 0, p_eq_remain_ops->len);    //清空outdata

    /* 2. input 数据 */
    if (sp_number < EQ_READSIZE) {
        /* 本次输入数据不够EQ_READSIZE个点 */
        /* log_info("sp_number %d, EQ_READSIZE %d\n", sp_number, EQ_READSIZE); */
        return 0;
    }

    /* 3. 运算run */
    fix_iir_filter_run(p_eq_soundin->p_dbuf, inbuf, p_eq_remain_ops->obuf, EQ_READSIZE);
    /* memcpy(g_eq_f_obuf, (void *)inbuf, EQ_READSIZE * 2); */

    /* 4. 设置需要 output 数据量 */
    set_remain_len(p_eq_remain_ops, EQ_READSIZE * sizeof(short));      //设置output需要输出一包的数据量

    /* 5. 输出 */
    remain_output(p_io->priv, p_eq_remain_ops);

    return EQ_READSIZE * sizeof(short);    //成功返回读取byte长度
}

void *pcm_eq_float_phy(void *obuf, PCM_EQ_HDL *p_eq_hdl, void **ppsound)
{
    void *p_dbuf = &p_eq_hdl->eq_float_workbuf[0];
    void *p_obuf = &p_eq_hdl->eq_f_obuf[0];
    struct iir_filter_param *p_parm = &p_eq_hdl->iir_parm;
    EFFECT_OBJ *pcm_eq_obj = &p_eq_hdl->obj;

    p_eq_hdl->io.priv   = &pcm_eq_obj->sound;
    p_eq_hdl->io.output = sound_output;

    p_eq_hdl->si.ops = 0;
    p_eq_hdl->si.p_dbuf = p_dbuf;

    pcm_eq_obj->p_si = &p_eq_hdl->si;
    pcm_eq_obj->run = pcm_eq_float_run;
    pcm_eq_obj->sound.p_obuf = obuf;
    *ppsound = &pcm_eq_obj->sound;

    //open
    u32 ret = fix_iir_filter_init(p_dbuf, p_parm);
    if (ret) {
        log_error("fix_iir_filter_init error 0x%x\n", ret);
        return NULL;
    }
    ret = init_remain_obuf(&p_eq_hdl->eq_float_remain_ops, sound_output, p_obuf, EQ_OBUF_SIZE);
    if (ret) {
        log_error("eq_init_remain_obuf error 0x%x\n", ret);
        return NULL;
    }
    sound_in_obj *p_si = pcm_eq_obj->p_si;
    p_si->priv = (void *)p_eq_hdl;
    return &p_eq_hdl->obj;
}

//只能切换EQ段数相同的参数
void update_pcm_eq_float_param(void *peffect)
{
    if (NULL == peffect) {
        return;
    }
    PCM_EQ_HDL *p_eq_f_hdl = peffect;
    struct iir_filter_param *p_parm = &p_eq_f_hdl->iir_parm;
    if (NULL == p_parm->sos_matrix) {
        /* 原本就没有参数，也就是没初始化 */
        log_error("eq not_init_yet\n");
        return;
    }
    EQ_COEFF_BUFF *p_coeff_buf = &p_eq_f_hdl->buff;
    local_irq_disable();
    p_parm->sos_matrix = get_eq_coeff_tab(p_coeff_buf->sample_rate, &p_eq_f_hdl->buff);
    fix_iir_filter_update(&p_eq_f_hdl->eq_float_workbuf[0], p_parm);
    local_irq_enable();
}

#endif








