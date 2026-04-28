//*********************************************************************************//
// Module name : p33_interface.h
// Description : common use subroutine                                             //
// By Designer : Nelson.long
// Dat changed :                                                                   //
//*********************************************************************************//
#ifndef __P33_INTERFACE_
#define __P33_INTERFACE_


extern u8 p33_buf(u8 buf);
#define p33_xor_1byte(addr, data0)      (*((volatile u8 *)&addr + 0x300*8)  = data0);   asm volatile ("csync")
#define p33_or_1byte(addr, data0)       (*((volatile u8 *)&addr + 0x200*8)  = data0);   asm volatile ("csync")
#define p33_and_1byte(addr, data0)      (*((volatile u8 *)&addr + 0x100*8)  = data0);   asm volatile ("csync")
#define p33_tx_1byte(addr, data0)       (*((volatile u8 *)&addr )           = data0);   asm volatile ("csync")
#define p33_rx_1byte(addr)               *((volatile u8 *)&addr )
//#define P33_CON_SET(sfr, start, len, data)  (sfr = (sfr & ~((~(0xffffffff << (len))) << (start))) | \
//	 (((data) & (~(0xffffffff << (len)))) << (start)))
//#define P33_CON_GET(sfr)    (sfr)




//extern void p33_rx_nbyte(volatile u32 *addr, u8 *buf_ptr, u8 len);
//extern void p33_tx_1byte_fast(u16 addr, u32 data0);
//extern void P33_CON_SET(u16 addr, u8 start, u8 len, u8 data);

#define P33_INTERFACE 0
#endif

//*********************************************************************************//
//                                                                                 //
//                               end of this module                                //
//                                                                                 //
//*********************************************************************************//

