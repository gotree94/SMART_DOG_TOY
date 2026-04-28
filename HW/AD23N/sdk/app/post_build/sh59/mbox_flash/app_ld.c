// *INDENT-OFF*
#include  "maskrom_stubs.ld"
//config

UPDATA_SIZE     = 0x200;
UPDATA_BEG      = _MASK_EXPORT_MEM_BEGIN - UPDATA_SIZE;

_BOOT_RAM_BEGIN = _MASK_EXPORT_MEM_BEGIN;

RAM0_END = _BOOT_RAM_BEGIN;
MEMORY
{
    //app_code(rx)        : ORIGIN = 0xC000100,            LENGTH = 64M-0x100
    app_code(rx)        : ORIGIN = _SFC_MEMORY_START_ADDR  + 0x100,            LENGTH = 2M
    ram0(rw)            : ORIGIN = _RAM_LIMIT_L,         LENGTH = _BOOT_RAM_BEGIN - _RAM_LIMIT_L
}

ENTRY(_start)

SECTIONS
{
    /* L1 memory sections */


    . = ORIGIN(ram0);
    //TLB 起始需要4K 对齐；
    .mmu_tlb ALIGN(0x1000):
    {
        MMU_TBL_BEG_CHECK = .;
        *(.mmu_tlb_segment);
    } > ram0

    .data ALIGN(4) : SUBALIGN(4)
    {
        PROVIDE(data_buf_start = .);
        *(.data*)
        *(.*.data)

        cache_Lx_code_text_begin = .;
        *(.common)
        *(.mp3_dec_data)
        *(.wav_dec_data)
        *(.*.text.cache.L1)
        *(.*.text.cache.L2)
        *(.*.text.cache.L3)
        . = ALIGN(4);
        cache_Lx_code_text_end = .;
    } > ram0

    .debug_data ALIGN(4) : SUBALIGN(4)
    {
        PROVIDE(debug_buf_start = .);
        *(.debug_bss)
        *(.debug_data)
    } > ram0


    .bss ALIGN(4) (NOLOAD) : SUBALIGN(4)
    {
        PROVIDE(bss_buf_start = .);
        . = ALIGN(4);
        _cpu0_sstack_begin = .;
        . = ALIGN(4);
        *(.intr_stack)
        . = ALIGN(4);
        *(.stack_magic);
        . = ALIGN(4);
        *(.stack)
        . = ALIGN(4);
        *(.stack_magic0);
        . = ALIGN(4);
        _cpu0_sstack_end = .;
        . = ALIGN(4);
        _system_data_begin = .;
        *(.bss)
        *(.mp3_dec_bss)
        *(.wav_dec_bss)
        *(.*.data.bss)
        . = ALIGN(4);
        _system_data_end = .;
        *(.DAC_BUFFER)
        *(.AUDIO_ADC_BUFFER)
    } > ram0

    /* OVERLAY : */
    .effect_buf ALIGN(4) : SUBALIGN(4)
    {
        PROVIDE(effect_buf_start = .);
        . = ALIGN(4);
        *(.sp_data)
        . = ALIGN(4);
        *(.rs_data)
        /* .d_sp {  *(.sp_data) } */
        /* .d_rs { *(.rs_data) } */
    } > ram0
    /* _ram0_end = .; */

    /* . = ORIGIN(ram1); */
    OVERLAY : AT(0x200000)
    {
        /* 小音箱应用 */
        .d_music_play {
            PROVIDE(mode_music_overlay_data_start = .);
            *(.mode_music_overlay_data);
            /* *(.usb_h_dma); */
            *(.fat_buf);
            PROVIDE(mode_music_overlay_data_end = .);
        }
        .d_fat_tmp
        {
            . = mode_music_overlay_data_end;
            *(.fat_tmp_buf)
        }

        .d_simple_decode
        {
            PROVIDE(mode_smpl_dec_ovly_start = .);
            *(.mode_smpl_dec_data);
            PROVIDE(mode_smpl_dec_ovly_end = .);
        }
        .d_midi
        {
            . = MAX(mode_music_overlay_data_end, mode_smpl_dec_ovly_end);
            /* . = mode_smpl_dec_ovly_end; */
            PROVIDE(midi_buf_start = .);
            *(.midi_buf);
            PROVIDE(midi_buf_end = .);
            PROVIDE(midi_ctrl_buf_start = .);
            *(.midi_ctrl_buf);
            PROVIDE(midi_ctrl_buf_end = .);
        }
        .d_rec
        {
            rec_data_start = .;
            *(.rec_data)
            *(.ans_data)
            rec_data_end = .;
        }
        .d_aux
        {
            /* . = aux_data_end; */
            aux_data_start = .;
            *(.aux_data)
            aux_data_end = .;
        }
        .d_vp_data0
        {
            speaker_data_start = .;
            *(.speaker_data)
            speaker_data_end = .;
            . = ALIGN(4);
            *(.notch_howling_data)
            . = ALIGN(4);
            *(.notch_howling_lib)
            howling_data_end = .;
        }

    } > ram0

    _HEAP_BEGIN = . ;
    .heap_buf ALIGN(4) : SUBALIGN(4)
    {
        PROVIDE(_free_start = .);
        . = LENGTH(ram0) + ORIGIN(ram0) - 1;
        PROVIDE(_free_end = .);
    } > ram0
    _HEAP_END = RAM0_END;

    _ram_end = .;


    . = ORIGIN(app_code);
    .app_code ALIGN(4) : SUBALIGN(4)
    {
        app_code_text_begin = .;
        *startup.o(.text)
        . = ALIGN(4);
        *(*.f1a_code)
        *(.mp3_dec_code)
        *(.mp3_dec_sparse_code)
        *(.wav_dec_code)
        *(.wav_dec_sparse_code)
        *(.fft_code)
        *(.ns_code)
        *(.ns_sparse_code)
        *(.opcore_maskrom)
        /* *(.mp2_encode_code) */
        /* *(.mp2_encode_sparse_code) */
        _VERSION_BEGIN = .;
        KEEP(*(.version))
        _VERSION_END = .;
        *(.debug)
        *(*.mp3_dec_const)
        *(*.wav_dec_const)
        *(*.fft_const)
        *(*.bark_const)
        /* *(.mp2_encode_const) */
        *(.debug_const)
        *(.debug_code)
        *(.debug_string)
        /* *memset.o(.text .rodata*) */
        /* *memcmp.o(.text .rodata*) */
        *(*.text.const)
        *(*.text)
        *(.text.*)
        *(.app_root)
        *(.vm)
        . = ALIGN(4);
        _SPI_CODE_START = .;
        *(.spi_code)
        . = ALIGN(4);
        _SPI_CODE_END = .;
		. = ALIGN(4);
        _SFC_TRIM_CODE_START = . ;
        *(.sfc_trim_code)
		. = ALIGN(4);
        _SFC_TRIM_CODE_END = . ;

        *(.rodata*)
        *(.ins)
        app_code_text_end = .;

        /* *(.poweroff_text) */
        . = ALIGN(4);
        loop_detect_handler_begin = .;
            KEEP(*(.loop_detect_region))
        loop_detect_handler_end = .;

        . = ALIGN(4);
        device_node_begin = .;
        PROVIDE(device_node_begin = .);
        KEEP(*(.device))
        _device_node_end = .;
        PROVIDE(device_node_end = .);

        vfs_ops_begin = .;
        KEEP(*(.vfs_operations))
        vfs_ops_end = .;
        . = ALIGN(4);

        malloc_cnt_begin = .;
        KEEP(*(.d_malloc_cnt))
        malloc_cnt_end = .;
        . = ALIGN(4);
        lp_target_begin = .;
        PROVIDE(lp_target_begin = .);
        KEEP(*(.lp_target))
        lp_target_end = .;
        PROVIDE(lp_target_end = .);

        . = ALIGN(4);
	    lp_request_begin = .;
	    PROVIDE(lp_request_begin = .);
	    KEEP(*(.lp_request))
	    lp_request_end = .;
	    PROVIDE(lp_request_end = .);

		. = ALIGN(4);
        deepsleep_target_begin = .;
        PROVIDE(deepsleep_target_begin = .);
        KEEP(*(.deepsleep_target))
        deepsleep_target_end = .;
        PROVIDE(deepsleep_target_end = .);

		. = ALIGN(4);
		p2m_msg_handler_begin = .;
		PROVIDE(p2m_msg_handler_begin = .);
		KEEP(*(.p2m_msg_handler))
		PROVIDE(p2m_msg_handler_end = .);
		p2m_msg_handler_end = .;

		. = ALIGN(4);
		phw_begin = .;
		PROVIDE(phw_begin = .);
		KEEP(*(.phw_operation))
		PROVIDE(phw_end = .);
		phw_end = .;

		hsb_critical_handler_begin = .;
		KEEP(*(.hsb_critical_txt))
		hsb_critical_handler_end = .;

		lsb_critical_handler_begin = .;
		KEEP(*(.lsb_critical_txt))
		lsb_critical_handler_end = .;

        app_size = .;
        /* . = LENGTH(app_code) - LENGTH_OF_ENTRY_LIST; */
        /* *(.sec.array_maskrom_export) */
        . = ALIGN(4);
        text_end = .;
    } >app_code


    bss_begin       = ADDR(.bss);
    bss_size        = SIZEOF(.bss);

    /*除堆栈外的bss区*/
    bss_size1       = _system_data_end - _system_data_begin;
    bss_begin1      = _system_data_begin;

    data_addr  = ADDR(.data) ;
    data_begin = text_end ;
    data_size =  SIZEOF(.data) + SIZEOF(.debug_data);

    text_size       = SIZEOF(.app_code);

    _sdk_text_addr = ADDR(.app_code);
    _sdk_text_size = text_size;
    _sdk_text_addr_end = _sdk_text_addr + _sdk_text_size;

    _sdk_data_size = data_size;

    ASSERT((bss_size1 % 4) == 0, "bss size not 4")
    ASSERT((bss_begin1 % 4) == 0, "data size not 4")

    ASSERT((data_size % 4) == 0, "data size not 4")
    ASSERT((data_addr % 4) == 0, "data_addr size not 4")
    ASSERT((data_begin % 4) == 0, "data_begin size not 4")

}
//===================== HEAP Info Export =====================//
PROVIDE(HEAP_BEGIN = _HEAP_BEGIN);
PROVIDE(HEAP_END = _HEAP_END);
_MALLOC_SIZE = _HEAP_END - _HEAP_BEGIN;
PROVIDE(MALLOC_SIZE = _HEAP_END - _HEAP_BEGIN);

