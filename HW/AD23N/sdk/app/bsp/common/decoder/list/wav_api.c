#include "cpu.h"
#include "config.h"
#include "typedef.h"
#include "hwi.h"
#include "decoder_api.h"
/* #include "dev_manage.h" */
#include "vfs.h"
#include "circular_buf.h"
#include "errno-base.h"
#include "msg.h"
#include "decoder_msg_tab.h"
#include "app_config.h"
#include "app_modules.h"

#if defined(DECODER_WAV_EN) && (DECODER_WAV_EN)

#define LOG_TAG_CONST       NORM
#define LOG_TAG             "[wav]"
#include "log.h"

#define WAV_DECBUF_SIZE     (2776 + 3)
#define WAV_OBUF_SIZE 		(DAC_DECODER_BUF_SIZE*4)
#define WAV_KICK_SIZE		(WAV_OBUF_SIZE - 2*DAC_PACKET_SIZE)

typedef struct _PARM_DECODE_CHV_ {
    u32  ch_value;
} PARM_DECODE_CHV;

typedef struct _wav_dec_data {
    PARM_DECODE_CHV parm_nchv;
    cbuffer_t cbuf_wav;
    u16 obuf_wav[WAV_OBUF_SIZE / 2];
    u32 wav_decode_buff[WAV_DECBUF_SIZE / 4];
} wav_dec_data;
#define D_WAV_RESOURCE  (sizeof(wav_dec_data))
#define WAV_CAL_BUF ((void *)&p_wav_data->wav_decode_buff[0])

dec_obj dec_wav_hld;
const int FILE_DEC_SUPPORT_HIGH_SAMPLE_RATE_FLAG = 0;               //是否支持64k,88.2k,96k采样率
const int const_audio_codec_wav_dec_support_aiff = 0;               //是否打开aiff解码开关
const u8  config_wav_id3_enable = 0;                                //文件中的歌手信息等解析开关
const int const_audio_codec_wav_dec_bitDepth_set_en = 0;            //32bit/24bit 是否支持高位宽配置输出
const int const_audio_codec_wav_checkdst_enable = 0;                //是否做dts检查
const int const_audio_codec_wav_dec_support_AB_Repeat_en = 0;       //是否支持AB点跟循环
const int const_audio_codec_wav_dec_supoort_POS_play = 0;           //是否支持指定位置播放
const int WAV_DECODER_PCM_POINTS = 480;                             //最多一次输出点数配置
const int WAV_MAX_BITRATEV = (48 * 2 * 32);                         //最大码率限制，超过这个码率，返回不支持；可以根据卡的读数速度来限制
const int const_audio_wav_downmix_flag = 1; //wav单声道输出

#define CMD_SET_DECODE_CH   0x91

enum {
    FAST_L_OUT = 0x01,                  //输出左声道
    FAST_R_OUT = 0x02,                  //输出右声道
    FAST_LR_OUT = 0x04                 //输出左右声道混合
};


const struct if_decoder_io wav_dec_io0 = {
    &dec_wav_hld,      //input跟output函数的第一个参数，解码器不做处理，直接回传，可以为NULL
    mp_input,
    NULL,
    mp_output,
    decoder_get_flen,
    NULL,
};

#define D_THIS         wav_dec
#define D_THIS_NAME    "wav_dec"
#define D_THIS_DEC_RESOURCE   D_WAV_RESOURCE
static u8 EXPAND_CONCAT(D_THIS, _mcnt) = 0;
D_DUMP_CNT();
u32 wav_decoder_res_release(void *priv)
{
    dec_obj *obj = priv;
    if (obj == NULL) {
        return EINVAL;
    }

    D_FERR(obj->pdec_res);
    obj->p_dbuf = NULL;
    obj->p_dp_buf = NULL;
    return 0;
}

