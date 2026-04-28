#pragma bss_seg(".app.data.bss")
#pragma data_seg(".app.data")
#pragma const_seg(".app.text.const")
#pragma code_seg(".app.text")
#pragma str_literal_override(".app.text.const")

#include "app_modules.h"
#include "app_config.h"
#include "config.h"
#include "common.h"
#include "app.h"
#include "msg.h"
#include "music_play.h"
/* #include "usb_slave_mode.h" */
#include "linein_mode.h"
#include "record_mode.h"
#include "simple_decode.h"
#include "midi_dec_mode.h"
#include "midi_keyboard_mode.h"
#include "loudspk_mode.h"
#include "softoff_mode.h"
#include "idle_mode.h"
#include "jiffies.h"
#include "ui_api.h"
#include "key.h"
#include "dac_api.h"
#include "vm_api.h"
#include "device.h"
#include "bsp_loop.h"

#define LOG_TAG_CONST       NORM
#define LOG_TAG             "[mbox_app]"
#include "log.h"

u8 work_mode;

AT(.tick_timer.text.cache.L2)
void tick_timer_ram_loop(void)
{
    /* 该函数2ms回调一次，该函数中所有内容需在ram运行 */
#if LED_5X7
    LED5X7_scan();
#endif
}

void app_timer_loop(void)
{
    static u16 cnt = 0;
    cnt++;
#if TCFG_PC_ENABLE
    if (0 == (cnt % 10)) {
        uac_inc_sync();
    }
    if (0 == (cnt % 500)) {
        /* uac_1s_sync(); */
    }
#endif
    if (cnt >= 1000) {
        cnt = 0;
    }
}

void app_next_mode(void)
{
    work_mode++;
    if (work_mode >= MAX_LOOP_MODE) {
        work_mode = MUSIC_MODE;
    }
}


void app(void)
{
    log_info("Mbox-Flash App\n");

    delay_10ms(50);//等待系统稳定
    /* pa_mute(0); */

    /* UI */
    UI_init();
    SET_UI_MAIN(MENU_POWER_UP);
    UI_menu(MENU_POWER_UP, 0);

    u8 vol = 0;
    u32 res = vm_read(VM_INDEX_VOL, &vol, sizeof(vol));
    if ((vol <= 31) && (res == sizeof(vol))) {
        dac_vol(0, vol);
        log_info("powerup set vol : %d\n", vol);
    }
    /* work_mode = MUSIC_MODE; */
    /* work_mode = RECORD_MODE; */
    /* work_mode = AUX_MODE; */
    /* work_mode = MIDI_DEC_MODE; */
    /* work_mode = MIDI_KEYBOARD_MODE; */
    work_mode = SIMPLE_DEC_MODE;
    /* work_mode = LOUDSPEAKER_MODE; */
    /* work_mode = IDLE_MODE; */
    while (1) {
        clear_all_message();
        //切换模式前做预擦除动作
        vm_pre_erase();
        switch (work_mode) {
#if MUSIC_MODE_EN
        case MUSIC_MODE:
            music_app();
            break;
#endif
#if TCFG_PC_ENABLE
        case USB_SLAVE_MODE:
            usb_slave_app();
            break;
#endif
#if SIMPLE_DEC_EN
        case SIMPLE_DEC_MODE:
            simple_decode_app();
            break;
#endif
#if RECORD_MODE_EN
        case RECORD_MODE:
            record_app();
            break;
#endif
#if LINEIN_MODE_EN
        case AUX_MODE:
            linein_app();
            break;
#endif
#if LOUDSPEAKER_EN
        case LOUDSPEAKER_MODE:
            loudspeaker_app();
            break;
#endif
#if DECODER_MIDI_EN
        case MIDI_DEC_MODE:
            midi_decode_app();
            break;
#endif
#if DECODER_MIDI_KEYBOARD_EN
        case MIDI_KEYBOARD_MODE:
            midi_keyboard_app();
            break;
#endif
        case IDLE_MODE:
            idle_app();
            break;
        case SOFTOFF_MODE:
            softoff_app();
            break;
        default:
            work_mode++;
            if (work_mode >= MAX_WORK_MODE) {
                work_mode = MUSIC_MODE;
            }
            break;
        }
    }
}




