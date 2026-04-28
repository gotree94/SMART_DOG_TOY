#ifndef __INIT_H__
#define __INIT_H__

void debug_init();
void exception_irq_handler(void);
void exception_analyze(unsigned int *sp);
void emu_test();

void early_system_init(void);
void system_init(void);
void all_init_isr(void);
void app(void);
#endif
