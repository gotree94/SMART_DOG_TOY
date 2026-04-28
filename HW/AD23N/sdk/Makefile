
# make 编译并下载
# make VERBOSE=1 显示编译详细过程
# make clean 清除编译临时文件
#
# 注意： Linux 下编译方式：
#     1. 从 http://pkgman.jieliapp.com/doc/all 处找到下载链接
#     2. 下载后，解压到 /opt/jieli 目录下，保证
#       /opt/jieli/common/bin/clang 存在（注意目录层次）
#     3. 确认 ulimit -n 的结果足够大（建议大于8096），否则链接可能会因为打开文件太多而失败
#       可以通过 ulimit -n 8096 来设置一个较大的值
#

# 工具路径设置
ifeq ($(OS), Windows_NT)
# Windows 下工具链位置
TOOL_DIR := C:/JL/pi32/bin
CC    := clang.exe
CXX   := clang.exe
LD    := pi32v2-lto-wrapper.exe
AR    := llvm-ar.exe
MKDIR := mkdir_win -p
RM    := rm -rf

SYS_LIB_DIR := C:/JL/pi32/pi32v2-lib/r3
SYS_INC_DIR := C:/JL/pi32/pi32v2-include
EXT_CFLAGS  := # Windows 下不需要 -D__SHELL__
export PATH:=$(TOOL_DIR);$(PATH)

## 后处理脚本
FIXBAT          := tools/utils/fixbat.exe # 用于处理 utf8->gbk 编码问题
POST_SCRIPT     := app/post_build/sh59/download.bat
RUN_POST_SCRIPT := app\post_build\sh59\download.bat
else
# Linux 下工具链位置
TOOL_DIR := /opt/jieli/pi32v2/bin
CC    := clang
CXX   := clang
LD    := lto-wrapper
AR    := lto-ar
MKDIR := mkdir -p
RM    := rm -rf
export OBJDUMP := $(TOOL_DIR)/objdump
export OBJCOPY := $(TOOL_DIR)/objcopy
export OBJSIZEDUMP := $(TOOL_DIR)/objsizedump

SYS_LIB_DIR := $(TOOL_DIR)/../lib/r3
SYS_INC_DIR := $(TOOL_DIR)/../include
EXT_CFLAGS  := -D__SHELL__ # Linux 下需要这个保证正确处理 download.c
export PATH:=$(TOOL_DIR):$(PATH)

## 后处理脚本
FIXBAT          := touch # Linux下不需要处理 bat 编码问题
POST_SCRIPT     := app/post_build/sh59/download.sh
RUN_POST_SCRIPT := bash $(POST_SCRIPT)
endif

CC  := $(TOOL_DIR)/$(CC)
CXX := $(TOOL_DIR)/$(CXX)
LD  := $(TOOL_DIR)/$(LD)
AR  := $(TOOL_DIR)/$(AR)
# 输出文件设置
OUT_ELF   := app/post_build/sh59/sdk.elf
OBJ_FILE  := $(OUT_ELF).objs.txt
# 编译路径设置
BUILD_DIR := objs

# 编译参数设置
CFLAGS := \
	-target pi32v2 \
	-mcpu=r3v2 \
	-integrated-as \
	-flto \
	-Wuninitialized \
	-Wno-invalid-noreturn \
	-fno-common \
	-integrated-as \
	-Oz \
	-g \
	-flto \
	-fallow-pointer-null \
	-fprefer-gnu-section \
	-Wno-shift-negative-value \
	-Wundef \


# C++额外的编译参数
CXXFLAGS :=


