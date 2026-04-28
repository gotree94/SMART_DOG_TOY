#ifndef __AUDIO_ADC_CPU_H__
#define __AUDIO_ADC_CPU_H__

#include "typedef.h"

//=============================================================================
//=                                                                           =
//=                       Audio adc Physics Definition                        =
//=                                                                           =
//=============================================================================

//ADC_CON0
#define AUADC0_AFIFO_PND       (1 << 28)
#define AUADC0_AFIFO_CPND      (1 << 27)
#define AUADC0_AFIFO_IE        (1 << 26)
#define AUADC0_D2A_CP_EN       (1 << 19)
#define AUADC0_ADC_24B         (1 << 18)
#define AUADC0_ADC_CHE(n)      ((n & 0xf) << 14)
#define AUADC0_ADCUDE          (1 << 11)
#define AUADC0_PND             (1 << 7)
#define AUADC0_CPND            (1 << 6)
#define AUADC0_ADC_IE          (1 << 5)
#define AUADC0_DMA_EN          (1 << 4)
#define AUADC0_ADC_SMP(n)      ((n & 0xf) << 0)


typedef struct __AUDIO_ADC_CTRL_HDL {
    void *buf;
    u32 con0;
    u16 pns;
    u16 sp_total;
    u16 throw_cnt;//抛弃开始的数据包数目
    u8  sp_size;
} AUDIO_ADC_CTRL_HDL;

#define AUDIO_ADC_SP_BITS     16
#define AUDIO_ADC_PACKET_SIZE (48*5)
#define AUDIO_ADC_SP_SIZE     (AUDIO_ADC_SP_BITS / 8)
#define AUDIO_ADC_SP_PNS      (AUDIO_ADC_PACKET_SIZE / 3)

#if (16 == AUDIO_ADC_SP_BITS)
#define ADC_DEFAULT_CON0 (AUADC0_ADC_CHE(0b0001) | AUADC0_ADCUDE)
#endif

#if (24 == AUDIO_ADC_SP_BITS)
#define ADC_DEFAULT_CON0 (AUADC0_ADC_24B | AUADC0_ADC_CHE(0b0001) | AUADC0_ADCUDE)
#endif

typedef struct __AUDIO_ADC_DIG_MIC_HDL {
    u8 clk_io;
    u8 data_io;
    u8 data_ch;
    bool enable;
} AUDIO_ADC_DIG_MIC_HDL;


//------------------------------------------
// AUDIO ADC 是否工作在低电耗模式
// * 低电耗模式，耗电下降，性能也会下降
//--------------------------------------------
typedef enum __attribute__((packed))
{
    AUADC_STANDARD_PERF = 0,  //标准性能配置
    AUADC_LOWPOWER_PERF = 1,  //低功耗性能配置
}
AUADC_PERFORM_MODE; //外部供电一定时，AUDIO_ADC的性能配置

/**
 * AUDIO ADC MIC的两种工作模式选择 涉及
 * MIC内置偏置电阻的使能与选择
 * MIC LDO电源使能与输送到PA13
 * MIC LDO电源反馈点选择pad还是内部；
 */
typedef enum __attribute__((packed))
{
    MIC_RS_INSIDE = 0,  //PA13输出MICLDO供电, 内部电源+内部偏置电阻
    MIC_RS_OUTSIDE = 1, //PA13输出MICLDO供电, 内部电源+外部偏置电阻
}
AUDIO_MIC_RS_MODE;

typedef enum __attribute__((packed))
{
    MIC_INPUT_ANA0_PB2  = 0,           //PB2单端MIC输入
    MIC_INPUT_ANA1_PA14 = 1,           //PA14单端MIC输入
    MIC_INPUT_ANA_APAP  = 2,           //APA口差分输入(使用该通路时，只能APAP&APAN差分输入，且输入时不能使用APA输出)
    MIC_INPUT_MIC0_CAPLESS_PA13   = 3,      //PA13省电容mic输入(PA13同时固定为micldo/midbias输出,不可用作差分), 使用时MIC_PGA_GAIN不可超过15dB
}
AUDIO_MIC_INPUT_PORT;

