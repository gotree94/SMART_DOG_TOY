#include "pcm_eq_api.h"
#include "fix_iir_filter_api.h"
#include "pcm_eq_float/pcm_eq_float.h"
#include "config.h"
#include "sound_effect_api.h"
#include "remain_output.h"
#include "math_fast_function.h"
#include "app_modules.h"

#define LOG_TAG_CONST       NORM
#define LOG_TAG             "[eq]"
#include "log.h"



static void eq_get_AllpassCoeff(void *Coeff)
{
    const float EQ_AllpassCoeff[5] = { //直通参数
        1, 0, 0, 0, 0
    };
    memcpy(Coeff, EQ_AllpassCoeff, sizeof(EQ_AllpassCoeff));
}

static int eq_seg_design(struct eq_seg_info *seg, int sample_rate, float *coeff)
{
    /* log_info("seg[index] %d, seg[iir] %d, seg[freq] %d\n", seg->index, seg->iir_type , seg->freq); */
    if ((seg->freq >= (((u32)sample_rate / 2 * 29491) >> 15)) || (!seg->q || !seg->freq)) {
        if (seg->freq >= 65000) {
            log_error(" cur eq freq:%dHz not support sample_rate %dHz , so cur eq section set allpass \n", seg->freq, sample_rate);
        }
        /* log_error("param err %d\n", seg->index); */
        //uc03为了保持计算出来的系数能保持滤波器稳定，加上前面两处判断
        //参数错误就默认给直通的滤波系数
        eq_get_AllpassCoeff(coeff);
        return false;
    }
    switch (seg->iir_type) {
    case EQ_IIR_TYPE_HIGH_PASS:
        design_hp(seg->freq, sample_rate, seg->q, coeff);
        break;
    case EQ_IIR_TYPE_LOW_PASS:
        design_lp(seg->freq, sample_rate, seg->q, coeff);
        break;
    case EQ_IIR_TYPE_BAND_PASS:
        design_pe(seg->freq, sample_rate, seg->gain, seg->q, coeff);
        break;
    case EQ_IIR_TYPE_HIGH_SHELF:
        design_hs(seg->freq, sample_rate, seg->gain, seg->q, coeff);
        break;
    case EQ_IIR_TYPE_LOW_SHELF:
        design_ls(seg->freq, sample_rate, seg->gain, seg->q, coeff);
        break;
    case EQ_IIR_TYPE_BAND_PASS_NEW:
        design_bp(seg->freq, sample_rate, seg->q, coeff);
        break;
    }

    /* log_info("%d %d %d %d %d", coeff[0], coeff[1], coeff[2], coeff[3], coeff[4]); */
    int status = eq_stable_check(coeff);
    if (status) {
        log_error("eq_stable_check err:%d ", status);
        /* log_info("%d %d %d %d %d", coeff[0], coeff[1], coeff[2], coeff[3], coeff[4]); */
        //参数错误就默认给直通的滤波系数
        eq_get_AllpassCoeff(coeff);
        return false;
    }
    return true;

}

bool calculate_eq_param(EQ_COEFF_BUFF *ptr, u32 seg_num)
{
    struct PCM_EQ_F_PARAM *p_param = ptr->eq_param;
    struct eq_seg_info *p_param_tab = (struct eq_seg_info *)p_param->seg;
    float *coeff_buf = ptr->p_eq_coeff_tab;
    /* float *gain_tptr = coeff_buf; */
    /* log_info("coeff_buf 0x%x\n", coeff_buf); */
    /* log_info_hexdump((u8 *)gain_tptr, 16); */
    /* log_info("%d %d %d %d %d", coeff_buf[0], coeff_buf[1], coeff_buf[2], coeff_buf[3], coeff_buf[4]); */
    if (NULL != p_param_tab) {
        for (int i = 0; i < seg_num; i++) {
            /* log_info("i %d\n", i); */
            eq_seg_design(&p_param_tab[i], ptr->sample_rate, coeff_buf);
            coeff_buf += 5;
        }
        coeff_buf[0] = dB_Convert_Mag(p_param->global_gain) * coeff_buf[0];
        return true;
    }
    log_error("eq_calculate_err\n");
    return false;
    /* gain_tptr[0] = dB_Convert_Mag(p_param->global_gain) * gain_tptr[0]; */
    /* log_info("dB_trans\n"); */
    /* log_info_hexdump((u8 *)gain_tptr, 16); */
    /* gain_tptr = coeff_buf; */
    /* log_info("coeff_buff\n"); */
    /* log_info_hexdump((u8 *)gain_tptr, 16); */
}


