/*
 ****************************************************************************
 *							Audio FFT Demo
 *
 *Description	: Audio FFT使用范例
 *Notes			: 本demo为开发测试范例，请不要修改该demo， 如有需求，请自行
 *				  复制再修改
 ****************************************************************************
 */

#include "hw_fft.h"

#define LOG_TAG_CONST       NORM
#define LOG_TAG             "[fft_demo]"
#include "log.h"
// For 128 point Real_FFT test
void hw_fft_demo_real()
{

    int tmpbuf[130];  // 130 = (128/2+1)*2
    unsigned int fft_config;

    log_info("********* test start  **************\n");

    for (int i = 0; i < 128; i++) {
        tmpbuf[i] = i + 1;
        log_info("tmpbuf[%d]: %d \n", i, tmpbuf[i]);
    }

    // Do 128 point FFT
    fft_config = hw_fft_config(128, 7, 1, 0, 1);

    hw_fft_run(fft_config, tmpbuf, tmpbuf);

    for (int i = 0; i < 128; i++) {
        log_info("tmpbuf_[%d]: %d \n", i, tmpbuf[i]);
    }

    // Do 128 point IFFT
    fft_config = hw_fft_config(128, 7, 1, 1, 1);

    hw_fft_run(fft_config, tmpbuf, tmpbuf);

    for (int i = 0; i < 128; i++) {
        log_info("tmpbuf_[%d]: %d \n", i, tmpbuf[i]);
    }
}

// For 128 point Complex_FFT test
void hw_fft_demo_complex()
{

    int tmpbuf[280];    // 280 = (128/2+1)*2*2
    unsigned int fft_config;

    log_info("********* test start  **************\n");

    for (int i = 0; i < 128; i++) {
        tmpbuf[2 * i] = i + 1;
        tmpbuf[2 * i + 1] = i + 1;
        log_info("tmpbuf[%d]: %d \n", 2 * i, tmpbuf[2 * i]);
        log_info("tmpbuf[%d]: %d \n", 2 * i + 1, tmpbuf[2 * i + 1]);
    }

    // Do 128 point FFT

    fft_config = hw_fft_config(128, 7, 1, 0, 0);

    hw_fft_run(fft_config, tmpbuf, tmpbuf);

    for (int i = 0; i < 128; i++) {
        log_info("tmpbuf[%d]: %d \n", 2 * i, tmpbuf[2 * i]);
        log_info("tmpbuf[%d]: %d \n", 2 * i + 1, tmpbuf[2 * i + 1]);
    }

    // Do 128 point IFFT
    fft_config = hw_fft_config(128, 7, 1, 1, 0);

    hw_fft_run(fft_config, tmpbuf, tmpbuf);

    for (int i = 0; i < 128; i++) {
        log_info("tmpbuf[%d]: %d \n", 2 * i, tmpbuf[2 * i]);
        log_info("tmpbuf[%d]: %d \n", 2 * i + 1, tmpbuf[2 * i + 1]);
    }
}



