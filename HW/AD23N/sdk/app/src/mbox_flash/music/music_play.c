#pragma bss_seg(".music_play.data.bss")
#pragma data_seg(".music_play.data")
#pragma const_seg(".music_play.text.const")
#pragma code_seg(".music_play.text")
#pragma str_literal_override(".music_play.text.const")

#include "music_play.h"
#include "music_device.h"
#include "play_file.h"
#include "device_mge.h"
#include "vfs.h"
#include "vm_api.h"
#include "msg.h"
#include "jiffies.h"
#include "ui_api.h"
#include "hot_msg.h"
/* #include "power_api.h" */
/* #include "asm/power_interface.h" */
/* #include "pa_mute.h" */

#include "decoder_api.h"
#include "decoder_msg_tab.h"

#include "dac_api.h"
/* #include "dev_update.h" */

/* #include "usb/host/usb_host.h" */
/* #include "usb/device/usb_stack.h" */
/* #include "usb/otg.h" */

#define LOG_TAG_CONST       NORM
#define LOG_TAG             "[music]"
#include "log.h"

#if MUSIC_MODE_EN

static const char *const dir_inr_tab[] = {
    "/dir_song",
};
/* 复用变量 -->start */
play_control pctl[1] AT(.mode_music_overlay_data);
u8 err_device AT(.mode_music_overlay_data);
static dp_buff breakpoint[1] AT(.mode_music_overlay_data);
/* 复用变量 -->end */
u8 dec_eq_mode = 0;