# 宏定义
DEFINES := \
	-DFPGA=0 \
	-DCPU_SH59=1 \
	-DAUDIO_ADC_EN=1 \
	-DROM_SECURE_BOOT \
	-DSPEAKER_EN \
	-DHAS_VOICE_PITCH_EN \
	-DHAS_VOICE_CHANGER_EN \
	-DHAS_PCM_EQ_FLOAT_EN \
	-DAUX_EN \
	-DENCODER_EN \
	-DHAS_UMP3_DECODER \
	-DHAS_MP3_ST_DECODER \
	-DHAS_WAV_DECODER \
	-DHAS_F1A_DECODER \
	-DHAS_MAX_F1A_NUMBER=2 \
	-DHAS_MIDI_DECODER \
	-DHAS_MIDI_KEYBOARD_DECODER \
	-DHAS_A_DECODER \
	-DHAS_ANS_EN \
	-DHAS_SPEED_EN \
	-DHAS_EXT_FLASH_EN \
	-DHAS_USB_EN=0 \
	-DHAS_SDMMC_EN \
	-DHAS_HW_SRC_MODULE=1 \
	-DHAS_UPDATE_EN=1 \
	-DHAS_ECHO_EN \
	-DHOWLING_EN \
	-DHAS_NORFS_EN \
	-DHAS_FATFS_EN \
	-DHAS_FREEFS_EN \
	-DSIMPLE_FATFS_ENABLE=0 \
	-DSYS_VM_EN=0 \
	-DHAS_MP3_ENCODER \
	-DHAS_UMP3_ENCODER \
	-DHAS_A_ENCODER \
	-DHAS_SIMPLE_DEC_MODE \
	-DHAS_MUSIC_MODE \
	-DNOFLOAT \


DEFINES += $(EXT_CFLAGS) # 额外的一些定义

# 头文件搜索路径
INCLUDES := \
	-Iapp/src \
	-Iapp/bsp/common \
	-Iapp/bsp/common/fs \
	-Iapp/bsp/common/iic_soft \
	-Iapp/bsp/common/eeprom \
	-Iapp/bsp/common/msg \
	-Iapp/bsp/common/file_operate \
	-Iapp/bsp/common/api_mg \
	-Iapp/bsp/common/key \
	-Iapp/bsp/common/power_manage \
	-Iapp/bsp/common/norflash \
	-Iapp/bsp/common/spi_soft \
	-Iapp/bsp/common/reserved_area \
	-Iapp/bsp/common/uart_update \
	-Iapp/bsp/common/vm \
	-Iapp/bsp/cpu/sh59 \
	-Iapp/bsp/cpu/sh59/spi \
	-Iapp/bsp/cpu/sh59/wdt \
	-Iapp/bsp/cpu/sh59/audio \
	-Iapp/bsp/lib \
	-Iapp/bsp/modules \
	-Iapp/bsp/modules/timer \
	-Iapp/bsp/start/sh59 \
	-Iapp/post_build/sh59 \
	-Iinclude_lib \
	-Iinclude_lib/common \
	-Iinclude_lib/cpu/sh59 \
	-Iinclude_lib/cpu \
	-Iinclude_lib/fs \
	-Iinclude_lib/msg \
	-Iinclude_lib/fs/sydf \
	-Iinclude_lib/dev_mg \
	-Iinclude_lib/audio \
	-Iinclude_lib/sdmmc \
	-Iinclude_lib/update \
	-Iinclude_lib/decoder \
	-Iinclude_lib/decoder/list \
	-Iinclude_lib/encoder \
	-Iinclude_lib/encoder/list \
	-Iinclude_lib/device \
	-Iinclude_lib/ex_mcu \
	-Iinclude_lib/agreement \
	-Iinclude_lib/remain_output \
	-Iapp/src/mbox_flash \
	-Iapp/src/mbox_flash/sh59 \
	-Iapp/src/mbox_flash/common \
	-Iapp/src/mbox_flash/common/ui \
	-Iapp/src/mbox_flash/simple_decode \
	-Iapp/src/mbox_flash/linein \
	-Iapp/src/mbox_flash/loudspeaker \
	-Iapp/src/mbox_flash/idle \
	-Iapp/src/mbox_flash/record \
	-Iapp/src/mbox_flash/midi_dec \
	-Iapp/src/mbox_flash/midi_keyboard \
	-Iapp/src/mbox_flash/softoff_app \
	-Iapp/src/mbox_flash/usb_slave \
	-Iapp/src/mbox_flash/music \
	-Iinclude_lib/ans \
	-Iapp/bsp/common/speaker \
	-Iinclude_lib/speaker \
	-Iapp/bsp/common/vo_pitch \
	-Iinclude_lib/vo_pitch \
	-Iinclude_lib/vo_changer \
	-Iapp/bsp/modules/midi \
	-Iapp/bsp/modules/midi/pi32v2_lto_r3v2 \
	-Iapp/bsp/common/bt_common \
	-I$(SYS_INC_DIR) \


