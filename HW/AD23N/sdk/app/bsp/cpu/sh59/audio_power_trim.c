#include "typedef.h"
#include "clock.h"
#include "vm_api.h"
#include "gpadc.h"
#include "audio_cpu.h"
#include "audio_trim_cpu.h"
#include "errno-base.h"

/* #define LOG_TAG_CONST       OFF */
#define LOG_TAG_CONST       AUDIO
#define LOG_TAG             "[audio_power_trim]"
#include "log.h"
/*****************AUDIO_trim 配置************************************************/
autrim_res g_adda_trim_res; //存放AUDIO的校准结果

/*****************AUDIO_trim 配置 END********************************************/

u32 audio_vbg_adjust(u8 sta_bit, u8 level, u32 *sfr_addr)
{
    u32 *adda_con_sfr = sfr_addr;
    /* log_info("adjust %d, %d\n", level, sta_bit); */
    SFR(*adda_con_sfr, sta_bit, 4, level);
    mdelay(5); //delay 5ms
    u32 vol = adc_get_voltage_blocking_filter(AD_CH_AUDIO_VCMBUF, 1);
    log_info("gear %d, vol %d\n", level, vol);
    /* log_info("JL_ADDA->ADDA_CON0 0x%x\n", JL_ADDA->ADDA_CON0); */
    /* log_info("JL_ADDA->ADDA_CON1 0x%x\n", JL_ADDA->ADDA_CON1); */
    return vol;
}

u32 audio_vbg_trim(u32 need_mv, autrim_res *p_autrim_res)
{
    autrim_para audio_trim_para; //校准前传入的参数
    memset((u8 *)&audio_trim_para, 0, sizeof(audio_trim_para));
    audio_trim_para.fun = audio_vbg_adjust;
    audio_trim_para.need_mv = need_mv;
    audio_trim_para.result_sel = 0;
    audio_trim_para.max_limit_mv = 5000;
    audio_trim_para.vcm05_en = au_vcm05_en;

    if (au_vcm_cap_en == 0) {
        //VCM不挂电容，trim AUDIO_VBG，对应寄存器JL_ADDA->ADDA_CON0
        audio_trim_para.max_gear = 15;
        audio_trim_para.trim_start_bit = 21;
        audio_trim_para.sfr_addr = (u32 *)&JL_ADDA->ADDA_CON0;
        log_info("trim AUDIO_VBG\n");
    } else {
        //VCM挂电容，trim PMUBG，对应寄存器JL_ADDA->ADDA_CON1
        audio_trim_para.max_gear = 15;
        audio_trim_para.trim_start_bit = 9;
        audio_trim_para.sfr_addr = (u32 *)&JL_ADDA->ADDA_CON1;
        log_info("trim PMUBG\n");
    }
    p_autrim_res->audio2vm_res.withcap_en = au_vcm_cap_en;
    p_autrim_res->audio2vm_res.vcm05_en = au_vcm05_en;
    u32 trim_ret = aud_vbg_trim(&audio_trim_para, p_autrim_res);
    log_info("res_mv %d, %d\n", p_autrim_res->res_mv, p_autrim_res->audio2vm_res.gear);

    return trim_ret;

}


void audio_voltage_trim()
{
    memset((u8 *)&g_adda_trim_res, 0, sizeof(g_adda_trim_res));
    u32 rlen = 0;
    rlen = vm_read(VM_INDEX_AUDIO_VBG_TRIM, (u8 *)&g_adda_trim_res.audio2vm_res, sizeof(autrim_res_vm));
    if (rlen == sizeof(autrim_res_vm)) {
        /* log_info("read_trim_value\n"); */
        if ((au_vcm_cap_en != g_adda_trim_res.audio2vm_res.withcap_en) || (au_vcm05_en != g_adda_trim_res.audio2vm_res.vcm05_en)) {
            log_info("vcm_cap_en %d->%d\n", g_adda_trim_res.audio2vm_res.withcap_en, au_vcm_cap_en);
            log_info("vcm05_en %d->%d\n", g_adda_trim_res.audio2vm_res.vcm05_en, au_vcm05_en);
            log_info("need_to_retrim Audio\n");
            goto __autrim_process;
        }
        return;
    }

__autrim_process: {
        u32 mbg = adc_get_voltage_blocking_filter(AD_CH_PMU_MBG08, 2);
        log_info("mbg %d\n", mbg);

        u32 need_mv;
        if (au_vcm05_en) {
            need_mv = (mbg * 5) / 8; //0.5V
        } else {
            need_mv = (mbg * 6) / 8; //0.6V
        }
        log_info("need_mv %d\n", need_mv);

        audio_adda_trim_analog_open(au_vcm_cap_en);

        u32 res = audio_vbg_trim(need_mv, &g_adda_trim_res);

        audio_adda_trim_analog_close();


        if (0 == res) {
            log_info("audio_trim_success\n");
            vm_write(VM_INDEX_AUDIO_VBG_TRIM, (u8 *)&g_adda_trim_res.audio2vm_res, sizeof(autrim_res_vm));
        }
    }
    return;
}



