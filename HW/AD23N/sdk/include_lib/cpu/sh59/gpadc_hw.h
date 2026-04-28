#ifndef  __GPADC_HW_H__
#define  __GPADC_HW_H__
//sh59
#include "gpadc_hw_v11.h"
#include "typedef.h"
#include "gpio.h"
#include "clock.h"
#include "asm/power_interface.h"

#define ADC_CH_MASK_TYPE_SEL	0xffff0000
#define ADC_CH_MASK_CH_SEL	    0x000000ff
#define ADC_CH_MASK_PMU_VBG_CH_SEL   0x0000ff00

#define ADC_CH_TYPE_OCP     (0x0<<16)
#define ADC_CH_TYPE_AUDIO  	(0x1<<16)
#define ADC_CH_TYPE_PMU    	(0x2<<16)
#define ADC_CH_TYPE_LRC200K (0x3<<16)
#define ADC_CH_TYPE_SYSPLL  (0x4<<16)
#define ADC_CH_TYPE_CLASSD  (0x5<<16)
#define ADC_CH_TYPE_LPCTM   (0x6<<16)
#define ADC_CH_TYPE_WAT     (0x7<<16)
#define ADC_CH_TYPE_IO_PA7  (0x8<<16)
#define ADC_CH_TYPE_IO_PA8  (0x9<<16)
#define ADC_CH_TYPE_IO_PA9  (0xa<<16)
#define ADC_CH_TYPE_IO_PB4  (0xb<<16)
#define ADC_CH_TYPE_IO_PB5  (0xc<<16)
#define ADC_CH_TYPE_IO		(0x10<<16)

