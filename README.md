# NanoIconPack 图标包APP模板

[![Developing](https://img.shields.io/badge/Developing-v1.9.0-green.svg)](art/CHANGELOG.txt)
[![Release](https://img.shields.io/badge/Release-v1.4.0-brightgreen.svg)](https://github.com/by-syk/NanoIconPack/releases/tag/1.4.0)
[![Download](https://img.shields.io/badge/Download-Sample%20APP-brightgreen.svg)](https://github.com/by-syk/NanoIconPack/raw/master/out/com.by_syk.nanoiconpack.sample_v1.9.0.6.nightly(17030100).apk)
[![License](https://img.shields.io/badge/License-Apache%202.0-yellowgreen.svg)](https://github.com/by-syk/NanoIconPack/blob/master/LICENSE)

![NanoIconPack](art/ic_launcher_nanoiconpack.png)


NanoIconPack 是一个简单轻量的图标包APP模板，支持
+ 展示所有图标，按目标APP名（或图标名）拼音排序
+ 过滤展示已适配图标
+ 列出未适配APP，可快速申请适配图标、快速获取APP代码
+ 图标详情对话框，显示目标APP名（或图标名）、高清图标
+ 图标栅格
+ 新原图标对比显示
+ 快速保存图标
+ 直接应用到启动器、从支持的启动器设置中应用
+ 隐藏式控制台，查看申请适配统计（由服务器提供支持）

相关截图（更多截图去[这里](art/SCREENSHOTS.md)查看）：

[![NanoIconPack](art/screenshots_nano.png)](art/SCREENSHOTS.md)

您可以基于此进行二次开发，只需要装配图标、修改少量文件即可用于发布。


### 支持启动器

目前核心支持以下3个元老级的启动器：

| Launcher | ICON |
| :---- | :----: |
| Nova Launcher | [![Nova](art/ic_launcher_nova.png)](http://www.coolapk.com/apk/com.teslacoilsw.launcher) |
| Apex Launcher | [![Apex](art/ic_launcher_apex.png)](http://www.coolapk.com/apk/com.anddoes.launcher) |
| ADW Launcher | [![ADW](art/ic_launcher_adw.png)](http://www.coolapk.com/apk/org.adw.launcher) |

这三个应该是启动器界的元老了（未考究），很多后来启动器沿用或支持它们的图标包规范。

NanoIconPack 同时还支持许多未列出的启动器，比如
+ Smart Launcher Pro
+ Action 3
+ Aviate
+ Holo Launcher
+ Arrow桌面
+ S桌面
+ Hola桌面
+ Go桌面
+ 等……

以及一些系统默认启动器，比如
+ Xperia Home Launcher
+ 氢桌面
+ 等……

> 已知不支持并不打算支持：
> + ~~TSF桌面~~
> + ~~Atom桌面~~


### 二次开发

去 [:book: Wiki](https://github.com/by-syk/NanoIconPack/wiki/%E4%BA%8C%E6%AC%A1%E5%BC%80%E5%8F%91%E6%AD%A5%E9%AA%A4) 页面查看二次开发步骤。


### 服务器支持

NanoIconPack 拥有一个轻量的服务器，提供图标申请和申请统计两大服务。

服务器由神奇的 **[Nodejs](https://nodejs.org/en/)** 驱动，够简单、够轻量。代码已经同项目一并开源（[去查看](https://github.com/by-syk/NanoIconPack/tree/master/server/nodejs)），如果您有兴趣，欢迎和我一起改进。

目前服务器由我维护，如果您正在基于 NanoIconPack 开发自己的图标包，可以免费接入它。或者，您也可以利用开源的代码自己搭建服务器。

> 为什么需要服务器？
>
> 在图标申请这个功能上，当前已有的图标包模版主要采用的是邮件方式，不过这并不友好，不仅用户需要多步操作，而且作者对收到的申请邮件也难于进一步统计。所以我们决定借助服务器的支持来简化这一过程，使用户一步申请图标、作者直接看到申请统计结果。
>
> 这种方案最初见于 [@sorcerer](http://www.coolapk.com/u/420016) 的 **[Sorcery 图标](http://www.coolapk.com/apk/com.sorcerer.sorcery.iconpack)**（未考究）。NanoIconPack 只是重走了 **Sorcery 图标** 的路。


### 基于 NanoIconPack 的 APP

| APP | ICON |
| :---- | :----: |
| [@Childish / Anormaly <sub>old version</sub>](http://www.coolapk.com/apk/com.childish.cooldog) | [![Anormaly](art/ic_launcher_anormaly.png)](http://www.coolapk.com/apk/com.childish.cooldog) |
| [@ArchieLiu / 左下图标包 <sub>old version</sub>](http://www.coolapk.com/apk/com.zuoxia.iconpack) | [![LeftBlow](art/ic_launcher_left_below.png)](http://www.coolapk.com/apk/com.zuoxia.iconpack) |
| [@pandecheng / PDC图标包 <sub>new version</sub>](http://www.coolapk.com/apk/com.pandecheng.iconpack) | [![PDC](art/ic_launcher_pdc.png)](http://www.coolapk.com/apk/com.pandecheng.iconpack) |
| [@sftmi / OMFG图标包](http://www.coolapk.com/apk/com.sftmi.iconpack.omfg) | [![OMFG](art/ic_launcher_omfg.png)](http://www.coolapk.com/apk/com.sftmi.iconpack.omfg) |
| [@大神sjk / Smalite图标包](http://www.coolapk.com/apk/com.sjk.smaliteiconpack) | [![Smalite](art/ic_launcher_smalite.png)](http://www.coolapk.com/apk/com.sjk.smaliteiconpack) |
| [@派大鑫 / Party Star](http://www.coolapk.com/apk/com.paidax.iconpack.partystar) | [![Smalite](art/ic_launcher_party_star.png)](http://www.coolapk.com/apk/com.paidax.iconpack.partystar) |
| [@可以and不行 / Pelmix图标包](http://www.coolapk.com/apk/com.edward.iconpack.pelmix) | [![Pelmix](art/ic_launcher_pelmix.png)](http://www.coolapk.com/apk/com.edward.iconpack.pelmix) |
| [@Markuss / Aeroblast图标包](http://www.coolapk.com/apk/com.markusslugia.iconpack.aeroblast) | [![Aeroblast](art/ic_launcher_aeroblast.png)](http://www.coolapk.com/apk/com.markusslugia.iconpack.aeroblast) |

如果您基于 NanoIconPack 开发出了自己的图标包作品，请不吝[知会我](#联系开发者)，以便展示在这里。


### 使用开源项目和参考资料

NanoIconPack 使用了如下开源项目，致敬开源：
+ [Li Min / pinyin4j](https://sourceforge.net/projects/pinyin4j/) licensed under [GPLv2](https://www.gnu.org/licenses/old-licenses/gpl-2.0.html)
+ [fython / AlipayZeroSdk](https://github.com/fython/AlipayZeroSdk) licensed under [Apache-2.0](http://www.apache.org/licenses/LICENSE-2.0)
+ [kenglxn / QRGen](https://github.com/kenglxn/QRGen) licensed under [Apache-2.0](http://www.apache.org/licenses/LICENSE-2.0)
+ [square / retrofit](https://github.com/square/retrofit) licensed under [Apache-2.0](http://www.apache.org/licenses/LICENSE-2.0)
+ [bumptech / glide](https://github.com/bumptech/glide) licensed under [BSD, part MIT and Apache-2.0](https://github.com/bumptech/glide/blob/master/LICENSE)
+ [timusus / RecyclerView-FastScroll](https://github.com/timusus/RecyclerView-FastScroll) licensed under [Apache-2.0](http://www.apache.org/licenses/LICENSE-2.0)

参考了如下资料：
+ [teslacoil / Example_NovaTheme](https://github.com/teslacoil/Example_NovaTheme)
+ [[GUIDE] Apex Launcher Theme Tutorial](https://forum.xda-developers.com/showthread.php?t=1649891)
+ [IconShowcase](https://github.com/jahirfiquitiva/IconShowcase)
+ [bjzhou / Coolapk](https://github.com/bjzhou/Coolapk)


### 更好的图标包模板

NanoIconPack 追求简单轻量，只支持图标，如果您有更多需求，比如还需要支持壁纸、字体，还需要支持更多启动器，还需要更漂亮的 UI 等，可以参考以下开源项目：

| Dashboard | ICON |
| :---- | :----: |
| [jahirfiquitiva / IconShowcase-Dashboard](https://github.com/jahirfiquitiva/IconShowcase-Dashboard) | ![IconShowcase](art/ic_launcher_iconshowcase.png) |
| [afollestad / polar-dashboard](https://github.com/afollestad/polar-dashboard) | ![Polar](art/ic_launcher_polar.png) |
| [danimahardhika / candybar-library](https://github.com/danimahardhika/candybar-library) | ![CandyBar](art/ic_launcher_candybar.png) |


### 联系开发者

+ E-mail: [By_syk@163.com](mailto:By_syk@163.com "By_syk")
+ 酷安主页：[@By_syk](http://www.coolapk.com/u/463675)


### License

    Copyright 2017 By_syk

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


*Copyright &#169; 2017 By_syk. All rights reserved.*