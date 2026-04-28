#ifndef  __GPIO_HW_H__
#define  __GPIO_HW_H__

#include "typedef.h"
// #include "power_interface.h"


#define IO_GROUP_NUM 		16


#define IO_PORTA_00 				(IO_GROUP_NUM * 0 + 0)
#define IO_PORTA_01 				(IO_GROUP_NUM * 0 + 1)
#define IO_PORTA_02 				(IO_GROUP_NUM * 0 + 2)
#define IO_PORTA_03 				(IO_GROUP_NUM * 0 + 3)
#define IO_PORTA_04 				(IO_GROUP_NUM * 0 + 4)
#define IO_PORTA_05 				(IO_GROUP_NUM * 0 + 5)
#define IO_PORTA_06 				(IO_GROUP_NUM * 0 + 6)
#define IO_PORTA_07 				(IO_GROUP_NUM * 0 + 7)
#define IO_PORTA_08 				(IO_GROUP_NUM * 0 + 8)
#define IO_PORTA_09 				(IO_GROUP_NUM * 0 + 9)
#define IO_PORTA_10 				(IO_GROUP_NUM * 0 + 10)
#define IO_PORTA_11 				(IO_GROUP_NUM * 0 + 11)
#define IO_PORTA_12 				(IO_GROUP_NUM * 0 + 12)
#define IO_PORTA_13 				(IO_GROUP_NUM * 0 + 13)
#define IO_PORTA_14 				(IO_GROUP_NUM * 0 + 14)
#define IO_PORTA_15 				(IO_GROUP_NUM * 0 + 15)
#define IO_PORT_PA_MASK              0xffff

#define IO_PORTB_00 				(IO_GROUP_NUM * 1 + 0)
#define IO_PORTB_01 				(IO_GROUP_NUM * 1 + 1)
#define IO_PORTB_02 				(IO_GROUP_NUM * 1 + 2)
#define IO_PORTB_03 				(IO_GROUP_NUM * 1 + 3)
#define IO_PORTB_04 				(IO_GROUP_NUM * 1 + 4)
#define IO_PORTB_05 				(IO_GROUP_NUM * 1 + 5)
#define IO_PORT_PB_MASK              0x003f


// #define IO_PORTC_00 				(IO_GROUP_NUM * 2 + 0)
#define IO_PORT_PC_MASK              0x0
// #define IO_PORTD_00 				(IO_GROUP_NUM * 3 + 0)
#define IO_PORT_PD_MASK              0x0

#define IO_PORTF_00 				(IO_GROUP_NUM * 5 + 0)
#define IO_PORTF_01 				(IO_GROUP_NUM * 5 + 1)
#define IO_PORTF_02 				(IO_GROUP_NUM * 5 + 2)
#define IO_PORTF_03 				(IO_GROUP_NUM * 5 + 3)
#define IO_PORTF_04 				(IO_GROUP_NUM * 5 + 4)
#define IO_PORTF_05 				(IO_GROUP_NUM * 5 + 5)
#define IO_PORT_PF_MASK              0x003f

#define IO_PORTP_00 				(IO_GROUP_NUM * 13 + 0)
#define IO_PORTP_01 				(IO_GROUP_NUM * 13 + 1)
#define IO_PORT_PP_MASK              0x0002

#define IO_PORT_LDOIN   IO_PORTP_00

#define IO_MAX_NUM 					(IO_PORTP_01 + 1)

#define IO_PORT_DP                  (IO_GROUP_NUM * 14 + 0)
#define IO_PORT_DM                  (IO_GROUP_NUM * 14 + 1)
#define IO_PORT_USB_MASK            0x03
#define IS_PORT_USB(x)              (x <= IO_PORT_DM)//无usb赋0

//无pr
// #define IO_PORT_PR_00               (IO_GROUP_NUM * 15 + 0)//pr固定15
// #define IO_PORT_PR_01               (IO_GROUP_NUM * 15 + 1)
// #define IO_PORT_PR_MASK              0x03

#define IO_PORT_MAX					(IO_PORT_DM + 1)

#define P33_IO_OFFSET               0
#define IO_CHGFL_DET                (IO_PORT_MAX + P33_IO_OFFSET + 0)
#define IO_VBGOK_DET                (IO_PORT_MAX + P33_IO_OFFSET + 1)
#define IO_VBTCH_DET                (IO_PORT_MAX + P33_IO_OFFSET + 2)
#define IO_LDOIN_DET                (IO_PORT_MAX + P33_IO_OFFSET + 3)
#define IO_VBATDT_DET               (IO_PORT_MAX + P33_IO_OFFSET + 4)

#define GPIOA                       (IO_GROUP_NUM * 0)
#define GPIOB                       (IO_GROUP_NUM * 1)
// #define GPIOC                       (IO_GROUP_NUM * 2)//无
// #define GPIOD                       (IO_GROUP_NUM * 3)//无
// #define GPIOE                       (IO_GROUP_NUM * 4)//无
#define GPIOF                       (IO_GROUP_NUM * 5)
#define GPIOP                       (IO_GROUP_NUM * 13)
#define GPIOUSB                     (IO_GROUP_NUM * 14)
// #define GPIOR                       (IO_GROUP_NUM * 15) //no pr
#define GPIOP33                     (IO_PORT_MAX + P33_IO_OFFSET)

