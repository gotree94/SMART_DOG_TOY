#include "config.h"
#include "clock.h"
#include "uart_debug.h"
#include "uart.h"
#include "gpio.h"
#include "board_cfg.h"

#if UART_LOG


//new uart driver

/* #define     TCFG_UART_BAUDRATE  1000000 */
#define     TCFG_UART_TX_PORT   UART_OUTPUT_CH_PORT
#define     TCFG_UART_RX_PORT   -1

void log_uart_init(u32 freq)
{
    if (FALSE == libs_debug) {
        return;
    }
#if 1
    struct uart_config debug_uart_config = {
        .baud_rate = freq/*TCFG_UART_BAUDRATE*/,
        .tx_pin = TCFG_UART_TX_PORT,
        .rx_pin = TCFG_UART_RX_PORT,
    };
    /* DEBUG_UART->TX_CON0 &= ~BIT(0);//close uart */
    uart_clk_src_std48m();
    uart_init(DEBUG_UART_NUM, &debug_uart_config);
#else
    JL_PORTA->DIR &= ~BIT(5);
    JL_PORTA->OUT |=  BIT(5);
    JL_PORTA->DIE |=  BIT(5);

    JL_OMAP->PA5_OUT = FO_UART0_TX;

    JL_UART0->RX_CON0 = BIT(12) | BIT(10);
    JL_UART0->TX_CON0 = BIT(13);

    //set baud
    JL_UART0->TX_CON0 &= ~BIT(0);
    JL_UART0->RX_CON0 |= BIT(12) | BIT(10);
    JL_UART0->TX_CON0 |= BIT(13);
    JL_UART0->BAUD = (24000000 / freq) / 4 - 1;
    JL_UART0->RX_CON0 |= BIT(12) | BIT(10);
    JL_UART0->TX_CON0 |= BIT(13) | BIT(0);

    JL_UART0->BUF = '\n';
#endif
}

AT(..log_ut.text.cache.L2)
static void ut_putchar(char a)
{
    if (FALSE == libs_debug) {
        return;
    }
    /* if(a == '\n'){                          */
    /*     uart_log_putbyte(DEBUG_UART_NUM, '\r'); */
    /* }                                       */
    uart_log_putbyte(DEBUG_UART_NUM, a);
}

static int ut_getchar(void)
{
    if (FALSE == libs_debug) {
        return 0;
    }
    return uart_getbyte(DEBUG_UART_NUM);
}


#endif

char simple_ut_getchar(void)
{
    if (FALSE == libs_debug) {
        return 0;
    }
    char c;
    c = 0;
    if (DEBUG_UART->RX_CON0 & BIT(14)) {
        c = DEBUG_UART->BUF;
        DEBUG_UART->RX_CON0 |= BIT(12);

    }
    return c;
}

AT(..log_ut.text.cache.L2)
int putchar_in_ram(char a)
{
    if (FALSE == libs_debug) {
        return -1;
    }

    u32 i = 0x10000;
    if (!(DEBUG_UART->TX_CON0 & BIT(0))) {
        return -1;
    }
    while (((DEBUG_UART->TX_CON0 & BIT(15)) == 0) && (0 != i)) {  //TX IDLE
        i--;
    }
    DEBUG_UART->TX_CON0 |= BIT(13);  //清Tx pending

    DEBUG_UART->BUF = a;
    __asm__ volatile("csync");
    return a;
}

AT(..log_ut.text.cache.L2)
int putchar(int a)
{
    /* ut_putchar(a); */
    return putchar_in_ram(a);
}

int getchar(void)
{
    return simple_ut_getchar();
}