#define ADC_CH_OCP_			(ADC_CH_TYPE_OCP | 0x0)
#define ADC_CH_AUDIO_IREFN2P5U  (ADC_CH_TYPE_AUDIO | BIT(1) | BIT(0))
#define ADC_CH_AUDIO_VBGVDD     (ADC_CH_TYPE_AUDIO | BIT(2) | BIT(0))
#define ADC_CH_AUDIO_VCMBUF     (ADC_CH_TYPE_AUDIO | BIT(3) | BIT(0))
#define ADC_CH_AUDIO_LDACVDD    (ADC_CH_TYPE_AUDIO | (0b000 << 5) | BIT(4) | BIT(0))
#define ADC_CH_AUDIO_RDACVDD    (ADC_CH_TYPE_AUDIO | (0b001 << 5) | BIT(4) | BIT(0))
#define ADC_CH_AUDIO_DACLP      (ADC_CH_TYPE_AUDIO | (0b011 << 5) | BIT(4) | BIT(0))
#define ADC_CH_AUDIO_DACLN      (ADC_CH_TYPE_AUDIO | (0b100 << 5) | BIT(4) | BIT(0))
#define ADC_CH_AUDIO_DACRP      (ADC_CH_TYPE_AUDIO | (0b101 << 5) | BIT(4) | BIT(0))
#define ADC_CH_AUDIO_DACRN      (ADC_CH_TYPE_AUDIO | (0b110 << 5) | BIT(4) | BIT(0))
#define ADC_CH_AUDIO_FIFOVDD    (ADC_CH_TYPE_AUDIO | (0b111 << 5) | BIT(4) | BIT(0))
#define ADC_CH_AUDIO_MICBIAS0   (ADC_CH_TYPE_AUDIO | (0b000 << 9) | BIT(8) | BIT(0))
#define ADC_CH_AUDIO_MICBIAS1   (ADC_CH_TYPE_AUDIO | (0b001 << 9) | BIT(8) | BIT(0))
#define ADC_CH_AUDIO_MICLDO     (ADC_CH_TYPE_AUDIO | (0b010 << 9) | BIT(8) | BIT(0))
#define ADC_CH_AUDIO_ADCLDO0    (ADC_CH_TYPE_AUDIO | (0b011 << 9) | BIT(8) | BIT(0))
#define ADC_CH_AUDIO_ADCLDO1    (ADC_CH_TYPE_AUDIO | (0b100 << 9) | BIT(8) | BIT(0))
#define ADC_CH_PMU_WBG04  	    (ADC_CH_TYPE_PMU | (0x0<<8) | 0x0)//WBG04
#define ADC_CH_PMU_MBG08  	    (ADC_CH_TYPE_PMU | (0x1<<8) | 0x0)//MBG08
#define ADC_CH_PMU_LVDVBG  	    (ADC_CH_TYPE_PMU | (0x2<<8) | 0x0)//LVDVBG
#define ADC_CH_PMU_MVBG  	    (ADC_CH_TYPE_PMU | (0x3<<8) | 0x0)//MVBG
#define ADC_CH_PMU_VP17_VMAX_BUF    (ADC_CH_TYPE_PMU | 0x1)
#define ADC_CH_PMU_VPRE_LDO     (ADC_CH_TYPE_PMU | 0x2)
#define ADC_CH_PMU_MVBG_2UA     (ADC_CH_TYPE_PMU | 0x3)
#define ADC_CH_PMU_VTEMP	    (ADC_CH_TYPE_PMU | 0x4)
#define ADC_CH_PMU_VPWR_4 	    (ADC_CH_TYPE_PMU | 0x5) //1/4vpwr
#define ADC_CH_PMU_IOVDD_4 	    (ADC_CH_TYPE_PMU | 0x6) //1/4iovdd
#define ADC_CH_PMU_IOVDD_2 	    (ADC_CH_TYPE_PMU | 0x7) //1/2iovdd
#define ADC_CH_PMU_VPWR_2 	    (ADC_CH_TYPE_PMU | 0x8) //1/2vpwr
#define ADC_CH_PMU_DVDD_POR_TEST    (ADC_CH_TYPE_PMU | 0x9)
#define ADC_CH_PMU_DCVDD	    (ADC_CH_TYPE_PMU | 0xa)
#define ADC_CH_PMU_DVDD		    (ADC_CH_TYPE_PMU | 0xb)
#define ADC_CH_PMU_WVBG_15NA    (ADC_CH_TYPE_PMU | 0xc)
#define ADC_CH_PMU_WVDD  	    (ADC_CH_TYPE_PMU | 0xd)
#define ADC_CH_LRC200K_     (ADC_CH_TYPE_LRC200K | 0x0)
#define ADC_CH_SYSPLL_		(ADC_CH_TYPE_SYSPLL | 0x0)
#define ADC_CH_CLASSD_		(ADC_CH_TYPE_CLASSD | 0x0)
#define ADC_CH_LPCTM_		(ADC_CH_TYPE_LPCTM | 0x0)
#define ADC_CH_WAT_		    (ADC_CH_TYPE_WAT | 0x0)
#define ADC_CH_IO_PA7       (ADC_CH_TYPE_IO_PA7 | 0x0)
#define ADC_CH_IO_PA8       (ADC_CH_TYPE_IO_PA8 | 0x0)
#define ADC_CH_IO_PA9       (ADC_CH_TYPE_IO_PA9 | 0x0)
#define ADC_CH_IO_PB4       (ADC_CH_TYPE_IO_PB4 | 0x0)
#define ADC_CH_IO_PB5       (ADC_CH_TYPE_IO_PB5 | 0x0)
#define ADC_CH_IO_PA0       (ADC_CH_TYPE_IO | 0x0)
#define ADC_CH_IO_PA1	    (ADC_CH_TYPE_IO | 0x1)
#define ADC_CH_IO_PA2	    (ADC_CH_TYPE_IO | 0x2)
#define ADC_CH_IO_PA3       (ADC_CH_TYPE_IO | 0x3)
#define ADC_CH_IO_DP        (ADC_CH_TYPE_IO | 0x4)
#define ADC_CH_IO_DM        (ADC_CH_TYPE_IO | 0x5)
#define ADC_CH_IO_PA4       (ADC_CH_TYPE_IO | 0x6)
#define ADC_CH_IO_PA5       (ADC_CH_TYPE_IO | 0x7)
#define ADC_CH_IO_PA6       (ADC_CH_TYPE_IO | 0x8)
#define ADC_CH_IO_PA10      (ADC_CH_TYPE_IO | 0x9)
#define ADC_CH_IO_PA11      (ADC_CH_TYPE_IO | 0xA)
#define ADC_CH_IO_PA12      (ADC_CH_TYPE_IO | 0xB)
#define ADC_CH_IO_FSPG      (ADC_CH_TYPE_IO | 0xC)
#define ADC_CH_IO_PB0       (ADC_CH_TYPE_IO | 0xD)
#define ADC_CH_IO_PB1       (ADC_CH_TYPE_IO | 0xE)
#define ADC_CH_IO_DB3       (ADC_CH_TYPE_IO | 0xF)


