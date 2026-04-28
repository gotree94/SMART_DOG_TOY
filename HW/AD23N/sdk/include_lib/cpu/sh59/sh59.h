#ifndef __SH59__
#define __SH59__

//===============================================================================//
//
//      sfr define
//
//===============================================================================//

#define hs_base            0x7ce0000
#define ls_base            0x7cd0000
#define as_base            0x7c10000
#define wl_base            0x7c00000

#define __RW               volatile       // read write
#define __RO               volatile const // only read
#define __WO               volatile       // only write

#define __u8               unsigned int   // u8  to u32 special for struct
#define __u16              unsigned int   // u16 to u32 special for struct
#define __u32              unsigned int

#define __s8(x)            char(x); char(reserved_1_##x); char(reserved_2_##x); char(reserved_3_##x)
#define __s16(x)           short(x); short(reserved_1_##x)
#define __s32(x)           int(x)

#define map_adr(grp, adr)  ((64 * grp + adr) * 4)     // grp(0x0-0xff), adr(0x0-0x3f)

//===============================================================================//
//
//      high speed sfr address define
//
//===============================================================================//

//............. 0x0000 - 0x00ff............ for hemu
typedef struct {
    __RW __u32 WREN;
    __RW __u32 CON0;
    __RW __u32 CON1;
    __RW __u32 CON2;
    __RW __u32 CON3;
    __RW __u32 MSG0;
    __RW __u32 MSG1;
    __RW __u32 MSG2;
    __RW __u32 MSG3;
    __RO __u32 ID;
} JL_HEMU_TypeDef;

#define JL_HEMU_BASE                    (hs_base + map_adr(0x00, 0x00))
#define JL_HEMU                         ((JL_HEMU_TypeDef    *)JL_HEMU_BASE)
//............. 0x0100 - 0x01ff............ for hsb memcfg
//typedef struct {
//    __RW __u32 MCFG_RF1P;
//    __RW __u32 MCFG_RF2P;
//    __RW __u32 MCFG_RM1P;
//    __RW __u32 MCFG_RM2P;
//    __RW __u32 MCFG_VROM;
//    __RW __u32 MPWR_VROM;
//} JL_HMEMCFG_TypeDef;

//#define JL_HMEMCFG_BASE                 (hs_base + map_adr(0x01, 0x00))
//#define JL_HMEMCFG                      ((JL_HMEMCFG_TypeDef *)JL_HMEMCFG_BASE)

//............. 0x0200 - 0x02ff............ for sfc
typedef struct {
    __RW __u32 CON0;
    __RW __u32 CON1;
    __RW __u32 USER_CMD;
    __RW __u32 USER_CRM;
    __RW __u32 OFFSET_ADR;
    __RW __u32 CLK_CON;
    __RW __u32 DTR_CON;
} JL_SFC_TypeDef;

#define JL_SFC_BASE                     (hs_base + map_adr(0x02, 0x00))
#define JL_SFC                          ((JL_SFC_TypeDef    *)JL_SFC_BASE)

typedef struct {
    __RW __u32 CON;
    __RW __u32 MASK;
    __WO __u32 KEY_ADDR;
    __WO __u32 KEY_DATL;
    __WO __u32 KEY_DATH;
    __RW __u32 NDEC_SADDR;
    __RW __u32 NDEC_EADDR;
} JL_SFCDEC_TypeDef;

#define JL_SFCDEC_BASE                  (hs_base + map_adr(0x02, 0x07))
#define JL_SFCDEC                       ((JL_SFCDEC_TypeDef    *)JL_SFCDEC_BASE)

typedef struct  {
    __RW __u32 CON;
    __RW __u32 SADDR;
    __RW __u32 EADDR;
    __RW __u32 OFFSET_ADR;
} JL_BOOTMMU_TypeDef;

#define JL_BOOTMMU_BASE                 (hs_base + map_adr(0x02, 0x0e))
#define JL_BOOTMMU                      ((JL_BOOTMMU_TypeDef   *)JL_BOOTMMU_BASE)

//............. 0x0300 - 0x03ff............
typedef struct {
    __RW __u16 MODE_CON;
    __RW __u16 PROT_CON;
} JL_MODE_TypeDef;

#define JL_MODE_BASE                    (hs_base + map_adr(0x03, 0x00))
#define JL_MODE                         ((JL_MODE_TypeDef     *)JL_MODE_BASE)

//............. 0x0400 - 0x04ff............ for syscfg
typedef struct {
    __RW __u32 RST_SRC;
} JL_HSBRST_TypeDef;

#define JL_HSBRST_BASE                  (hs_base + map_adr(0x04, 0x00))
#define JL_HSBRST                       ((JL_HSBRST_TypeDef        *)JL_HSBRST_BASE)

typedef struct {
    __RW __u32 PWR_CON;
    __RW __u32 SYS_CON0;
    __RW __u32 HSB_DIV;
    __RW __u32 HSB_SEL;
} JL_HSBCLK_TypeDef;

#define JL_HSBCLK_BASE                   (hs_base + map_adr(0x04, 0x01))
#define JL_HSBCLK                        ((JL_HSBCLK_TypeDef      *)JL_HSBCLK_BASE)

//............. 0x0500 - 0x05ff............ for pll0_ctl
//............. 0x0600 - 0x06ff............
//............. 0x0700 - 0x07ff............
#define JL_SPI0_BASE                    (hs_base + map_adr(0x07, 0x00))
#define JL_SPI0                         ((JL_SPI_TypeDef      *)JL_SPI0_BASE)

