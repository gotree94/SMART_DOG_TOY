#include "src.h"

#define LOG_TAG_CONST       NORM
#define LOG_TAG             "[src]"
#include "log.h"

#define SRC_HW(IN_SR, OUT_SR)    ((IN_SR) * (SRC_MAX_POINT) / (OUT_SR))
#define SRC_HW_MAX_IN_SP    (SRC_HW(32000, 8000))  //输入32K，输出8K的最大输入样点个数是512
#define SRC_HW_MAX_OUT_SP   ((SRC_MAX_LEN) * 2)

#define SRC_INBUF_LEN       SRC_HW_MAX_IN_SP * 2
#define SRC_OUTBUF_LEN      SRC_HW_MAX_OUT_SP * 2
static u32 s_src_ibuff[SRC_INBUF_LEN / 4];
static u32 s_src_obuff[SRC_OUTBUF_LEN / 4];

u8 *src_buf_ptr_get(u8 is_in)
{
    if (is_in) {
        return (u8 *)&s_src_ibuff;
    } else {
        return (u8 *)&s_src_obuff;
    }
}

u32 src_buff_len_check(u32 need_len, u8 is_in)
{
    u32 buf_size = sizeof(s_src_obuff);
    char *buf_name = "s_src_obuf";
    if (is_in) {
        buf_size = sizeof(s_src_ibuff);
        buf_name = "s_src_ibuf";
    }
    if (need_len > buf_size) {
        log_error("%s len %d, need %d\n", buf_name, buf_size, need_len);
        return 1;
    }
    return 0;
}