enum AD_CH {
    AD_CH_OCP = ADC_CH_OCP_,
    AD_CH_AUDIO_IREFN2P5U = ADC_CH_AUDIO_IREFN2P5U,
    AD_CH_AUDIO_VBGVDD = ADC_CH_AUDIO_VBGVDD,
    AD_CH_AUDIO_VCMBUF = ADC_CH_AUDIO_VCMBUF,
    AD_CH_AUDIO_LDACVDD = ADC_CH_AUDIO_LDACVDD,
    AD_CH_AUDIO_RDACVDD = ADC_CH_AUDIO_RDACVDD,
    AD_CH_AUDIO_DACLP = ADC_CH_AUDIO_DACLP,
    AD_CH_AUDIO_DACLN = ADC_CH_AUDIO_DACLN,
    AD_CH_AUDIO_DACRP = ADC_CH_AUDIO_DACRP,
    AD_CH_AUDIO_DACRN = ADC_CH_AUDIO_DACRN,
    AD_CH_AUDIO_FIFOVDD = ADC_CH_AUDIO_FIFOVDD,
    AD_CH_AUDIO_MICBIAS0 = ADC_CH_AUDIO_MICBIAS0,
    AD_CH_AUDIO_MICBIAS1 = ADC_CH_AUDIO_MICBIAS1,
    AD_CH_AUDIO_MICLDO   = ADC_CH_AUDIO_MICLDO,
    AD_CH_AUDIO_ADCLDO0 = ADC_CH_AUDIO_ADCLDO0,
    AD_CH_AUDIO_ADCLDO1 = ADC_CH_AUDIO_ADCLDO1,
    AD_CH_PMU_WBG04 = ADC_CH_PMU_WBG04,
    AD_CH_PMU_MBG08 = ADC_CH_PMU_MBG08,
    AD_CH_PMU_LVDVBG = ADC_CH_PMU_LVDVBG,
    AD_CH_PMU_MVBG = ADC_CH_PMU_MVBG,
    AD_CH_PMU_VP17_VMAX_BUF,
    AD_CH_PMU_VPRE_LDO,
    AD_CH_PMU_MVBG_2UA,
    AD_CH_PMU_VTEMP,
    AD_CH_PMU_VPWR_4,
    AD_CH_PMU_IOVDD_4,
    AD_CH_PMU_IOVDD_2,
    AD_CH_PMU_VPWR_2,
    AD_CH_PMU_DVDD_POR_TEST,
    AD_CH_PMU_DCVDD,
    AD_CH_PMU_DVDD,
    AD_CH_PMU_WVBG_15NA,
    AD_CH_PMU_WVDD,
    AD_CH_LRC200K = ADC_CH_LRC200K_,
    AD_CH_SYSPLL = ADC_CH_SYSPLL_,
    AD_CH_CLASSD = ADC_CH_CLASSD_,
    AD_CH_LPCTM	= ADC_CH_LPCTM_,
    AD_CH_WAT = ADC_CH_WAT_,
    AD_CH_IO_PA7 = ADC_CH_IO_PA7,
    AD_CH_IO_PA8 = ADC_CH_IO_PA8,
    AD_CH_IO_PA9 = ADC_CH_IO_PA9,
    AD_CH_IO_PB4 = ADC_CH_IO_PB4,
    AD_CH_IO_PB5 = ADC_CH_IO_PB5,
    AD_CH_IO_PA0 = ADC_CH_IO_PA0,
    AD_CH_IO_PA1,
    AD_CH_IO_PA2,
    AD_CH_IO_PA3,
    AD_CH_IO_DP,
    AD_CH_IO_DM,
    AD_CH_IO_PA4,
    AD_CH_IO_PA5,
    AD_CH_IO_PA6,
    AD_CH_IO_PA10,
    AD_CH_IO_PA11,
    AD_CH_IO_PA12,
    AD_CH_IO_FSPG,
    AD_CH_IO_PB0,
    AD_CH_IO_PB1,
    AD_CH_IO_PB3,

