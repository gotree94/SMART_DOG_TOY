#ifdef SUPPORT_MS_EXTENSIONS
#pragma bss_seg(".key_wakeup.data.bss")
#pragma data_seg(".key_wakeup.data")
#pragma const_seg(".key_wakeup.text.const")
#pragma code_seg(".key_wakeup.text")
#endif
#include "asm/power_interface.h"
#include "app_config.h"
#include "gpio.h"
#include "board_cfg.h"

void key_wakeup_init(void)
{
    struct _p33_io_wakeup_config port = {0};
    port.pullup_down_mode   = PORT_INPUT_PULLUP_10K;
    port.filter      		= PORT_FLT_1ms;
    port.gpio               = POWER_WAKEUP_IO;
    port.edge               = FALLING_EDGE;
    /* port.callback			= key_wakeup_callback; */
    p33_io_wakeup_port_init(&port);
    p33_io_wakeup_enable(POWER_WAKEUP_IO, 1);
}

