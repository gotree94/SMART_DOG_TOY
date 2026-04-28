#include "my_malloc.h"

#define LOG_TAG_CONST       NORM
#define LOG_TAG             "[normal]"
#include "log.h"


extern struct mcnt_operations malloc_cnt_begin[];
extern struct mcnt_operations malloc_cnt_end[];

#define list_for_each_mcnt_operation(ops) \
    for (ops = malloc_cnt_begin; ops < malloc_cnt_end; ops++)



void all_malloc_cnt_dump(void)
{
    struct mcnt_operations *ops;
    /* log_info("malloc_cnt_begin 0x%x, cnt_end 0x%x\n", malloc_cnt_begin, malloc_cnt_end); */
    list_for_each_mcnt_operation(ops) {
        if (NULL != ops->malloc_cnt_dump) {
            ops->malloc_cnt_dump();
        }
    }
}