//............. 0x0800 - 0x08ff............
//............. 0x0900 - 0x09ff............
//............. 0x0a00 - 0x0aff............
typedef struct {
    __RW __u32 CON;
    __RW __u32 ADR;
    __RW __u32 MEMSET_DATL;
    __RW __u32 MEMSET_DATH;
} JL_DCP_TypeDef;

#define JL_DCP_BASE                     (hs_base + map_adr(0x0a, 0x00))
#define JL_DCP                          ((JL_DCP_TypeDef  *)JL_DCP_BASE)

//............. 0x0b00 - 0x0bff............
//............. 0x0c00 - 0x0cff............  EQ
//............. 0x0d00 - 0x0dff............ for src0(src_v2)
typedef struct {
    __RW __u32 CON0;
    __RW __u32 CON1;
    __RW __u32 CON2;
    __RW __u32 CON3;
    __RW __u32 IDAT_ADR;
    __RW __u32 IDAT_LEN;
    __RW __u32 ODAT_ADR;
    __RW __u32 ODAT_LEN;
    __RW __u32 FLTB_ADR;
    __WO __u32 ODAT_ADR_START;
    __WO __u32 ODAT_ADR_END;
    __RW __u32 STOP_FLAG;
    __RW __u32 INSR;
    __RW __u32 OUTSR;
    __RW __u32 PHASE;
} JL_SRC_TypeDef;
#define JL_SRC_BASE                     (hs_base + map_adr(0x0d, 0x00))
#define JL_SRC                          ((JL_SRC_TypeDef			*)JL_SRC_BASE)

//............. 0x0e00 - 0x0eff............ for src1(src_v2)
//............. 0x0f00 - 0x0fff............
//............. 0x1000 - 0x10ff............
//............. 0x1100 - 0x11ff............
//............. 0x1200 - 0x12ff............
//............. 0x1300 - 0x13ff............ for
//............. 0x1400 - 0x14ff............
//............. 0x1500 - 0x15ff............ for aes
//............. 0x1600 - 0x16ff............ for txha
//............. 0x1700 - 0x17ff............ for gpcrc
typedef struct {
    __RW __u32 CON;
    __RW __u32 POL;
    __WO __u32 INIT;
    __RW __u32 REG;
} JL_GPCRC_TypeDef;

#define JL_GPCRC_BASE            (hs_base + map_adr(0x17, 0x00))
#define JL_GPCRC                 ((JL_GPCRC_TypeDef    *)JL_GPCRC_BASE)

//............. 0x1800 - 0x18ff............ for vdo_mbist
//............. 0x1900 - 0x19ff............ for vdo_com
//............. 0x2000 - 0x20ff............ for gpu2d
//............. 0x2300 - 0x23ff............ for mtap
typedef struct {
    __RW __u32 MBISTPG_CTL ;
    __RO __u32 MBISTPG_SO  ;
    __RO __u32 MBISTPG_GO  ;
    __RO __u32 MBISTPG_DONE;
    __RW __u32 MBISTPG_SEL ;
} JL_MTAP_TypeDef;

#define JL_MTAP_BASE                    (hs_base + map_adr(0x23, 0x00))
#define JL_MTAP                         ((JL_MTAP_TypeDef       *)JL_MTAP_BASE)

//............. 0x2400 - 0x24ff............ for jibc
typedef struct {
    __RW __u8  ID;
    __RW __u8  STATUS;
    __RW __u8  PASS;
    __RW __u8  TERMINAL;
    __RW __u32 EN ;
    __RW __u32 RADR ;
    __RO __u32 RDAT ;
    __RW __u32 WADR ;
    __RW __u32 WDAT ;
    __RW __u32 INIT ;
} JL_JIBC_TypeDef;

#define JL_JIBC_BASE                    (hs_base + map_adr(0x24, 0x00))
#define JL_JIBC                         ((JL_JIBC_TypeDef       *)JL_JIBC_BASE)
//===============================================================================/
//      low speed sfr address define
//
//===============================================================================//

//............. 0x0000 - 0x00ff............
typedef struct {
    __RW __u32 WREN;
    __RW __u32 CON0;
    __RW __u32 CON1;
    __RW __u32 CON2;
    __RW __u32 CON3;
    __RW __u32 MSG0;
    __RW __u32 MSG1;
    __RW __u32 MSG2;
    __RW __u32 MSG3;
    __RO __u32 ID;
} JL_LEMU_TypeDef;

#define JL_LEMU_BASE                    (ls_base + map_adr(0x00, 0x00))
#define JL_LEMU                         ((JL_LEMU_TypeDef    *)JL_LEMU_BASE)

//............. 0x0100 - 0x01ff............ for pmu_ctl
typedef struct {
    __RW __u32 PMU_CON;
    __RW __u32 PMU_STA;
    __RW __u32 KST_P11;
} JL_PMU_TypeDef;

#define JL_PMU_BASE                     (ls_base + map_adr(0x01, 0x00))
#define JL_PMU                          ((JL_PMU_TypeDef        *)JL_PMU_BASE)

////............. 0x0200 - 0x02ff............ for efuse
typedef struct {
    __RW __u32 CON  ;
    __RW __u32 RFC  ;
    __WO __u32 DLOCK;
    __RO __u32 RDAT ;
    __RO __u32 RF   ;
} JL_EFUSE_TypeDef;

#define JL_EFUSE_BASE          (ls_base + map_adr(0x02, 0x00))
#define JL_EFUSE               ((JL_EFUSE_TypeDef    *)JL_EFUSE_BASE)

