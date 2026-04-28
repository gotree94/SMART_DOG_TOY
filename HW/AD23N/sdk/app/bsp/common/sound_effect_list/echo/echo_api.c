/* #include "resample_api.h" */
#include "echo_api.h"
/* #include "resample.h" */
#include "decoder_api.h"
#include "config.h"
#include "sound_effect_api.h"
#include "app_modules.h"

#if defined(ECHO_EN) && (ECHO_EN)

#define LOG_TAG_CONST       NORM
#define LOG_TAG             "[normal]"
#include "log.h"

/* 该宏仅仅是通过采样率得出需要buf的大小，echo没有采样率限制 */
#define MAX_RATE 24000

#if (MAX_RATE == 16000)
#define echo_work_buf_len   (3692 + 3)
#elif (MAX_RATE == 24000)
#define echo_work_buf_len   (5292 + 3)
#endif

/*1代表下采，0代表不下采。下采的话，申请的Buf会减少，但是回声部分的高频也会丢失*/
const  int DOWN_S_FLAG = 0;

typedef struct _ECHO_HDL {
    EFFECT_OBJ obj;//必须在第一个
    sound_in_obj si;
    ECHO_IO_CONTEXT io;
    ECHO_API_STRUCT echo;
    u8 echo_work_buf[echo_work_buf_len];//echo混响buf长度，采样率改变，buf长度会变
} ECHO_HDL;
#define ECHO_MAX_MALLOC_CNT  20
static u8 s_echo_hdl_mcnt = 0;

static void dump_echo_hdl_mcnt()
{
    log_info("s_echo_hdl_mcnt %d\n", s_echo_hdl_mcnt);
}
const struct mcnt_operations echp_hdl_mcnt sec_used(.d_malloc_cnt) = {
    .malloc_cnt_dump = dump_echo_hdl_mcnt,
};


void echo_release(void **ppeffect)
{
    ECHO_HDL *p_echo_data = *ppeffect;
    if (NULL != p_echo_data) {
        log_info("FREE1 ECHO_DATA:0x%x\n", (u32)p_echo_data);
        /* 再释放音效_data */
        D_MALLOC_CNT_DEC(s_echo_hdl_mcnt);
        free(p_echo_data);
        *ppeffect = NULL;
    }
}

void *echo_api(void *obuf, u32 sr, void **ppsound)
{
    ECHO_PARM eparm;
    memset(&eparm, 0, sizeof(ECHO_PARM));
    log_info("echo_api\n");
    eparm.echo_parm_obj.decayval          	= 60;  //decay(0~70)回声衰减比
    eparm.echo_parm_obj.delay             	= 200; //回声延时0~300ms :范围0到max_ms，超过max_ms的话，会当成max_ms的
    eparm.echo_parm_obj.energy_vad_threshold = 512; //mute阈值
    eparm.echo_parm_obj.direct_sound_enable  = 1;   //如果运算结果需要叠加干声，则这个置成1，否则置0
    eparm.echo_fix_parm.max_ms               = 100; //最大延时100ms
    eparm.echo_fix_parm.sr                   = sr;  //采样率
    eparm.echo_fix_parm.wetgain              = 3000;//湿声增益
    eparm.echo_fix_parm.drygain              = 4096;//干声增益，如果direct_sound_enable是0，则这个参数无效，因为不叠加干声了
    /* eparm.ptr = echo_work_buf; */
    /* eparm.ptr_len = echo_work_buf_len; */

    return echo_phy(obuf, &eparm, ppsound);
}