    AD_CH_IOVDD = ADC_CH_TYPE_IO | 0xffff,
};

#define     ADC_VBG_CENTER        800
#define     ADC_VBG_TRIM_STEP     0
#define     ADC_VBG_DATA_WIDTH    0
#define     ADC_EXTERN_VOLTAGE_TRIM_EN  0

//防编译报错
extern const u8 gpadc_battery_mode;
extern const u32 gpadc_ch_power;
extern const u8 gpadc_ch_power_div;
extern const u8 gpadc_power_supply_mode;
#define AD_CH_PMU_VBG   AD_CH_PMU_MBG08
#define AD_CH_LDOREF    AD_CH_PMU_VBG
#define AD_CH_LPCTMU    AD_CH_LPCTM
#define AD_CH_PMU_VPWR  AD_CH_PMU_VPWR_4
#define AD_CH_PMU_VBAT  gpadc_ch_power
#define AD_CH_PMU_VBAT_DIV  gpadc_ch_power_div
#ifndef IO_PORT_FSPG
#define IO_PORT_FSPG    0
#endif

#define ADC_PMU_VBG_TEST_SEL(x)     SFR(P3_PMU_ADC0, 4, 2, x)
#define ADC_PMU_VBG_TEST_EN(x)      SFR(P3_PMU_ADC0, 3, 1, x)
#define ADC_PMU_VBG_BUFFER_EN(x)    SFR(P3_PMU_ADC0, 2, 1, x)
#define ADC_PMU_VBG_TEST_OE(x)      SFR(P3_PMU_ADC0, 1, 1, x)
#define ADC_PMU_TOADC_EN(x)         SFR(P3_PMU_ADC0, 0, 1, x)
#define ADC_PMU_CHANNEL_ADC(x)      SFR(P3_PMU_ADC1, 0, 4, x) //CHANNEL_ADC_S


#define SADC_BIT_WIDE        10
#define SADC_ALL_BITS        ((1UL << SADC_BIT_WIDE) - 1)
#define sadc_value_wrong(n)  ((~SADC_ALL_BITS) & (n))

#define ADC_CH_PMU_VBAT         AD_CH_PMU_VPWR_4
#define ADC_CH_PMU_1_4VBAT      AD_CH_PMU_VPWR_4
#define ADC_VALUE_NONE       0xffff

// #define ADC_PMU_PMUTS_POR_SEL(x)    SFR(P3_PMU_ADC0, 7, 1, x)
// #define ADC_PMU_PMUTS_OE(x)         SFR(P3_PMU_ADC0, 6, 1, x)
// #define ADC_PADC0_TOADC_EN(x)       SFR(P3_PMU_ADC2, 0, 1, x)
#define ADC_PMU_CH_CLOSE()  {   ADC_PMU_TOADC_EN(0);\
                                ADC_PMU_VBG_TEST_OE(0);\
                                ADC_PMU_VBG_TEST_EN(0);\
                            }
#endif  /*GPADC_HW_H*/

