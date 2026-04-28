#ifndef __AUDIO_CPU_H__
#define __AUDIO_CPU_H__

#include "typedef.h"
// #include "asm/power_interface.h"

void audio_clk_open(u32 dly);
void audio_clk_close(void);

void dpa_clk_open();
u32 vbgldo_gear_sel();

extern const unsigned char config_adda_low_voltage_mode;

#define audio_analog_open   audio_common_analog_open
#define audio_analog_close  audio_common_analog_close
#define audio_isr_init()
#define audio_clk_init()    {  \
                                audio_clk_open(2000); \
                            }
#define audio_clk_close     audio_clk_close
#define audio_get_iovdd_vol()
#define audio_vcm_trim()    audio_voltage_trim()

//各个音频模块工作依赖常量
extern const u8 au_const_adda_common_en;
extern const u8 au_const_dpa_digital_en;
extern const u8 au_const_dac_en;
extern const u8 au_const_apa_en;

//adda音频电压模式配置
extern const u8 config_adda_low_voltage_vbg;


extern const u8 au_vcm_cap_en;
extern const u8 au_vcm05_en;

/************************************
             AUDIO COMMON参数定义
************************************/
typedef struct {
    u8 vbg_v_trim_value0;       // PVBG电压档位获取,应用于挂电容时，上电trim流程获取
    u8 vbg_v_trim_value1;       // VBG电压档位获取,应用于无电容时，上电trim流程获取
    u8 vcm_cap_en;              // 0: vcm挂105电容  1：vcm不挂105电容
    u8 vbat_type;               // 0:3.3v  1:1.8v
    u8 vbgldo_vsel;             // default value =3
    u8 vcm0d5_mode;             // 0:0.6v  1:0.5v
} AUDIO_COMMON_POWERUP;

#endif