# 需要编译的 .c 文件
c_SRC_FILES := \
	app/bsp/common/config/lib_power_config.c \
	app/bsp/common/decoder/decoder_api.c \
	app/bsp/common/decoder/decoder_msg_tab.c \
	app/bsp/common/decoder/decoder_point.c \
	app/bsp/common/decoder/eq.c \
	app/bsp/common/decoder/list/a_api.c \
	app/bsp/common/decoder/list/f1a_api.c \
	app/bsp/common/decoder/list/f1x_parsing.c \
	app/bsp/common/decoder/list/midi_api.c \
	app/bsp/common/decoder/list/midi_ctrl_api.c \
	app/bsp/common/decoder/list/mp3_standard_api.c \
	app/bsp/common/decoder/list/ump3_api.c \
	app/bsp/common/decoder/list/wav_api.c \
	app/bsp/common/decoder/mp_io.c \
	app/bsp/common/decoder/sine_play.c \
	app/bsp/common/encoder/encoder_api.c \
	app/bsp/common/encoder/list/a_encoder.c \
	app/bsp/common/encoder/list/mp3_encoder.c \
	app/bsp/common/encoder/list/ump3_encoder.c \
	app/bsp/common/fs/fat/fat_resource.c \
	app/bsp/common/fs/free_fs/free_fs.c \
	app/bsp/common/fs/free_fs/free_fs_resource.c \
	app/bsp/common/fs/nor_fs/nor_fs_resource.c \
	app/bsp/common/fs/sydf/sydf_resource.c \
	app/bsp/common/fs/vfs.c \
	app/bsp/common/fs/vfs_fat.c \
	app/bsp/common/fs/vfs_resource.c \
	app/bsp/common/key/key.c \
	app/bsp/common/key/key_drv_ad.c \
	app/bsp/common/key/key_drv_io.c \
	app/bsp/common/key/key_matrix.c \
	app/bsp/common/msg/msg.c \
	app/bsp/common/norflash/norflash.c \
	app/bsp/common/power_manage/app_power_mg.c \
	app/bsp/common/remain_output/remain_output.c \
	app/bsp/common/reserved_area/reserved_area.c \
	app/bsp/common/sound_effect_list/ans/ans_api.c \
	app/bsp/common/sound_effect_list/echo/echo_api.c \
	app/bsp/common/sound_effect_list/energe_detect/energe_detect_api.c \
	app/bsp/common/sound_effect_list/notch_howling/notch_howling_api.c \
	app/bsp/common/sound_effect_list/pcm_eq_float/pcm_eq_float.c \
	app/bsp/common/sound_effect_list/pcm_eq_float/pcm_eq_param.c \
	app/bsp/common/sound_effect_list/pitch_howling/howling_pitchshifter_api.c \
	app/bsp/common/sound_effect_list/pitch_howling/pitch_howling_phy.c \
	app/bsp/common/sound_effect_list/speed/speed_api.c \
	app/bsp/common/sound_effect_list/vo_pitch/vo_pitch_api.c \
	app/bsp/common/sound_effect_list/voice_change/voicechanger.c \
	app/bsp/common/sound_mge/sound_mge.c \
	app/bsp/common/speaker/speak_api.c \
	app/bsp/common/src/src_buff.c \
	app/bsp/common/update/dev_update.c \
	app/bsp/common/update/update.c \
	app/bsp/common/vm/new_vm/nvm_api.c \
	app/bsp/common/vm/vm_api.c \
	app/bsp/cpu/iic_api.c \
	app/bsp/cpu/iic_soft.c \
	app/bsp/cpu/sh59/apa_cpu.c \
	app/bsp/cpu/sh59/audio_adc_cpu.c \
	app/bsp/cpu/sh59/audio_power_trim.c \
	app/bsp/cpu/sh59/dac_cpu.c \
	app/bsp/cpu/sh59/fft_demo.c \
	app/bsp/cpu/sh59/malloc_cnt_debug.c \
	app/bsp/cpu/sh59/power/key_wakeup.c \
	app/bsp/cpu/sh59/power/power_api.c \
	app/bsp/cpu/sh59/power/power_app.c \
	app/bsp/cpu/sh59/power/power_config.c \
	app/bsp/cpu/sh59/power/power_port.c \
	app/bsp/cpu/uart_debug.c \
	app/bsp/lib/adc_api.c \
	app/bsp/lib/audio.c \
	app/bsp/lib/common.c \
	app/bsp/lib/dac_api.c \
	app/bsp/lib/my_malloc.c \
	app/bsp/lib/tab_read.c \
	app/bsp/modules/midi/midi_dec.c \
	app/bsp/modules/midi/midi_event.c \
	app/bsp/modules/midi/midi_play.c \
	app/bsp/modules/timer/tick_timer_modules.c \
	app/bsp/modules/timer/tick_timer_sys.c \
	app/bsp/start/sh59/bsp_loop.c \
	app/bsp/start/sh59/device_list.c \
	app/bsp/start/sh59/init.c \
	app/bsp/start/sh59/main.c \
	app/src/mbox_flash/app.c \
	app/src/mbox_flash/app_config.c \
	app/src/mbox_flash/common/device_mge.c \
	app/src/mbox_flash/common/hot_msg.c \
	app/src/mbox_flash/common/idle_check.c \
	app/src/mbox_flash/common/simple_play_file.c \
	app/src/mbox_flash/common/ui/led5x7_driver.c \
	app/src/mbox_flash/common/ui/ui_api.c \
	app/src/mbox_flash/common/ui/ui_common.c \
	app/src/mbox_flash/idle/idle_mode.c \
	app/src/mbox_flash/idle/idle_mode_key.c \
	app/src/mbox_flash/linein/linein_mode.c \
	app/src/mbox_flash/linein/linein_mode_key.c \
	app/src/mbox_flash/loudspeaker/loudspk_mode.c \
	app/src/mbox_flash/loudspeaker/loudspk_mode_key.c \
	app/src/mbox_flash/midi_dec/midi_config.c \
	app/src/mbox_flash/midi_dec/midi_dec_mode.c \
	app/src/mbox_flash/midi_dec/midi_dec_mode_key.c \
	app/src/mbox_flash/midi_keyboard/midi_keyboard_mode.c \
	app/src/mbox_flash/midi_keyboard/midi_keyboard_mode_key.c \
	app/src/mbox_flash/music/break_point.c \
	app/src/mbox_flash/music/music_device.c \
	app/src/mbox_flash/music/music_key_table.c \
	app/src/mbox_flash/music/music_play.c \
	app/src/mbox_flash/music/play_file.c \
	app/src/mbox_flash/record/enc_in_fatfs.c \
	app/src/mbox_flash/record/enc_in_norfs.c \
	app/src/mbox_flash/record/record_mode.c \
	app/src/mbox_flash/record/record_mode_key.c \
	app/src/mbox_flash/sh59/cpu_config.c \
	app/src/mbox_flash/simple_decode/simple_decode.c \
	app/src/mbox_flash/simple_decode/simple_decode_key.c \
	app/src/mbox_flash/softoff_app/softoff_mode.c \
	app/src/mbox_flash/usb_slave/usb_slave.c \
	app/src/mbox_flash/usb_slave/usb_slave_key.c \