enum gpio_port {
    PORTA = 0,
    PORTB = 1,
    // PORTC = 2,
    //PORTD = 3,
    PORTF = 5,
    PORTP = 13,
    PORTUSB = 14,
    // PORTR = 15,  //无pr
};
#define IS_PORT_ALL_PERIPH(PORT) (((PORT) == PORTA) || \
                                  ((PORT) == PORTB) || \
                                  ((PORT) == PORTF) || \
                                  ((PORT) == PORTP) || \
                                  ((PORT) == PORTUSB))

enum port_op_mode {
    PORT_SET = 1,
    PORT_AND,
    PORT_OR,
    PORT_XOR,
};

struct port_reg {
    volatile unsigned int in;
    volatile unsigned int out;
    volatile unsigned int dir;
    volatile unsigned int die;
    volatile unsigned int dieh;
    volatile unsigned int pu;
    volatile unsigned int pd;
    volatile unsigned int hd;
    volatile unsigned int spl;
    volatile unsigned int con;

    volatile unsigned int out_bsr;
    volatile unsigned int dir_bsr;
    volatile unsigned int die_bsr;
    volatile unsigned int dieh_bsr;
    volatile unsigned int pu0_bsr;
    volatile unsigned int pu1_bsr;
    volatile unsigned int pd0_bsr;
    volatile unsigned int pd1_bsr;
    volatile unsigned int hd0_bsr;
    volatile unsigned int hd1_bsr;
    volatile unsigned int spl_bsr;
    volatile unsigned int con_bsr;
};
#define GPIO_PX_PU_REG_NUM 2
#define GPIO_PX_PD_REG_NUM 2
#define GPIO_PX_HD_REG_NUM 2
#define GPIO_PX_DIEH_REG_NUM 1
#define GPIO_PX_SPL_REG_NUM  1
#define GPIO_PX_BSR_REG_NUM  1
#define GPIO_PX_HVT_REG_NUM 1

#define usb_reg port_reg
#define GPIO_USB_PU_REG_NUM 2
#define GPIO_USB_PD_REG_NUM 2
#define GPIO_USB_HD_REG_NUM 0
#define GPIO_USB_NEW_HD_EN  1 //new hd(portusb_con:bit5,sh59)
#define GPIO_USB_DIEH_REG_NUM 1
#define GPIO_USB_SPL_REG_NUM  1
#define GPIO_USB_BSR_REG_NUM  1
#define GPIO_USB_HVT_REG_NUM 0

//无PR
// struct port_pr_reg {
//     volatile unsigned int in;
//     volatile unsigned int out;
//     volatile unsigned int dir;
//     volatile unsigned int die;
//     volatile unsigned int pu0;
//     // volatile unsigned int pu1;
//     volatile unsigned int pd0;
//     // volatile unsigned int pd1;
//     volatile unsigned int hd0;
//     // volatile unsigned int hd1;
// };
// #define GPIO_PR_PU_REG_NUM 0
// #define GPIO_PR_PD_REG_NUM 0
// #define GPIO_PR_HD_REG_NUM 0
// #define GPIO_PR_DIEH_REG_NUM 0
// #define GPIO_PR_SPL_REG_NUM  0
// #define GPIO_PR_BSR_REG_NUM  0
// #define GPIO_PR_HVT_REG_NUM 0

#define GPIO_PU_REG_NUM 2 //max_num
#define GPIO_PD_REG_NUM 2 //max_num
#define GPIO_HD_REG_NUM 2 //max_num
#define GPIO_NEW_PUPDHD_EN 1 //new pu pd hd(需usb,pr,px都支持新上下拉,sh59)


// ----------------------------------------
// enum gpio_hd_mode {//no use
//     GPIO_HD_2p4mA,		#<{(| 最大驱动电流  2.4mA |)}>#
//     GPIO_HD_8p0mA,		#<{(| 最大驱动电流  8.0mA |)}>#
//     GPIO_HD_26p4mA,		#<{(| 最大驱动电流  26.4mA  |)}>#
//     GPIO_HD_50p0mA,		#<{(| 最大驱动电流 50.0mA |)}>#
// };

//===================================================//
// Crossbar API
//===================================================//
enum PFI_TABLE {
    PFI_NULL = 0,
    PFI_GP_ICH0 = ((u32)(&(JL_IMAP->FI_GP_ICH0))),
    PFI_GP_ICH1 = ((u32)(&(JL_IMAP->FI_GP_ICH1))),
    PFI_GP_ICH2 = ((u32)(&(JL_IMAP->FI_GP_ICH2))),
    PFI_GP_ICH3 = ((u32)(&(JL_IMAP->FI_GP_ICH3))),
    PFI_SD0_CMD = ((u32)(&(JL_IMAP->FI_SD0_CMD))),
    PFI_SD0_DA0 = ((u32)(&(JL_IMAP->FI_SD0_DA0))),
    // PFI_SD0_DA1 = ((u32)(&(JL_IMAP->FI_SD0_DA1))),
    // PFI_SD0_DA2 = ((u32)(&(JL_IMAP->FI_SD0_DA2))),
    // PFI_SD0_DA3 = ((u32)(&(JL_IMAP->FI_SD0_DA3))),

    PFI_SPI1_CLK = ((u32)(&(JL_IMAP->FI_SPI1_CLK))),
    PFI_SPI1_DA0 = ((u32)(&(JL_IMAP->FI_SPI1_DA0))),
    PFI_SPI1_DA1 = ((u32)(&(JL_IMAP->FI_SPI1_DA1))),
    PFI_SPI1_DA2 = ((u32)(&(JL_IMAP->FI_SPI1_DA2))),
    PFI_SPI1_DA3 = ((u32)(&(JL_IMAP->FI_SPI1_DA3))),

