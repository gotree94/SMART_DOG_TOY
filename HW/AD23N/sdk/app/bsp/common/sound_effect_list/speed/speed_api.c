/* #include "resample_api.h" */
#include "speed_api.h"
/* #include "resample.h" */
#include "typedef.h"
#include "decoder_api.h"
#include "config.h"
#include "sound_effect_api.h"
#include "app_modules.h"


#define LOG_TAG_CONST       NORM
#define LOG_TAG             "[normal]"
#include "log.h"

#if defined(AUDIO_SPEED_EN) && (1 == AUDIO_SPEED_EN)

#define SPEED_BUF_SIZE   (0xbf0)

typedef struct _SPEED_HDL {
    EFFECT_OBJ speed_obj;
    sound_in_obj speed_si;
    SPEEDPITCH_IO_CONTEXT sppitch_io;
    SPEED_PITCH_PARA_STRUCT sp_parm;
    u32 SP_BUFLEN[SPEED_BUF_SIZE / 4];            //处理32k音频变速变调需要的空间
} SPEED_HDL;

/* const struct _SPEEDPITCH_IO_CONTEXT_ sppitch_io = { */
/* &speed_obj.sound,      //input跟output函数的第一个参数，解码器不做处理，直接回传，可以为NULL */
/* sound_output, */
/* }; */
const int SPITCH_PARM_PR_HResolution = 0;

#define SP_MAX_MALLOC_CNT  20
static u8 s_sp_hdl_mcnt = 0;

static void dump_sp_hdl_mcnt()
{
    log_info("s_sp_hdl_mcnt %d\n", s_sp_hdl_mcnt);
}
const struct mcnt_operations sp_hdl_mcnt sec_used(.d_malloc_cnt) = {
    .malloc_cnt_dump = dump_sp_hdl_mcnt,
};

void sp_release(void **ppeffect)
{
    SPEED_HDL *p_sp_data = *ppeffect;
    if (NULL != p_sp_data) {
        log_info("FREE SP_DATA:0x%x\n", (u32)p_sp_data);
        /* 再释放音效_data */
        D_MALLOC_CNT_DEC(s_sp_hdl_mcnt);
        free(p_sp_data);
        *ppeffect = NULL;
    }
}
void *speed_api(void *obuf, u32 insample, void **ppsound)
{
    log_info("speed_api\n");

    SPEED_PITCH_PARA_STRUCT sp_parm;
    sp_parm.insample = insample;             //输入音频采样率
    if (SPITCH_PARM_PR_HResolution) {
        sp_parm.pitchrate = 32768;           //变调比例，32768:为不变调，<32768:音调变高，>32768:音调变低，声音更沉
    } else {
        sp_parm.pitchrate = 100;           //变调比例，128:为不变调，<128:音调变高，>128:音调变低，声音更沉
    }
    //speedout/speedin 的结果大于0.6小于1.8.变快变慢最好都不要超过2倍
    sp_parm.speedin = 80;
    sp_parm.speedout = 144;          //sp_parm.speedin：sp_parm.speedout为变速比例，例如speedin=1;speedout=2;则为变慢1倍
    sp_parm.quality = 4;            //变调运算的运算质量，跟速度成正比，配置范围3-8
    sp_parm.pitchflag = 1;     //如果pitchflag=1,就是输入输出速度只由speedin：speedout决定,调节音调的时候，速度比例保持不变。例如设置speedout：speedin=1:2 ，这时候，去调节pitchrate，都是只变调，速度一直是保持放慢1倍。
    //如果pitchflag=0，就是变速变调比例分开控制 ，这样实际变速的比例是 （speedout/speedin)*pitchrate了。

    return speed_phy(obuf, &sp_parm, ppsound);
}
#endif

/***************************phy***************************************************************/

static int speed_run(void *hld, short *inbuf, int len)
{
    SPEEDPITCH_STUCT_API *ops;
    int res = 0;
    sound_in_obj *p_si = hld;
    ops = p_si->ops;
    res = ops->run(p_si->p_dbuf, inbuf, len);
    return res;
}

void *speed_phy(void *obuf, SPEED_PITCH_PARA_STRUCT *psp_parm, void **ppsound)
{
    u32 buff_len;
    SPEEDPITCH_STUCT_API *ops;
    log_info("speed_api\n");

    ops = get_sppitch_context();           //获取变采样函数接口
    buff_len = ops->need_buf(psp_parm->insample);                          //运算空间获取
    if (buff_len > SPEED_BUF_SIZE) {
        log_error("speed buff need : 0x%x\n", buff_len);
        return NULL;
    }
    D_MALLOC_CNT_INC(s_sp_hdl_mcnt, "s_sp_hdl_mcnt", SP_MAX_MALLOC_CNT);
    SPEED_HDL *sp_hdl = malloc(sizeof(SPEED_HDL));
    log_info("MALLOC sp_hdl 0x%x\n", sp_hdl);
    if (NULL == sp_hdl) {
        log_error("speed_cannot_malloc_any_ram!!\n");
        return NULL;
    }
    memcpy(&sp_hdl->sp_parm, psp_parm, sizeof(SPEED_PITCH_PARA_STRUCT));
    sp_hdl->sppitch_io.priv = &sp_hdl->speed_obj.sound;
    sp_hdl->sppitch_io.output = sound_output;
    /******************************************/
    //初始化：rs_buf：运算Buf; rs_parm：参数指针，传完可以释放的，里面不会记录这个指针的。sppitch_io:output接口，说明如下
    ops->open(&sp_hdl->SP_BUFLEN[0], &sp_hdl->sp_parm, (void *)&sp_hdl->sppitch_io);
    /*************************************************/
    /* memset(&speed_obj, 0, sizeof(speed_obj)); */
    sp_hdl->speed_si.ops = ops;
    sp_hdl->speed_si.p_dbuf = &sp_hdl->SP_BUFLEN[0];
    /*************************************************/
    sp_hdl->speed_obj.p_si = &sp_hdl->speed_si;
    sp_hdl->speed_obj.run = speed_run;
    sp_hdl->speed_obj.sound.p_obuf = obuf;
    *ppsound = &sp_hdl->speed_obj.sound;
    log_info("speed_succ\n");
    return &sp_hdl->speed_obj;
}