typedef enum __attribute__((packed))
{
    AUMIC_2v0 = 0, //高压对应2.0v,低压对应1.6v
    AUMIC_2v1 = 1, //高压对应2.1v,低压对应1.7v
    AUMIC_2v2 = 2, //高压对应2.2v,低压对应1.8v
    AUMIC_2v3 = 3, //高压对应2.3v,低压对应1.9v
    AUMIC_2v4 = 4, //高压对应2.4v,低压对应2.0v
    AUMIC_2v6 = 5, //高压对应2.6v,低压对应2.2v
    AUMIC_2v8 = 6, //高压对应2.8v,低压对应2.4v
    AUMIC_3v0 = 7, //高压对应3.0v,低压对应2.6v
}
AUDIO_MICLDO_VS;
extern AUDIO_MICLDO_VS const audio_adc_mic_ldo_vs;

typedef enum __attribute__((packed))
{
    AUMIC_0k3 = 0,
    AUMIC_0k5 = 1,
    AUMIC_1k0 = 2,
    AUMIC_1k5 = 3,
    AUMIC_2k0 = 4,
    AUMIC_2k5 = 5,
    AUMIC_3k0 = 6,
    AUMIC_3k5 = 7,
    AUMIC_4k0 = 8,
    AUMIC_4k5 = 9,
    AUMIC_5k5 = 0xa,
    AUMIC_6k5 = 0xb,
    AUMIC_7k5 = 0xc,
    AUMIC_8k5 = 0xd,
    AUMIC_9k5 = 0xe,
    AUMIC_10k5 = 0xf,
}
AUDIO_MICBIAS_RS;

typedef enum __attribute__((packed))
{
    // MIC_PGA增益档位说明
    AUMIC_0db = 0,
    AUMIC_3db = 1,
    AUMIC_9db = 2,
    AUMIC_15db = 3,
    AUMIC_21db = 4,
    AUMIC_27db = 5,
}
AUDIO_MICPGA_G;

//控制模拟初始化流程的参数
typedef struct __AUADC_ANA_PARAM {
    AUADC_PERFORM_MODE perf_mode;
    AUDIO_MICLDO_VS micldo_vs;
    AUDIO_MIC_RS_MODE rs_mode;
    AUDIO_MICBIAS_RS micbias_rs;
    AUDIO_MIC_INPUT_PORT auadc_input_port;
    AUDIO_MICPGA_G mic_pga_gain;
    bool auadc_diff_in;
} AUADC_ANA_PARAM;

//AD23需要2bit控制IO上下拉电阻，并且上下拉各自集成在一个寄存器
//所以寄存器bit和io_index对应关系bit0 = io_index * 2, bit1 = io_index * 2 + 1
//比如需要设置PA3的上拉电阻,则配置SFR(JL_PORTA->PU, 3 * 2,  2,  0b01)
#define AUADC_GPIO_SET_HIGHZ(port,num) \
            JL_PORT##port->DIR |=  BIT(num);  \
            JL_PORT##port->DIE &= ~BIT(num);  \
            JL_PORT##port->PU &= ~(BIT(2 * num) | BIT(2 * num + 1));  \
            JL_PORT##port->PD &= ~(BIT(2 * num) | BIT(2 * num + 1));

extern const AUADC_PERFORM_MODE    auadc_perf_mode;
extern const AUDIO_MIC_RS_MODE     auadc_mic_rs_pwr_mode;//
extern const AUDIO_MICPGA_G        audio_adc_mic_pga_g;//后级增益
extern const AUDIO_MICBIAS_RS      audio_adc_mic_bias_rs;//micbias内部偏置电阻选择
extern const AUDIO_MICLDO_VS       audio_adc_mic_ldo_vs;//micldo偏置电压选择
extern const bool audio_adc_diff_mic_mode; // 差分amic使能(N端固定PA15) 0:单端amic  1:差分amic
extern const bool audio_adc_diff_aux_mode; // 差分aux使能(N端固定PA15) 0:单端aux  1:差分aux
extern const u8 audio_adc_dcc;


void au_dmic_clk_open(void);
void au_dmic_clk_close(void);


u32 audio_adc_analog_init(AUADC_ANA_PARAM *p_param);
void audio_adc_analog_close();


void fadc_init(const AUDIO_ADC_CTRL_HDL *ops);
u32 audio_adc_phy_init(u32 sr, u8 adc0_mic_sel);
void audio_adc_enable_phy();
void audio_adc_disable(void);
void audio_adc_phy_off(void);

#endif
