/***********************************Jieli tech************************************************
  File : dac_cpu.c
  By   : mawancheng
  Email: mawancheng@zh-jieli.com
  date : 2025-5-22
********************************************************************************************/
/* #include "rdac.h" */
#include "dac_api.h"
#include "dac.h"
#include "config.h"
#include "audio.h"
#include "audio_analog.h"
#include "audio_dac_cpu.h"
#include "audio_apa_cpu.h"
#include "audio_dpa_cpu.h"
/* #include "asm/power_interface.h" */

#define LOG_TAG_CONST       AUDIO
/* #define LOG_TAG_CONST       OFF */
#define LOG_TAG             "[dac cpu]"
#include "log.h"


static u16 audio_dpa_buf[DAC_PACKET_SIZE * 2];

#define DAC_MAX_SP  (sizeof(audio_dpa_buf) / 2)
#define DAC_PNS     (DAC_MAX_SP / 3)

DPA_CTRL_HDL audio_dpa_ops = {
    .buf         = audio_dpa_buf,
    .sp_total    = DAC_MAX_SP,
    .sp_max_free = (DAC_MAX_SP - DAC_PNS) / 2,
    .con0        = DPA_CON0_DEFAULT,
    .pns         = DAC_PNS,
};

void audio_dac_para_init(void)
{
    memset(&audio_dpa_buf[0], 0, sizeof(audio_dpa_buf));
    fdpa_resource_init((void *)&audio_dpa_ops);
}


void audio_dac_init(u32 sr, bool delay_flag)
{
    HWI_Install(IRQ_DPA_IDX, (u32)dpa_isr, IRQ_AUDAC_IP) ;

    /* dpa_digital */
    dpa_clk_open();
    audio_dpa_common_digital_init(dpa_sr_lookup(sr));

    if (au_const_dac_en) {
        audio_dac_analog_init((AUDAC_ANA_PARA *)&g_audac_para);
        audio_dac_digital_init();
    }

    if (au_const_apa_en) {
        apa_init(sr);
    }

    JL_AUD->AUD_CON0 |= BIT(15);  // dpa_cken
    dump_audio_sfr();
}

void audio_dac_sr_api(u32 sr)
{
    dpa_sr_set(dpa_sr_lookup(sr));
}

u32 dac_sr_read(void)
{
    return dpa_sr_read();
}

void audio_dac_off_api(void)
{
    audio_dac_digital_close();
    audio_dac_analog_close((AUDAC_ANA_PARA *)&g_audac_para);
    apa_close();

}


void dump_audio_sfr(void)
{
    log_debug("\n");
    //clock
    log_debug("JL_LSBCLK->PRP_CON1 = %8xH", JL_LSBCLK->PRP_CON1);
    log_debug("JL_AUD->AUD_CON0    = %8xH", JL_AUD->AUD_CON0);
    log_debug("JL_ADDA->ADDA_CON0  = %8xH", JL_ADDA->ADDA_CON0);
    log_debug("JL_ADDA->ADDA_CON1  = %8xH", JL_ADDA->ADDA_CON1);
    log_debug("\n");

    //DPA_common_digtial
    log_debug("JL_DPA->DPA_CON0    = %8xH", JL_DPA->DPA_CON0);
    log_debug("JL_DPA->DPA_CON1    = %8xH", JL_DPA->DPA_CON1);
    log_debug("JL_DPA->DPA_CON8    = %8xH", JL_DPA->DPA_CON8);
    log_debug("JL_DPA->DPA_ADR     = %8xH", JL_DPA->DPA_ADR);
    log_debug("JL_DPA->DPA_LEN     = %8xH", JL_DPA->DPA_LEN);
    log_debug("JL_DPA->DPA_PNS     = %8xH", JL_DPA->DPA_PNS);
    log_debug("JL_DPA->DPA_HRP     = %8xH", JL_DPA->DPA_HRP);
    log_debug("JL_DPA->DPA_VL0     = %8xH", JL_DPA->DPA_VL0);
    log_debug("\n");

    //DAC
    log_debug("JL_ADDA->DAA_CON0     = %8xH", JL_ADDA->DAA_CON0);
    log_debug("JL_ADDA->DAA_CON1     = %8xH", JL_ADDA->DAA_CON1);
    log_debug("JL_AUD->AUD_CON4      = %8xH", JL_AUD->AUD_CON4);
    log_debug("JL_AUD->AUD_CON5      = %8xH", JL_AUD->AUD_CON5);
    log_debug("JL_DPA->DAC_DIT       = %8xH", JL_DPA->DAC_DIT);
    log_debug("JL_DPA->DAC_TM        = %8xH", JL_DPA->DAC_TM);
    log_debug("JL_DPA->DPA_CON8      = %8xH", JL_DPA->DPA_CON8);
    log_debug("JL_DPA->DPA_CON9      = %8xH", JL_DPA->DPA_CON9);
    log_debug("JL_DPA->DPA_CON10     = %8xH", JL_DPA->DPA_CON10);
    log_debug("JL_DPA->DPA_CON11     = %8xH", JL_DPA->DPA_CON11);
    log_debug("\n");

    //APA
    log_debug("JL_DPA->DPA_CON0    = %8xH", JL_DPA->DPA_CON0);
    log_debug("JL_DPA->DPA_CON1    = %8xH", JL_DPA->DPA_CON1);
    log_debug("JL_DPA->DPA_CON2    = %8xH", JL_DPA->DPA_CON2);
    log_debug("JL_DPA->DPA_CON3    = %8xH", JL_DPA->DPA_CON3);
    log_debug("JL_DPA->DPA_CON4    = %8xH", JL_DPA->DPA_CON4);
    log_debug("JL_DPA->DPA_CON5    = %8xH", JL_DPA->DPA_CON5);
    log_debug("JL_DPA->DPA_CON7    = %8xH", JL_DPA->DPA_CON7);
    log_debug("JL_ADDA->CLD_CON0   = %8xH", JL_ADDA->CLD_CON0);
}