    PFI_SPI2_CLK = ((u32)(&(JL_IMAP->FI_SPI2_CLK))),
    PFI_SPI2_DA0 = ((u32)(&(JL_IMAP->FI_SPI2_DA0))),
    PFI_SPI2_DA1 = ((u32)(&(JL_IMAP->FI_SPI2_DA1))),
    PFI_SPI2_DA2 = ((u32)(&(JL_IMAP->FI_SPI2_DA2))),
    PFI_SPI2_DA3 = ((u32)(&(JL_IMAP->FI_SPI2_DA3))),

    PFI_IIC0_SCL = ((u32)(&(JL_IMAP->FI_IIC0_SCL))),
    PFI_IIC0_SDA = ((u32)(&(JL_IMAP->FI_IIC0_SDA))),

    PFI_UART0_RX = ((u32)(&(JL_IMAP->FI_UART0_RX))),
    PFI_UART1_RX = ((u32)(&(JL_IMAP->FI_UART1_RX))),

    PFI_QDEC0_A  = ((u32)(&(JL_IMAP->FI_QDEC0_A))),
    PFI_QDEC0_B  = ((u32)(&(JL_IMAP->FI_QDEC0_B))),

    PFI_ALNK0_MCLK = ((u32)(&(JL_IMAP->FI_ALNK0_MCLK))),
    PFI_ALNK0_SCLK = ((u32)(&(JL_IMAP->FI_ALNK0_SCLK))),
    PFI_ALNK0_LRCK = ((u32)(&(JL_IMAP->FI_ALNK0_LRCK))),
    PFI_ALNK0_DAT0 = ((u32)(&(JL_IMAP->FI_ALNK0_DAT0))),
    PFI_ALNK0_DAT1 = ((u32)(&(JL_IMAP->FI_ALNK0_DAT1))),

    PFI_TOTAl = ((u32)(&(JL_IMAP->FI_TOTAL))),
};

#define  INPUT_GP_ICH_MAX  4
#define  INPUT_GP_ICH_BIT_WIDTH 5
#define  OUTPUT_GP_OCH_MAX 4
#define  OUTPUT_GP_OCH_BIT_WIDTH 8
#define  INPUT_GP_ICH_SIGNAL_NUM (32/INPUT_GP_ICH_BIT_WIDTH)
#define  OUTPUT_GP_OCH_SIGNAL_NUM (32/OUTPUT_GP_OCH_BIT_WIDTH)
#if 1
enum OUTPUT_CH_SIGNAL {
    OCH_TIMER0_PWM,//8
    OCH_TIMER1_PWM,
    OCH_TIMER2_PWM,
    OCH_GP_ICH0,
    OCH_GP_ICH1,
    OCH_GP_ICH2,
    OCH_GP_ICH3,
    OCH_UART1_RTS,
    OCH_CLOCK_OUT0,
    OCH_CLOCK_OUT1,
    OCH_CLOCK_OUT2,
    OCH_AUD_DBG_CLKO,
    OCH_AUD_DBG_DATO0,
    OCH_AUD_DBG_DATO1,
    OCH_AUD_DBG_DATO2,
    OCH_AUD_DBG_DATO3,
    OCH_AUD_DBG_DATO4,
    OCH_P33_CLK_DBG,
    OCH_P33_SIG_DBG0,
    OCH_P33_SIG_DBG1,
    OCH_USB_DBG_OUT,
    OCH_DISABLE0,//OCH_P11_DBG_OUT,
    OCH_ADC_DMIC_CLK,
};

enum INPUT_CH_TYPE {
    ICH_TYPE_GP_ICH = 0,
    ICH_TYPE_TIME0_PWM = INPUT_GP_ICH_MAX,//部分芯片不连续
    ICH_TYPE_TIME1_PWM,
    ICH_TYPE_TIME2_PWM,
    ICH_TYPE_MAX,
};

enum INPUT_CH_SIGNAL {
    //ICH_CON0
    ICH_TIMER0_CIN = 0,//4
    ICH_TIMER1_CIN,
    ICH_TIMER2_CIN,
    ICH_TIMER0_CAPTURE,
    ICH_TIMER1_CAPTURE,
    ICH_TIMER2_CAPTURE,

    //ICH_CON1
    ICH_UART1_CTS,
    ICH_CAP,
    ICH_CLK_PIN,
    ICH_EXT_CLK,
    ICH_SPI1_CS,
    ICH_SPI2_CS,

    //ICH_CON2
    ICH_ADV_TMR0_IC1,
    ICH_ADV_TMR0_IC2,
    ICH_ADV_TMR0_IC3,
    ICH_ADV_TMR0_IC4,
    ICH_ADV_TMR0_ETR_IN,
    ICH_ADV_TMR0_BRK_IN1,

    //ICH_CON3
    ICH_ADV_TMR0_BRK_IN2,
    ICH_ADC_DMIC_IDAT0,
    ICH_ADC_DMIC_IDAT1,
};
#else
#endif

enum gpio_function {
    PORT_FUNC_NULL,    //null
    PORT_FUNC_UART0_TX, //out
    PORT_FUNC_UART0_RX,//in
    PORT_FUNC_UART1_TX, //out
    PORT_FUNC_UART1_RX,//in
    // PORT_FUNC_UART2_TX, //out
    // PORT_FUNC_UART2_RX,//in
    PORT_FUNC_UART1_RTS,//out
    PORT_FUNC_UART1_CTS,//in

