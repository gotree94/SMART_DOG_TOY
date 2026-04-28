#ifndef __DECODER_CPU_H__
#define __DECODER_CPU_H__
#include "decoder_api.h"

// f1a
#define HAS_F1A_MASK_TAB            0
// a
#define A_DEC_OBUF_SIZE             (DAC_DECODER_BUF_SIZE)
#define A_DBUF_SIZE                 (244)
// midi_dec
#define MIDI_DEC_OBUF_SIZE          (DAC_DECODER_BUF_SIZE)
#define MIDI_DEC_DBUF_SIZE          (4448)

#define MP3_ST_DEC_CONFING() \
    do { \
        p_modevalue->mode = 1;/*output是否判断返回值*/ \
        ops->dec_confing(MP3_ST_CAL_BUF, SET_DECODE_MODE, p_modevalue); \
    } while(0)

#endif
