#ifndef	_LED5x7_DRIVER_H_
#define _LED5x7_DRIVER_H_

#include "config.h"


typedef struct _LED5X7_VAR {
    u8  bCoordinateX;       //<X 坐标
    u8  bFlashChar;         //<字符位闪烁
    u8  bFlashIcon;         //<图标闪烁
    u8  bShowBuff[5];       //<显示缓存
    u8  bBrightness;        //<亮度控制
    u8  bShowBuff1[8];
} LED5X7_VAR;


void LED5X7_setX(u8 X);
void LED5X7_show_string_menu(u8 menu);
void set_LED_fade_out(void);
void set_LED_all_on(void);

void LED5X7_init(void);
void LED5X7_scan(void);
void LED5X7_clear_icon(void);
void LED5X7_show_char(u8 chardata);
void LED5X7_show_number(u8 number);
void LED5X7_show_Hi(void);
void LED5X7_show_music_main(int arg);
void LED5X7_show_filenumber(int arg);
void LED5X7_show_volume(void);
void LED5X7_show_dec_eq(u32 arg);
void LED5X7_show_hw_eq(void);
void LED5X7_show_IR_number(void);
void LED5X7_show_playmode(int arg);
void LED5X7_show_fm_main(void);
void LED5X7_show_fm_station(void);
void LED5X7_show_alarm(void);
void LED5X7_show_RTC_main(void);
void LED5X7_show_pc_main(void);
void LED5X7_show_pc_vol_up(void);
void LED5X7_show_pc_vol_down(void);
void LED5X7_show_aux_main(void);
void LED5X7_show_pause(void);
void LED5X7_show_waiting(void);

extern LED5X7_VAR LED5X7_var;

#define LED_STATUS  LED5X7_var.bShowBuff[4]

#define LED_A   BIT(0)
#define LED_B   BIT(1)
#define LED_C   BIT(2)
#define LED_D   BIT(3)
#define LED_E   BIT(4)
#define LED_F   BIT(5)
#define LED_G   BIT(6)
#define LED_H   BIT(7)

//for LED_STATUS
#define LED_PLAY	LED_A
#define LED_PAUSE	LED_B
#define LED_USB		LED_C
#define LED_SD		LED_D
#define LED_2POINT	LED_E
#define LED_MHZ		LED_F
#define LED_MP3		LED_G
#define LED_FM	    LED_H

#if 1
#define LED_PORT0   BIT(12)
#define LED_PORT1   BIT(11)
#define LED_PORT2   BIT(10)
#define LED_PORT3   BIT(9)
#define LED_PORT4   BIT(7)
#define LED_PORT5   BIT(6)
#define LED_PORT6   BIT(4)
#define LED_PORT_ALL    (LED_PORT0 | LED_PORT1 | LED_PORT2 |LED_PORT3 |LED_PORT4 |LED_PORT5 |LED_PORT6)

#else

#define LED_PORT0   IO_PORTA_04
#define LED_PORT1   IO_PORTA_06
#define LED_PORT2   IO_PORTA_07
#define LED_PORT3   IO_PORTA_09
#define LED_PORT4   IO_PORTA_10
#define LED_PORT5   IO_PORTA_11
#define LED_PORT6   IO_PORTA_12
// #define LED_PORT_ALL    (LED_PORT0 | LED_PORT1 | LED_PORT2 |LED_PORT3 |LED_PORT4 |LED_PORT5 |LED_PORT6)

#endif


#endif