    // PORT_FUNC_SPI0_CLK,
    // PORT_FUNC_SPI0_DA0,
    // PORT_FUNC_SPI0_DA1,
    // PORT_FUNC_SPI0_DA2,
    // PORT_FUNC_SPI0_DA3,
    PORT_FUNC_SPI1_CS,
    PORT_FUNC_SPI1_CLK,
    PORT_FUNC_SPI1_DA0,
    PORT_FUNC_SPI1_DA1,
    PORT_FUNC_SPI1_DA2,
    PORT_FUNC_SPI1_DA3,
    PORT_FUNC_SPI2_CS,
    PORT_FUNC_SPI2_CLK,
    PORT_FUNC_SPI2_DA0,
    PORT_FUNC_SPI2_DA1,
    PORT_FUNC_SPI2_DA2,
    PORT_FUNC_SPI2_DA3,

    PORT_FUNC_IIC0_SCL,
    PORT_FUNC_IIC0_SDA,
    // PORT_FUNC_IIC1_SCL,
    // PORT_FUNC_IIC1_SDA,

    PORT_FUNC_SD0_CLK,//out
    PORT_FUNC_SD0_CMD,
    PORT_FUNC_SD0_DA0,
    // PORT_FUNC_SD0_DA1,
    // PORT_FUNC_SD0_DA2,
    // PORT_FUNC_SD0_DA3,

    // PORT_FUNC_CAN0_TX,
    // PORT_FUNC_CAN0_RX,

    PORT_FUNC_GPADC,    //in
    PORT_FUNC_PWM_LED,

    // PORT_FUNC_PLNK_SCLK,//out
    // PORT_FUNC_PLNK_DAT0,//in
    // PORT_FUNC_PLNK_DAT1,//in

    PORT_FUNC_OCH_CLOCK_OUT0,
    PORT_FUNC_OCH_CLOCK_OUT1, //不连续 PORT_FUNC_OCH_RESERVED0
    PORT_FUNC_OCH_CLOCK_OUT2,
    // PORT_FUNC_OCH_CLOCK_OUT3,

    PORT_FUNC_TIMER0_PWM,
    PORT_FUNC_TIMER1_PWM,
    PORT_FUNC_TIMER2_PWM,
    // PORT_FUNC_TIMER3_PWM,
    PORT_FUNC_TIMER0_CAPTURE,
    PORT_FUNC_TIMER1_CAPTURE,
    PORT_FUNC_TIMER2_CAPTURE,
    // PORT_FUNC_TIMER3_CAPTURE,
    PORT_FUNC_IRFLT_0, //实际只有1个IRFLT
    PORT_FUNC_IRFLT_1,
    PORT_FUNC_IRFLT_2,
    // PORT_FUNC_IRFLT_3,
    PORT_FUNC_CLK_PIN,
    PORT_FUNC_CAP,
    PORT_FUNC_EXT_CLK,
    // PORT_FUNC_PORT_WKUP,

    // PORT_FUNC_MCPWM0_H,
    // PORT_FUNC_MCPWM0_L,
    // PORT_FUNC_MCPWM1_H,
    // PORT_FUNC_MCPWM1_L,
    // PORT_FUNC_MCPWM2_H,
    // PORT_FUNC_MCPWM2_L,
    // PORT_FUNC_MCPWM0_FP,
    // PORT_FUNC_MCPWM1_FP,
    // PORT_FUNC_MCPWM2_FP,

    // PORT_FUNC_COMP0_OUT,
    // PORT_FUNC_COMP1_OUT,
    // PORT_FUNC_COMP2_OUT,
    // PORT_FUNC_COMP3_OUT,

    // PORT_FUNC_LEDC0_OUT,
    // PORT_FUNC_LEDC1_OUT,
    // PORT_FUNC_RDEC0_PORT0,
    // PORT_FUNC_RDEC0_PORT1,
    PORT_FUNC_QDEC0_A,
    PORT_FUNC_QDEC0_B,

    PORT_FUNC_TMR0_OC1P,
    PORT_FUNC_TMR0_OC2P,
    PORT_FUNC_TMR0_OC3P,
    PORT_FUNC_TMR0_OC4P,
    PORT_FUNC_TMR0_OC1N,
    PORT_FUNC_TMR0_OC2N,
    PORT_FUNC_TMR0_OC3N,
    PORT_FUNC_TMR0_OC4N,
    // PORT_FUNC_TMR1_OC1P,
    // PORT_FUNC_TMR1_OC2P,
    // PORT_FUNC_TMR1_OC3P,
    // PORT_FUNC_TMR1_OC4P,
    // PORT_FUNC_TMR1_OC1N,
    // PORT_FUNC_TMR1_OC2N,
    // PORT_FUNC_TMR1_OC3N,
    // PORT_FUNC_TMR1_OC4N,
    PORT_FUNC_TMR0_IC1,
    PORT_FUNC_TMR0_IC2,
    PORT_FUNC_TMR0_IC3,
    PORT_FUNC_TMR0_IC4,
    // PORT_FUNC_TMR1_IC1,
    // PORT_FUNC_TMR1_IC2,
    // PORT_FUNC_TMR1_IC3,
    // PORT_FUNC_TMR1_IC4,
    PORT_FUNC_TMR0_ETR_IN,
    PORT_FUNC_TMR0_BRK_IN1,
    PORT_FUNC_TMR0_BRK_IN2,
    // PORT_FUNC_TMR1_ETR_IN,
    // PORT_FUNC_TMR1_BRK_IN1,
    // PORT_FUNC_TMR1_BRK_IN2,