# 需要编译的 .S 文件
S_SRC_FILES :=


# 需要编译的 .s 文件
s_SRC_FILES :=


# 需要编译的 .cpp 文件
cpp_SRC_FILES :=


# 需要编译的 .cc 文件
cc_SRC_FILES :=


# 需要编译的 .cxx 文件
cxx_SRC_FILES :=


# 链接参数
LFLAGS := \
	--plugin-opt=-pi32v2-always-use-itblock=false \
	--plugin-opt=-enable-ipra=true \
	--plugin-opt=-pi32v2-merge-max-offset=4096 \
	--plugin-opt=-pi32v2-enable-simd=true \
	--plugin-opt=mcpu=r3v2 \
	--plugin-opt=-global-merge-on-const \
	--plugin-opt=-inline-threshold=5 \
	--plugin-opt=-inline-max-allocated-size=32 \
	--plugin-opt=-inline-normal-into-special-section=true \
	--plugin-opt=-dont-used-symbol-list=malloc,free,sprintf,printf,puts,putchar \
	--plugin-opt=save-temps \
	--plugin-opt=-pi32v2-enable-rep-memop \
	--sort-common \
	--dont-complain-call-overflow \
	include_lib/liba/ARCH/pi32v2_lto_r3v2/lib_energe.a \
	include_lib/liba/ARCH/pi32v2_lto_r3v2/libNoiseSuppress_pi32v2_OnChip.a \
	include_lib/liba/ARCH/pi32v2_lto_r3v2/libFFT_pi32v2_OnChip.a \
	include_lib/liba/sh59/mbox_flash/new_vm_lib.a \
	--start-group \
	include_lib/liba/sh59/mbox_flash/printf_lib.a \
	include_lib/liba/sh59/mbox_flash/cpu_lib.a \
	include_lib/liba/sh59/mbox_flash/fs_lib.a \
	include_lib/liba/sh59/mbox_flash/sydf_ex_lib.a \
	include_lib/liba/sh59/mbox_flash/audio_lib.a \
	include_lib/liba/sh59/mbox_flash/src_lib.a \
	include_lib/liba/sh59/mbox_flash/decoder_mge_lib.a \
	include_lib/liba/sh59/mbox_flash/ae_resample_lib.a \
	include_lib/liba/sh59/mbox_flash/dev_mg_lib.a \
	include_lib/liba/ARCH/pi32v2_lto_r3v2/lib_echo_cal.a \
	include_lib/liba/ARCH/pi32v2_lto_r3v2/lib_howling_fs.a \
	include_lib/liba/ARCH/pi32v2_lto_r3v2/howling.a \
	include_lib/liba/ARCH/pi32v2_lto_r3v2/lib_speed_pitch.a \
	include_lib/liba/sh59/mbox_flash/update_lib.a \
	include_lib/liba/sh59/mbox_flash/encoder_mge_lib.a \
	include_lib/liba/ARCH/pi32v2_lto_r3v2/ump3_dec_lib.a \
	include_lib/liba/ARCH/pi32v2_lto_r3v2/mp3_dec_lib.a \
	include_lib/liba/ARCH/pi32v2_lto_r3v2/wav_dec_lib.a \
	include_lib/liba/ARCH/pi32v2_lto_r3v2/lib_f1a_decode.a \
	include_lib/liba/ARCH/pi32v2_lto_r3v2/lib_midi_synth.a \
	include_lib/liba/ARCH/pi32v2_lto_r3v2/lib_limiter.a \
	include_lib/liba/ARCH/pi32v2_lto_r3v2/lib_a_decode.a \
	include_lib/liba/ARCH/pi32v2_lto_r3v2/ump2_enc_lib.a \
	include_lib/liba/ARCH/pi32v2_lto_r3v2/lib_mp2_code.a \
	include_lib/liba/ARCH/pi32v2_lto_r3v2/lib_a_code.a \
	include_lib/liba/sh59/mbox_flash/sdmmc_lib.a \
	include_lib/liba/ARCH/pi32v2_lto_r3v2/lib_vopitch_cal.a \
	include_lib/liba/ARCH/pi32v2_lto_r3v2/lib_voiceChanger_va.a \
	include_lib/liba/ARCH/pi32v2_lto_r3v2/lib_pcm_eq.a \
	include_lib/liba/sh59/mbox_flash/pcm_eq_float_lib.a \
	--end-group \
	-Tapp/post_build/sh59/app.ld \
	-M=app/post_build/sh59/app.map \
	--plugin-opt=mcpu=r3v2 \
	--plugin-opt=-mattr=+fprev1 \


