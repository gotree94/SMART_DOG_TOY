#ifndef __PCM_EQ_FLOAT_H___
#define __PCM_EQ_FLOAT_H___
#include "typedef.h"

#define EQ_SECTION_MAX      10

/*eq IIR type*/
typedef enum {
    EQ_IIR_TYPE_HIGH_PASS = 0x00,
    EQ_IIR_TYPE_LOW_PASS,
    EQ_IIR_TYPE_BAND_PASS,
    EQ_IIR_TYPE_HIGH_SHELF,
    EQ_IIR_TYPE_LOW_SHELF,
    EQ_IIR_TYPE_BAND_PASS_NEW,
} EQ_IIR_TYPE;

struct eq_seg_info {
    unsigned short index;      //eq段序号
    unsigned short iir_type;   //滤波器类型EQ_IIR_TYPE
    int freq;                  //中心截止频率
    float gain;                //增益（-12 ~12 db）
    float q;                   //q值（0.3~30）
};

struct PCM_EQ_F_PARAM {
    const struct eq_seg_info *seg;
    float global_gain; //对应系数总增益
};


typedef struct _eq_buff {
    struct PCM_EQ_F_PARAM *eq_param;
    unsigned short sample_rate;
    unsigned char max_nsection;
    float *p_eq_coeff_tab;//缓存运算后的系数表，用于传给算法
} EQ_COEFF_BUFF;


void update_pcm_eq_float_param();
bool calculate_eq_param(EQ_COEFF_BUFF *ptr, u32 seg_num);
void design_pe_for_int(int fc, int fs, int gain, int quality_factor, float *coeff);


//系数计算子函数
/*----------------------------------------------------------------------------*/
/**@brief    低通滤波器
   @param    fc:中心截止频率
   @param    fs:采样率
   @param    quality_factor:q值
   @param    coeff:计算后，系数输出地址
   @return
   @note
*/
/*----------------------------------------------------------------------------*/
extern void design_lp(int fc, int fs, float quality_factor, float *coeff);

/*----------------------------------------------------------------------------*/
/**@brief    高通滤波器
   @param    fc:中心截止频率
   @param    fs:采样率
   @param    quality_factor:q值
   @param    coeff:计算后，系数输出地址
   @return
   @note
*/
extern void design_hp(int fc, int fs, float quality_factor, float *coeff);

/*----------------------------------------------------------------------------*/
/**@brief    波峰滤波器
   @param    fc:中心截止频率
   @param    fs:采样率
   @param    gain:增益
   @param    quality_factor:q值
   @param    coeff:计算后，系数输出地址
   @return
   @note
*/
/*----------------------------------------------------------------------------*/
extern void design_pe(int fc, int fs, float gain, float quality_factor, float *coeff);

/*----------------------------------------------------------------------------*/
/**@brief    低频搁架式滤波器
   @param    fc:中心截止频率
   @param    fs:采样率
   @param    gain:增益
   @param    quality_factor:q值
   @param    coeff:计算后，系数输出地址
   @return
   @note
*/
/*----------------------------------------------------------------------------*/
extern void design_ls(int fc, int fs, float gain, float quality_factor, float *coeff);

/*----------------------------------------------------------------------------*/
/**@brief    高频搁架式滤波器
   @param    fc:中心截止频率
   @param    fs:采样率
   @param    gain:增益
   @param    quality_factor:q值
   @param    coeff:计算后，系数输出地址
   @return
   @note
*/
/*----------------------------------------------------------------------------*/
extern void design_hs(int fc, int fs, float gain, float quality_factor, float *coeff);

/*----------------------------------------------------------------------------*/
/**@brief    带通滤波器
   @param    fc:中心截止频率
   @param    fs:采样率
   @param    quality_factor:q值
   @param    coeff:计算后，系数输出地址
   @return
   @note
*/
/*----------------------------------------------------------------------------*/
extern void design_bp(int fc, int fs, float quality_factor, float *coeff);

/*----------------------------------------------------------------------------*/
/**@brief    滤波器系数检查
   @param    coeff:滤波器系数
   @return
   @note
*/
/*----------------------------------------------------------------------------*/
extern int eq_stable_check(float *coeff);



#endif