    PORT_FUNC_ADC_DMIC_IDAT0,
    PORT_FUNC_ADC_DMIC_IDAT1,
    PORT_FUNC_ADC_DMIC_CLK,

    // PORT_FUNC_HRTIM_EXTEVNT1,
    // PORT_FUNC_HRTIM_EXTEVNT2,
    // PORT_FUNC_HRTIM_EXTEVNT3,
    // PORT_FUNC_HRTIM_EXTEVNT4,
    // PORT_FUNC_HRTIM_EXTEVNT5,
    // PORT_FUNC_HRTIM_EXTEVNT6,
    // PORT_FUNC_HRTIM_EXTEVNT7,
    // PORT_FUNC_HRTIM_EXTEVNT8,
    // PORT_FUNC_HRTIM_EXTEVNT9,
    // PORT_FUNC_HRTIM_EXTEVNT10,
    // PORT_FUNC_HRTIM_FAULT1,
    // PORT_FUNC_HRTIM_FAULT2,
    // PORT_FUNC_HRTIM_FAULT3,
    // PORT_FUNC_HRTIM_FAULT4,
    // PORT_FUNC_HRTIM_FAULT5,
    // PORT_FUNC_HRTIM_PWMA1,
    // PORT_FUNC_HRTIM_PWMA2,
    // PORT_FUNC_HRTIM_PWMB1,
    // PORT_FUNC_HRTIM_PWMB2,
    // PORT_FUNC_HRTIM_PWMC1,
    // PORT_FUNC_HRTIM_PWMC2,

    PORT_FUNC_ALNK0_MCLK,
    PORT_FUNC_ALNK0_SCLK,
    PORT_FUNC_ALNK0_LRCK,
    PORT_FUNC_ALNK0_DAT0,
    PORT_FUNC_ALNK0_DAT1,
};
/**************************************************/
#define __struct(x) (struct x##_reg *)
#define _struct(x) __struct(x)
#ifdef GPIOA
#define __PORTPA ((struct port_reg *)JL_PORTA)
#endif
#ifdef GPIOB
#define __PORTPB ((struct port_reg *)JL_PORTB)
#endif
#ifdef GPIOC
#define __PORTPC ((struct port_reg *)JL_PORTC)
#endif
#ifdef GPIOD
#define __PORTPD ((struct port_reg *)JL_PORTD)
#endif
#ifdef GPIOE
#define __PORTPE ((struct port_reg *)JL_PORTE)
#endif
#ifdef GPIOF
#define __PORTPF ((struct port_reg *)JL_PORTF)
#endif
#ifdef GPIOG
#define __PORTPG ((struct port_reg *)JL_PORTG)
#endif
#ifdef GPIOH
#define __PORTPH ((struct port_reg *)JL_PORTH)
#endif
#ifdef GPIOP
#define __PORTPP ((struct port_reg *)JL_PORTP)
#endif
#ifdef GPIOR
#define __PORTPR ((struct port_pr_reg *)R3_PR_IO_P)
#endif
#ifdef GPIOUSB
#define __PORTPU ((struct usb_reg *)JL_PORTUSB)
#endif
#define __portx(x,y) __PORT##x->y
#define _portx(x,y)  __portx(x,y)
#define __toggle_port(x,y) __PORT##x->out ^= y;
#define _toggle_port(port,pin) __toggle_port(port,pin)

//log:
#define GPIO_NO_SUPPORT_FUN "------"
#if defined(GPIO_NEW_PUPDHD_EN) && GPIO_NEW_PUPDHD_EN
#define GPIO_LOG_ITEM "port   :out     dir     die     dieh    pu      pd      hd      spl"
#define GPIO_LOG_FORMAT "0x%04x  0x%04x  0x%04x  0x%04x  0x%04x  0x%04x  0x%04x  0x%04x"
#define GPIO_LOG_PORT(x,y) JL_PORT##x->OUT&y,JL_PORT##x->DIR&y,JL_PORT##x->DIE&y,JL_PORT##x->DIEH&y,JL_PORT##x->PU&y,JL_PORT##x->PD&y,JL_PORT##x->HD&y,JL_PORT##x->SPL&y
#define GPIO_LOG_PORTP JL_PORTP->OUT,JL_PORTP->DIR,JL_PORTP->DIE,JL_PORTP->DIEH,JL_PORTP->PU,JL_PORTP->PD,JL_PORTP->HD,JL_PORTP->SPL
#else
#define GPIO_LOG_ITEM "port   :out     dir     die     dieh    pu0   ,pu1     pd0   ,pd1     hd0   ,hd1     spl"
#define GPIO_LOG_FORMAT "0x%04x  0x%04x  0x%04x  0x%04x  0x%04x,0x%04x  0x%04x,0x%04x  0x%04x,0x%04x  0x%04x"
#define GPIO_LOG_PORT(x,y) JL_PORT##x->OUT&y,JL_PORT##x->DIR&y,JL_PORT##x->DIE&y,JL_PORT##x->DIEH&y,JL_PORT##x->PU0&y,JL_PORT##x->PU1&y,JL_PORT##x->PD0&y,JL_PORT##x->PD1&y,JL_PORT##x->HD0&y,JL_PORT##x->HD1&y,JL_PORT##x->SPL&y
#define GPIO_LOG_PORTP JL_PORTP->OUT,JL_PORTP->DIR,JL_PORTP->DIE,JL_PORTP->DIEH,JL_PORTP->PU0,JL_PORTP->PU1,JL_PORTP->PD0,JL_PORTP->PD1,JL_PORTP->HD0,JL_PORTP->HD1,JL_PORTP->SPL&y
#endif

