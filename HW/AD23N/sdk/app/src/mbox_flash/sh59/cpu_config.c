#include "typedef.h"
#include "audio_cpu.h"
/* #include "audio_dpa_cpu.h" */
#include "audio_dac_cpu.h"
#include "audio_adc_cpu.h"
/* ***********************
 * 在不同供电场景下config_adda_voltage_mode配置不同的值：
 * 0：高压供电VPWR，工作电压在2.7v ~ 4.5v之间
 * 1：低压供电到IOVDD&VPWR，工作电压在1.8v ~ 3.6v之间
 * 注：低压供电时，IOVDD需要和VPWR短接；
 * 注：此项配置只会影响到audio dac & audio adc的性能；不会对APA性能产生影响
 * ************************/
const unsigned char config_adda_low_voltage_mode = 0;


//*********************************************************************************//
//                        AUDIO  Configuration                                     //
//*********************************************************************************//
/*
+============================+==============+==============+==============+
|         AD23N AUDIO & APA 工作依赖常量                                  |
+----------------------------+--------------+--------------+--------------+
|                            | Audio DAC    | Audio ADC    | Audio APA    |
+----------------------------+--------------+--------------+--------------+
|   au_const_adda_common_en  | ✔            | ✔            | ✘            |
-----------------------------+--------------+--------------+--------------+
|   au_const_dpa_digital_en  | ✔            | ✘            | ✔            |
+----------------------------+--------------+--------------+--------------+
|   au_const_dac_en          | ✔            | ✘            | ✘            |
+----------------------------+--------------+--------------+--------------+
|   au_const_apa_en          | ✘            | ✘            | ✔            |
+----------------------------+--------------+--------------+--------------+
*/
const u8 au_const_adda_common_en = 1;
const u8 au_const_dpa_digital_en = 1;
const u8 au_const_dac_en = 1;
const u8 au_const_apa_en = 1;

const u8 au_vcm_cap_en = 0;   //0:without vcmcap  1:with vcmcap(PB2)
const u8 au_vcm05_en   = 0;   //0:0.6v 1:0.5v  高压时选择0.6VDAC底噪性能好，低压时选择0.5V有助于优化DAC底噪性能，有无vcm电容均可使用


//*********************************************************************************//
//                        AUDIO DAC  Configuration                                 //
//*********************************************************************************//
const AUDAC_ANA_PARA g_audac_para = {
    .channel_mode     = AUDAC_CH_SINGLE, // DAC输出方式，可选择单端/差分
    .ana_gain         = AUDAC_GAIN_0dB,  // DAC模拟增益
};


//*********************************************************************************//
//                        AUDIO ADC  Configuration                                 //
//*********************************************************************************//

const char MIC_PGA_G        = 14;    //0 ~ 14,无效参数

const bool audio_adc_diff_mic_mode = 0;// 差分amic使能(N端固定PA15) 0:单端amic  1:差分amic
const bool audio_adc_diff_aux_mode = 0;// 差分aux使能(N端固定PA15) 0:单端aux  1:差分aux

const AUADC_PERFORM_MODE    auadc_perf_mode = AUADC_STANDARD_PERF; //audio_adc性能
const AUDIO_MIC_RS_MODE     auadc_mic_rs_pwr_mode = MIC_RS_INSIDE;
const AUDIO_MICPGA_G        audio_adc_mic_pga_g = AUMIC_27db;//MIC_PGA_GAIN
const AUDIO_MICBIAS_RS      audio_adc_mic_bias_rs = AUMIC_1k5;//micbias内部偏置电阻选择
const AUDIO_MICLDO_VS       audio_adc_mic_ldo_vs = AUMIC_2v0;//micldo偏置电压选择

const AUDIO_MIC_INPUT_PORT audio_adc_mic_input_port = MIC_INPUT_ANA0_PB2;//micin输入口
const AUDIO_MIC_INPUT_PORT audio_adc_aux_input_port = MIC_INPUT_ANA1_PA14;//linein输入口
const u8 audio_adc_dcc = 1; //0~14:滤波器系数，档位越低，截止点越低 15:bypass

