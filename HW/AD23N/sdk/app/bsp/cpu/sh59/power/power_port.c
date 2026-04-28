#include "asm/power_interface.h"
#include "app_config.h"
#include "board_cfg.h"

#define LOG_TAG_CONST       PMU
#define LOG_TAG             "[PMU]"
#include "log.h"

__attribute__((weak))
void board_gpio_config_soft_poweroff(u32 *gpio_config)
{
    //注册在板卡
}

void gpio_config_soft_poweroff(void)
{
    PORT_TABLE(g);
    log_info("---------------%s--------------", __FUNCTION__);
    board_gpio_config_soft_poweroff(gpio_config);
    PORT_PROTECT(POWER_WAKEUP_IO);
    __port_init((u32)gpio_config);
}


