#ifndef HWI_H
#define HWI_H

#include "typedef.h"

#define IRQ_SDTAP_IDX       0
#define IRQ_EXCEPTION_IDX   1
#define IRQ_SYSCALL_IDX     2
#define IRQ_TICK_TMR_IDX    3
#define IRQ_TICKTMR_IDX    3

#define IRQ_TIME0_IDX       4
#define IRQ_TIME1_IDX       5
#define IRQ_TIME2_IDX       6
#define IRQ_UART0_IDX       7
#define IRQ_UART1_IDX       8

#define IRQ_SPI1_IDX        10
#define IRQ_SPI2_IDX        11
#define IRQ_PORT_IDX        12
#define IRQ_GPADC_IDX       13

#define IRQ_GPCNT0_IDX      15
#define IRQ_GPCNT1_IDX      16
#define IRQ_ROTATE_IDX      17
#define IRQ_SD0_IDX 	    18
#define IRQ_USB_SOF_IDX     19
#define IRQ_USB_CTRL_IDX    20
#define IRQ_WWDG_IDX        21

#define IRQ_SD0_BRK_IDX     24

#define IRQ_CTM0_IDX        27

#define IRQ_IIC0_IDX        31
#define IRQ_PRP_TMR0_IDX    32

#define IRQ_DMA0_IDX        40
#define IRQ_DMA1_IDX        41

#define IRQ_P33_INT_IDX     53
#define IRQ_PINR_IDX        54
#define IRQ_PMU_TMR0_IDX    55
#define IRQ_PMU_TMR1_IDX    56
#define IRQ_VLVD_INT_IDX    57

#define IRQ_SRC_HW_IDX      66

#define IRQ_SPI0_IDX        68

#define IRQ_DCP_IDX         70

#define IRQ_DPA_IDX         84

#define IRQ_ALNK_SOFT       87
#define IRQ_ADC_IDX         88

#define IRQ_FFT_IDX         116

#define IRQ_SOFT0_IDX       120
#define IRQ_SOFT1_IDX       121
#define IRQ_SOFT2_IDX       122
#define IRQ_SOFT3_IDX       123
#define IRQ_SOFT4_IDX       124
#define IRQ_SOFT5_IDX       125
#define IRQ_SOFT6_IDX       126
#define IRQ_SOFT7_IDX       127

#define MAX_IRQ_ENTRY_NUM   128


#define IRQ_AUDIO_IDX      IRQ_DAC_SOFT
#define IRQ_AUDIO_B_IDX    IRQ_ADC_SOFT


//系统使用到的
extern const int IRQ_IRTMR_IP;
extern const int IRQ_AUDIO_IP;
extern const int IRQ_AUDAC_IP;
extern const int IRQ_AUAPA_IP;
extern const int IRQ_AUADC_IP;
extern const int IRQ_DECODER_IP;
extern const int IRQ_WFILE_IP;
extern const int IRQ_ADC_IP;
extern const int IRQ_ENCODER_IP;
extern const int IRQ_TICKTMR_IP;
extern const int IRQ_USB_IP;
extern const int IRQ_SD_IP;
//系统还未使用到的
extern const int IRQ_UART0_IP;
extern const int IRQ_UART1_IP;
extern const int IRQ_ALINK0_IP;



extern u32 _IRQ_MEM_ADDR[];

#define IRQ_MEM_ADDR        (_IRQ_MEM_ADDR)

void bit_clr_swi(unsigned char index);
void bit_set_swi(unsigned char index);

void bit_set_swi0(void);

void interrupt_init();

void irq_resume(void);
void irq_enable(u8 index);
void reg_set_ip(unsigned char index, unsigned char priority);
extern void request_irq_rom(u8 index, u8 priority, void (*handler)(void), u8 cpu_id);
void HWI_Install(unsigned char index, unsigned int isr, unsigned char priority);
#define     request_irq(idx,ip,hdl,arg) HWI_Install(idx,(int)hdl,ip)
void unrequest_irq(u8 index);

void bit_clr_ie(unsigned char index);
void bit_set_ie(unsigned char index);

void irq_unmask_set(u8 index);
void irq_unmask_disable(u8 index);

#ifdef IRQ_TIME_COUNT_EN
void irq_handler_enter(int irq);

void irq_handler_exit(int irq);

void irq_handler_times_dump();
#else

#define irq_handler_enter(irq)      do { }while(0)
#define irq_handler_exit(irq)       do { }while(0)
#define irq_handler_times_dump()    do { }while(0)

#endif


static inline int core_num(void)
{
    return 0;
}
#endif

