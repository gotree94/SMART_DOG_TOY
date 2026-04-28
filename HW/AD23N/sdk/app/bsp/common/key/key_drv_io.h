#ifndef __KEY_DRV_IO_H__
#define __KEY_DRV_IO_H__

#include "gpio.h"
#include "key.h"
#if 0 //code_for_AD24
#define IS_KEY0_DOWN()    	(!(JL_PORTA->IN & BIT(10)))
#define IS_KEY1_DOWN()    	(!(JL_PORTA->IN & BIT(11)))
#define IS_KEY2_DOWN()    	(!(JL_PORTA->IN & BIT(12)))

#define KEY_INIT()        do{\
                            /**key0 init*/\
                            JL_PORTA->PU0 |= BIT(10),\
                            JL_PORTA->PD0 &= ~BIT(10),\
                            JL_PORTA->DIE |= BIT(10),\
                            JL_PORTA->DIR |= BIT(10);\
                            /**key1 init*/\
                            JL_PORTA->PU0 |= BIT(11),\
                            JL_PORTA->PD0 &= ~BIT(11),\
                            JL_PORTA->DIE |= BIT(11),\
                            JL_PORTA->DIR |= BIT(11);\
                            /**key2 init*/\
                            JL_PORTA->PU |= BIT(12),\
                            JL_PORTA->PD &= ~BIT(12),\
                            JL_PORTA->DIE |= BIT(12),\
                            JL_PORTA->DIR |= BIT(12);\
                            }while(0)
#endif

//AD23需要2bit控制IO上下拉电阻，并且上下拉各自集成在一个寄存器
//所以寄存器bit和io_index对应关系bit0 = io_index * 2, bit1 = io_index * 2 + 1
//比如需要设置PA3的上拉电阻,则配置SFR(JL_PORTA->PU, 3 * 2,  2,  0b01)
#define KEY0_IO_INDEX  10
#define KEY1_IO_INDEX  11
#define KEY2_IO_INDEX  12

#define IS_KEY0_DOWN()    	(!(JL_PORTA->IN & BIT(KEY0_IO_INDEX)))
#define IS_KEY1_DOWN()    	(!(JL_PORTA->IN & BIT(KEY1_IO_INDEX)))
#define IS_KEY2_DOWN()    	(!(JL_PORTA->IN & BIT(KEY2_IO_INDEX)))


#define KEY_INIT()        do{\
                            /**key0 init*/\
                            SFR(JL_PORTA->PU, KEY0_IO_INDEX * 2,  2,  0b01),\
                            SFR(JL_PORTA->PD, KEY0_IO_INDEX * 2,  2,  0b00),\
                            JL_PORTA->DIE |= BIT(KEY0_IO_INDEX),\
                            JL_PORTA->DIR |= BIT(KEY0_IO_INDEX);\
                            /**key1 init*/\
                            SFR(JL_PORTA->PU, KEY1_IO_INDEX * 2,  2,  0b01),\
                            SFR(JL_PORTA->PD, KEY1_IO_INDEX * 2,  2,  0b00),\
                            JL_PORTA->DIE |= BIT(KEY1_IO_INDEX),\
                            JL_PORTA->DIR |= BIT(KEY1_IO_INDEX);\
                            /**key2 init*/\
                            SFR(JL_PORTA->PU, KEY2_IO_INDEX * 2,  2,  0b01),\
                            SFR(JL_PORTA->PD, KEY2_IO_INDEX * 2,  2,  0b00),\
                            JL_PORTA->DIE |= BIT(KEY2_IO_INDEX),\
                            JL_PORTA->DIR |= BIT(KEY2_IO_INDEX);\
                            }while(0)
extern const key_interface_t key_io_info;

#endif/*__KEY_DRV_IO_H__*/
