#include "voiceChanger_av_api.h"
#include "sound_effect_api.h"
#include "app_modules.h"

#define LOG_TAG_CONST       NORM
#define LOG_TAG             "[voice_changer]"
#include "log.h"

#if VO_CHANGER_EN
#define VOICECHANGER_BUF_SIZE (0x2010)

typedef struct _VOICECHANGER_HDL {
    EFFECT_OBJ vchange_obj;
    sound_in_obj vchange_si;
    VCAD_IO_CONTEXT vc_pitch_io;
    VOICESYN_AV_PARM vs_parm;
    VOICECHANGER_AV_PARM vc_parm;
    u32 vc_sr;
    u32 workbuf[VOICECHANGER_BUF_SIZE / 4];
} VOICECHANGER_HDL;

#define VC_MAX_MALLOC_CNT  20
static u8 s_vc_hdl_mcnt = 0;

static void dump_vc_hdl_mcnt()
{
    log_info("s_vc_hdl_mcnt %d\n", s_vc_hdl_mcnt);
}
const struct mcnt_operations vc_hdl_mcnt sec_used(.d_malloc_cnt) = {
    .malloc_cnt_dump = dump_vc_hdl_mcnt,
};


void voicechanger_release(void **ppeffect)
{
    VOICECHANGER_HDL *p_vc_data = *ppeffect;
    if (NULL != p_vc_data) {
        log_info("FREE1 PH_DATA:0x%x\n", (u32)p_vc_data);
        /* 再释放音效_data */
        D_MALLOC_CNT_DEC(s_vc_hdl_mcnt);
        free(p_vc_data);
        *ppeffect = NULL;
    }
}

const int VC_NG_THRES = 712;      //建议用于底噪较大的方案
void *voice_changer_api(void *obuf, u32 sr, void **ppsound)
{
    VOICECHANGER_AV_PARM vc_parm;
    vc_parm.shiftv = 65;
    vc_parm.formant_shift = 100;
    vc_parm.speedv = 80;
    vc_parm.effect_v = EFFECT_VC_AV_BIRD5;

    VOICESYN_AV_PARM vs_parm;
    vs_parm.randpercent = 100;
    vs_parm.vibrate_lenCtrol = 30;
    vs_parm.vibrate_rate_u = 0;
    vs_parm.vibrate_rate_d = 100;

    log_info("vc_parm.shiftv %d\n", vc_parm.shiftv);
    log_info("vc_parm.formant_shift %d\n", vc_parm.formant_shift);
    log_info("vc_parm.speedv %d\n", vc_parm.speedv);
    log_info("vc_parm.effect_v 0x%x\n", vc_parm.effect_v);

    log_info("vs_parm.randpercent %d\n", vs_parm.randpercent);
    log_info("vs_parm.vibrate_lenCtrol %d\n", vs_parm.vibrate_lenCtrol);
    log_info("vs_parm.vibrate_rate_u %d\n", vs_parm.vibrate_rate_u);
    log_info("vs_parm.vibrate_rate_d %d\n", vs_parm.vibrate_rate_d);
    return voice_changer_phy(obuf, sr, &vc_parm, &vs_parm, ppsound);
}


int voice_changer_run(void *hld, short *inbuf, int len)
{
    VOICECHANGER_A_FUNC_API *ops;
    int res = 0;
    sound_in_obj *p_si = hld;
    ops = p_si->ops;
    res = ops->run(p_si->p_dbuf, inbuf, len);
    return res;
}