LIBPATHS := \
	-L$(SYS_LIB_DIR) \


LIBS := \
	$(SYS_LIB_DIR)/libm.a \
	$(SYS_LIB_DIR)/libc.a \
	$(SYS_LIB_DIR)/libm.a \
	$(SYS_LIB_DIR)/libcompiler-rt.a \



c_OBJS    := $(c_SRC_FILES:%.c=%.c.o)
S_OBJS    := $(S_SRC_FILES:%.S=%.S.o)
s_OBJS    := $(s_SRC_FILES:%.s=%.s.o)
cpp_OBJS  := $(cpp_SRC_FILES:%.cpp=%.cpp.o)
cxx_OBJS  := $(cxx_SRC_FILES:%.cxx=%.cxx.o)
cc_OBJS   := $(cc_SRC_FILES:%.cc=%.cc.o)

OBJS      := $(c_OBJS) $(S_OBJS) $(s_OBJS) $(cpp_OBJS) $(cxx_OBJS) $(cc_OBJS)
DEP_FILES := $(OBJS:%.o=%.d)


OBJS      := $(addprefix $(BUILD_DIR)/, $(OBJS))
DEP_FILES := $(addprefix $(BUILD_DIR)/, $(DEP_FILES))


VERBOSE ?= 0
ifeq ($(VERBOSE), 1)
QUITE :=
else
QUITE := @
endif

