# fw-AD23N
About Firmware for Generic MCU SDK（AD23N series）, Support AD23N

[tag download]:https://github.com/Jieli-Tech/AD23N/tags
[tag_badgen]:https://img.shields.io/github/v/tag/Jieli-Tech/AD23N?style=plastic&labelColor=ffffff&color=informational&label=Tag&

# fw-AD23N_SDK   [![tag][tag_badgen]][tag download]

[中文](./README.md) | EN

firmware for Generic MCU SDK（AD23 series）

This repository contains the Jieli source code, aims at helping the developers for the toy & generic MCU applications.
It must be combined with lib.a and the repositories that use the same
naming convention to build the provided samples and to use the additional
subsystems and libraries.

![Alt](jl_ad_chip.png)

Getting Started
------------

Welcome to JL open source! See the `Introduction to SDK` for a high-level overview,
and the documentation's `Getting Started Guide` to start developing.

Toolchain
------------

How to get the `JL Toolchain` and setup the build enviroment,see below

* Complie Tool ：install the JL complie tool to setup the build enviroment, [download link](https://pan.baidu.com/s/1f5pK7ZaBNnvbflD-7R22zA) code: `ukgx`
* Compiler invitation code: 4VlZaQCq-lImlZX2u-GBeCs501-ektNxDGu

* USB updater : program flash tool to download the `hex` file to the target board, please accquire the tool form the [link](https://item.taobao.com/item.htm?spm=a1z10.1-c-s.w4004-22883854875.5.504d246bXKwyeH&id=620295020803) and check the related configuration and [document](.doc/stuff/ISD_CONFIG.INI配置文件说明.pdf)


Documentation
------------

* Chipset brief : [SoC datasheet](./doc)

* Product Select Guide : [SoC Select Guide.pdf](./doc杰理科技32位AD系列语音MCU选型表.pdf)

* SDK Version: [SDK History](./doc/AD23N_SDK_发布版本信息.pdf)

* SDK introduction : [SDK quick start guide](./doc/AD23N_SDK手册_v1.0.pdf)

* SDK Online documentation : [SDK Online documentation](https://doc.zh-jieli.com/AD23/zh-cn/master/index.html)

* SDK architure : [SDK module architure ](./doc/)

* Video resource: [Video resource](https://space.bilibili.com/3493277347088769/dynamic)

* FAE support document: [FAE support](https://gitee.com/jieli-tech_fae/fw-jl)

* MIDI Application Development Manual : [MIDI Application Development Manual](https://doc.zh-jieli.com/MIDI/zh-cn/master/index.html)

Build
-------------
Select a project to build. The following folders contains buildable projects:

* APP_TOY: ./sdk/AD23N_mbox_flash.cbp, usage: 


Comming Soon：
-------------

SDK support Codeblock to build to project,make sure you already setup the enviroment

* Codeblock build : enter the project directory and find the `.cbp`,double click and build.

* Makefile build : `apps/app_cfg` select the target you want to build,double click the `make_prompt` and excute `make`

  `before build the project make sure the USB updater is connect and enter the update mode correctly`


Hardware
-------------

* EV Board ：(https://shop321455197.taobao.com/?spm=a230r.7195193.1997079397.2.2a6d391d3n5udo)

* Production Tool : massive prodution and program the SoC, please accquire the tool from the [link](https://item.taobao.com/item.htm?spm=a1z10.1-c-s.w4004-22883854875.8.504d246bXKwyeH&id=620941819219) and check the releated [doc](./doc/stuff/烧写器使用说明文档.pdf)

Packaging, audio file conversion, midi and other general audio tools
-------------

* [download link](https://pan.baidu.com/s/1ajzBF4BFeiRFpDF558ER9w#list/path=%2F) code: `3jey`
  
SDK function
-------------
* Support decoding and playback of built-in FLASH
* Support decoding and playback of external FLASH
* Support decoding MIO function
* Support decoding and playback of three formats: .a/.b/.e, .f1a/.f1b/.f1c, mp3, wav, and ump3
* Support MIDI playback
* Support sydfs, norfs, free fs, and fatfs file systems;
* Support AD, IO and AD matrix buttons;
* Support hardware SRC resampling;
* Support ANS noise reduction, speed change, ECHO reverb, vo_pitch pitch shifting, voice_changer voice effects, PCM floating-point EQ and other sound effect algorithms;
* Support at least three-way simultaneous audio decoding and playback of .a/.b/.e + .f1a/.f1b/.f1c + .f1a/.f1b/.f1c formats.
* DAC supports differential and single-ended output, and supports external amplifier
* Class-D amplifier (APA) Output, optional 8K, 11.025k, 12k, 16k, 22.05k, 24k, 32K, 44.1k, 48K, 64k, 88.2k, 96k sampling rate;
* Support analog DAC output, optional 8K, 11.025k, 12k, 16k, 22.05k, 24k, 32k, 44.1k, 48k, 64k, 88.2k, 96k sampling rate; (some chip packages do not have DAC pins and cannot use this function)
* Support 10-bit SARADC driver;
* Support vm power-off memory function;
* Support key prompt sound;
* Support system FLASH hardware write protection;
* Support soft shutdown Softoff, power consumption 2uA+;
* Supports wake-up in place and low power consumption Powerdown, power consumption 35uA+;
  
MCU information
-------------
* 32bit RISC / 288MHz /152K+32K
* flash 
* 16bit audio adc
* 16bit audio dac
* 16bit Class-D Speaker Driver
* 1 x Full speed USB
* 1 x SD host controller

Community
--------------

* [Dingtalk Group](./doc/stuff/dingtalk.jpg)

Disclaimer
------------

AD23N_SDK supports development with AD23 series devices.
AD23 Series devices (which are pre-production) and Toy applications are supported for development in Release for production and deployment in end products.
