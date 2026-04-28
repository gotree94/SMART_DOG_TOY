/*********************************************************************************************
    *   Filename        : main.c

    *   Description     :

    *   Author          :

    *   Email           :

    *   Last modifiled  :

    *   Copyright:(c)JIELI  2011-2017  @ , All Rights Reserved.
*********************************************************************************************/
#include "config.h"
#include "common.h"
#include "maskrom.h"
#include "app_config.h"
#include "clock.h"
#include "mmu/malloc.h"
#include "init.h"
#include "efuse.h"
#include "asm/power_interface.h"
/* #include "vm_api.h" */
/* #include "power_api.h" */

#define LOG_TAG_CONST       MAIN
#define LOG_TAG             "[main]"
#include "log.h"


void interrupt_init(void)
{
    int i ;

    local_irq_disable();

    volatile unsigned int *icfg_ptr = &(q32DSP(core_num())->ICFG00);
    for (i = 0 ; i < 32 ; i++) {
        icfg_ptr[i] = 0;
    }

    local_irq_enable();
}

void maskrom_init(void)
{
    struct maskrom_argv argv;
    memset((void *)&argv, 0, sizeof(struct maskrom_argv));
    argv.pchar = (void (*)(char))putchar;
    argv.exp_hook = exception_analyze;
    argv.flt = NULL;
    argv.udelay = udelay;
    argv.local_irq_enable = local_irq_enable;
    argv.local_irq_disable = local_irq_disable;
    mask_init(&argv);
    interrupt_init();
    request_irq(IRQ_EXCEPTION_IDX, 7, exception_irq_handler, 0);
    debug_init();
}

extern void enter_critical_hook_init(void *enter);
extern void exit_critical_hook_init(void *enter);
void critical_hook_init()
{
    enter_critical_hook_init((void *)enter_critical_hook);
    exit_critical_hook_init((void *)exit_critical_hook);
}

int c_main(int cfg_addr)
{
    maskrom_init();
    wdt_init(WDT_8S);
    efuse_init();
    clk_voltage_init(CLOCK_MODE_ADAPTIVE, DVDD_VOL_123V);
    clk_early_init(PLL_REF_LRC, 200000, 480000000);

    log_init(1000000);
    register_handle_printf_putchar(putchar);

    log_info("--------sh59-apps-------------\n");
    power_early_flowing();
    board_power_init();
    clock_dump();
    efuse_dump();

    mem_init();
    /* mem_stats(); */
    critical_hook_init();
    early_system_init();
    power_later_flowing();
    xprintf("sh59 main!\n");

    system_init();
    app();
    while (1) {
        wdt_clear();
        mdelay(500);
        putchar('h');
    }


    return 0;

}


