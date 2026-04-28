# Android-JL_Health
**中文**|[EN](https://github.com/Jieli-Tech/Android-JL_Health/blob/main/README-en.md)

<br/>

<div align="center">
	
![Android](https://img.shields.io/badge/Android-5.1+-blue.svg)
![Android Studio](https://img.shields.io/badge/Android_Studio-Latest-orange.svg)
![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)       

<strong style="font:24px;color:#000000">杰理健康SDK(Android)</strong>

**杰理健康SDK**是<strong style="color:#ee2233">珠海市杰理科技股份有限公司</strong>(以下简称“本公司”)开发，专门提供本公司蓝牙穿戴类产品的功能集成SDK。

</div>





## 快速开始

为了帮助开发者快速接入<strong style="color:red">杰理健康SDK</strong>，请开发前详细阅读:

- [杰理健康SDK(Android)开发文档(Android)](https://doc.zh-jieli.com/Apps/Android/health/zh-cn/master/index.html)
- [开发说明文档](./doc/)
- [核心库](./libs/)



## 开发资料介绍

```tex
 ├── apk   --- 测试APK
      ├── app  --- 宜动健康测试APK， 建议在应用商店下载
      ├── tool --- 手表测试工具, 用于测试手表功能
 ├── code  --- 参考Demo源码存放
      ├── app  --- 宜动健康开放源码
      ├── tool --- 手表测试工具开放源码
 ├── doc   --- 开发文档
      ├── 杰理健康SDK(Android)开发说明
      ├── 杰理OTA外接库(Android)开发文档
      ├── 杰理连接库(Android)开发文档
 ├── libs  --- 相关库
      ├── ALi
           ├── AliAgent-release-4.3.7-202408021708.aar         --- 支付宝激活库
      ├── JL
	       ├── ui
		       ├── jl_dialog_V1.3.0_10300-debug.aar            --- 杰理对话框样式
			   ├── jl_health_http_V1.4.0_10311-release.aar     --- 杰理健康服务器相关
			   ├── jl-component-lib_V1.4.0_10400-release.aar   --- 杰理工具类
	       ├── jldecryption_v0.4-release.aar                   --- 加密相关
	       ├── jl_bluetooth_connect_V2.0.0_10703-release.aar   --- 蓝牙连接相关
	       ├── jl_bt_ota_V1.11.0_11015-release.aar             --- 杰理OTA相关           
	       ├── jl_rcsp_V0.8.0_705-release.aar                  --- 基础协议相关
	       ├── JL_Watch_V1.14.0_11307-release.aar              --- 杰理健康相关
	       ├── BmpConvert_V1.6.0_10605-release.aar             --- 图像转换相关
		   ├── GifConvert_V1.3.0_42-release.aar                --- GIF转换相关
	       ├── jl_audio_decode_V2.1.0_20012-release.aar        --- Opus和Speex音频解码相关
 ├── Jieli_Health_SDK_Android_Releases.pdf                     --- 杰理健康SDK发布记录
 └── ReadMe.txt
```





## 版本说明



| 版本   | 日期       | 修改记录                                                     |
| ------ | ---------- | ------------------------------------------------------------ |
| 1.14.0 | 2026/01/30 | 1. 新增功能<br />1.1 增加 ``FIND MY`` 设备回连的支持<br />1.2 增加AC707N图像压缩算法支持<br />1.3 增加AC707N GIF格式支持<br />1.4 增加复用空间特殊升级路程支持<br />2. 优化功能<br />2.1 增加Android 15的兼容处理<br />3. 修复问题<br />3.1 修改大文件传输偶现失败的问题<br />3.2 修复已知的问题 |
| 1.13.1 | 2024/11/29 | 1. 新增功能<br/>1.1 增加AC707N图像转码算法支持<br/>1.2 增加图片验证码机制<br />2. 修复问题<br />2.1 修复抬腕亮屏导致闪退的问题<br />2.2 修复大文件传输中设备回复错误偏移导致闪退的问题 |
| 1.13.0 | 2024/03/15 | 1. 新增功能<br />1.1 增加4G 模块OTA功能<br />1.2 增加表盘拓展参数<br />1.3 完善AI表盘功能 |
| 1.12.0 | 2024/01/05 | 1. 新增功能<br />1.1 增加AI表盘功能<br />1.2 Nand Flash存储器信息拓展支持<br />1.3 增加大文件传输错误码<br />2. 修复问题<br />2.1 修复偶现读取文件失败的问题 |
| 1.11.0 | 2023/09/15 | 1. 新增功能<br />1.1 增加AI云服务功能                        |
| 1.10.0 | 2023/06/26 | 1. 新增功能<br />1.1 增加闹钟溢出数据处理<br />1.2 增加大文件传输设置文件名编码方式<br />1.3 增加x86，x86_64平台的支持<br />2. 修复问题<br />2.1 修复设备开关连续心率测试闪退的问题<br />2.2 修复unZipFolder的安全漏洞问题<br />2.3 修复Android 12+ 不强制要求位置权限 |
| 1.9.1  | 2023/03/28 | 1. 修复问题<br />1.1 修复拼包出错导致数据丢失问题            |
| 1.9.0  | 2023/03/17 | 1. 新增功能<br />1.1 增加取消大文件传输接口<br />2. 优化功能<br />2.1 统一错误吗<br />3. 修复问题<br />3.1 修复多国语言(阿拉伯语)导致闪退的问题<br />3.2 兼容 Android 13 BLE 广播格式 |
| 1.8.0  | 2022/11/17 | 1. 新增功能<br />1.1 增加设备设置适配功能<br />1.2 增加图像转换支持ARGB图像的处理<br />2. 修复问题<br />2.1 修复跌倒提醒-设置紧急联系人的数据异常问题<br />2.2 修复健康设置命令失败状态的处理<br />2.3 修复OTA过程中偶现其他命令的异常 |
| 1.7.5  | 2022/09/26 | 1. 优化功能<br />1.1 增加Android 12的兼容处理<br />2. 修复问题<br />2.1 修复音乐传输后刷新列表没找到文件<br />2.2 修复健康设置命令回复失败状态导致闪退的问题<br />2.3 修复双模同地址单备份OTA SPP方式升级异常的问题 |
| 1.7.1  | 2022/07/27 | 1. 修复问题<br />1.1 修复杰理蓝牙连接库在无过滤搜索设备的情况下会死循环<br />1.2 修复已知问题 |
| 1.7.0  | 2022/07/12 | 1. 新增功能<br />1.1 获取手表剩余空间大小和获取表盘文件大小的接口<br />2. 优化功能<br />2.1 兼容单备份OTA流程<br />3. 修复问题<br />3.1 抬腕亮屏类型错误<br />3.2 连续测量心率的下限值无效<br />3.3 小文件传输异常 |
| 1.6.7  | 2022/05/13 | 1. 新增功能<br />1.1 增加BLE切换SPP的连接策略<br />2. 修复问题<br />2.1 设备文件浏览可能出现重复列表的问题 |
| 1.6.5  | 2022/02/18 | 1. 增加功能<br />1.1 表盘操作：表盘文件浏览，插入表盘文件，删除表盘文件，插入表盘自定义背景等等<br />1.2 大文件传输：音乐文件传输等等<br />1.3 常用联系人：同步联系人<br />1.4 健康数据同步：心率，运动步数等<br />1.5 运动数据同步：运动信息同步<br />1.6 天气信息同步：同步天气情况<br />1.7 消息同步<br />1.8 健康设置：跌倒提醒、久坐提醒、心率测试等<br />1.9 设备文件浏览<br />1.10 闹钟功能：增加、删除、修改闹钟<br />1.11 设备查找功能 |



## 功能实现示例

### 宜动健康

1. 蓝牙连接实现，可以参考 ``com.jieli.healthaide.tool.bluetooth.BluetoothHelper``
2. 健康SDK实现，可以参考 ``com.jieli.healthaide.tool.watch.WatchManager``
3. OTA功能实现，可以参考 ``com.jieli.healthaide.tool.upgrade.OTAManager``
4. 4G模块OTA功能实现，合并到OTA功能实现了。可以参考  ``com.jieli.healthaide.ui.device.upgrade.UpgradeViewModel``



**测试功能可以参考<strong style="color:red">test包</strong>的单元测试示例**




### 手表测试工具
1. 蓝牙连接实现，可以参考  ``com.jieli.watchtesttool.tool.bluetooth.BluetoothHelper``
2. 健康SDK实现，可以参考 ``com.jieli.watchtesttool.tool.watch.WatchManager``
3. OTA功能实现，可以参考 ``com.jieli.watchtesttool.tool.upgrade.OTAManager``
4. 图像转换功能，可以参考<strong style="color:red">test包</strong>的 ``com.jieli.watchtesttool.BmpConvertDemo``
5. 自定义命令功能，可以参考<strong style="color:red">test包</strong>的 ``com.jieli.watchtesttool.CustomCommandDemo``





## 技术支持

- 🌐 **官方网站**: [杰理科技](https://www.zh-jieli.com/)
- 📧 **技术支持**: 请通过官方渠道联系





## 许可证

本项目采用 [Apache License 2.0](./LICENSE) 开源协议。

```
Copyright 2024 珠海市杰理科技股份有限公司

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

**© 2024 珠海市杰理科技股份有限公司 | Licensed under Apache License 2.0**

</div>
