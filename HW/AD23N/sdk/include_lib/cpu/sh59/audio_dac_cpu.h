#ifndef __AUDIO_DAC_CPU_H__
#define __AUDIO_DAC_CPU_H__


#include "typedef.h"
#include "audio_cpu.h"
#include "audio_dpa_cpu.h"

/************************************
             dac模式
************************************/



#define D_PHY_VOL_SET_FUNC(n)


#define dac_sp_handle(n)

//-------------------------------------------------------
// AD23N 支持单端和差分
//-------------------------------------------------------
//|  channel_mode  |  channel_num  |       Output       |
//-------------------------------------------------------
//-------------------------------------------------------
//|     Single     |       1       |  LP(PB0)           |
//|     Diff       |       1       |  LP & LN (PB0&PB1) |
//-------------------------------------------------------
typedef enum __attribute__((packed))
{
    AUDAC_CH_SINGLE   = 0,
    AUDAC_CH_DIFF     = 1,
}
AUDAC_CH_MODE;

typedef enum __attribute__((packed))
{
    AUDAC_GAIN_0dB   = 0,
    AUDAC_GAIN_N6dB  = 1,
    AUDAC_GAIN_N12dB = 2,
    AUDAC_GAIN_N60dB = 3,
}
AUDAC_GAIN;


typedef struct __attribute__((packed))
{
    AUDAC_CH_MODE channel_mode;         // 模拟通道模式，单端/差分
    AUDAC_GAIN ana_gain;                // 模拟增益,  0: 0dB  1:-6dB  2:-12dB  3:-60dB
}
AUDAC_ANA_PARA;

void audio_dac_analog_init(AUDAC_ANA_PARA *p_audac_para);
void audio_dac_analog_close(AUDAC_ANA_PARA *p_audac_para);

void audio_dac_digital_init();
void audio_dac_digital_close();


extern const AUDAC_ANA_PARA g_audac_para;
#endif

