#ifndef HW_FFT_H
#define HW_FFT_H

#include "typedef.h"

/*
 *******************************************************************
 *						FFT Definitions
 *******************************************************************
 */

/*
 * 标准模式:支持2的指数次幂点数
 * Platforms：br23/br25/br30/br34/br36/br29
 */
#define FFT_V3				3

/*
 * 扩展模式 V1:FFT硬件模块支持非2的指数次幂点数.
 * Platforms：br27/br28/br50/br52
 */
#define FFT_EXT				4

/*
 * 扩展模式 V2:
 * Platforms：br56
 */
#define FFT_EXT_V2			5

/*
 * 扩展模式 V2_EXP2:
 * Platforms：br60
 */
#define FFT_EXT_V2_EXP2		6

typedef struct {
    unsigned int fft_config;
    const int *in;
    int *out;
} pi32v2_hw_fft_ctx;

/*********************************************************************
*                  _fixfft_wrap
* Description: fft/ifft运算函数
* Arguments  :ctx fft数据结构；
			  in  输入地址
			  out 输出地址
* Return	 : void
* Note(s)    : None.
*********************************************************************/
void _fixfft_wrap(pi32v2_hw_fft_ctx *ctx, const int *in, int *out);

/*********************************************************************
*                  hw_fft_config
* Description: 根据配置生成 FFT_config
* Arguments  :N 运算数据量；
			  log2N 运算数据量的对数值
			  is_same_addr 输入输出是否同一个地址，0:否，1:是
			  is_ifft 运算类型 0:FFT运算, 1:IFFT运算
			  is_real 运算数据的类型  1:实数, 0:复数
* Return	 :ConfgPars 写入FFT寄存器
* Note(s)    : None.
*********************************************************************/
unsigned int hw_fft_config(int N, int log2N, int is_same_addr, int is_ifft, int is_real);

/*********************************************************************
*                  hw_fft_run
* Description: fft/ifft运算函数
* Arguments  :fft_config FFT运算配置寄存器值
			  in  输入数据地址
			  out 输出数据地址
* Return	 : void
* Note(s)    : None.
*********************************************************************/
void hw_fft_run(unsigned int fft_config, const int *in, int *out);

#endif/*HW_FFT_H*/


