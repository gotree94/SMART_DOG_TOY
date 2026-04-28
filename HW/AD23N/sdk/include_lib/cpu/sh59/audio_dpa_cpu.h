#ifndef _AUDIO_DPA_CPU_H_
#define _AUDIO_DPA_CPU_H_

#include "typedef.h"
#include "dac.h"

//dpa 输出采样率
#define AUDAC_SR96K       (0b0000)
#define AUDAC_SR88K2      (0b0001)
#define AUDAC_SR64K       (0b0010)
#define AUDAC_SR48K       (0b0100)
#define AUDAC_SR44K1      (0b0101)
#define AUDAC_SR32K       (0b0110)
#define AUDAC_SR24K       (0b1000)
#define AUDAC_SR22K05     (0b1001)
#define AUDAC_SR16K       (0b1010)
#define AUDAC_SR12K       (0b1100)
#define AUDAC_SR11K025    (0b1101)
#define AUDAC_SR8K        (0b1110)
#define AUDAC_SRBITS      (0b1111)

//DPA_CON0
#define DPA0_R_FADE_ACT      (1 << 28)
#define DPA0_FADE_EN         (1 << 27)  // 数字淡入淡出使能
#define DPA0_FADE_SLOW(n)    ((n & 0xf) << 23)   // 每(pints_step+1)个点调整一次音量
#define DPA0_STEP(n)         ((n & 0xf) << 19)   // 每次调节音量的步进为volume_step
#define DPA0_APA_VOL_INV     (1 << 18)
#define DPA0_APA_DCC(n)      ((n & 0xf) << 14)
#define DPA0_PND             (1 << 13)
#define DPA0_CPND            (1 << 12)
#define DPA0_IE              (1 << 11)
#define DPA0_DMA_EN          (1 << 10)
#define DPA0_PWM_MUTE        (1 << 5)
#define DPA0_PWM_EN          (1 << 2)
#define DPA0_DSM_EN          (1 << 1)


//DPA_CON1
#define DPA1_APA_FIFO_PND        (1 << 29)
#define DPA1_APA_FIFO_CPND       (1 << 28)
#define DPA1_APA_FIFO_IE         (1 << 27)
#define DPA1_APA_FIFO_RST        (1 << 26)
#define DPA1_APA_FIFO_SYNC_EN    (1 << 25)
#define DPA1_IIR_PAR(n)          ((n & 0x3) << 24)
#define DPA1_IIR_EN              (1 << 22)

#define DPA_CON0_DEFAULT  \
    ( \
      DPA0_APA_DCC(14)     |\
      DPA0_CPND |\
      DPA0_IE    \
    )


#define SR_DEFAULT        32000

void audio_dpa_common_digital_init(u32 sr_sfr);
void audio_dpa_common_digital_close(void);

u32 dpa_sr_lookup(u32 sr);
u32 dpa_sr_read(void);
u32 dpa_sr_set(u32 sr);

void set_dpa_digital_vol(u32 vol);

void dpa_isr(void);
void dpa_isr_phy(void);

typedef struct _DPA_COMMON_DIGITAL {
    u16 gain;
    u8 ch;
    // u8 dcc_level;               // DAC去直流滤波器档位, 0~7:关闭    8~15：开启(档位越大，高通截止点越小)
    // u8 fade_points_step;        // 每(pints_step+1)个点调整一次音量
    // u8 fade_volume_step;        // 每次调节音量的步进为volume_step
    // u8 fade_en;                 // 数字淡入淡出使能
    // u8 pwm_mute;
} DPA_COMMON_DIGITAL;

typedef struct _DPA_CTRL_HDL {
    void *buf;
    u32  con0;
    u32  pns;          //dac中断门槛
    u16  sp_total;
    u16  sp_max_free;  //填充数据后允许的最大Free空间，不够填零
    /* u8   sp_size; */
} DPA_CTRL_HDL;

void fdpa_resource_init(const DPA_CTRL_HDL *ops);

extern DPA_COMMON_DIGITAL g_dpa_para;

void dump_audio_sfr(void);
#endif