//............. 0x0300 - 0x03ff............
//............. 0x0400 - 0x04ff............
typedef struct {
    __WO __u32 KEY;
    __RW __u32 CR;
    __RW __u32 CFG;
    __RO __u32 SR;
} JL_WWDG_TypeDef;

#define JL_WWDG_BASE      (ls_base + map_adr(0x04, 0x00))
#define JL_WWDG           ((JL_WWDG_TypeDef        *)JL_WWDG_BASE)


//............. 0x0500 - 0x05ff............
//............. 0x0600 - 0x06ff............
typedef struct {
    __RW __u32 CON;
    __RW __u32 CNT;
    __RW __u32 PRD;
    __RW __u32 PWM;
    __RW __u32 IRFLT;
} JL_TIMER_TypeDef;

#define JL_TIMER0_BASE      (ls_base + map_adr(0x06, 5*0))
#define JL_TIMER1_BASE      (ls_base + map_adr(0x06, 5*1))
#define JL_TIMER2_BASE      (ls_base + map_adr(0x06, 5*2))

#define JL_TIMER0           ((JL_TIMER_TypeDef     *)JL_TIMER0_BASE)
#define JL_TIMER1           ((JL_TIMER_TypeDef     *)JL_TIMER1_BASE)
#define JL_TIMER2           ((JL_TIMER_TypeDef     *)JL_TIMER2_BASE)

//............. 0x0700 - 0x07ff............
typedef struct {
    __RW __u8  CON;
    __RW __u8  DAT;
    __RW __u8  SMP;
    __RW __u8  DBE;
} JL_QDEC_TypeDef;

#define JL_QDEC0_BASE                   (ls_base + map_adr(0x07, 0x00))
#define JL_QDEC0                        ((JL_QDEC_TypeDef       *)JL_QDEC0_BASE)

//............. 0x0800 - 0x09ff............
typedef struct {
    __RW __u16 TX_CON0;
    __RW __u16 TX_CON1;
    __RW __u16 RX_CON0;
    __RW __u16 RX_CON1;
    __RW __u16 CON2;
    __RW __u16 BAUD;
    __RW __u8  BUF;
    __RW __u32 OTCNT;
    __RW __u32 TXADR;
    __WO __u16 TXCNT;
    __RW __u32 RXSADR;
    __RW __u32 RXEADR;
    __RW __u32 RXCNT;
    __RO __u16 HRXCNT;
    __RO __u16 RX_ERR_CNT;
} JL_UART_TypeDef;

#define JL_UART0_BASE                   (ls_base + map_adr(0x08, 0x00))
#define JL_UART0                        ((JL_UART_TypeDef       *)JL_UART0_BASE)
#define JL_UART1_BASE                   (ls_base + map_adr(0x09, 0x00))
#define JL_UART1                        ((JL_UART_TypeDef       *)JL_UART1_BASE)

//............. 0x0a00 - 0x0bff............
typedef struct {
    __RW __u32 CON;
    __RW __u32 BAUD;
    __RW __u32 BUF;
    __WO __u32 ADR;
    __RW __u32 CNT;
    __RW __u32 CON1;
} JL_SPI_TypeDef;

#define JL_SPI1_BASE                    (ls_base + map_adr(0x0a, 0x00))
#define JL_SPI1                         ((JL_SPI_TypeDef      *)JL_SPI1_BASE)
#define JL_SPI2_BASE                    (ls_base + map_adr(0x0b, 0x00))
#define JL_SPI2                         ((JL_SPI_TypeDef      *)JL_SPI2_BASE)

//............. 0x0c00 - 0x0cff............
typedef struct {
    __RW __u32 CON    ;
    __RW __u32 PND    ;
    __RW __u32 TX_BUF ;
    __RW __u32 TASK   ;
    __RO __u32 RX_BUF ;
    __RW __u32 ADDR   ;
    __RW __u32 BAUD   ;
    __RW __u32 TSU    ;
    __RW __u32 THD    ;
    __RO __u32 DBG    ;
    __RW __u32 TRIG_EN;
} JL_IIC_TypeDef;

#define JL_IIC0_BASE                     (ls_base + map_adr(0x0c, 0x00))
#define JL_IIC0                          ((JL_IIC_TypeDef       *)JL_IIC0_BASE)

//............. 0x0d00 - 0x0dff............
typedef struct {
    __RW __u32 CON0;
    __RW __u32 CON1;
    __RW __u32 CON2;
    __WO __u32 CPTR;
    __WO __u32 DPTR;
    __RW __u32 CTU_CON;
    __WO __u32 CTU_CNT;
    __RW __u32 BRK;
    __RW __u8  ENCCON ;
    __WO __u16 ENCKEY ;
    __WO __u16 ENCADR ;
} JL_SD_TypeDef;

#define JL_SD0_BASE                     (ls_base + map_adr(0x0d, 0x00))
#define JL_SD0                          ((JL_SD_TypeDef        *)JL_SD0_BASE)

//............. 0x0e00 - 0x0eff............
typedef struct {
    __RW __u32 CON;
    __RO __u32 RES;
} JL_ADC_TypeDef;

#define JL_ADC_BASE                     (ls_base + map_adr(0x0e, 0x00))
#define JL_ADC                          ((JL_ADC_TypeDef       *)JL_ADC_BASE)

//............. 0x0f00 - 0x0fff............
//............. 0x1000 - 0x10ff............
typedef struct {
    __RW __u32 CON;
    __RW __u32 VAL;
} JL_PCNT_TypeDef;

