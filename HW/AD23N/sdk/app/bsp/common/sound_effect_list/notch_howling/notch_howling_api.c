#include "notch_howling_api.h"
#include "decoder_api.h"
#include "config.h"
#include "sound_effect_api.h"
#include "app_modules.h"


#define LOG_TAG_CONST       NORM
#define LOG_TAG             "[normal]"
#include "log.h"
#if defined(NOTCH_HOWLING_EN) && (NOTCH_HOWLING_EN)


/****************************phy********************************************************************/
#include "circular_buf.h"
typedef struct _NH_HOWLING_HDL {
    EFFECT_OBJ obj;//必须在第一个
    sound_in_obj si;
    NH_IO_CONTEXT io;
    NotchHowlingParam parm;
    /* u32 update; */
    /* cbuffer_t cbuf; */
} NH_HOWLING_HDL;

u32 notch_howling_work_buf[(3280 + 3) / 4] AT(.notch_howling_data); //由于该算法硬件fft会操作该buf，所以该buf地址使用物理地址,notchhowling算法的fft输入输出都在work_buf
/* NH_HOWLING_HDL notch_howling_hdl_save; */
/* u8 notch_howling_in_buf[160 * 2] AT(.notch_howling_data); //陷波器啸叫抑制，固定160点运算一次 */

#define NOTCHHOWLING_MAX_MALLOC_CNT  20
static u8 s_notchhowling_hdl_mcnt = 0;

static void dump_notchhowling_hdl_mcnt()
{
    log_info("s_notchhowling_hdl_mcnt %d\n", s_notchhowling_hdl_mcnt);
}
const struct mcnt_operations notchhowling_hdl_mcnt sec_used(.d_malloc_cnt) = {
    .malloc_cnt_dump = dump_notchhowling_hdl_mcnt,
};

void notchhowling_release(void **ppeffect)
{
    NH_HOWLING_HDL *p_nh_data = *ppeffect;
    if (NULL != p_nh_data) {
        log_info("FREE NH_DATA:0x%x\n", (u32)p_nh_data);
        /* 再释放音效_data */
        D_MALLOC_CNT_DEC(s_notchhowling_hdl_mcnt);
        free(p_nh_data);
        *ppeffect = NULL;
    }
}

void notch_howing_parm_update(void *peffect, NotchHowlingParam *parm)
{
    if (NULL == peffect) {
        return;
    }
    NH_STRUCT_API *ops;
    NH_HOWLING_HDL *howling_hdl = peffect;
    if (howling_hdl) {
        if (parm) {
            sound_in_obj *p_si = &howling_hdl->si;
            ops = (NH_STRUCT_API *)p_si->ops;
            local_irq_disable();
            memcpy(&howling_hdl->parm, parm, sizeof(NotchHowlingParam));
            ops->update(p_si->p_dbuf, &howling_hdl->parm);
            local_irq_enable();
        }
    }
}
#if 0//目前没有config接口
void get_notch_howling_freq(void)
{
    NH_HOWLING_HDL *howling_hdl = &notch_howling_hdl_save;
    if (!howling_hdl) {
        return;
    }

    HowlingFreq hfreq = {0};
    int res = 0;
    sound_in_obj *p_si = &howling_hdl->si;
    NH_STRUCT_API *ops = (NH_STRUCT_API *)p_si->ops;
    res = ops->config(p_si->p_dbuf, GET_HOWLING_FREQ, &hfreq);
    log_info("Total:%d 1:%d 2:%d 3:%d 4:%d 5:%d\n", hfreq.num, \
             hfreq.freq[0], \
             hfreq.freq[1], \
             hfreq.freq[2], \
             hfreq.freq[3], \
             hfreq.freq[4]);

}
#endif

static int notch_howing_run(void *hld, short *inbuf, int len)
{
    /* NH_HOWLING_HDL *howling_hdl = &notch_howling_hdl_save; */
    /* if (!howling_hdl) { */
    /* return 0; */
    /* } */
    if ((NULL == hld) || (NULL == inbuf)) {
        return 0;
    }
    NH_STRUCT_API *ops;
    int res = 0;
    sound_in_obj *p_si = hld;
    ops = (NH_STRUCT_API *)p_si->ops;
    res = ops->run(p_si->p_dbuf, inbuf, len);//len 为indata 字节数，返回值为实际消耗indata的字节数

    /* if (howling_hdl->update) { */
    /* howling_hdl->update = 0; */
    /* ops->update(p_si->p_dbuf, &howling_hdl->parm); */
    /* } */

    return res;

}


