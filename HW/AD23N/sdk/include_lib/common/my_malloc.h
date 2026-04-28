#ifndef __MY_MALLOC_H__
#define __MY_MALLOC_H__

#include "typedef.h"



#define configUSE_MALLOC_FAILED_HOOK    0

#ifndef portPOINTER_SIZE_TYPE
#define portPOINTER_SIZE_TYPE u32
#endif

#define portBYTE_ALIGNMENT      4   //对齐规则


#if portBYTE_ALIGNMENT == 32
#define portBYTE_ALIGNMENT_MASK ( 0x001f )
#endif

#if portBYTE_ALIGNMENT == 16
#define portBYTE_ALIGNMENT_MASK ( 0x000f )
#endif

#if portBYTE_ALIGNMENT == 8
#define portBYTE_ALIGNMENT_MASK ( 0x0007 )
#endif

#if portBYTE_ALIGNMENT == 4
#define portBYTE_ALIGNMENT_MASK ( 0x0003 )
#endif

#if portBYTE_ALIGNMENT == 2
#define portBYTE_ALIGNMENT_MASK ( 0x0001 )
#endif

#if portBYTE_ALIGNMENT == 1
#define portBYTE_ALIGNMENT_MASK ( 0x0000 )
#endif

#define vTaskSuspendAll()
#define traceMALLOC(...)
#define configASSERT       ASSERT

extern void xTaskResumeAll(void);


#define pdFALSE   0
#define pdTRUE    1

extern const char MM_ASSERT;


typedef enum _mm_type {
    MM_NONE = 0,
    MM_VFS,
    MM_SYDFS,
    MM_SYDFF,
    MM_NORFS,
    MM_NORFF,
    MM_FATFS,
    MM_FATFF,
    MM_FAT_TMP,
    MM_SRC, //SRC_DATA
    MM_MIO,
    MM_SWIN_BUF,
    MM_VFSCAN_BUF,
    MM_SCAN_BUF,
    MM_FF_APIS_BUF,
    MM_HW_SRC_BUF,
    MM_SW_SRC_BUF,
    MM_SARADC_BUF,
    MM_FREEFS = 0x80,
    MM_FREEFF,
} mm_type;


#ifndef traceFREE
#define traceFREE(pvAddress, uSize)
#endif




void vPortInit(void *pAddr, uint32_t xLen);
// void *pvPortMalloc( size_t xWantedSize );

void *pvPortMalloc(size_t xWantedSize, mm_type type);
void vPortFree(void *pv);
void *my_malloc(u32 size, mm_type xType);
void *my_free(void *pv);
// void *my_malloc(u32 size);
void my_malloc_init(void);


#define D_MALLOC_CNT_INC(cnt, str, max_cnt)    \
            if (cnt < max_cnt) { \
                cnt++;           \
                log_info("%s malloc %d\n", str, cnt); \
            } else {             \
                log_error("%s malloc overflow\n", str);  \
            }

#define D_MALLOC_CNT_DEC(cnt)    \
            if (cnt != 0) { \
                cnt--; \
            }

// 拼接宏，保证参数先展开
#define CONCAT(a, b) a##b
#define EXPAND_CONCAT(a, b) CONCAT(a, b)

#define CONCAT3(a, b, c) a##b##c
#define EXPAND_CONCAT_3(a, b, c) CONCAT3(a, b, c)

extern const u8 const_debug_mcnt;

#define MAX_CNT 20
#define D_MALLOC(p, size)                                    \
    do {                                                     \
        if(const_debug_mcnt)                                 \
        {                                                    \
            if (EXPAND_CONCAT(D_THIS, _mcnt) < MAX_CNT) {          \
                EXPAND_CONCAT(D_THIS, _mcnt)++;              \
                    log_info("%s malloc %d\n", D_THIS_NAME, EXPAND_CONCAT(D_THIS, _mcnt)); \
            } else {                                             \
                log_error("%s malloc overflow\n", D_THIS_NAME);  \
            }                                                    \
        }                                                    \
        p = malloc(size);                                    \
    } while(0)


#define D_FERR(p)                                            \
    do {                                                     \
        if(const_debug_mcnt)                                 \
        {                                                    \
            if (EXPAND_CONCAT(D_THIS, _mcnt) != 0) {         \
                EXPAND_CONCAT(D_THIS, _mcnt)--;              \
            }                                                \
        }                                                    \
        free(p);                                             \
        p = NULL;                                            \
    } while (0)


#define D_DUMP_CNT()                                               \
    static void EXPAND_CONCAT_3(dump_, D_THIS, _mcnt)(void) {       \
        log_info("%s_mcnt %d\n",D_THIS_NAME, EXPAND_CONCAT(D_THIS, _mcnt));    \
    }                                                              \
    const struct mcnt_operations EXPAND_CONCAT_3(f_, D_THIS, _mcnt) \
        sec_used(.d_malloc_cnt) = {                                \
        .malloc_cnt_dump = EXPAND_CONCAT_3(dump_, D_THIS, _mcnt),   \
    };



struct mcnt_operations {
    // const char *name;
    void (*malloc_cnt_dump)(void);
};
void all_malloc_cnt_dump(void);
#endif

