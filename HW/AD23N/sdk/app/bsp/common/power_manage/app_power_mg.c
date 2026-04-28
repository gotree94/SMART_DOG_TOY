#include "app_power_mg.h"
#include "gpadc.h"
#include "key.h"
#include "msg.h"
#include "efuse.h"
#include "clock.h"
#include "asm/power_interface.h"

#define LOG_TAG_CONST       NORM
#define LOG_TAG             "[normal]"
#include "log.h"

#if LVD_WARNING_FOR_LOW_POWER
static u16 lvd_warning_voltage;//低电检测电压,一般设置为比lvd电压大200mV
#endif

/* TODO:后续移回PMU */
#define LVD_VOL_MIN     1800
#define LVD_VOL_STEP    100
#define GET_VLVD_VOL_SEL(sel)       (P33_CON_GET(P3_VLVD_CON1) & 0x7)
u32 get_lvd_vol(void)
{
    return (LVD_VOL_MIN + GET_VLVD_VOL_SEL() * LVD_VOL_STEP);
}

static void lvd_warning_init(void)
{
    u16 lvd_voltage = get_lvd_vol();
    lvd_warning_voltage = lvd_voltage + 200;
    log_info("lvd_warning_voltage : %d real : %d\n", lvd_warning_voltage, lvd_voltage);
}

void app_power_init(void)
{
    low_power_warning_init();
    adc_add_sample_ch(ADC_CH_PMU_VBAT);
}

u32 app_power_get_vbat(void)
{
    u32 vol = adc_get_voltage(ADC_CH_PMU_VBAT);
    if (-1 != vol) {
        vol *= 4;
    }
    return vol;
}

void app_power_scan(void)
{
    static u16 low_power_cnt = 0;
    u32 vol = adc_get_voltage(ADC_CH_PMU_VBAT);

    if (-1 != vol) {
        vol = vol * 4;
        if (vol <= LOW_POWER_VOL) {
            low_power_cnt++;
            if (low_power_cnt == 10) {
                log_error(LOW_POWER_LOG);
                post_msg(1, MSG_LOW_POWER);
            }
        } else {
            low_power_cnt = 0;
        }
    }

    /* #if (!(KEY_AD_EN || KEY_MATRIX_EN)) */
    /* adc_kick_start(NULL); */
    /* #endif */
}