# 一些旧的 make 不支持 file 函数，需要 make 的时候指定 LINK_AT=0 make
LINK_AT ?= 1

# 表示下面的不是一个文件的名字，无论是否存在 all, clean, pre_build 这样的文件
# 还是要执行命令
# see: https://www.gnu.org/software/make/manual/html_node/Phony-Targets.html
.PHONY: all clean pre_build

# 不要使用 make 预设置的规则
# see: https://www.gnu.org/software/make/manual/html_node/Suffix-Rules.html
.SUFFIXES:

all: pre_build $(OUT_ELF)
	$(info +POST-BUILD)
	$(QUITE) $(RUN_POST_SCRIPT) sdk

pre_build:
	$(info +PRE-BUILD)
	$(QUITE) $(CC) $(CFLAGS) $(DEFINES) $(INCLUDES) -D__LD__ -E -P app/post_build/sh59/mbox_flash/app_ld.c -o app/post_build/sh59/app.ld
	$(QUITE) $(CC) $(CFLAGS) $(DEFINES) $(INCLUDES) -D__LD__ -E -P app/post_build/sh59/mbox_flash/download_bat.c -o $(POST_SCRIPT)
	$(QUITE) $(FIXBAT) $(POST_SCRIPT)
	$(QUITE) $(CC) $(CFLAGS) $(DEFINES) $(INCLUDES) -D__LD__ -E -P app/post_build/sh59/mbox_flash/isd_config_ini.c -o app/post_build/sh59/mbox_flash/isd_config.ini

clean:
	$(QUITE) $(RM) $(OUT_ELF)
	$(QUITE) $(RM) $(BUILD_DIR)



ifeq ($(LINK_AT), 1)
$(OUT_ELF): $(OBJS)
	$(info +LINK $@)
	$(shell $(MKDIR) $(@D))
	$(file >$(OBJ_FILE), $(OBJS))
	$(QUITE) $(LD) -o $(OUT_ELF) @$(OBJ_FILE) $(LFLAGS) $(LIBPATHS) $(LIBS)
else
$(OUT_ELF): $(OBJS)
	$(info +LINK $@)
	$(shell $(MKDIR) $(@D))
	$(QUITE) $(LD) -o $(OUT_ELF) $(OBJS) $(LFLAGS) $(LIBPATHS) $(LIBS)
endif


$(BUILD_DIR)/%.c.o : %.c
	$(info +CC $<)
	$(QUITE) $(MKDIR) $(@D)
	$(QUITE) $(CC) $(CFLAGS) $(DEFINES) $(INCLUDES) -MMD -MF $(@:.o=.d) -c $< -o $@

$(BUILD_DIR)/%.S.o : %.S
	$(info +AS $<)
	$(QUITE) $(MKDIR) $(@D)
	$(QUITE) $(CC) $(CFLAGS) $(DEFINES) $(INCLUDES) -MMD -MF $(@:.o=.d) -c $< -o $@

$(BUILD_DIR)/%.s.o : %.s
	$(info +AS $<)
	$(QUITE) $(MKDIR) $(@D)
	$(QUITE) $(CC) $(CFLAGS) $(DEFINES) $(INCLUDES) -MMD -MF $(@:.o=.d) -c $< -o $@

$(BUILD_DIR)/%.cpp.o : %.cpp
	$(info +CXX $<)
	$(QUITE) $(MKDIR) $(@D)
	$(QUITE) $(CXX) $(CXXFLAGS) $(CFLAGS) $(DEFINES) $(INCLUDES) -MMD -MF $(@:.o=.d) -c $< -o $@

$(BUILD_DIR)/%.cxx.o : %.cxx
	$(info +CXX $<)
	$(QUITE) $(MKDIR) $(@D)
	$(QUITE) $(CXX) $(CXXFLAGS) $(CFLAGS) $(DEFINES) $(INCLUDES) -MMD -MF $(@:.o=.d) -c $< -o $@

$(BUILD_DIR)/%.cc.o : %.cc
	$(info +CXX $<)
	$(QUITE) $(MKDIR) $(@D)
	$(QUITE) $(CXX) $(CXXFLAGS) $(CFLAGS) $(DEFINES) $(INCLUDES) -MMD -MF $(@:.o=.d) -c $< -o $@

-include $(DEP_FILES)