#ifdef GPIOR
#define GPIO_LOG_FORMAT_R "0x%04x  0x%04x  0x%04x  %s  0x%04x,0x%04x  0x%04x,0x%04x  0x%04x,0x%04x  %s"
#define GPIO_LOG_PORTR R3_PR_OUT,R3_PR_DIR,R3_PR_DIE,GPIO_NO_SUPPORT_FUN,R3_PR_PU0,R3_PR_PU1,R3_PR_PD0,R3_PR_PD1,R3_PR_HD0,R3_PR_HD1,GPIO_NO_SUPPORT_FUN
#endif
#ifdef GPIOUSB
#if defined(GPIO_NEW_PUPDHD_EN) && GPIO_NEW_PUPDHD_EN
#else
#define GPIO_LOG_FORMAT_U "0x%04x  0x%04x  0x%04x  0x%04x  0x%04x,%s  0x%04x,%s  %s,%s  0x%04x"
#define GPIO_LOG_PORTU _portx(PU,out),_portx(PU,dir),_portx(PU,die),_portx(PU,dieh),_portx(PU,pu0),GPIO_NO_SUPPORT_FUN,_portx(PU,pd0),GPIO_NO_SUPPORT_FUN,GPIO_NO_SUPPORT_FUN,GPIO_NO_SUPPORT_FUN,_portx(PU,spl)
#endif
#endif
/*************************function*************************/
struct port_reg *gpio2reg(const u32 gpio);
/**
 * @brief usb_iomode
 *
 * @param enable 1，使能；0，关闭
 */
void usb_iomode(const u32 enable);
/**
 * @brief gpio_write
 *
 * @param gpio 参考宏IO_PORTx_xx，如IO_PORTA_00
 * @param value 1，输出1；0，输出0
 *
 * @return
 */
int gpio_hw_write(const u32 gpio, const u32 value);//return <0:error
/**
 * @brief gpio_read
 *
 * @param gpio 参考宏IO_PORTx_xx，如IO_PORTA_00
 *
 * @return
 */
int gpio_hw_read(const u32 gpio);//return <0:error

int get_gpio(const char *p);//return <0:error
const char *gpio_get_name(u32 gpio);

/**************************************************************/
/*********************multi pin interface***************************/
int gpio_hw_port_pin_judge(const enum gpio_port port, u32 pin);
/**
 * @brief port_set_direction
 *
 * @param port 参考枚举gpio_port：PORTA，PORTB
 * @param pin 参考宏：PORT_PIN_0，PORT_PIN_1
 * @param value 1，输入；0，输出
 *
 * @return <0 :error
 */
int gpio_hw_set_direction(const enum gpio_port port, u32 pin, const u32 value);
/**
 * @brief port_direction_input
 *
 * @param port 参考枚举gpio_port：PORTA，PORTB
 * @param pin 参考宏：PORT_PIN_0，PORT_PIN_1
 *
 * @return <0 :error
 */
int gpio_hw_direction_input(const enum gpio_port port, u32 pin);
/**
 * @brief port_direction_output/port_write_port/port_set_output_value
 *
 * @param port 参考枚举gpio_port：PORTA，PORTB
 * @param pin 参考宏：PORT_PIN_0，PORT_PIN_1
 * @param value 1，输出1；0，输出0
 * @return <0 :error
 */
int gpio_hw_direction_output(const enum gpio_port port, u32 pin, const int value);/////////
int gpio_hw_write_port(const enum gpio_port port, u32 pin, const u32 value);
int gpio_hw_set_output_value(const enum gpio_port port, u32 pin, const u32 value);
// #include "gpio.h"
/**
 * @brief port_set_pull_up/port_set_pull_down/port_set_hd/
 *
 * @param port 参考枚举gpio_port：PORTA，PORTB
 * @param pin 参考宏：PORT_PIN_0，PORT_PIN_1
 * @param value 参考枚举gpio_pullup_mode/gpio_pulldown_mode/gpio_drive_strength
 * @return <0 :error
 */
int gpio_hw_set_pull_up(const enum gpio_port port, u32 pin, const enum gpio_pullup_mode value);
int gpio_hw_set_pull_down(const enum gpio_port port, u32 pin, const enum gpio_pulldown_mode value);//portabcdpr:pd0,pd1,usb:pd0
int gpio_hw_set_drive_strength(const enum gpio_port port, u32 pin, const enum gpio_drive_strength value);
/**
 * @brief port_set_die/port_set_dieh
 *
 * @param port 参考枚举gpio_port：PORTA，PORTB
 * @param pin 参考宏：PORT_PIN_0，PORT_PIN_1
 * @param value 1，设置1；0，设置0
 * @return <0 :error
 */
int gpio_hw_set_die(const enum gpio_port port, u32 pin, const int value);
int gpio_hw_set_dieh(const enum gpio_port port, u32 pin, const u32 value);

//开漏输出
int gpio_hw_set_spl(const enum gpio_port port, u32 pin, const u32 value);
//hvt必须开下拉100k
int gpio_hw_set_hvt(const enum gpio_port port, u32 pin, const u32 value);//输入io1/2分压到adc(仅HVT类型io有效,其他io无效,影响功耗)
/**
 * @brief gpio_hw_read_port/gpio_hw_read_out_level/gpio_hw_read_drive_strength
 *
 * @param port 参考枚举gpio_port：PORTA，PORTB
 * @param pin 参考宏：PORT_PIN_0，PORT_PIN_1
 *
 * @return <0 :error ；other：ok
 */
