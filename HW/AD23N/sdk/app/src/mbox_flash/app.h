#ifndef __APP_H__
#define __APP_H__

#include "app_config.h"

typedef enum {
    MUSIC_MODE = 0,
    USB_SLAVE_MODE,
    AUX_MODE,
    RECORD_MODE,
    MIDI_DEC_MODE,
    MIDI_KEYBOARD_MODE,
    SIMPLE_DEC_MODE,
    LOUDSPEAKER_MODE,
    IDLE_MODE,
    // RTC_MODE,
    MAX_LOOP_MODE,

    MAX_WORK_MODE,

    SOFTOFF_MODE = 0xfe//该模式只由POWER_OFF消息进入
} ENUM_WORK_MODE;

/*
SDK引脚分布
SD卡:
LED数码管:
LINEIN:
ADKEY:
UART:
IRKEY:
功放MUTE:
*/

extern u8 work_mode;
// extern bool Sys_IRInput;
// extern u16 Input_Number;
void app_powerdown_deal(u8 is_busy);
void music_vol_update(void);
void app_next_mode(void);
void app(void);
#endif