static void music_info_init(u8 *p_dev)
{
#if KEY_IR_EN
    Sys_IRInput = 1;
#endif
    key_table_sel(music_key_msg_filter);
    decoder_init();
    dac_fade_out_api();
    err_device = 0;
    fsn_music = 0;
    err_device = 0;

    memset(&pctl[0], 0, sizeof(pctl));
    memset(&dev_scan_info[0], 0, sizeof(dev_scan_info));
    memset(&breakpoint[0], 0, sizeof(breakpoint));

    pctl[0].pdp = &breakpoint[0];
    pctl[0].dev_index = NO_DEVICE;
    pctl[0].dec_type = BIT_WAV | BIT_MP3_ST | BIT_F1A1 | BIT_A | BIT_UMP3;  //播放需要使用的解码器
    pctl[0].pdir = (void *)&dir_inr_tab[0];
    pctl[0].dir_index = 0;
    u8 device;
    if (sizeof(u8) == vm_read(VM_INDEX_ACTIVE_DEV, &device, sizeof(u8))) {
        *p_dev = device;
    } else {
        *p_dev = 0;
    }
}
int decoder_eq_mode_switch(dec_obj *obj)
{
    if ((obj == NULL) || (obj->eq == NULL)) {
        return -1;
    }
    dec_eq_mode++;
    u32 ret = obj->eq(obj, dec_eq_mode);
    if (ret != -1) {
        dec_eq_mode = ret;
    } else {
        dec_eq_mode = 0;
        ret = obj->eq(obj, dec_eq_mode);
    }
    log_info("switch dec_eq mode:%d \n", ret);
    return ret;
}
void music_app(void)
{
    log_info("run music_app\n");
    vm_write(VM_INDEX_SYSMODE, &work_mode, sizeof(work_mode));
    u32 dac_sr = dac_sr_read();
    dac_sr_api(SR_DEFAULT);
    u8 music_vol, dindex, used_device;
    int msg[2], err;

    music_info_init(&used_device);
    post_msg(1, MSG_MUSIC_SELECT_NEW_DEVICE);

    while (1) {
        err = get_msg(2, &msg[0]);
        bsp_loop();

        if (MSG_NO_ERROR != err) {
            msg[0] = NO_MSG;
            log_info("get msg err 0x%x\n", err);
        }

        switch (msg[0]) {
        case MSG_MUSIC_NEXT_EQ:
            decoder_eq_mode_switch((void *)((pctl[0].p_dec_obj)));
            if (dec_eq_mode != (u8)(-1)) {
                UI_menu(MENU_DEC_EQ, dec_eq_mode);
            }
            break;
        case MSG_PP:
            log_info("PP\n");
            decoder_pause(pctl[0].p_dec_obj);
            if ((0 == (pctl[0].p_dec_obj->sound.enable & B_DEC_PAUSE))) {
                SET_UI_MAIN(MENU_MUSIC_MAIN);
                UI_menu(MENU_MUSIC_MAIN, (int)&pctl[0]);
            } else {
                SET_UI_MAIN(MENU_PAUSE);
                UI_menu(MENU_PAUSE, (int)&pctl[0]);
            }
            break;
        //--------------设备离线
        case MSG_USB_DISK_OUT:
            log_info("UDISK_OUT\n");
            dev_scan_info[UDISK_INDEX].active = 0;
            if (decoder_disk_out(&pctl[0], UDISK_INDEX)) {
                music_play_control(&pctl[0], DEV_CMD_NEXT, 0, NEED_WAIT);
            }
            break;
        case MSG_USB_DISK_IN://应用为了节省代码将插U盘和插卡消息分支写在一起，中间不可插入其他消息，否则影响设备升级
        case MSG_SDMMCA_IN:
            if (time_before(jiffies, 150)) {
                break;//上电1.5s内不响应设备上线消息
            }
            used_device = msg[0] - MSG_USB_DISK_IN;
            log_info("DEV_IN %d\n", used_device);
            post_msg(1, MSG_MUSIC_SELECT_NEW_DEVICE);
            break;
        case MSG_SDMMCA_OUT:
            log_info("SD0_OUT\n");
            dev_scan_info[SD0_INDEX].active = 0;
            if (decoder_disk_out(&pctl[0], SD0_INDEX)) {
                music_play_control(&pctl[0], DEV_CMD_NEXT, 0, NEED_WAIT);
            }
            break;
        //-------------查找指定设备
        case MSG_MUSIC_SELECT_NEW_DEVICE:
__find_last_device:
            log_info("MSG_MUSIC_SELECT_NEW_DEVICE\n");
            err_device = 0;
            if (!music_play_control(&pctl[0], DEV_CMD_SEL_NEW_DEV, used_device, NO_WAIT)) {
                music_play_control(&pctl[0], DEV_CMD_NEXT, 0, NEED_WAIT);
            }
            break;
        //-------------切换上下设备
        case MSG_PREV_DEVICE:
            log_info("MSG_PREV_DEVICE\n");
            music_play_control(&pctl[0], DEV_CMD_PREV, 0, NO_WAIT);
            break;
        case MSG_NEXT_DEVICE:
            log_info("MSG_NEXT_DEVICE\n");
            music_play_control(&pctl[0], DEV_CMD_NEXT, 0, NO_WAIT);
            break;
        //-------------切换上下曲
        case MSG_PREV_FILE:
            log_info("MSG_PREV_FILE\n");
            music_play_control(&pctl[0], FILE_CMD_PREV, 0, NO_WAIT);
            break;
        case MSG_NEXT_FILE:
            log_info("MSG_NEXT_FILE\n");
            music_play_control(&pctl[0], FILE_CMD_NEXT, 0, NO_WAIT);
            break;
#if KEY_IR_EN
        case MSG_INPUT_TIMEOUT:
            /*由红外界面返回*/
            if ((Input_Number <= pctl[0].ftotal) && (Input_Number > 0)) {
                music_play_control(&pctl[0], FILE_CMD_PLAY_BY_INDEX, Input_Number, NO_WAIT);
            } else {
                UI_menu(MENU_MUSIC_MAIN, (int)&pctl[0]);
            }
            Input_Number = 0;
            break;
#endif
        case MSG_NEXT_PLAYMODE:
            pctl[0].play_mode++;
            if (pctl[0].play_mode >= MAX_PLAY_MODE) {
                pctl[0].play_mode = REPEAT_ALL;
            }
            UI_menu(MENU_PLAYMODE, (int)&pctl[0]);
            log_info("MSG_NEXT_PLAYMODE : %d\n", pctl[0].play_mode);
            break;
        //-------------快进快退
        case MSG_MUSIC_FF:
            decoder_ff(pctl[0].p_dec_obj, 2);
            UI_menu(MENU_HALF_SEC_REFRESH, (int)&pctl[0]);
            SET_UI_MAIN(MENU_MUSIC_MAIN);
            UI_menu(MENU_MUSIC_MAIN, (int)&pctl[0]);
            break;
        case MSG_MUSIC_FR:
            decoder_fr(pctl[0].p_dec_obj, 2);
            UI_menu(MENU_HALF_SEC_REFRESH, (int)&pctl[0]);
            SET_UI_MAIN(MENU_MUSIC_MAIN);
            UI_menu(MENU_MUSIC_MAIN, (int)&pctl[0]);
            break;
        //-------------解码结束处理
        case MSG_WAV_FILE_END:
        case MSG_MP3_FILE_END:
        case MSG_F1A1_FILE_END:
            log_info("FILE_END:0x%x\n", msg[0]);
            if (pctl[0].p_dec_obj->sound.enable & B_DEC_ERR) {
                music_play_control(&pctl[0], FILE_CMD_AUTO_NEXT, 0, NEED_WAIT);
            }
            break;
        case MSG_WAV_FILE_ERR:
        case MSG_MP3_FILE_ERR:
        case MSG_F1A1_FILE_ERR:
            log_info("FILE_ERR:0x%x\n", msg[0]);
            if (pctl[0].p_dec_obj->sound.enable & B_DEC_ERR) {
                music_play_control(&pctl[0], FILE_CMD_AUTO_NEXT, 0, NO_WAIT);
            }
            break;
        //-------------模式退出处理
        case MSG_CHANGE_WORK_MODE:
            goto __out_music_mode;
        case MSG_500MS:
            /* decoder_time(pctl[0].p_dec_obj); */
            UI_menu(MENU_MAIN, (int)&pctl[0]);
            UI_menu(MENU_HALF_SEC_REFRESH, (int)&pctl[0]);
            if (MUSIC_PLAY != get_decoder_status(pctl[0].p_dec_obj)) {
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
__out_music_mode:
    music_vol_update();
    for (u8 i = 0; i < MAX_DEVICE; i++) {
        decoder_disk_out(&pctl[0], i);
    }
#if KEY_IR_EN
    Sys_IRInput = 0;
#endif
    SET_UI_MAIN(MENU_POWER_UP);
    UI_menu(MENU_POWER_UP, 0);
    key_table_sel(NULL);
    dac_sr_api(dac_sr);
    dac_fade_in_api();
}
#endif