#define JL_PCNT_BASE                    (ls_base + map_adr(0x10, 0x00))
#define JL_PCNT                         ((JL_PCNT_TypeDef       *)JL_PCNT_BASE)

//............. 0x1100 - 0x11ff............
//............. 0x1200 - 0x12ff............
typedef struct {
    __RW __u32 CON;
    __RO __u32 NUM;
} JL_GPCNT_TypeDef;

#define JL_GPCNT0_BASE                   (ls_base + map_adr(0x11, 0x00))
#define JL_GPCNT0                        ((JL_GPCNT_TypeDef     *)JL_GPCNT0_BASE)
#define JL_GPCNT1_BASE                   (ls_base + map_adr(0x12, 0x00))
#define JL_GPCNT1                        ((JL_GPCNT_TypeDef     *)JL_GPCNT0_BASE)

//  typedef struct {
//      __WO __u32 CON;
//      __RW __u32 NUM;
//  } JL_LRCT_TypeDef;
//
//  #define JL_LRCT_BASE                    (ls_base + map_adr(0x12, 0x00))
//  #define JL_LRCT                         ((JL_LRCT_TypeDef     *)JL_LRCT_BASE)

//............. 0x1300 - 0x13ff............
//............. 0x1400 - 0x14ff............
//............. 0x1500 - 0x15ff............ for lsb peri(spi0/sd0)
//............. 0x1600 - 0x16ff............
typedef struct {
    __RO __u32 R64L;
    __RO __u32 R64H;
} JL_RAND_TypeDef;

#define JL_RAND_BASE                    (ls_base + map_adr(0x16, 0x00))
#define JL_RAND                         ((JL_RAND_TypeDef   *)JL_RAND_BASE)

//............. 0x1700 - 0x17ff............ for tcrm
typedef struct {
    __RW __u32 CON0;
} JL_TCRM_TypeDef;

#define JL_TCRM_BASE                    (ls_base + map_adr(0x17,0x00))
#define JL_TCRM                         ((JL_TCRM_TypeDef   *)JL_TCRM_BASE)

//............. 0x1800 - 0x18ff............ for tmr0 (adv_timer)
//............. 0x1900 - 0x19ff............ for tmr1 (adv_timer)
typedef struct {
    __RW __u32  CR1;
    __RW __u32  CR2;
    __RW __u32  SMCR;
    __RW __u32  DIER;
    __RW __u32  SR;
    __WO __u32  EGR;
    __RW __u32  CCMR1;
    __RW __u32  CCMR2;
    __RW __u32  CCMR3;
    __RW __u32  CCER;
    __RW __u32  CNT;
    __RW __u32  PSC;
    __RW __u32  ARR;
    __RW __u32  RCR;
    __RW __u32  CCR1;
    __RW __u32  CCR2;
    __RW __u32  CCR3;
    __RW __u32  CCR4;
    __RW __u32  CCR5;
    __RW __u32  CCR6;
    __RW __u32  BDTR;
    __RW __u32  DCR;
    __RW __u32  DMAR;
    __RW __u32  AF1;
    __RW __u32  AF2;
} JL_ADV_TIMER_TypeDef;

#define JL_ADV_TIMER0_BASE              (ls_base + map_adr(0x18,0x00))
#define JL_ADV_TIMER1_BASE              (ls_base + map_adr(0x19,0x00))

#define JL_ADV_TIMER0                   ((JL_ADV_TIMER_TypeDef  *)JL_ADV_TIMER0_BASE)
#define JL_ADV_TIMER1                   ((JL_ADV_TIMER_TypeDef  *)JL_ADV_TIMER1_BASE)

//............. 0x1a00 - 0x1aff............ for led
//............. 0x1b00 - 0x1bff............ for lcd
//............. 0x1c00 - 0x1dff............ for udma
typedef struct {
    __RW __u32 SADR;
    __RW __u32 DADR;
    __RW __u32 CON0;
    __RW __u32 CON1;
} JL_UDMA_CH;

typedef struct {
    __RW __u32 CON0;
    __RW __u32 SEL0;
    __RW __u32 SEL1;
    __RO __u32 RESERVED[1];
} JL_DMAGEN_CH;

typedef struct {
    JL_UDMA_CH   JL_UDMA_CH0;
    JL_DMAGEN_CH JL_DMAGEN_CH0;
    JL_UDMA_CH   JL_UDMA_CH1;
    JL_DMAGEN_CH JL_DMAGEN_CH1;
} JL_UDMA_TypeDef;

#define JL_UDMA_BASE                  (ls_base + map_adr(0x1c, 0x00))
#define JL_UDMA                       ((JL_UDMA_TypeDef *)JL_UDMA_BASE)
#define JL_DMAGEN                     ((JL_UDMA_TypeDef *)JL_UDMA_BASE)

//............. 0x1e00 - 0x1eff............
//............. 0x1f00 - 0x1fff............ for wl uart (only FPGA)
typedef struct {
    __RW __u16 CON0;
    __RW __u16 CON1;
    __RW __u16 CON2;
    __RW __u16 BAUD;
    __RW __u8  BUF;
} JL_WL_UART_LITE_TypeDef;

#define JL_WL_UART_BASE                 (ls_base + map_adr(0x1f, 0x00))
#define JL_WL_UART                      ((JL_WL_UART_LITE_TypeDef *)JL_WL_UART_BASE)

