#ifndef __AUDIO_TRIM_CPU_H__
#define __AUDIO_TRIM_CPU_H__

#include "typedef.h"

typedef struct __autrim_para {
    u32(*fun)(u8, u8, u32 *);
    u16 need_mv;
    u16 max_limit_mv;
    u32 *sfr_addr;
    u8 result_sel;   //0:选择偏大的值  1:选择偏小的值  2:选择最接近目标的值，无论大小
    u8 max_gear;
    u8 trim_start_bit;
    u8 vcm05_en;
} autrim_para;

typedef struct __autrim_res_vm {
    u8 gear;
    u8 withcap_en;
    u8 vcm05_en;
} autrim_res_vm;

typedef struct __autrim_res {
    u16 res_mv;
    autrim_res_vm audio2vm_res;
} autrim_res;

extern const u8 au_vcm_cap_en;   //0:without vcmcap  1:with vcmcap(PB2)
extern const u8 au_vcm05_en;   //0:0.6v 1:0.5v  高压时选择0.6VDAC底噪性能好，低压时选择0.5V有助于优化DAC底噪性能，有无vcm电容均可使用
extern autrim_res g_adda_trim_res; //存放AUDIO校准结果
extern const u8 config_adda_low_voltage_vbg;
void audio_adda_trim_analog_open(u8 withcap_en);
void audio_adda_trim_analog_close();
u32 aud_vbg_trim(autrim_para *p_autrim_para, autrim_res *p_autrim_res);
u32 audio_vbg_adjust(u8 sta_bit, u8 level, u32 *sfr_addr);
u32 audio_vbg_trim(u32 need_mv, autrim_res *p_autrim_res);


#endif
