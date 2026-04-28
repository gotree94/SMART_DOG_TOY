#include "cpu.h"
#include "config.h"
#include "typedef.h"
#include "decoder_api.h"
#include "vfs.h"
#include "circular_buf.h"
#include "dac.h"
/* #include "resample.h" */
#include "msg.h"
#include "errno-base.h"
#include "midi_api.h"
#include "MIDI_CTRL_API.h"
#include "MIDI_DEC_API.h"
#include "boot.h"
#include "decoder_msg_tab.h"
#include "dac.h"
#include "decoder_cpu.h"
#include "app_modules.h"

#if defined(DECODER_MIDI_EN) && (DECODER_MIDI_EN)

#define LOG_TAG_CONST       NORM
#define LOG_TAG             "[normal]"
#include "log.h"

/* midi乐谱解码最大同时发声的key数,该值影响音符的叠加,值越大需要的解码buffer越大,需要的buffer大小由need_dcbuf_size()获取 */
extern const int MAX_DEC_PLAYER_CNT;//该值在app_config.c中定义

/* param */
static u32 midi_tone_tab = 0;
/* decode */
dec_obj dec_midi_hld;
/* #define MIDI_CAL_BUF ((void *)&midi_decode_buff[0]) */

typedef struct _midi_dec_data {
    MIDI_INIT_STRUCT init_info;
    MIDI_CONFIG_PARM midi_t_parm;
    cbuffer_t cbuf_midi;
    u16 obuf_midi[MIDI_DEC_OBUF_SIZE / 2];
    u16 midi_decode_buff_nomark[MIDI_DEC_DBUF_SIZE / 2];
} midi_dec_data;

#define D_MIDI_DEC_RESOURCE  sizeof(midi_dec_data)

const struct if_decoder_io midi_dec_io0 = {
    &dec_midi_hld,      //input跟output函数的第一个参数，解码器不做处理，直接回传，可以为NULL
    mp_input,
    NULL,
    mp_output,
    NULL,
    NULL,
};

MIDI_PLAY_CTRL_MODE *get_midi_mode(void)
{
    midi_dec_data *p_midi_dec_data = dec_midi_hld.pdec_res;
    MIDI_INIT_STRUCT *t_init_info = &p_midi_dec_data->init_info;
    return &t_init_info->mode_info;
}

u32 *get_midi_switch_info(void)
{
    midi_dec_data *p_midi_dec_data = dec_midi_hld.pdec_res;
    MIDI_INIT_STRUCT *t_init_info = &p_midi_dec_data->init_info;
    return &t_init_info->switch_info;
}

static u8 midi_musicsr_to_cfgsr(u32 sr)
{
    const int midi_sr_tab[] = {
        48000,
        44100,
        32000,
        24000,
        22050,
        16000,
        12000,
        11025,
        8000,
    };

    for (int i = 0; i < sizeof(midi_sr_tab) / sizeof(midi_sr_tab[0]); i++) {
        if (sr == midi_sr_tab[i]) {
            return i;
        }
    }

    ASSERT(0, "midi error sr !!!:%d \n", sr);
    return 0;
}

__attribute__((weak))
void midi_init_info(MIDI_INIT_STRUCT *init_info)
{

}

