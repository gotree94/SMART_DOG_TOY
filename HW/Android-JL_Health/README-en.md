# Android-JL_Health

[中文](https://github.com/Jieli-Tech/Android-JL_Health/blob/main/README.md)|**EN**

<br/>

<div align="center">

![Android](https://img.shields.io/badge/Android-5.1+-blue.svg)
![Android Studio](https://img.shields.io/badge/Android_Studio-Latest-orange.svg)
![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)       

<strong style="font:24px;color:#000000">JieLi Health SDK (Android)</strong>

**JieLi Health SDK** is developed by <strong style="color:red"> Zhuhai Jieli Technology Co., Ltd.</strong> (hereinafter referred to as "our company"), specially providing a function integration SDK for our company's Bluetooth wearable products.

</div>





- ## **Quick Start**

  To help developers quickly integrate the <strong style="color:red">JieLi Health SDK</strong>, please read carefully before development:

  - [Jieli Health SDK (Android) Development Documentation (Android)](https://doc.zh-jieli.com/Apps/Android/health/en-us/master/index.html)
  - [Development Documentation](./doc/)
  - [Core Libraries](./libs/)



## Development Resources Introduction



```tex
 ├── apk   --- Test APKs
      ├── app  --- Yidong Health test APK, recommended to download from app store
      ├── tool --- Watch test tool, used for testing watch functions
 ├── code  --- Reference Demo source code storage
      ├── app  --- Yidong Health open source code
      ├── tool --- Watch test tool open source code
 ├── doc   --- Development documents
      ├── Jieli Health SDK (Android) Development Instructions
      ├── Jieli OTA External Library (Android) Development Document
      ├── Jieli Connection Library (Android) Development Document
 ├── libs  --- Related libraries
      ├── ALi
           ├── AliAgent-release-4.3.7-202408021708.aar         --- Alipay activation library
      ├── JL
	       ├── ui
		       ├── jl_dialog_V1.3.0_10300-debug.aar            --- Jieli dialog style
			   ├── jl_health_http_V1.4.0_10311-release.aar     --- Jieli health server related
			   ├── jl-component-lib_V1.4.0_10400-release.aar   --- Jieli utility classes
	       ├── jldecryption_v0.4-release.aar                   --- Encryption related
	       ├── jl_bluetooth_connect_V2.0.0_10703-release.aar   --- Bluetooth connection related
	       ├── jl_bt_ota_V1.11.0_11015-release.aar             --- Jieli OTA related           
	       ├── jl_rcsp_V0.8.0_705-release.aar                  --- Basic protocol related
	       ├── JL_Watch_V1.14.0_11307-release.aar              --- Jieli health related
	       ├── BmpConvert_V1.6.0_10605-release.aar             --- Image conversion related
		   ├── GifConvert_V1.3.0_42-release.aar                --- GIF conversion related
	       ├── jl_audio_decode_V2.1.0_20012-release.aar        --- Opus and Speex audio decoding related
 ├── Jieli_Health_SDK_Android_Releases.pdf                     --- Jieli Health SDK release records
 └── ReadMe.txt
```





## Version Information



| Version | Date       | Change Log                                                   |
| ------- | ---------- | ------------------------------------------------------------ |
| 1.14.0  | 2026/01/30 | 1. New Features<br />1.1 Added support for `FIND MY` device reconnection<br />1.2 Added AC707N image compression algorithm support<br />1.3 Added AC707N GIF format support<br />1.4 Added special upgrade process support for reusable space<br />2. Optimized Features<br />2.1 Added Android 15 compatibility processing<br />3. Bug Fixes<br />3.1 Fixed occasional large file transfer failures<br />3.2 Fixed known issues |
| 1.13.1  | 2024/11/29 | 1. New Features<br />1.1 Added AC707N image transcoding algorithm support<br />1.2 Added image verification code mechanism<br />2. Bug Fixes<br />2.1 Fixed wrist raise screen wake causing crashes<br />2.2 Fixed crashes caused by device reply error offset during large file transfer |
| 1.13.0  | 2024/03/15 | 1. New Features<br />1.1 Added 4G module OTA function<br />1.2 Added dial extension parameters<br />1.3 Improved AI dial function |
| 1.12.0  | 2024/01/05 | 1. New Features<br />1.1 Added AI dial function<br />1.2 Nand Flash memory information extension support<br />1.3 Added large file transfer error codes<br />2. Bug Fixes<br />2.1 Fixed occasional file reading failure issues |
| 1.11.0  | 2023/09/15 | 1. New Features<br />1.1 Added AI cloud service function     |
| 1.10.0  | 2023/06/26 | 1. New Features<br />1.1 Added alarm overflow data processing<br />1.2 Added large file transfer setting filename encoding method<br />1.3 Added x86, x86_64 platform support<br />2. Bug Fixes<br />2.1 Fixed crashes when switching continuous heart rate measurement on device<br />2.2 Fixed unZipFolder security vulnerability issue<br />2.3 Fixed Android 12+ not requiring location permission by default |
| 1.9.1   | 2023/03/28 | 1. Bug Fixes<br />1.1 Fixed packet concatenation errors causing data loss |
| 1.9.0   | 2023/03/17 | 1. New Features<br />1.1 Added cancel large file transfer interface<br />2. Optimized Features<br />2.1 Unified error codes<br />3. Bug Fixes<br />3.1 Fixed crashes caused by multilingual (Arabic) support<br />3.2 Compatible with Android 13 BLE broadcast format |
| 1.8.0   | 2022/11/17 | 1. New Features<br />1.1 Added device setting adaptation function<br />1.2 Added image conversion support for ARGB image processing<br />2. Bug Fixes<br />2.1 Fixed fall detection reminder - emergency contact data anomaly issue<br />2.2 Fixed health setting command failure status handling<br />2.3 Fixed occasional command anomalies during OTA process |
| 1.7.5   | 2022/09/26 | 1. Optimized Features<br />1.1 Added Android 12 compatibility processing<br />2. Bug Fixes<br />2.1 Fixed music transfer followed by refresh not finding files<br />2.2 Fixed crashes caused by health setting command reply failure status<br />2.3 Fixed dual-mode same address single backup OTA SPP upgrade anomalies |
| 1.7.1   | 2022/07/27 | 1. Bug Fixes<br />1.1 Fixed JieLi Bluetooth connection library infinite loop when searching devices without filtering<br />1.2 Fixed known issues |
| 1.7.0   | 2022/07/12 | 1. New Features<br />1.1 Get remaining watch space size and get dial file size interfaces<br />2. Optimized Features<br />2.1 Compatible with single backup OTA process<br />3. Bug Fixes<br />3.1 Wrist raise screen type error<br />3.2 Continuous heart rate measurement lower limit invalid<br />3.3 Small file transfer anomalies |
| 1.6.7   | 2022/05/13 | 1. New Features<br />1.1 Added BLE to SPP connection strategy<br />2. Bug Fixes<br />2.1 Device file browsing may show duplicate lists |
| 1.6.5   | 2022/02/18 | 1. Added Functions<br />1.1 Dial operations: dial file browsing, inserting dial files, deleting dial files, inserting custom dial backgrounds, etc.<br />1.2 Large file transfer: music file transfer, etc.<br />1.3 Common contacts: contact synchronization<br />1.4 Health data sync: heart rate, step count, etc.<br />1.5 Sports data sync: sports information sync<br />1.6 Weather info sync: weather condition sync<br />1.7 Message sync<br />1.8 Health settings: fall detection, sedentary reminders, heart rate testing, etc.<br />1.9 Device file browsing<br />1.10 Alarm function: add, delete, modify alarms<br />1.11 Device finder function |



## Function Implementation Examples

### WearLink

1. Bluetooth connection implementation, refer to `com.jieli.healthaide.tool.bluetooth.BluetoothHelper`
2. Health SDK implementation, refer to `com.jieli.healthaide.tool.watch.WatchManager`
3. OTA function implementation, refer to `com.jieli.healthaide.tool.upgrade.OTAManager`
4. 4G module OTA function implementation, integrated into OTA function. Refer to `com.jieli.healthaide.ui.device.upgrade.UpgradeViewModel`



**Test functions can refer to unit test examples in the <strong style="color:red">test package</strong>**



### WatchTestTool

1. Bluetooth connection implementation, refer to `com.jieli.watchtesttool.tool.bluetooth.BluetoothHelper`
2. Health SDK implementation, refer to `com.jieli.watchtesttool.tool.watch.WatchManager`
3. OTA function implementation, refer to `com.jieli.watchtesttool.tool.upgrade.OTAManager`
4. Image conversion function, refer to test package `com.jieli.watchtesttool.BmpConvertDemo`
5. Custom command function, refer to test package `com.jieli.watchtesttool.CustomCommandDemo`



## Technical Support

- 🌐 **Official Website**: [Jieli Technology](https://www.zh-jieli.com/)
- 📧 **Technical Support**: Please contact through official channels





## License

This project uses the [Apache License 2.0](./LICENSE) open source license.

```
Copyright 2024 Zhuhai JieLi Technology Co., Ltd.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

<div align="center">

**© 2024 Zhuhai Jieli Technology Co., Ltd. | Licensed under Apache License 2.0**

</div>