//............. 0x2000 - 0x24ff............ for sie
typedef struct {
    __RW __u32 FADDR;
    __RW __u32 POWER;
    __RO __u32 INTRTX1;
    __RO __u32 INTRTX2;
    __RO __u32 INTRRX1;
    __RO __u32 INTRRX2;
    __RO __u32 INTRUSB;
    __RW __u32 INTRTX1E;
    __RW __u32 INTRTX2E;
    __RW __u32 INTRRX1E;
    __RW __u32 INTRRX2E;
    __RW __u32 INTRUSBE;
    __RO __u32 FRAME1;
    __RO __u32 FRAME2;
    __RO __u32 RESERVED14;
    __RW __u32 DEVCTL;
    __RO __u32 RESERVED10_0x16[0x16 - 0x10 + 1];
} JL_USB_SIE_TypeDef;

#define JL_USB_SIE_BASE                 (ls_base + map_adr(0x20, 0x00))
#define JL_USB_SIE                      ((JL_USB_SIE_TypeDef *)JL_USB_SIE_BASE)

typedef struct {
    __RO __u32 RESERVED0;
    __RW __u32 CSR0;
    __RO __u32 RESERVED2_5[5 - 1];
    __RO __u32 COUNT0;

} JL_USB_EP0_TypeDef;

#define JL_USB_EP0_BASE                 (ls_base + map_adr(0x20, 0x10))
#define JL_USB_EP0                      ((JL_USB_EP0_TypeDef *)JL_USB_EP0_BASE)

typedef struct {
    __RW __u32 TXMAXP;
    __RW __u32 TXCSR1;
    __RW __u32 TXCSR2;
    __RW __u32 RXMAXP;
    __RW __u32 RXCSR1;
    __RW __u32 RXCSR2;
    __RO __u32 RXCOUNT1;
    __RO __u32 RXCOUNT2;
    __RW __u32 TXTYPE;
    __RO __u32 TXINTERVAL;
    __RW __u32 RXTYPE;
    __RO __u32 RXINTERVAL;

} JL_USB_EP_TypeDef;

#define JL_USB_EP1_BASE                 (ls_base + map_adr(0x21, 0x10))
#define JL_USB_EP1                      ((JL_USB_EP_TypeDef *)JL_USB_EP1_BASE)

#define JL_USB_EP2_BASE                 (ls_base + map_adr(0x22, 0x10))
#define JL_USB_EP2                      ((JL_USB_EP_TypeDef *)JL_USB_EP2_BASE)

#define JL_USB_EP3_BASE                 (ls_base + map_adr(0x23, 0x10))
#define JL_USB_EP3                      ((JL_USB_EP_TypeDef *)JL_USB_EP3_BASE)

// #define JL_USB_EP4_BASE                 (ls_base + map_adr(0x24, 0x10))
// #define JL_USB_EP4                      ((JL_USB_EP_TypeDef *)JL_USB_EP4_BASE)

//............. 0x2500 - 0x25ff............
typedef struct {
    __RW __u32 CON0;
    __RW __u32 CON1;
    __WO __u32 EP0_CNT;
    __WO __u32 EP1_CNT;
    __WO __u32 EP2_CNT;
    __WO __u32 EP3_CNT;
    __WO __u32 EP4_CNT;
    __WO __u32 EP0_ADR;
    __WO __u32 EP1_TADR;
    __WO __u32 EP1_RADR;
    __WO __u32 EP2_TADR;
    __WO __u32 EP2_RADR;
    __WO __u32 EP3_TADR;
    __WO __u32 EP3_RADR;
    __WO __u32 EP4_TADR;
    __WO __u32 EP4_RADR;
    __RW __u32 TXDLY_CON;
    __RW __u32 EP1_RLEN;
    __RW __u32 EP2_RLEN;
    __RW __u32 EP3_RLEN;
    __RW __u32 EP4_RLEN;
    __RW __u32 EP1_MTX_PRD;
    __RW __u32 EP1_MRX_PRD;
    __RO __u32 EP1_MTX_NUM;
    __RO __u32 EP1_MRX_NUM;
    __RW __u32 SOF_STA_CON;
} JL_USB_TypeDef;

#define JL_USB_BASE                     (ls_base + map_adr(0x25, 0x00))
#define JL_USB                          ((JL_USB_TypeDef       *)JL_USB_BASE)


//............. 0x3000 - 0x34ff............ for port
typedef struct {
    __RO __u32 IN;
    __RW __u32 OUT;
    __RW __u32 DIR;
    __RW __u32 DIE;
    __RW __u32 DIEH;
    __RW __u32 PU;
    __RW __u32 PD;
    __RW __u32 HD;
    __RW __u32 SPL;
    __RW __u32 CON; // usb phy only
    __RW __u32 OUT_BSR;
    __RW __u32 DIR_BSR;
    __RW __u32 DIE_BSR;
    __RW __u32 DIEH_BSR;
    __RW __u32 PUL_BSR;
    __RW __u32 PUH_BSR;
    __RW __u32 PDL_BSR;
    __RW __u32 PDH_BSR;
    __RW __u32 HDL_BSR;
    __RW __u32 HDH_BSR;
    __RW __u32 SPL_BSR;
    __RW __u32 CON_BSR;   // usb phy only
} JL_PORT_TypeDef;

#define JL_PORTA_BASE                   (ls_base + map_adr(0x30, 0x00))
#define JL_PORTA                        ((JL_PORT_TypeDef *)JL_PORTA_BASE)

#define JL_PORTB_BASE                   (ls_base + map_adr(0x31, 0x00))
#define JL_PORTB                        ((JL_PORT_TypeDef *)JL_PORTB_BASE)

