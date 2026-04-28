
#include "encoder_mge.h"
#include "cpu.h"
#include "config.h"
#include "typedef.h"
#include "hwi.h"
#include "app_modules.h"
#include "circular_buf.h"

#if defined(ENCODER_MP3_EN) && (ENCODER_MP3_EN)

#define LOG_TAG_CONST       NORM
#define LOG_TAG             "[normal]"
#include "log.h"

#define MP3_ENC_OBUF_SIZE     (1024)
#define MP3_ENC_BUF_SIZE      (6736)

typedef struct __ump3_enc_data {
    cbuffer_t cbuf_emp3_o;
    u8 obuf_emp3_o[MP3_ENC_OBUF_SIZE];
    u32 mp3_encode_buff[MP3_ENC_BUF_SIZE / 4];
} mp3_enc_data;
#define D_MP3_ENC_RESOURCE   (sizeof(mp3_enc_data))
#define MP3_ENC_CAL_BUF      ((void *)&p_mp3_data->mp3_encode_buff[0])

static enc_obj enc_mp3_hdl;

static const EN_FILE_IO mp3_enc_io = {
    &enc_mp3_hdl,      //input跟output函数的第一个参数，解码器不做处理，直接回传，可以为NULL
    enc_input,
    enc_output,
};
const int mp2_encode_channel = 1;

#define D_THIS         mp3_enc
#define D_THIS_NAME    "mp3_enc"
#define D_THIS_ENC_RESOURCE   D_MP3_ENC_RESOURCE
static u8 EXPAND_CONCAT(D_THIS, _mcnt) = 0;
D_DUMP_CNT();
u32 mp3_encoder_release(void *priv)
{
    enc_obj *obj = priv;
    if (obj == NULL) {
        return EINVAL;
    }

    D_FERR(obj->penc_res);
    return 0;
}
u32 mp3_encode_api(void *p_file)
{
    u32 buff_len;
    ENC_OPS *ops;
    log_info("mp3_encode_api\n");
    if (enc_mp3_hdl.penc_res != NULL) {
        log_error("enc_res_not_null 0x%x\n", enc_mp3_hdl.penc_res);
        mp3_encoder_release(&enc_mp3_hdl);
    }
    D_MALLOC(enc_mp3_hdl.penc_res,  D_THIS_ENC_RESOURCE);
    if (NULL == enc_mp3_hdl.penc_res) {
        log_error("mp3_enc_cann't_malloc_any_resource\n");
        return E_UMP3_ENC_RESOURCE;
    }
    mp3_enc_data *p_mp3_data = enc_mp3_hdl.penc_res;

    ops = get_mp2_ops();
    buff_len = ops->need_buf();
    if (buff_len > MP3_ENC_BUF_SIZE) {
        log_error("mp3_encode need_buf_size %d\n", buff_len);
        mp3_encoder_release(&enc_mp3_hdl);
        return 0;
    }

    //mp2编码，open前要清dbuf
    memset(MP3_ENC_CAL_BUF, 0x00, MP3_ENC_BUF_SIZE);
    /******************************************/
    cbuf_init(&p_mp3_data->cbuf_emp3_o, &p_mp3_data->obuf_emp3_o[0], MP3_ENC_OBUF_SIZE);
    log_info("A\n");
    // log_info("B\n");
    enc_mp3_hdl.p_file = p_file;
    enc_mp3_hdl.p_ibuf = REC_ADC_CBUF;//adc_hdl.p_adc_cbuf;//&cbuf_emp3_i;
    enc_mp3_hdl.p_obuf = &p_mp3_data->cbuf_emp3_o;
    enc_mp3_hdl.p_dbuf = MP3_ENC_CAL_BUF;
    enc_mp3_hdl.enc_ops = ops;
    enc_mp3_hdl.info.sr = read_audio_adc_sr();
    /*br取值表：{8,16,24,32,40,48,56,64,80,96,112,128,144,160}*/
    enc_mp3_hdl.info.br = 80;
    enc_mp3_hdl.info.nch = mp2_encode_channel;
    enc_mp3_hdl.encoder_res_release = mp3_encoder_release;


    log_info("D\n");
    /******************************************/
    ops->open(MP3_ENC_CAL_BUF, (void *)&mp3_enc_io);  //传入io接口，说明如下
    ops->set_info(MP3_ENC_CAL_BUF, &enc_mp3_hdl.info);
    ops->init(MP3_ENC_CAL_BUF);
    /* enc_mp3_hdl.enable = B_ENC_ENABLE; */
    //debug_u32hex(enc_mp3_hdl.enable);
    return (u32)&enc_mp3_hdl;
}
#endif