void *link_echo_sound(void *p_sound_out, void *p_dac_cbuf, void **pp_effect, u32 sr)
{
    sound_out_obj *p_next_sound = 0;
    sound_out_obj *p_curr_sound = p_sound_out;
    p_curr_sound->effect = echo_api(p_curr_sound->p_obuf, sr, (void **)&p_next_sound);
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

/*****************************************************************************************/


void echo_parm_update(void *priv, ECHO_PARM_SET *parm)
{
    if ((NULL == parm) || (NULL == priv)) {
        return;
    }
    EFFECT_OBJ *e_obj = (EFFECT_OBJ *)priv;
    sound_in_obj *p_si = e_obj->p_si;
    ECHO_FUNC_API *ops = p_si->ops;

    local_irq_disable();
    ops->init(p_si->p_dbuf, parm);
    local_irq_enable();
}


static int echo_run(void *hld, short *inbuf, int len)
{
    ECHO_FUNC_API *ops;
    int res = 0;
    sound_in_obj *p_si = hld;
    ops = (ECHO_FUNC_API *)p_si->ops;
    res = ops->run(p_si->p_dbuf, inbuf, len);
    return res;
}


void echo_parm_debug(ECHO_HDL *echo_hdl)
{
    log_info("parm.echo_parm_obj.decayval %d\n", echo_hdl->echo.echo_parm_obj.decayval);
    log_info("parm.echo_parm_obj.delay %d\n", echo_hdl->echo.echo_parm_obj.delay);
    log_info("parm.echo_parm_obj.energy_vad_threshold %d\n", echo_hdl->echo.echo_parm_obj.energy_vad_threshold);
    log_info("parm.echo_parm_obj.direct_sound_enable %d\n", echo_hdl->echo.echo_parm_obj.direct_sound_enable);

    log_info("parm.echo_fix_parm.max_ms %d\n", echo_hdl->echo.echo_fix_parm.max_ms);
    log_info("parm.echo_fix_parm.sr %d\n", echo_hdl->echo.echo_fix_parm.sr);
    log_info("parm.echo_fix_parm.wetgain %d\n", echo_hdl->echo.echo_fix_parm.wetgain);
    log_info("parm.echo_fix_parm.drygain %d\n", echo_hdl->echo.echo_fix_parm.drygain);
}

void *echo_phy(void *obuf, ECHO_PARM *parm, void **ppsound)
{
    u32 buf_len, i;
    ECHO_FUNC_API *ops;
    EFFECT_OBJ *echo_obj;
    sound_in_obj *echo_si;
    if (!parm) {
        log_error("echo parm NULL\n");
        return NULL;
    }

    ops = (ECHO_FUNC_API *)get_echo_func_api(); //接口获取
    buf_len = ops->need_buf(NULL, (EF_REVERB_FIX_PARM *)&parm->echo_fix_parm);           //运算空间获取
    log_info("echo work_buf_len %d\n", buf_len);
    D_MALLOC_CNT_INC(s_echo_hdl_mcnt, "s_echo_hdl_mcnt", ECHO_MAX_MALLOC_CNT);
    ECHO_HDL *echo_hdl = (void *)malloc(sizeof(ECHO_HDL));
    log_info("MALLOC echo_hdl 0x%x\n", echo_hdl);
    if (NULL == echo_hdl) {
        log_error("echo_cannot_malloc_any_ram!!\n");
        return NULL;
    }
    memset(echo_hdl, 0x0, sizeof(ECHO_HDL));
    parm->ptr = &echo_hdl->echo_work_buf;
    parm->ptr_len = echo_work_buf_len;
    if (echo_hdl && parm) {
        memcpy(&echo_hdl->echo.echo_parm_obj, &parm->echo_parm_obj, sizeof(ECHO_PARM_SET));
        memcpy(&echo_hdl->echo.echo_fix_parm, &parm->echo_fix_parm, sizeof(EF_REVERB_FIX_PARM));
        echo_parm_debug(echo_hdl);
    }
    echo_hdl->echo.ptr = (unsigned int *)parm->ptr;
    log_info("echo_hdl->echo.ptr %x\n", echo_hdl->echo.ptr);
    if (parm->ptr_len < buf_len) {
        log_error("echo work buf less %d, need len %d", parm->ptr_len, buf_len);
        D_MALLOC_CNT_DEC(s_echo_hdl_mcnt);
        free(echo_hdl);
        return NULL;
    }

    echo_obj = &echo_hdl->obj;
    echo_hdl->io.priv = &echo_obj->sound;
    echo_hdl->io.output = sound_output;


    /*************************************************/
    echo_si =  &echo_hdl->si;
    echo_si->ops = ops;
    echo_si->p_dbuf = echo_hdl->echo.ptr;
    /*************************************************/
    echo_obj->p_si = echo_si;
    echo_obj->run = echo_run;
    echo_obj->sound.p_obuf = obuf;
    *ppsound = &echo_obj->sound;

    log_info("echo_obj->sound.p_obuf 0x%x\n", echo_obj->sound.p_obuf);
    log_info("o2 sound 0x%x\n", &echo_obj->sound);
    ops->open(echo_hdl->echo.ptr, &echo_hdl->echo.echo_parm_obj, &echo_hdl->echo.echo_fix_parm, &echo_hdl->io);

    return echo_obj;
}
#endif