#define JL_PORTC_BASE                   (ls_base + map_adr(0x32, 0x00))
#define JL_PORTC                        ((JL_PORT_TypeDef *)JL_PORTC_BASE)

#define JL_PORTF_BASE                   (ls_base + map_adr(0x33, 0x00))
#define JL_PORTF                        ((JL_PORT_TypeDef *)JL_PORTF_BASE)

#define JL_PORTP_BASE                   (ls_base + map_adr(0x34, 0x00))
#define JL_PORTP                        ((JL_PORT_TypeDef *)JL_PORTP_BASE)

#define JL_PORTUSB_BASE                 (ls_base + map_adr(0x35, 0x00))
#define JL_PORTUSB                      ((JL_PORT_TypeDef *)JL_PORTUSB_BASE)
//............. 0x3d00 - 0x3dff............ for port others
typedef struct {
    __RW __u32 EN;
    __RW __u32 EDGE;
    __WO __u32 CLRPND;
    __RO __u32 PND;
    __RW __u32 EN2;
    __RW __u32 EDGE2;
    __WO __u32 CLRPND2;
    __RO __u32 PND2;
} JL_WAKEUP_TypeDef;

#define JL_WAKEUP_BASE                  (ls_base + map_adr(0x3e, 0x00))
#define JL_WAKEUP                       ((JL_WAKEUP_TypeDef   *)JL_WAKEUP_BASE)

//    __RW __u32 FSPG_CON;
typedef struct {
    __RW __u32 FSPG_CON;
    __RW __u32 SPI_IOMC0  ;
    __RW __u32 SDC_IOMC0  ;
    __RW __u32 SYS_IOMC0  ;
    __RW __u32 SYS_IOMC1  ;
    __RW __u32 ASS_IOMC0  ;
    __RW __u32 WL_IOMC0   ;
    __RW __u32 VDO_IOMC0  ;
    __RW __u32 ICH_IOMC0  ;
    __RW __u32 ICH_IOMC1  ;
    __RW __u32 ICH_IOMC2  ;
    __RW __u32 ICH_IOMC3  ;
    __RW __u32 ICH_IOMC4  ;
    __RW __u32 ICH_IOMC5  ;
    __RW __u32 ICH_IOMC6  ;
    __RW __u32 OCH_IOMC0  ;
    __RW __u32 OCH_IOMC1  ;
    __RW __u32 OCH_IOMC2  ;
    __RW __u32 OCH_IOMC3  ;
    __RW __u32 OCH_IOMC4  ;
    __RW __u32 OCH_IOMC5  ;
    __RW __u32 LCDPG_CON  ;
    __RW __u32 MTPG_CON   ;
    __RW __u32 STPG_CON   ;
    __RW __u32 WAT_CON    ;
} JL_IOMC_TypeDef;

#define JL_IOMC_BASE                    (ls_base + map_adr(0x3e, 0x08))
#define JL_IOMC                         ((JL_IOMC_TypeDef     *)JL_IOMC_BASE)


//............. 0x4000 - 0x40ff............ for syscfg
typedef struct {
    __RW __u32 RST_SRC;
} JL_LSBRST_TypeDef;

#define JL_LSBRST_BASE                  (ls_base + map_adr(0x40, 0x00))
#define JL_LSBRST                       ((JL_LSBRST_TypeDef        *)JL_LSBRST_BASE)

typedef struct {
    __RW __u32 PWR_CON;
    __RW __u32 SYS_CON0;
    __RW __u32 LSB_SEL;
    __RW __u32 STD_CON0;
    __RW __u32 STD_CON1;
    __RW __u32 STD_CON2;
    __RW __u32 PRP_CON0;
    __RW __u32 PRP_CON1;
    __RW __u32 PRP_CON2;
    __RW __u32 PCLK_SEL;
} JL_LSBCLK_TypeDef;

#define JL_LSBCLK_BASE                   (ls_base + map_adr(0x40, 0x01))
#define JL_LSBCLK                        ((JL_LSBCLK_TypeDef      *)JL_LSBCLK_BASE)

typedef struct {
    __RW __u32 DPLL_CON     ;
    __RW __u32 DPLL_PREDIV0 ;
    __RW __u32 DPLL_POSTDIV0;
    __RW __u32 DPLL_PREDIV1 ;
    __RW __u32 DPLL_POSTDIV1;
} JL_DPLL_TypeDef;

#define JL_DPLL_BASE                    (ls_base + map_adr(0x40, 0x0b))
#define JL_DPLL                         ((JL_DPLL_TypeDef      *)JL_DPLL_BASE)

typedef struct {
    __RW __u32 XOSC_CON0    ;
    __RW __u32 XOSC_CON1    ;
    __RW __u32 XOSC_CON2    ;
    __RW __u32 XOSC_CON3    ;
    __RW __u32 XOSC_CON4    ;
} JL_XOSC_TypeDef;

#define JL_XOSC_BASE                    (ls_base + map_adr(0x40, 0x10))
#define JL_XOSC                         ((JL_XOSC_TypeDef      *)JL_XOSC_BASE)


//............. 0x4100 - 0x41ff............ for pll0_ctl
typedef struct {
    __RW __u32 CON0;
    __RW __u32 CON1;
    __RW __u32 CON2;
    __RW __u32 CON3;
    __RW __u32 NR;
    __RW __u32 CON8;
    __RW __u32 CON9;
    __RW __u32 CON10;
    __RW __u32 CON11;
    __RW __u32 INTF0;
    __RW __u32 INTF1;
    __RW __u32 TRIM_CON0;
    __RW __u32 TRIM_CON1;
    __RW __u32 TRIM_PND;
    __RW __u32 FRQ_CNT;
    __RW __u32 FRC_SCA;
} JL_SYSPLL_TypeDef;

