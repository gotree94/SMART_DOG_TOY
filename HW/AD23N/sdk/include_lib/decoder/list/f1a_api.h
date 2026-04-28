#ifndef __F1A_API_H__
#define __F1A_API_H__
#include "if_decoder_ctrl.h"
#include "app_modules.h"


u32 f1a_decode_api_1(void *p_file, void **p_pdec, void *p_dp_buf);
u32 f1a_decode_api_2(void *p_file, void **p_pdec, void *p_dp_buf);

u32 f1a_1_buff_api(dec_buf *p_dec_buf);
u32 f1a_2_buff_api(dec_buf *p_dec_buf);




#define F1X_MAX_UNIT  10
typedef struct _f1x_ctl_ {
    u16 index;
    /* u16 max_unit; */
    u8  cnt;
    u8  loop_tab[F1X_MAX_UNIT];
    u32 buff[F1X_MAX_UNIT];
} f1x_ctl;

void *f1x_play_api(void *pfile, u8 *loop_tab, u32 size, u8 index, u32 addr, f1x_ctl *p_f1x_ctl);

extern const int F1A_MASKROM_TAB;



#endif


