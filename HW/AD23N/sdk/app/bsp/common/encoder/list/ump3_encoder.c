#include "encoder_mge.h"
#include "cpu.h"
#include "config.h"
#include "typedef.h"
#include "hwi.h"
#include "app_modules.h"
#include "circular_buf.h"

#if defined(ENCODER_UMP3_EN) && (ENCODER_UMP3_EN)

#define LOG_TAG_CONST       NORM
#define LOG_TAG             "[normal]"
#include "log.h"

#define UMP3_ENC_OBUF_SIZE     (1024)
#define UMP3_ENC_BUF_SIZE      (3320)

typedef struct __ump3_enc_data {
    cbuffer_t cbuf_ump3_o;
    u8 obuf_ump3_o[UMP3_ENC_OBUF_SIZE];
    u32 ump3_encode_buff[UMP3_ENC_BUF_SIZE / 4];
} ump3_enc_data;
#define D_UMP3_ENC_RESOURCE   (sizeof(ump3_enc_data))
#define UMP3_ENC_CAL_BUF      ((void *)&p_ump3_data->ump3_encode_buff[0])

static enc_obj enc_ump3_hdl;

static const EN_FILE_IO ump3_enc_io = {
    &enc_ump3_hdl,      //input跟output函数的第一个参数，解码器不做处理，直接回传，可以为NULL
    enc_input,
    enc_output,
};

#define D_THIS         ump3_enc
#define D_THIS_NAME    "ump3_enc"
#define D_THIS_ENC_RESOURCE   D_UMP3_ENC_RESOURCE
static u8 EXPAND_CONCAT(D_THIS, _mcnt) = 0;
D_DUMP_CNT();
u32 ump3_encoder_release(void *priv)
{
    enc_obj *obj = priv;
    if (obj == NULL) {
        return EINVAL;
    }

    D_FERR(obj->penc_res);
    return 0;
}
u32 ump3_encode_api(void *p_file)
{
    /* memset(&enc_ump3_hdl, 0, sizeof(enc_obj)); */
    /* memset(&obuf_ump3_o[0], 0x00, sizeof(obuf_ump3_o)); */
    /* memset(UMP3_ENC_CAL_BUF, 0x00, UMP3_ENC_BUF_SIZE); */

    u32 buff_len;
    ENC_OPS *ops;
    log_info("ump3_encode_api\n");
    if (enc_ump3_hdl.penc_res != NULL) {
        log_error("enc_res_not_null 0x%x\n", enc_ump3_hdl.penc_res);
        ump3_encoder_release(&enc_ump3_hdl);
    }
    D_MALLOC(enc_ump3_hdl.penc_res,  D_THIS_ENC_RESOURCE);
    if (NULL == enc_ump3_hdl.penc_res) {
        log_error("ump3_enc_cann't_malloc_any_resource\n");
        return E_UMP3_ENC_RESOURCE;
    }
    ump3_enc_data *p_ump3_data = enc_ump3_hdl.penc_res;
    ops = get_ump2_ops();
    buff_len = ops->need_buf();
    if (buff_len > UMP3_ENC_BUF_SIZE) {
        log_error("ump3_enc_buf_not_enough need 0x%x, has 0x%x\n", buff_len, UMP3_ENC_BUF_SIZE);
        ump3_encoder_release(&enc_ump3_hdl);
        return 0;
    }
    /******************************************/
    cbuf_init(&p_ump3_data->cbuf_ump3_o, &p_ump3_data->obuf_ump3_o[0], UMP3_ENC_OBUF_SIZE);
    log_info("A\n");
    // log_info("B\n");
    enc_ump3_hdl.p_file = p_file;
    enc_ump3_hdl.p_ibuf = REC_ADC_CBUF;//adc_hdl.p_adc_cbuf;//&cbuf_emp3_i;
    enc_ump3_hdl.p_obuf = &p_ump3_data->cbuf_ump3_o;
    enc_ump3_hdl.p_dbuf = UMP3_ENC_CAL_BUF;
    enc_ump3_hdl.enc_ops = ops;
    enc_ump3_hdl.info.sr = read_audio_adc_sr();
    /*br的范围是：sr*16/压缩比，压缩比的范围是3~8
     * 例如sr = 24k，那么br的范围是 (24*16/8) ~ (24*16/3)*/
    enc_ump3_hdl.info.br = 80;
    enc_ump3_hdl.info.nch = 1;
    enc_ump3_hdl.encoder_res_release = ump3_encoder_release;

    log_info("D\n");
    /******************************************/
    ops->open(UMP3_ENC_CAL_BUF, (void *)&ump3_enc_io);           //传入io接口，说明如下
    ops->set_info(UMP3_ENC_CAL_BUF, &enc_ump3_hdl.info);
    ops->init(UMP3_ENC_CAL_BUF);
    /* enc_ump3_hdl.enable = B_ENC_ENABLE; */
    //debug_u32hex(enc_ump3_hdl.enable);
    return (u32)&enc_ump3_hdl;
}
#endif