#define JL_SYSPLL_BASE                   (ls_base + map_adr(0x41, 0x00))
#define JL_SYSPLL                        ((JL_SYSPLL_TypeDef      *)JL_SYSPLL_BASE)


//............. 0x4200 - 0x42ff............ for pll_ctl
//............. 0x4300 - 0x43ff...........
//............. 0x4400 - 0x44ff............
//............. 0x4500 - 0x45ff............
//............. 0x4600 - 0x46ff............ for lp ctm0
typedef struct {
    __RW __u32 CON0;
    __RW __u32 CHEN;
    __RW __u32 CNUM;
    __RW __u32 PPRD;
    __RW __u32 DPRD;
    __RW __u32 ECON;
    __RW __u32 EXEN;
    __RW __u32 CHIS;
    __RW __u32 CLKC;
    __WO __u32 WCON;
    __RW __u32 ANA0;
    __RW __u32 ANA1;
    __RO __u32 RES;
    __RW __u32 DMA_START_ADR;
    __RW __u32 DMA_HALF_ADR;
    __RW __u32 DMA_END_ADR;
    __RW __u32 DMA_CON;
    __RW __u32 MSG_CON;
    __RO __u32 DMA_WADR;
    __RW __u32 SLEEP_CON;
} JL_LPCTM_TypeDef;

#define JL_LPCTM0_BASE                  (ls_base + map_adr(0x46, 0x00))
#define JL_LPCTM0                       ((JL_LPCTM_TypeDef    *)JL_LPCTM0_BASE)

//............. 0xxx00 - 0xxxff............ TODO
//typedef struct {
//    __WO __u32 CHIP_ID;
//} JL_SYSTEM_TypeDef;
//
//#define JL_SYSTEM_BASE                  (ls_base + map_adr(0xxx, 0x00))
//#define JL_SYSTEM                       ((JL_SYSTEM_TypeDef   *)JL_SYSTEM_BASE)

#include "io_omap.h"
#include "io_imap.h"

//===============================================================================//
//
//  ass  sfr address define
//
//===============================================================================//

//............. 0x0000 - 0x00ff............ ass_memcfg
typedef struct {
    __RW __u32 MCFG_RF1P;
    __RW __u32 MCFG_RF2P;
    __RW __u32 MCFG_RM1P;
    __RW __u32 MCFG_RM2P;
    __RW __u32 MCFG_VROM;
    __RW __u32 MPWR_VROM;
} JL_ASS_MEMCFG_TypeDef;

#define JL_ASS_MEMCFG_BASE               (as_base + map_adr(0x00, 0x00))
#define JL_ASS_MEMCFG                    ((JL_ASS_MEMCFG_TypeDef  *)JL_ASS_MEMCFG_BASE)


//............. 0x0100 - 0x01ff............ as_aud

typedef struct {
    __RW __u32 AUD_CON0;
    __RW __u32 AUD_CON1;
    __RW __u32 AUD_CON2;
    __RW __u32 AUD_CON3;
    __RW __u32 AUD_CON4;
    __RW __u32 AUD_CON5;
    __WO __u32 AUD_COP;
} JL_AUD_TypeDef;
#define JL_AUD_BASE                (as_base + map_adr(0x01, 0x00))
#define JL_AUD                     ((JL_AUD_TypeDef  *)JL_AUD_BASE)


//............. 0x0200 - 0x02ff............ as_dac
//#define JL_AUDDAC_BASE               (as_base + map_adr(0x02, 0x00))
//#define JL_AUDDAC                    ((JL_AUDDAC_TypeDef   *)JL_AUDDAC_BASE)

//............. 0x0300 - 0x03ff............ as_adc
typedef struct {
    __RW __u32(ADC_CON0);
    __RW __u32(ADC_CON1);
    __RW __u32(ADC_CON2);
    __RW __u32(ADC_CON3);
    __RW __u32(ADC_CON4);
    __RW __u32(ADC_CON5);
    __RW __u32(ADC_CON6);
    __WO __u32(ADC_DCW);
    __RO __u32(ADC_DCR);

    __RW __u32(ADC_ADR);
    __RW __u16(ADC_LEN);
    __RW __u16(ADC_PNS);
    __RW __u16(ADC_HWP);
    __RW __u16(ADC_SRP);
    __RW __u16(ADC_SRN);
} JL_AUDADC_TypeDef;
#define JL_AUDADC_BASE               (as_base + map_adr(0x03, 0x00))
#define JL_AUDADC                    ((JL_AUDADC_TypeDef   *)JL_AUDADC_BASE)

//............. 0x0400 - 0x04ff............ as_ad2da

//#define JL_AD2DA_BASE               (as_base + map_adr(0x04, 0x00))
//#define JL_AD2DA                    ((JL_AD2DA_TypeDef   *)JL_AD2DA_BASE)

//............. 0x0500 - 0x05ff............ as_anc
//#define JL_ANC_BASE                  (as_base + map_adr(0x05, 0x00))
//#define JL_ANC                       ((JL_ANC_TypeDef       *)JL_ANC_BASE)
//
//#define JL_ANC_CORE_BASE             (as_base + map_adr(0x09, 0x00))
//#define JL_ANC_CORE                  ((JL_ANC_CORE_TypeDef   *)JL_ANC_CORE_BASE)


