#pragma bss_seg(".linein_mode.data.bss")
#pragma data_seg(".linein_mode.data")
#pragma const_seg(".linein_mode.text.const")
#pragma code_seg(".linein_mode.text")
#pragma str_literal_override(".linein_mode.text.const")

#include "linein_mode.h"
#include "app_modules.h"
#include "common.h"
#include "msg.h"
/* #include "ui_api.h" */
#include "hot_msg.h"
#include "circular_buf.h"
#include "jiffies.h"
#include "pa_mute.h"
#include "vm_api.h"
#include "sound_effect_api.h"
#include "audio_adc.h"
#include "dac_api.h"

#define LOG_TAG_CONST       NORM
#define LOG_TAG             "[aux]"
#include "log.h"

#if LINEIN_MODE_EN

#define LINEIN_SR  SR_DEFAULT
cbuffer_t cbuf_digital_linein        AT(.aux_data);
u16 obuf_digital_linein[1024 / 2]    AT(.aux_data);
sound_out_obj digital_linein_sound     AT(.aux_data);

void linein_app(void)
{
    vm_write(VM_INDEX_SYSMODE, &work_mode, sizeof(work_mode));
    /* SET_UI_MAIN(MENU_AUX_MAIN); */
    /* UI_menu(MENU_AUX_MAIN, 0); */
    key_table_sel(linein_key_msg_filter);

    log_info("digital linein init!\n");
    memset(&digital_linein_sound, 0, sizeof(digital_linein_sound));
    cbuf_init(&cbuf_digital_linein, &obuf_digital_linein[0], sizeof(obuf_digital_linein));
    digital_linein_sound.p_obuf = &cbuf_digital_linein;

    u32 sr = dac_sr_read();
    /* log_info("dac_sr %d\n", sr); */
    dac_sr_api(LINEIN_SR);
    audio_adc_init_api(LINEIN_SR, ADC_LINE_IN, audio_adc_aux_input_port);
    regist_dac_channel(&digital_linein_sound, NULL);
    regist_audio_adc_channel(&digital_linein_sound, NULL);

    audio_adc_enable(1);
    digital_linein_sound.enable |= B_DEC_RUN_EN | B_DEC_FIRST;

    int msg[2];
    u32 err;
    u8 mute = 0;
    while (1) {
        err = get_msg(2, &msg[0]);
        bsp_loop();

        if (MSG_NO_ERROR != err) {
            msg[0] = NO_MSG;
            log_info("get msg err 0x%x\n", err);
        }
        switch (msg[0]) {
        case MSG_PP:
            if (mute == 0) {
                log_info("MUTE\n");
                mute = 1;
                dac_mute(1);
                /* pa_mute(1); */
            } else {
                log_info("UNMUTE\n");
                mute = 0;
                dac_mute(0);
                /* pa_mute(0); */
            }
            break;
        case MSG_CHANGE_WORK_MODE:
            goto __linein_app_exit;
        case MSG_500MS:
        /* UI_menu(MENU_MAIN, 0); */
        default:
            ap_handle_hotkey(msg[0]);
            break;
        }
    }
__linein_app_exit:
    if (0 != mute) {
        dac_mute(0);
        /* pa_mute(0); */
    }
    digital_linein_sound.enable &= ~B_DEC_RUN_EN;
    audio_adc_disable();
    unregist_audio_adc_channel(&digital_linein_sound);
    unregist_dac_channel(&digital_linein_sound);
    audio_adc_off_api();
    dac_sr_api(sr);
    /* SET_UI_MAIN(MENU_POWER_UP); */
    /* UI_menu(MENU_POWER_UP, 0); */
    key_table_sel(NULL);
}

#endif