int gpio_hw_read_port(const enum gpio_port port, u32 pin);
int gpio_hw_read_out_level(const enum gpio_port port, u32 pin);
u32 gpio_hw_read_drive_strength(const enum gpio_port port, u32 pin);//return hd1:高16位,  hd0:低16位

/**
 * @brief:同组多个io配置不同状态接口
 *
 * @param port 参考枚举gpio_port：PORTA，PORTB
 * @param pin 参考宏：PORT_PIN_0，PORT_PIN_1,可以多个io或|
 * @param value：对应pin的bit有效
 * @param op：参考枚举port_op_mode ：PORT_SET，PORT_AND,,,
 *
 * @return <0 :error ；other：ok
 */
int gpio_hw_op_dir(const enum gpio_port port, u32 pin, u32 value, const enum port_op_mode op);
int gpio_hw_op_out(const enum gpio_port port, u32 pin, u32 value, const enum port_op_mode op);
int gpio_hw_op_die(const enum gpio_port port, u32 pin, u32 value, const enum port_op_mode op);
int gpio_hw_op_dieh(const enum gpio_port port, u32 pin, u32 value, const enum port_op_mode op);
#if defined(GPIO_NEW_PUPDHD_EN) && GPIO_NEW_PUPDHD_EN
int gpio_hw_op_pu(const enum gpio_port port, u32 pin, u32 value, const enum port_op_mode op);
int gpio_hw_op_pd(const enum gpio_port port, u32 pin, u32 value, const enum port_op_mode op);
#else
int gpio_hw_op_pu0(const enum gpio_port port, u32 pin, u32 value, const enum port_op_mode op);
int gpio_hw_op_pu1(const enum gpio_port port, u32 pin, u32 value, const enum port_op_mode op);
int gpio_hw_op_pd0(const enum gpio_port port, u32 pin, u32 value, const enum port_op_mode op);
int gpio_hw_op_pd1(const enum gpio_port port, u32 pin, u32 value, const enum port_op_mode op);
#endif

//=================================================================================//
//@brief: CrossBar 获取某IO的输出映射寄存器
//@input:
// 		gpio: 需要输出外设信号的IO口; 如IO_PORTA_00
//@return:
// 		输出映射寄存器地址; 如&(JL_OMAP->PA0_OUT)
//=================================================================================//
u32 *gpio2crossbar_outreg(u32 gpio);

//=================================================================================//
//@brief: CrossBar 获取某IO的输入映射序号
//@input:
// 		gpio: 需要输出外设信号的IO口; 如IO_PORTA_00
//@return:
// 		输出映射序号; 如PA0_IN
//=================================================================================//
u32 gpio2crossbar_inport(u32 gpio);

//=================================================================================//
//@brief: CrossBar 输出设置 API, 将指定IO口设置为某个外设的输出
//@input:
// 		gpio: 需要输出外设信号的IO口;
// 		fun_index: 需要输出到指定IO口的外设信号, 可以输出外设信号列表请查看io_omap.h文件;
// 		dir_ctl: IO口方向由外设控制使能, 常设为1;
// 		data_ctl: IO口电平状态由外设控制使能, 常设为1;
//@return:
// 		1)0: 执行正确;
//		2)-EINVAL: 传参出错;
//@note: 所映射的IO需要在设置IO状态为输出配置;
//@example: 将UART0的Tx信号输出到IO_PORTA_05口:
// 			gpio_direction_output(IO_PORTA_05, 1); //设置IO为输出状态
//			gpio_set_fun_output_port(IO_PORTA_05, FO_UART0_TX, 1, 1); //将UART0的Tx信号输出到IO_PORTA_05口
//=================================================================================//
int gpio_set_fun_output_port(u32 gpio, u32 fun_index, u8 dir_ctl, u8 data_ctl);

//=================================================================================//
//@brief: CrossBar 输出设置 API, 将指定IO释放外设控制, 变为普通IO;
//@input:
// 		gpio: 需要释放外设控制IO口, 释放后变为普通IO模式;
//@return:
// 		1)0: 执行正确;
//		2)-EINVAL: 传参出错;
//@note:
//@example: 将IO_PORTA_05口被某一外设控制状态释放:
// 			gpio_disable_fun_output_port(IO_PORTA_05);
//=================================================================================//
int gpio_disable_fun_output_port(u32 gpio);

//=================================================================================//
//@brief: CrossBar 输入设置 API, 将某个外设的输入设置为从某个IO输入
//@input:
// 		gpio: 需要输入外设信号的IO口;
// 		pfun: 需要从指定IO输入的外设信号, 可以输入的外设信号列表请查看gpio.h文件enum PFI_TABLE枚举项;
//@return:
// 		1)0: 执行正确;
//		2)-EINVAL: 传参出错;
//@note: 所映射的IO需要在设置IO状态为输入配置;
//@example: 将UART0的Rx信号设置为IO_PORTA_05口输入:
//			gpio_set_die(IO_PORTA_05, 1); 		//数字输入使能
//			gpio_set_pull_up(IO_PORTA_05, 1);  //上拉输入使能
//			gpio_direction_input(IO_PORTA_05);  //设置IO为输入状态
//			gpio_set_fun_input_port(IO_PORTA_05, PFI_UART0_RX); //将UART0的Rx信号设置为IO_PORTA_05口输入
//=================================================================================//
int gpio_set_fun_input_port(u32 gpio, enum PFI_TABLE pfun);