void update_voice_changer_parm(void *peffect, VOICECHANGER_AV_PARM *new_vc_parm, VOICESYN_AV_PARM *new_vsyn_ctrol)
{
    if ((NULL == peffect) || (NULL == new_vc_parm) || (NULL == new_vsyn_ctrol)) {
        return;
    }
    VOICECHANGER_HDL *vc_hdl = (VOICECHANGER_HDL *)peffect;
    /* log_info("new_vc_parm->shiftv %d\n", new_vc_parm->shiftv); */
    /* log_info("new_vc_parm->formant_shift %d\n", new_vc_parm->formant_shift); */
    /* log_info("new_vc_parm->speedv %d\n", new_vc_parm->speedv); */
    /* log_info("new_vc_parm->effect_v 0x%x\n", new_vc_parm->effect_v); */

    /* log_info("new_vsyn_ctrol->vibrate_rate_u %d\n", new_vsyn_ctrol->vibrate_rate_u); */
    /* log_info("new_vsyn_ctrol->vibrate_rate_d %d\n", new_vsyn_ctrol->vibrate_rate_d); */
    /* log_info("new_vsyn_ctrol->vibrate_lenCtrol %d\n", new_vsyn_ctrol->vibrate_lenCtrol); */
    /* log_info("new_vsyn_ctrol->randpercent 0x%x\n", new_vsyn_ctrol->randpercent); */

    VOICECHANGER_A_FUNC_API *ops;
    ops = get_voiceChangerA_func_api();
    OS_ENTER_CRITICAL();
    ops->open((void *)&vc_hdl->workbuf[0], vc_hdl->vc_sr, new_vc_parm, new_vsyn_ctrol, NULL);
    OS_EXIT_CRITICAL();
}

void *link_voice_changer_sound(void *p_sound_out, void *p_dac_cbuf, void **pp_effect, u32 in_sr)
{
    log_info("linking voice changer sound\n");
    log_info("sample rate %d\n", in_sr);
    sound_out_obj *p_next_sound = 0;
    sound_out_obj *p_curr_sound = p_sound_out;
    p_curr_sound->effect = voice_changer_api(p_curr_sound->p_obuf, in_sr, (void **)&p_next_sound);
    if (NULL != p_curr_sound->effect) {
        if (NULL != pp_effect) {
            *pp_effect = p_curr_sound->effect;
        }
        p_curr_sound->enable |= B_DEC_EFFECT;
        p_curr_sound = p_next_sound;
        p_curr_sound->p_obuf = p_dac_cbuf;
        log_info("voice change init succ\n");
    } else {
        log_info("voice change init fail\n");
    }
    return p_curr_sound;
}


void *voice_changer_phy(void *obuf, u32 sr, VOICECHANGER_AV_PARM *pvc_parm, VOICESYN_AV_PARM *pvs_parm, void **ppsound)
{
    u32 need_buff_len;
    VOICECHANGER_A_FUNC_API *ops;
    ops = get_voiceChangerA_func_api();
    need_buff_len = ops->need_buf(sr, pvc_parm);
    if (need_buff_len > VOICECHANGER_BUF_SIZE) {
        log_error("buff_len not enough, need 0x%x\n", need_buff_len);
        return NULL;
    }
    log_info("need buff len 0x%x, use len 0x%x\n", need_buff_len, VOICECHANGER_BUF_SIZE);
    D_MALLOC_CNT_INC(s_vc_hdl_mcnt, "s_vc_hdl_mcnt", VC_MAX_MALLOC_CNT);
    VOICECHANGER_HDL *vc_hdl = malloc(sizeof(VOICECHANGER_HDL));
    log_info("MALLOC vc_hdl 0x%x\n", vc_hdl);
    if (NULL == vc_hdl) {
        log_error("vc_cannot_malloc_any_ram!!\n");
        return NULL;
    }
    memcpy(&vc_hdl->vc_parm, pvc_parm, sizeof(VOICECHANGER_AV_PARM));
    memcpy(&vc_hdl->vs_parm, pvs_parm, sizeof(VOICESYN_AV_PARM));
    vc_hdl->vc_sr = sr;
    EFFECT_OBJ *vchange_obj;
    vchange_obj = &vc_hdl->vchange_obj;

    vc_hdl->vchange_si.ops = ops;
    vc_hdl->vchange_si.p_dbuf = &vc_hdl->workbuf[0];

    vc_hdl->vc_pitch_io.priv = &vchange_obj->sound;
    vc_hdl->vc_pitch_io.output = sound_output;

    ops->open(&vc_hdl->workbuf[0], sr, &vc_hdl->vc_parm, &vc_hdl->vs_parm, (void *)&vc_hdl->vc_pitch_io);
    vchange_obj->p_si = &vc_hdl->vchange_si;
    vchange_obj->run = voice_changer_run;
    vchange_obj->sound.p_obuf = obuf;
    *ppsound = &vchange_obj->sound;

    return vchange_obj;
}
#endif
