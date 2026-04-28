
#include "energe_api.h"
#include "sound_effect_api.h"
#include "app_modules.h"


#define LOG_TAG_CONST       NORM
#define LOG_TAG             "[normal]"
#include "log.h"

#if 1
#define ED_WORKBUF_SIZE  (50)
void *p_energe_detect_dbuf = NULL;
u8 energe_status;

#define ED_MAX_MALLOC_CNT  20
static u8 s_ed_buf_mcnt = 0;

static void dump_ed_buf_mcnt()
{
    log_info("s_ed_buf_mcnt %d\n", s_ed_buf_mcnt);
}
const struct mcnt_operations ed_hdl_mcnt sec_used(.d_malloc_cnt) = {
    .malloc_cnt_dump = dump_ed_buf_mcnt,
};
void ed_buf_release()
{
    if (NULL != p_energe_detect_dbuf) {
        log_info("FREE SP_DATA:0x%x\n", (u32)p_energe_detect_dbuf);
        /* 再释放音效_data */
        D_MALLOC_CNT_DEC(s_ed_buf_mcnt);
        free(p_energe_detect_dbuf);
        p_energe_detect_dbuf = NULL;
    }
}
/*
 * @brief 获取detect_time 内pcm数据最大值的db值
 * @parm return : -1 分贝检测算法未初始化
 *              : r_value[0],mdb值
 */
int get_energe_db_api(void)
{
    if (energe_status & ENERGE_INIT) {
        int *r_value = get_energe_db(p_energe_detect_dbuf);
        log_info("db %d\n", r_value[0] / 1000);
        return r_value[0];
    } else {
        log_error("energe_dt not init\n");
    }
    return -1;
}

/*
 * @brief 运算分贝检测算法数据
 * @parm inbuf:数据：len:数据长度
 * @parm len:数据长度
 */
void energe_run_api(short *inbuf, int len)
{
    if (energe_status & ENERGE_INIT) {
        energe_run(p_energe_detect_dbuf, inbuf, len);
    } else {
        log_error("energe_dt not init\n");
    }
}

/*
 * @brief
 * @parm sr:分贝检测算法检测数据的采样率
 */
void energe_dt_api(u32 sr)
{
    struct energe_param energe_test;
    energe_test.sample_rate = sr;
    energe_test.channel = 1;
    energe_test.detect_time = 100;
    energe_test.attFactor = 0;
    energe_test.relFactor = 0;

    int need_len = get_energe_buf(&energe_test);

    if (ED_WORKBUF_SIZE < need_len) {
        log_info("dbuf need %d\n", need_len);
        return;
    }
    D_MALLOC_CNT_INC(s_ed_buf_mcnt, "s_ed_buf_mcnt", ED_MAX_MALLOC_CNT);
    p_energe_detect_dbuf = malloc(ED_WORKBUF_SIZE);
    if (NULL == p_energe_detect_dbuf) {
        log_error("energy_detect_cannot_get_any_ram\n");
        return;
    }
    energe_init(p_energe_detect_dbuf, &energe_test);

}

/*
 * @brief 分贝检测算法初始化
 * @parm sr:分贝检测算法检测数据的采样率
 */
void energe_detect_init(u32 sr)
{
    energe_dt_api(sr);
    energe_status |= ENERGE_INIT;
    log_info("energe_detect init succ\n");
}

#if 0
void demo()
{
    /* 分贝检测算法使用流程demo */

    u32 adc_sr = read_audio_adc_sr();
    /* 1. 分贝检测初始化 */
    energe_detect_init(adc_sr);

    /* 2. 把需要检测的数据和长度传入run函数 */
    energe_run_api(data, len);

    /* 3. 获取detect_time 内pcm数据最大值的db值 */
    get_energe_db_api();
}
#endif
#endif

