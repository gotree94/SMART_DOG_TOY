/***********************************Jieli tech************************************************
  File : apa_cpu.c
  By   : mawancheng
  Email: mawancheng@zh-jieli.com
  date : 2025-5-26
********************************************************************************************/
#include "dac_api.h"
#include "dac.h"
#include "config.h"
#include "audio.h"
#include "audio_analog.h"
#include "audio_dac_cpu.h"
#include "audio_apa_cpu.h"
/* #include "asm/power_interface.h" */

#define LOG_TAG_CONST       AUDIO
#define LOG_TAG             "[apa cpu]"
#include "log.h"

const u16 c_apa_dc_dit = 0; //APA DSM直流量,默认为0

/*----------------------------------------------------------------------------*/
/**@brief   初始化APA的函数
   @param   无
   @return  无
   @author  mawancheng
   @note    void apa_init(u32 sr)
*/
/*----------------------------------------------------------------------------*/
void apa_init(u32 sr)
{
    apa_clk_sel(APA_CLK_SEL);

    APA_PHY_PARA apa_para;
    memset((void *)&apa_para, 0, sizeof(apa_para));
    bool res = get_apa_para_1(&apa_para, sr, APA_DSM_CLK_MODE, APA_CLK_SEL);
    if (false == res) {
        log_error("apa_para_error\n");
        return;
    }

    apa_analog_init();
    apa_digital_init(&apa_para, APA_CON1_DEFAULT, APA_DSM_CLK_MODE);

}

/*----------------------------------------------------------------------------*/
/**@brief   关闭APA的函数
   @param   无
   @return  无
   @author  mawancheng
   @note    void aps_close(void)
*/
/*----------------------------------------------------------------------------*/
void apa_close(void)
{
    apa_digital_close();
    apa_analog_close();
}

