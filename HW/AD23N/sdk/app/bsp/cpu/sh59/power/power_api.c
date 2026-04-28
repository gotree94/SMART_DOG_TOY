#include "asm/power_interface.h"
/* #include "audio_adc.h" */
/* #include "audio_dac.h" */
#include "vm_api.h"


void sys_power_down(u32 usec)
{
    /* if (usec == -2) { */
    /* wdt_close(); */
    /* } */

    wdt_clear();
    /* audio_adc_deinit(); */
    /* audio_dac_deinit(); */

    vPortSuppressTicksAndSleep(usec);

    /* audio_adc_init(); */
    /* audio_dac_init(); */

    /* wdt_init(WDT_8S); */
}
void sys_softoff(void)
{
    vm_pre_erase();
    power_set_soft_poweroff();
}