#define D_THIS         midi_dec
#define D_THIS_NAME    "midi_dec"
#define D_THIS_DEC_RESOURCE   D_MIDI_DEC_RESOURCE
static u8 EXPAND_CONCAT(D_THIS, _mcnt) = 0;
D_DUMP_CNT();
u32 midi_decoder_res_release(void *priv)
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
u32 midi_decode_api(void *p_file, void **ppdec, void *p_dp_buf)
{
    dec_obj **p_dec = (dec_obj **)ppdec;
    u32 buff_len, sr;
    decoder_ops_t *ops;
    log_info("midi_decode_api\n");
    if (!midi_tone_tab) {
        return E_MIDI_NO_CFG;
    }

    if (dec_midi_hld.pdec_res != NULL) {
        //上次没释放
        log_error("dec_res_not_null 0x%x\n", &dec_midi_hld.pdec_res);
        midi_decoder_res_release(&dec_midi_hld);
    }
    local_irq_disable();
    memset(&dec_midi_hld, 0, sizeof(dec_obj));
    local_irq_enable();

    D_MALLOC(dec_midi_hld.pdec_res,  D_THIS_DEC_RESOURCE);
    if (NULL == dec_midi_hld.pdec_res) {
        log_error("midi_dec_cann't_malloc_any_resource\n");
        return E_MIDI_DEC_RESOURCE;
    }
    midi_dec_data *p_midi_dec_data = (void *)dec_midi_hld.pdec_res;
    u32 cal_buf_len;
    void *p_cal_buf;
    /* if (MIDI_MAX_MARK_CNT) { */
    /* cal_buf_len = sizeof(midi_decode_buff_full); */
    /* p_cal_buf = midi_decode_buff_full; */
    /* } else { */
    cal_buf_len = MIDI_DEC_DBUF_SIZE;
    p_cal_buf = p_midi_dec_data->midi_decode_buff_nomark;
    /* } */
    memset(p_cal_buf, 0, cal_buf_len);

    dec_midi_hld.type = D_TYPE_MIDI;

    ops = get_midi_ops();
    buff_len = ops->need_dcbuf_size();
    log_info("midi cal_buf_len %d\n", cal_buf_len);
    log_info("MIDI_DEC Need Buff Len:%d\n", buff_len);//buff大小会随MAX_DEC_PLAYER_CNT改变
    if (buff_len > cal_buf_len) {
        midi_decoder_res_release(&dec_midi_hld);
        return E_MIDI_DBUF;
    }
    /******************************************/
    cbuf_init(&p_midi_dec_data->cbuf_midi, &p_midi_dec_data->obuf_midi[0], MIDI_DEC_OBUF_SIZE);
    /* char name[VFS_FILE_NAME_LEN] = {0}; */
    int file_len = vfs_file_name(p_file, (void *) g_file_sname, sizeof(g_file_sname));
    /* u8 *name = vfs_file_name(p_file); */
    log_info("file name : %s\n", g_file_sname);

    dec_midi_hld.p_file = p_file;
    dec_midi_hld.sound.p_obuf = &p_midi_dec_data->cbuf_midi;
    dec_midi_hld.p_dbuf = p_cal_buf;
    dec_midi_hld.dec_ops = ops;
    dec_midi_hld.event_tab = (u8 *)&midi_evt[0];
    dec_midi_hld.decoder_res_release = midi_decoder_res_release;
    //
    /******************************************/
    ops->open(p_cal_buf, &midi_dec_io0, NULL);         //传入io接口，说明如下
    if (ops->format_check(p_cal_buf)) {                  //格式检查
        midi_decoder_res_release(&dec_midi_hld);
        return E_MIDIFORMAT;
    }
    /******************************************************************************/
    sr = dac_sr_read();                //获取采样率
    dec_midi_hld.sr = sr;
    //dac_sr_api(sr);
    /* log_info(">>>>>>>sr:%d \n", sr); */
    /**************相对MP3的调用流程多了这个，其他一致。这个一定要配置***************/
    p_midi_dec_data->midi_t_parm.player_t = MAX_DEC_PLAYER_CNT;                                //设置需要合成的最多按键个数，8到32可配
    p_midi_dec_data->midi_t_parm.sample_rate = midi_musicsr_to_cfgsr(sr);                            //采样率设为16k
    p_midi_dec_data->midi_t_parm.spi_pos = (u16 *)midi_tone_tab;                    //spi_memory为音色文件数据起始地址
    /* log_info_hexdump(midi_t_parm.spi_pos, 16); */
    memset((u8 *)&p_midi_dec_data->init_info, 0x00, sizeof(MIDI_INIT_STRUCT));
    p_midi_dec_data->init_info.init_info = p_midi_dec_data->midi_t_parm;
    midi_init_info(&p_midi_dec_data->init_info);
    ops->dec_confing(p_cal_buf, CMD_INIT_CONFIG, &p_midi_dec_data->init_info);

    /**输出dec handle*/
    *p_dec = &dec_midi_hld;

    regist_dac_channel(&dec_midi_hld.sound, kick_decoder); //注册到DAC;

    return 0;
}


u32 midi_decode_init(void)
{
    void *pvfs = 0;
    void *pvfile = 0;
    u32 err = 0;

    err = vfs_mount(&pvfs, (void *)NULL, (void *)NULL);
    if (err != 0) {
        return err;
    }

    err = vfs_openbypath(pvfs, &pvfile, "/midi_cfg/00_MIDI.mda");
    if (err != 0) {
        log_info("midi dec mda open fail, try old midi_cfg.bin!\n");
        err = vfs_openbypath(pvfs, &pvfile, "/midi_cfg/midi_cfg.bin");
        if (err != 0) {
            vfs_fs_close(&pvfs);
            return err;
        }
    }

    ///获取midi音色库的cache地址
    struct vfs_attr attr;
    vfs_get_attrs(pvfile, &attr);
    midi_tone_tab = get_app_addr() + attr.sclust;

    vfs_file_close(&pvfile);
    vfs_fs_close(&pvfs);
    return 0;
}

/* extern const u8 midi_buf_start[]; */
/* extern const u8 midi_buf_end[]; */
u32 midi_buff_api(dec_buf *p_dec_buf)
{
    /* p_dec_buf->start = (u32)&midi_buf_start[0]; */
    /* p_dec_buf->end   = (u32)&midi_buf_end[0]; */
    return 0;
}
#endif