u32 wav_decode_api(void *p_file, void **p_dec, void *p_dp_buf)
{
    u32 buff_len, i;
    /* void *name; */
    /* char name[VFS_FILE_NAME_LEN] = {0}; */
    decoder_ops_t *ops;
    log_info("wav_decode_api\n");
    if (dec_wav_hld.pdec_res != NULL) {
        //上次没释放
        log_error("dec_res_not_null 0x%x\n", dec_wav_hld.pdec_res);
        wav_decoder_res_release(&dec_wav_hld);
    }
    memset(&dec_wav_hld, 0, sizeof(dec_obj));

    D_MALLOC(dec_wav_hld.pdec_res,  D_THIS_DEC_RESOURCE);
    log_info("malloc_wav_data 0x%x\n", dec_wav_hld.pdec_res);
    if (NULL == dec_wav_hld.pdec_res) {
        log_error("wav_dec_cann't_malloc_any_resource\n");
        return E_WAV_DEC_RESOURCE;
    }
    wav_dec_data *p_wav_data = (void *)dec_wav_hld.pdec_res;
    dec_wav_hld.type = D_TYPE_WAV;
    dec_wav_hld.function = DEC_FUNCTION_FF_FR;
    ops = get_wav_ops();
    buff_len = ops->need_dcbuf_size();
    if (buff_len > WAV_DECBUF_SIZE) {
        log_error("wav file need dbuff : 0x%x 0x%lx\n", buff_len, WAV_DECBUF_SIZE);
        wav_decoder_res_release(&dec_wav_hld);
        return E_WAV_DBUF;
    }
    /******************************************/
    cbuf_init(&p_wav_data->cbuf_wav, &p_wav_data->obuf_wav[0], WAV_OBUF_SIZE);
    dec_wav_hld.p_file       = p_file;
    dec_wav_hld.sound.p_obuf = &p_wav_data->cbuf_wav;
    dec_wav_hld.sound.para = WAV_KICK_SIZE;
    dec_wav_hld.p_dbuf       = WAV_CAL_BUF;
    dec_wav_hld.dec_ops      = ops;
    dec_wav_hld.event_tab    = (u8 *)&wav_evt[0];
    dec_wav_hld.p_dp_buf     = p_dp_buf;
    dec_wav_hld.decoder_res_release = wav_decoder_res_release;
    //dac reg
    // dec_wav_hld.dac.obuf = &cbuf_wav;
    // dec_wav_hld.dac.vol = 255;
    // dec_wav_hld.dac.index = reg_channel2dac(&dec_wav_hld.dac);
    /******************************************/

    /* name = vfs_file_name(p_file); */
    int file_len = vfs_file_name(p_file, (void *)g_file_sname, sizeof(g_file_sname));
    log_info("file name : %s\n", g_file_sname);
    log_info(" -wav open\n");
    ops->open(WAV_CAL_BUF, &wav_dec_io0, p_dp_buf);         //传入io接口，说明如下
    log_info(" -wav open over\n");
    if (ops->format_check(WAV_CAL_BUF)) {                  //格式检查
        log_info(" wav format err : %s\n", g_file_sname);
        wav_decoder_res_release(&dec_wav_hld);
        return E_WAV_FORMAT;
    }

    PARM_DECODE_CHV *p_parm_nchv = &p_wav_data->parm_nchv;
    p_parm_nchv->ch_value = FAST_LR_OUT;
    ops->dec_confing(WAV_CAL_BUF, CMD_SET_DECODE_CH, p_parm_nchv);  //配置解码输出通道

    regist_dac_channel(&dec_wav_hld.sound, kick_decoder);//注册到DAC;
    i = ops->get_dec_inf(WAV_CAL_BUF)->sr;                //获取采样率
    dec_wav_hld.sr = i;
    log_info("file sr : %d\n", i);
    *p_dec = (void *)&dec_wav_hld;
    return 0;
}

/* extern const u8 wav_buf_start[]; */
/* extern const u8 wav_buf_end[]; */
u32 wav_buff_api(dec_buf *p_dec_buf)
{
    /* p_dec_buf->start = (u32)&wav_buf_start[0]; */
    /* p_dec_buf->end   = (u32)&wav_buf_end[0]; */
    return 0;
}
#endif