//............. 0x0600 - 0x06ff............ as_alnk
typedef struct {
    __RW __u16 CON0;
    __RW __u16 CON1;
    __RW __u8  CON2;
    __RW __u16 CON3;
    __RW __u32 ADR0;
    __RW __u32 ADR1;
    __RW __u32 ADR2;
    __RW __u32 ADR3;
    __RW __u16 LEN;
} JL_ALNK_TypeDef;
#define JL_ALNK0_BASE                (as_base + map_adr(0x06, 0x00))
#define JL_ALNK0                     ((JL_ALNK_TypeDef    *)JL_ALNK0_BASE)


//............. 0x0700 - 0x07ff............


//............. 0x0800 - 0x08ff............ as_plnk
//#define JL_PLNK_BASE                 (as_base + map_adr(0x08, 0x00))
//#define JL_PLNK                      ((JL_PLNK_TypeDef     *)JL_PLNK_BASE)


//............. 0x1000 - 0x10ff............ as_ana
typedef struct {
    __RW __u32 DAA_CON0;
    __RW __u32 DAA_CON1;
    __RW __u32 DAA_CON2;
    __RO __u32 DAA_CON7;
    __RW __u32 ADA_CON0;
    __RW __u32 ADA_CON1;
    __RW __u32 ADA_CON2;
    __RW __u32 ADA_CON3;
    __RW __u32 ADA_CON4;
    __RW __u32 CLD_CON0;
    __RW __u32 ADDA_CON0;
    __RW __u32 ADDA_CON1;
} JL_ADDA_TypeDef;

#define JL_ADDA_BASE               (as_base + map_adr(0x10, 0x00))
#define JL_ADDA                    ((JL_ADDA_TypeDef       *)JL_ADDA_BASE)


//............. 0x1100 - 0x11ff............ as_dpa
typedef struct {
    __RW __u32(DPA_VL0);      /* 00 */
    __WO __u32(DPA_DCW);      /* 01 */
    __RO __u32(DPA_DCR);      /* 02 */
    __RW __u32(DPA_ADR);      /* 03 */
    __RW __u16(DPA_LEN);      /* 04 */
    __RW __u16(DPA_PNS);      /* 05 */
    __RW __u16(DPA_HRP);      /* 06 */
    __RW __u16(DPA_SWP);      /* 07 */
    __RW __u16(DPA_SWN);      /* 08 */
    __RW __u32(DPA_CON0);     /* 09 */
    __RW __u32(DPA_CON1);     /* 0A */
    __RW __u32(DPA_CON2);     /* 0B */
    __RW __u32(DPA_CON3);     /* 0C */
    __RW __u32(DPA_CON4);     /* 0D */
    __RW __u32(DPA_CON5);     /* 0E */
    __RO __u32(DPA_CON6);     /* 0F */
    __RW __u32(DPA_CON7);     /* 10 */
    __RW __u32(DPA_CON8);     /* 11 */
    __RW __u32(DPA_CON9);     /* 12 */
    __RW __u32(DPA_CON10);    /* 13 */
    __RW __u32(DPA_CON11);    /* 14 */
    __RW __u32(DAC_DIT);      /* 15 */
    __RW __u32(DAC_TM);       /* 16 */
} JL_DPA_TypeDef;

#define JL_DPA_BASE                 (as_base + map_adr(0x11, 0x00))
#define JL_DPA                      ((JL_DPA_TypeDef   *)JL_DPA_BASE)


//............. 0x1200 - 0x12ff............ as_hadc


//............. 0x2000 - 0x20ff............ as_sbc
//  typedef struct {
//      __RW __u32 CON0;
//      __WO __u32 DEC_SRC_ADR;
//      __WO __u32 DEC_DST_ADR;
//      __WO __u32 DEC_PCM_WCNT;
//      __WO __u32 DEC_INBUF_LEN;
//      __WO __u32 ENC_SRC_ADR;
//      __WO __u32 ENC_DST_ADR;
//      __RO __u32 DEC_DST_BASE;
//      __WO __u32 DEC_WEIGHT_BASE;
//      __WO __u32 DEC_WEIGHT_ADD;
//
//  } JL_SBC_TypeDef;
//
//  #define JL_SBC_BASE                   (as_base + map_adr(0x20, 0x00))
//  #define JL_SBC                        ((JL_SBC_TypeDef *)JL_SBC_BASE)


//............. 0x2100 - 0x21ff............ as_sbcram
//  typedef struct {
//      __RW __u32 DATA[384];
//  } JL_SBCRAM_TypeDef;
//
//  #define JL_SBCRAM_BASE                (as_base + map_adr(0x21, 0x00))
//  #define JL_SBCRAM                    ((JL_SBCRAM_TypeDef *)JL_SBCRAM_BASE)


//............. 0x3000 - 0x30ff............
//
//............. 0x3100 - 0x31ff............ as_emu
typedef struct {
    __RW __u32 WREN;
    __RW __u32 CON0;
    __RW __u32 CON1;
    __RW __u32 CON2;
    __RW __u32 CON3;
    __RW __u32 MSG0;
    __RW __u32 MSG1;
    __RW __u32 MSG2;
    __RW __u32 MSG3;
    __RO __u32 ID;
} JL_AEMU_TypeDef;

#define JL_AEMU_BASE                    (as_base + map_adr(0x31, 0x00))
#define JL_AEMU                         ((JL_AEMU_TypeDef    *)JL_AEMU_BASE)


//===============================================================================//
//
//  wl_top
//
//===============================================================================//

#endif


