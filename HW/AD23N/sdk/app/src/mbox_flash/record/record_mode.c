#pragma bss_seg(".record_mode.data.bss")
#pragma data_seg(".record_mode.data")
#pragma const_seg(".record_mode.text.const")
#pragma code_seg(".record_mode.text")
#pragma str_literal_override(".record_mode.text.const")

#include "record_mode.h"
#include "device_mge.h"
#include "vfs.h"
#include "msg.h"
/* #include "ui_api.h" */
#include "hot_msg.h"
#include "encoder_mge.h"
/* #include "music_play.h" */
#include "simple_play_file.h"
#include "app.h"
#include "jiffies.h"
#include "vm_api.h"
#include "decoder_api.h"
#include "decoder_msg_tab.h"
#include "dac_api.h"
#include "audio_adc.h"

#include "mp3_encoder.h"
#include "a_encoder.h"
#if ENCODER_MP3_EN
#define ENCODER_API  mp3_encode_api
#elif ENCODER_UMP3_EN
#define ENCODER_API  ump3_encode_api
#elif ENCODER_A_EN
#define ENCODER_API  a_encode_api
#endif

#define LOG_TAG_CONST       NORM
#define LOG_TAG             "[rec]"
#include "log.h"

#if RECORD_MODE_EN

#define RECORD_AUDIO_ADC_SR     RECORD_ADC_SR_32K
Encode_Control record_obj;

void record_app(void)
{
    vm_write(VM_INDEX_SYSMODE, &work_mode, sizeof(work_mode));
    log_info("record_app\n");
    int msg[2];
    u32 err = 0;
    dec_obj *p_dec_obj = 0;
    /* u16 decode_type = (BIT_A | BIT_UMP3 | BIT_SPEED); */
    u16 decode_type = (BIT_MP3_ST | BIT_A | BIT_UMP3);
    key_table_sel(record_key_msg_filter);
    decoder_init();

    memset(&record_obj, 0, sizeof(record_obj));

    while (1) {
        err = get_msg(2, &msg[0]);
        bsp_loop();
        if (MSG_NO_ERROR != err) {
            msg[0] = NO_MSG;
            log_info("get msg err 0x%x\n", err);
        }

        switch (msg[0]) {
        case MSG_RECODE_START:
            if (ENC_ING == record_obj.enc_status) {
                /* 结束录音并播放录音 */
                encode_stop(&record_obj);
                post_msg(1, MSG_PP);
            } else {
                /* 开始录音 */
                decoder_stop(p_dec_obj, NEED_WAIT);
                encode_file_fs_close(&record_obj);
                u32 enc_err = 0;
                enc_err = encode_start(&record_obj);
                if (enc_err) {
                    log_error("enc_err 0x%x\n", enc_err);
                    break;
                }
                log_info("dev:%d fs_name:%s\n", record_obj.dev_index, record_obj.fs_name);
            }
            break;
        case MSG_PP:
            encode_stop(&record_obj);
            decoder_stop(p_dec_obj, NEED_WAIT);
            dec_obj *(*enc_file_decode)(Encode_Control * obj, u16 dec_type) = NULL;

            if (0 == (strcmp(record_obj.fs_name, "fat"))) {
                enc_file_decode = fatfs_enc_file_decode;
            } else if (0 == (strcmp(record_obj.fs_name, "norfs"))) {
                enc_file_decode = norfs_enc_file_decode;
            } else {
                log_info("record hasn't been started!\n");
                break;
            }
            p_dec_obj = enc_file_decode(&record_obj, decode_type);
            if (NULL == p_dec_obj) {
                log_info("record file decode fail!\n");
                encode_file_fs_close(&record_obj);
            }
            break;

        case MSG_REC_SPEED_EN:
            if (decode_type & BIT_SPEED) {
                log_info("record normal mode \n");
                decode_type &= ~BIT_SPEED;
            } else {
                log_info("record speed mode \n");
                decode_type |= BIT_SPEED;
            }
            break;
        case MSG_WFILE_FULL:
            log_info("MSG_WFILE_FULL\n");
            encode_stop(&record_obj);
            break;
        case MSG_WAV_FILE_END:
        case MSG_WAV_FILE_ERR:
        case MSG_MP3_FILE_END:
        case MSG_MP3_FILE_ERR:
        case MSG_A_FILE_END:
        case MSG_A_FILE_ERR:
            decoder_stop(p_dec_obj, NEED_WAIT);
            encode_file_fs_close(&record_obj);
            break;

        case MSG_CHANGE_WORK_MODE:
            goto __record_app_exit;
        case MSG_500MS:
            /* UI_menu(MENU_MAIN, 0); */
            if (record_obj.enc_status == ENC_ING) {
                log_char('R');
            }
            if ((MUSIC_PLAY != get_decoder_status(p_dec_obj)) && \
                (record_obj.enc_status == ENC_NULL)) {
                vm_pre_erase();
                app_powerdown_deal(0);
            } else {
                app_powerdown_deal(1);
            }
        default:
            ap_handle_hotkey(msg[0]);
            break;
        }
    }

__record_app_exit:
    key_table_sel(NULL);
    if (ENC_ING == record_obj.enc_status) {
        encode_stop(&record_obj);
    } else {
        decoder_stop(p_dec_obj, NEED_WAIT);
        encode_file_fs_close(&record_obj);
    }
    return;
}

void encode_file_fs_close(Encode_Control *obj)
{
    vfs_file_close(&obj->pfile);
    vfs_fs_close(&obj->pfs);
    device_close(obj->dev_index);
}

static void encode_stop(Encode_Control *obj)
{
    if (ENC_ING == obj->enc_status) {
        stop_encode(obj->enc_obj, obj->pfile, 0);
        encode_file_fs_close(obj);
        obj->enc_status = ENC_NULL;
    }
    audio_adc_off_api();
}

const u8 recoder_device_tab[3] = {
    UDISK_INDEX,
    SD0_INDEX,
    INNER_FLASH_RW
};
static int encode_start(Encode_Control *obj)
{
    u32 sr = RECORD_AUDIO_ADC_SR;
    /* u32 sr = dac_sr_read(); */
    /* log_info("adc sr:%d\n", sr); */
    int err = audio_adc_init_api(sr, ADC_MIC, audio_adc_mic_input_port);
    if (0 != err) {
        log_info(" audio adc init fail : 0x%x\n");
        return err;
    }

    u32 online_dev = device_online();
    u8 index = -1;
    for (u8 i = 0; i <= sizeof(recoder_device_tab); i++) {
        index = recoder_device_tab[i];
        if (online_dev & BIT(index)) {
            break;
        }
    }

    const char *p_fs_name = NULL;
    int (*enc_file_create)(Encode_Control * obj) = NULL;
    if (index > SD0_INDEX) {
        p_fs_name = "norfs";//内置flash录音
        enc_file_create = norfs_enc_file_create;
    } else {
        p_fs_name = "fat";//SD卡、U盘录音
        enc_file_create = fatfs_enc_file_create;
    }
    obj->dev_index = index;
    strcpy(obj->fs_name, p_fs_name);
    err = enc_file_create(obj);
    if (0 != err) {
        log_error("%s create 0x%x!\n", p_fs_name, err);
        return err;
    }
    record_obj.enc_obj = encoder_io(ENCODER_API, obj->pfile);
    /* record_obj.enc_obj = encoder_io(a_encode_api, obj->pfile); */
    if (NULL == record_obj.enc_obj) {
        return E_ENC_HDL_NULL;
    }
    obj->enc_status = ENC_ING;
    return 0;
}


#endif
