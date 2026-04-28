#ifndef __AUDIO_APA_CPU_H__
#define __AUDIO_APA_CPU_H__

#include "typedef.h"

//=============================================================================
//=                                                                           =
//=                       Audio APA Physics Definition                        =
//=                                                                           =
//=============================================================================

//DPA_CON1
#define APA1_FIFO_PND         (1 << 29)
#define APA1_FIFO_CPND        (1 << 28)
#define APA1_FIFO_IE          (1 << 27)
#define APA1_FIFO_RST         (1 << 26)
#define APA1_FIFOSYNC_EN      (1 << 25)
#define APA1_IIR_PAR(n)       ((n & 0x3) << 23)
#define APA1_IIR_EN           (1 << 22)

#define APA1_PBTL_MODE        (1 << 5)

#define APA1_SINGLE           (0 << 3) //单端模式
#define APA1_DIFF_SNR         (1 << 3) //差分模式(SNR)
#define APA1_DIFF_THD         (2 << 3) //差分模式(THD)


/*******************************/
#define APA_CLK_96M    (96)
#define APA_CLK_192M   (192)
#define APA_CLK_240M   (240)


#define APA_CLK_SEL         APA_CLK_192M

#define APA_DSM_CLK_MODE    1       // 0:384kHz  1:768kHz   2:1536kHz   3:3072kHz

#define APA_PWM_MODE        APA1_DIFF_THD

#define APA_CON1_DEFAULT    (APA_PWM_MODE)

typedef struct _APA_PHY_PARA {
    u16 pwm_dor;
    u8  dsm_dor;
    u8  dsm_margin;
} APA_PHY_PARA;

// apa库接口
void apa_analog_init();
void apa_analog_close(void);

bool apa_digital_init(APA_PHY_PARA *p_apa, u32 con1, u32 dsm_clk_mode);
void apa_digital_close();

bool get_apa_para_1(APA_PHY_PARA *p_apa, u32 sr, u32 dsm_clk_mode, u32 apa_clk_sel);

void apa_clk_sel(u8 clk_sel);

//对外接口
void apa_init(u32 sr);
void apa_close(void);

extern const u16 c_apa_dc_dit;
#endif