//=================================================================================//
//@brief: CrossBar 输入设置 API, 将某个外设信号释放IO口控制, 变为普通IO;
//@input:
// 		pfun: 需要释放由某个IO口输入的外设信号, 外设信号列表请查看gpio.h文件enum PFI_TABLE枚举项;
//@return:  默认为0, 无出错处理;
//@note:
//@example: 将外设信号PFI_UART0_RX释放由某个IO输入:
// 			gpio_disable_fun_input_port(PFI_UART0_RX);
//=================================================================================//
int gpio_disable_fun_input_port(enum PFI_TABLE pfun);

//=================================================================================//
//@brief: Output Channel输出设置 API, 将指定IO口设置为某个外设的输出
//@input:
// 		gpio: 需要输出外设信号的IO口;
// 		signal: 将enum OUTPUT_CH_SIGNAL列表中需要输出到指定IO口的外设信号, 可以输出的外设信号列表请查看gpio.h文件的enum OUTPUT_CH_SIGNAL枚举项;
//@return:  默认为0, 出错内部触发ASSERT;
//@note: 所映射的IO需要在设置IO状态为输出配置;
//@example: 将OCH_MC_PWM0_H的Tx信号输出到IO_PORTA_05口:
// 			gpio_direction_output(IO_PORTA_05, 1); //设置IO为输出状态
//			gpio_och_sel_output_signal(IO_PORTA_05, OCH_MC_PWM0_H); //将OCH_MC_PWM0_H信号输出到IO_PORTA_05口
//=================================================================================//
int gpio_och_sel_output_signal(u32 gpio, enum OUTPUT_CH_SIGNAL signal);

//=================================================================================//
//@brief: Output Channel 输出设置 API, 将指定IO释放外设控制, 变为普通IO;
//@input:
// 		gpio: 需要释放外设控制IO口, 释放后变为普通IO模式;
// 		signal: 将enum OUTPUT_CH_SIGNAL列表中需要取消输出的外设信号, 外设信号列表请查看gpio.h文件的enum OUTPUT_CH_SIGNAL枚举项;;
//@return:  默认为0, 无出错处理;
//@note:
//@example: 将OCH_MC_PWM0_H取消输出IO_PORTA_05:
// 			gpio_och_disable_output_signal(IO_PORTA_05, OCH_MC_PWM0_H);
//=================================================================================//
int gpio_och_disable_output_signal(u32 gpio, enum OUTPUT_CH_SIGNAL signal);

//=================================================================================//
//@brief: Input Channel 输入设置 API, 将某个外设的输入设置为从某个IO输入
//@input:
// 		gpio: 需要输入外设信号的IO口;
// 		signal: 需要从指定IO输入的外设信号, 可以输入的外设信号列表请查看gpio.h文件enum INPUT_CH_SIGNAL枚举项;
//      type: INPUT_CH 类型, 常设为ICH_TYPE_GP_ICH;
//@return:  默认为0, 出错内部触发ASSERT;
//@note: 所映射的IO需要在设置IO状态为输入配置;
//@example: 将ICH_TIMER0_CIN信号设置为IO_PORTA_05口输入:
//			gpio_set_die(IO_PORTA_05, 1); 		//数字输入使能
//			gpio_set_pull_up(IO_PORTA_05, 1);  //上拉输入使能
//			gpio_direction_input(IO_PORTA_05);  //设置IO为输入状态
//			gpio_ich_sel_input_signal(IO_PORTA_05, ICH_TIMER0_CIN, ICH_TYPE_GP_ICH); //将ICH_TIMER0_CIN信号设置为IO_PORTA_05口输入
//=================================================================================//
int gpio_ich_sel_input_signal(u32 gpio, enum INPUT_CH_SIGNAL signal, enum INPUT_CH_TYPE type);

//=================================================================================//
//@brief: Input Channel 输入设置 API, 将某个外设信号释放IO口控制, 变为普通IO;
//@input:
// 		gpio: 需要取消输入外设信号的IO口;
// 		signal: 需要取消输入的外设信号, 外设信号列表请查看gpio.h文件enum INPUT_CH_SIGNAL枚举项;
//      type: INPUT_CH 类型, 常设为ICH_TYPE_GP_ICH;
//@return:  默认为0, 无出错处理;
//@note:
//@example: 将外设信号ICH_TIMER0_CIN释放由某个IO输入:
// 			gpio_ich_disable_input_signal(IO_PORTA_05, ICH_TIMER0_CIN, ICH_TYPE_GP_ICH);
//=================================================================================//
int gpio_ich_disable_input_signal(u32 gpio, enum INPUT_CH_SIGNAL signal, enum INPUT_CH_TYPE type);

u32 gpio_get_ich_use_flag();
//获取空闲的gp_ich
//return: 0xff:error
u8 gpio_get_unoccupied_gp_ich();
//value:gp_ich序号
void gpio_release_gp_ich(u8 value);

u32 get_sfc_port(void);
//打印指定组别指定pin的crossbar信息
void gpio_crossbar_fo_dump(char px_name[], u8 max_px_out_num, u16 px_mask, u32 *omap_ptr);
void gpio_crossbar_fi_dump(char px_name[], u8 max_px_in_num, u16 px_mask, u8 px_in);
#endif  /*GPIO_H*/

