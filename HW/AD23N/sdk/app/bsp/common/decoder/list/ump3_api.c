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

#if defined(DECODER_UMP3_EN) && (DECODER_UMP3_EN)

#define LOG_TAG_CONST       NORM
#define LOG_TAG             "[normal]"
#include "log.h"

#define UMP3_DBUF_SIZE (0x1a18)
#define ump3_OBUF_SIZE DAC_DECODER_BUF_SIZE
dec_obj dec_ump3_hld;

typedef struct _ump3_dec_data {
    cbuffer_t cbuf_ump3;
    u16 obuf_ump3[ump3_OBUF_SIZE / 2];
    u32 ump3_decode_buff[UMP3_DBUF_SIZE / 4];
} ump3_dec_data;

#define D_UMP3_DEC_RESOURCE    (sizeof(ump3_dec_data))
#define ump3_CAL_BUF ((void *)&p_ump3_data->ump3_decode_buff[0])

const struct if_decoder_io ump3_dec_io0 = {
    &dec_ump3_hld,      //input跟output函数的第一个参数，解码器不做处理，直接回传，可以为NULL
    mp_input,
    NULL,
    mp_output,
    NULL,
    NULL,
};

#define D_THIS         ump3_dec
#define D_THIS_NAME    "ump3_dec"
#define D_THIS_DEC_RESOURCE   D_UMP3_DEC_RESOURCE
static u8 EXPAND_CONCAT(D_THIS, _mcnt) = 0;
D_DUMP_CNT();
u32 ump3_decoder_res_release(void *priv)
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
u32 ump3_decode_api(void *p_file, void **p_dec, void *p_dp_buf)
{
    u32 buff_len, i;
    /* void *name; */
    /* char name[VFS_FILE_NAME_LEN] = {0}; */
    decoder_ops_t *ops;
    log_info("ump3_decode_api\n");
    if (dec_ump3_hld.pdec_res != NULL) {
        //上次没释放
        log_error("dec_res_not_null 0x%x\n", dec_ump3_hld.pdec_res);
        ump3_decoder_res_release(&dec_ump3_hld);
    }

    memset(&dec_ump3_hld, 0, sizeof(dec_obj));
    D_MALLOC(dec_ump3_hld.pdec_res,  D_THIS_DEC_RESOURCE);
    if (NULL == dec_ump3_hld.pdec_res) {
        log_error("ump3_dec_cann't_malloc_any_resource\n");
        return E_UMP3_DEC_RESOURCE;
    }
    ump3_dec_data *p_ump3_data = (void *)dec_ump3_hld.pdec_res;
    dec_ump3_hld.type = D_TYPE_UMP3;
    ops = get_ump3_ops();
    buff_len = ops->need_dcbuf_size();
    if (buff_len > UMP3_DBUF_SIZE) {
        log_info("ump3 file dbuff : 0x%x 0x%lx\n", buff_len, UMP3_DBUF_SIZE);
        ump3_decoder_res_release(&dec_ump3_hld);
        return E_UMP3_DBUF;
    }
    /******************************************/
    cbuf_init(&p_ump3_data->cbuf_ump3, &p_ump3_data->obuf_ump3[0], ump3_OBUF_SIZE);
    dec_ump3_hld.p_file       = p_file;
    dec_ump3_hld.sound.p_obuf = &p_ump3_data->cbuf_ump3;
    dec_ump3_hld.p_dbuf       = ump3_CAL_BUF;
    dec_ump3_hld.dec_ops      = ops;
    dec_ump3_hld.event_tab    = (u8 *)&ump3_evt[0];
    dec_ump3_hld.p_dp_buf     = p_dp_buf;
    dec_ump3_hld.decoder_res_release = ump3_decoder_res_release;
    //dac reg
    // dec_ump3_hld.dac.obuf = &cbuf_ump3;
    // dec_ump3_hld.dac.vol = 255;
    // dec_ump3_hld.dac.index = reg_channel2dac(&dec_ump3_hld.dac);
    /******************************************/

    /* name = vfs_file_name(p_file); */
    int file_len = vfs_file_name(p_file, (void *)g_file_sname, sizeof(g_file_sname));
    log_info("file name : %s\n", g_file_sname);
    log_info(" -ump3 open\n");
    ops->open(ump3_CAL_BUF, &ump3_dec_io0, p_dp_buf);         //传入io接口，说明如下
    log_info(" -ump3 open over\n");
    if (ops->format_check(ump3_CAL_BUF)) {                  //格式检查
        log_info(" ump3 format err : %s\n", g_file_sname);
        ump3_decoder_res_release(&dec_ump3_hld);
        return E_UMP3_FORMAT;
    }

    regist_dac_channel(&dec_ump3_hld.sound, kick_decoder);//注册到DAC;
    i = ops->get_dec_inf(ump3_CAL_BUF)->sr;                //获取采样率
    dec_ump3_hld.sr = i;
    log_info("file sr : %d\n", i);
    *p_dec = (void *)&dec_ump3_hld;
    return 0;
    /* dec_ump3_hld.enable = B_DEC_ENABLE | B_DEC_KICK; */
    /* debug_u32hex(dec_ump3_hld.enable); */
    /* kick_decoder(); */
    /* return 0; */
}

/* extern const u8 ump3_buf_start[]; */
/* extern const u8 ump3_buf_end[]; */
u32 ump3_buff_api(dec_buf *p_dec_buf)
{
    /* p_dec_buf->start = (u32)&ump3_buf_start[0]; */
    /* p_dec_buf->end   = (u32)&ump3_buf_end[0]; */
    return 0;
}
#endif
