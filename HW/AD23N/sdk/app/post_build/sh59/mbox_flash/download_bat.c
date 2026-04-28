// *INDENT-OFF*
cd /d %~dp0

set OBJDUMP=C:\JL\pi32\bin\llvm-objdump.exe
set OBJCOPY=C:\JL\pi32\bin\llvm-objcopy.exe
set OBJSIZEDUMP=C:\JL\pi32\bin\llvm-objsizedump.exe

if exist %OBJDUMP% (
set NAME=sdk
)

if exist %NAME%.elf (

%OBJDUMP% -section-headers %NAME%.elf
%OBJDUMP% -d -print-imm-hex -print-dbg %NAME%.elf > %NAME%.lst
%OBJSIZEDUMP% -lite -skip-zero -enable-dbg-info %NAME%.elf > %NAME%.txt

%OBJCOPY% -O binary -j .app_code %NAME%.elf  %NAME%.bin
%OBJCOPY% -O binary -j .data %NAME%.elf  data.bin
%OBJCOPY% -O binary -j .debug_data %NAME%.elf  debug_data.bin
%OBJDUMP% -section-headers  %NAME%.elf
%OBJDUMP% -t %NAME%.elf > %NAME%.symbol.txt

copy /b %NAME%.bin+data.bin+debug_data.bin app.bin
copy app.bin mbox_flash/app.bin

)


cd mbox_flash
isd_download.exe -tonorflash -dev sh59 -div8 -wait 300 -uboot uboot.boot -app app.bin 0x40000 -res dir_a dir_song dir_eng dir_poetry dir_story dir_notice dir_bin_f1x dir_midi midi_cfg -output-ufw update.ufw


@REM
@rem -format vm
@rem -format all
@rem -reboot 500






ping /n 2 127.1>null
IF EXIST null del null