void notch_howling_parm_debug(NH_HOWLING_HDL *howling_hdl)
{
    log_info("howling->parm.gain %d", howling_hdl->parm.gain);
    log_info("howling->parm.Q %d", howling_hdl->parm.Q);
    log_info("howling->parm.fade_time %d", howling_hdl->parm.fade_time);
    log_info("howling->parm.threshold %d", howling_hdl->parm.threshold);
    log_info("howling->parm.SampleRate %d\n", howling_hdl->parm.SampleRate);
}


void *notch_howling_phy(void *obuf, NotchHowlingParam *parm, void **ppsound)
{
    u32 buf_len, i;
    NH_STRUCT_API *ops;
    EFFECT_OBJ *howling_obj;
    sound_in_obj *howling_si;
    if (!parm) {
        log_error("notch howling parm NULL\n");
        return NULL;
    }
    ops = (NH_STRUCT_API *)get_notchHowling_ops(); //接口获取
    buf_len = ops->need_buf(parm);           //运算空间获取
    log_info("notch_howling work_buf_len %d\n", buf_len);
    if (sizeof(notch_howling_work_buf) < buf_len) {
        log_error("notch howing work buf less %d, need len %d", sizeof(notch_howling_work_buf), buf_len);
        return NULL;
    }
    D_MALLOC_CNT_INC(s_notchhowling_hdl_mcnt, "s_notchhowling_hdl_mcnt", NOTCHHOWLING_MAX_MALLOC_CNT);
    NH_HOWLING_HDL *howling_hdl = malloc(sizeof(NH_HOWLING_HDL));
    log_info("MALLOC howling_hdl 0x%x\n", howling_hdl);
    if (NULL == howling_hdl) {
        log_error("notchhowling_cannot_malloc_any_ram!!\n");
        return NULL;
    }
    memset(howling_hdl, 0x0, sizeof(NH_HOWLING_HDL));
    if (howling_hdl && parm) {
        memcpy(&howling_hdl->parm, parm, sizeof(NotchHowlingParam));
        notch_howling_parm_debug(howling_hdl);
    }
    unsigned int *howling_hdl_ptr = (unsigned int *)notch_howling_work_buf;
    log_info("notch howling_hdl_ptr %x\n", howling_hdl_ptr);
    howling_obj = &howling_hdl->obj;
    howling_hdl->io.priv = &howling_obj->sound;
    howling_hdl->io.output = sound_output;

    /*************************************************/
    howling_si =  &howling_hdl->si;
    howling_si->ops = ops;
    howling_si->p_dbuf = howling_hdl_ptr;
    /*************************************************/
    howling_obj->p_si = howling_si;
    howling_obj->run = notch_howing_run;
    howling_obj->sound.p_obuf = obuf;
    *ppsound = &howling_obj->sound;

    ops->open(howling_hdl_ptr, &howling_hdl->parm, &howling_hdl->io);
    /* cbuf_init(&howling_hdl->cbuf, &notch_howling_in_buf[0], sizeof(notch_howling_in_buf)); */
    return howling_obj;
}
void *notch_howling_api(void *obuf, u32 sr, void **ppsound)
{
    /* 调试顺序：gain -> Q -> threshold。gain压制越多，Q越小会容易出现说话过程中声音断续的问题 */
    NotchHowlingParam nhparm    = {0};
    nhparm.gain                 = (int)(-10.0 * (1 << 20)); //陷波器压制程度，越大放啸叫越好，但发声啸叫频点误检时音质会更差
    nhparm.Q                    = (int)(2.0 * (1 << 24));   //陷波器带宽，越小放啸叫越好，但发声啸叫频点误检时音质会更差
    nhparm.fade_time            = 10;                       //启动时间与施放时间，越小启动与释放越快，可能导致杂音出现切音质变差
    nhparm.threshold            = (int)(25.0 * (1 << 15));  //频点啸叫判定阈值，越小越容易判定啸叫频点，但可能误检导致音质变差
    nhparm.SampleRate           = sr;                       //采样率

    return notch_howling_phy(obuf, &nhparm, ppsound);
}

void *link_notch_howling_sound(void *p_sound_out, void *p_dac_cbuf, void **pp_effect, u32 sr)
{
    sound_out_obj *p_next_sound = 0;
    sound_out_obj *p_curr_sound = p_sound_out;
    p_curr_sound->effect = notch_howling_api(p_curr_sound->p_obuf, sr, (void **)&p_next_sound);
    if (NULL != p_curr_sound->effect) {
        if (NULL != pp_effect) {
            *pp_effect = p_curr_sound->effect;
        }
        p_curr_sound->enable |= B_DEC_EFFECT;
        p_curr_sound = p_next_sound;
        p_curr_sound->p_obuf = p_dac_cbuf;
    } else {
        log_info("echo init fail\n");
    }
    return p_curr_sound;
}

#endif
