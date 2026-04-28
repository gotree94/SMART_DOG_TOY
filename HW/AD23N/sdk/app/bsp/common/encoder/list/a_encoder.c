#include "encoder_mge.h"
#include "cpu.h"
#include "config.h"
#include "typedef.h"
#include "hwi.h"
#include "app_modules.h"
#include "circular_buf.h"

#if defined(ENCODER_A_EN) && (ENCODER_A_EN)

#define LOG_TAG_CONST       NORM
#define LOG_TAG             "[normal]"
#include "log.h"

#define A_ENC_OBUF_SIZE     (1024)
#define A_ENC_BUF_SIZE      (368)

typedef struct __a_enc_data {
    cbuffer_t cbuf_ima_o;
    u8 obuf_ima_o[A_ENC_OBUF_SIZE];
    u8 a_encode_buff[A_ENC_BUF_SIZE];
} a_enc_data;
#define D_A_ENC_RESOURCE   (sizeof(a_enc_data))
#define A_ENC_CAL_BUF ((void *)&p_a_data->a_encode_buff[0])

enc_obj enc_a_hdl;

const EN_FILE_IO a_enc_io = {
    &enc_a_hdl,      //input跟output函数的第一个参数，解码器不做处理，直接回传，可以为NULL
    enc_input,
    enc_output,
};

#define D_THIS         a_enc
#define D_THIS_NAME    "a_enc"
#define D_THIS_ENC_RESOURCE   D_A_ENC_RESOURCE
static u8 EXPAND_CONCAT(D_THIS, _mcnt) = 0;
D_DUMP_CNT();

u32 a_encoder_release(void *priv)
{
    enc_obj *obj = priv;
    if (obj == NULL) {
        return EINVAL;
    }

    D_FERR(obj->penc_res);
    return 0;
}

u32 a_encode_api(void *p_file)
{
    u32 buff_len;
    ENC_OPS *ops;
    /* debug_puts("a_encode_api\n"); */
    if (enc_a_hdl.penc_res != NULL) {
        log_error("enc_res_not_null 0x%x\n", enc_a_hdl.penc_res);
        a_encoder_release(&enc_a_hdl);
    }
    D_MALLOC(enc_a_hdl.penc_res,  D_THIS_ENC_RESOURCE);
    if (NULL == enc_a_hdl.penc_res) {
        log_error("a_enc_cann't_malloc_any_resource\n");
        return E_A_ENC_RESOURCE;
    }
    a_enc_data *p_a_data = enc_a_hdl.penc_res;
    ops = get_ima_code_ops();
    buff_len = ops->need_buf();
    if (buff_len > A_ENC_BUF_SIZE) {
        log_error("buff_len no enough, need %d\n", buff_len);
        a_encoder_release(&enc_a_hdl);
        return 0;
    }
    /******************************************/
    cbuf_init(&p_a_data->cbuf_ima_o, &p_a_data->obuf_ima_o[0], A_ENC_OBUF_SIZE);
    /* debug_puts("A\n"); */
    // debug_puts("B\n");
    enc_a_hdl.p_file = p_file;
    /* debug_u32hex((u32)p_file); */
    enc_a_hdl.p_ibuf = REC_ADC_CBUF; //adc_hdl.p_adc_cbuf;//&cbuf_ima_i;
    enc_a_hdl.p_obuf = &p_a_data->cbuf_ima_o;
    enc_a_hdl.p_dbuf = A_ENC_CAL_BUF;
    enc_a_hdl.enc_ops = ops;
    enc_a_hdl.info.sr = read_audio_adc_sr();
    enc_a_hdl.info.br = 256;
    enc_a_hdl.info.nch = 1;
    enc_a_hdl.encoder_res_release = a_encoder_release;

    /* debug_puts("D\n"); */
    /******************************************/
    ops->open(A_ENC_CAL_BUF, (void *)&a_enc_io);  //传入io接口，说明如下
    ops->set_info(A_ENC_CAL_BUF, &enc_a_hdl.info);
    ops->init(A_ENC_CAL_BUF);
    /* enc_a_hdl.enable = B_ENC_ENABLE; */
    //debug_u32hex(enc_a_hdl.enable);
    return (u32)&enc_a_hdl;
}
#endif
