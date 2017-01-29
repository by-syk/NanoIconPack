# NanoIconPack 图标包模板


NanoIconPack 是一个简单轻量的图标包模板，支持
- 主页展示所有图标，按目标APP名（或图标名）排序
- 过滤展示已安装图标
- 图标详情对话框，显示目标APP名（或图标名）、大图标
- 图标栅格
- 新原图标对比显示
- 直接应用到启动器、从支持的启动器设置中应用

相关截图（更多截图去[这里](art/)查看）：

![Nano](art/screenshots_nano.png)

您可以基于此进行二次开发，只需要装配图标、修改少量文件即可用于发布。

PS：NanoIconPack 只支持图标，如果您还有更多需求，比如壁纸、字体等，可以参考 [Jahir](https://github.com/jahirfiquitiva) 开发并开源的图标包模板——[IconShowcase-Dashboard](https://github.com/jahirfiquitiva/IconShowcase-Dashboard)。


### 支持启动器

目前支持以下4个启动器：

| Launcher | ICON |
| :---- | :----: |
| Nova Launcher | ![Nova](art/ic_launcher_nova.png) |
| Apex Launcher | ![Apex](art/ic_launcher_apex.png) |
| ADW Launcher | ![ADW](art/ic_launcher_adw.png) |
| Aviate | ![Aviate](art/ic_launcher_aviate.png) |


### 二次开发

- 导入配置项目

从 GitHub 导入 NanoIconPack 项目到 Android Studio，修改配置`build.gradle`：
```
android {
    defaultConfig {
        // Package name format: com.by_syk.nanoiconpack.[name_of_your_icon_pack]
        applicationId "com.by_syk.nanoiconpack.sample"
        versionCode 17012900
        // Version name format: [version].[num_of_icons]
        versionName "1.1.0.1"
    }
}
```

修改`/res/values/strings.xml`：
```
<string name="app_name">NanoIconPack Sample</string>

<!-- For Apex -->
<string name="developer_name">By_syk</string>

<string name="copyright_desc">"Icon pack author: [@By_syk](copy:@By_syk)
Donate via Alipay: [By_syk@163.com](copy:By_syk@163.com)
Copyright &#169; 2017 By_syk. All rights reserved."</string>
```

修改APP图标`/res/mipmap/ic_launcher.png`。

- 装配图标

图标统一缩放（`192*192`最佳）、规则命名后复制到`/res/drawable-nodpi/`文件夹。

参考命名规则：小写字母+数字+`_`；不能数字打头；重名照`_1`添加后缀。

以“日历”APP为例，命名`calendar.png`。

- 登记图标

`/res/values/icon_pack.xml`中添加：
```
<!-- File name (no suffix) list of all icons in /res/drawable-nodpi/ -->
<string-array name="icons">
    <item>calendar</item>
</string-array>
<!-- Corresponding app name list of all icons in /res/drawable-nodpi/ -->
<!-- Just KEEP IT EMPTY BUT DO NOT DELETE IT if you do not want to collect app name list. -->
<string-array name="icon_labels">
    <item>Calendar</item>
</string-array>
```

`/res/xml/drawable.xml`中添加：
```
<category title="All" />
<item drawable="calendar" />
```

`/res/xml/appfilter.xml`中添加：
```
<item
    component="ComponentInfo{com.google.android.calendar/com.android.calendar.AllInOneActivity}"
    drawable="calendar" />
```


### 基于 NanoIconPack 的 APP

下载 Sample APP：[NanoIconPack Sample](https://github.com/by-syk/NanoIconPack/raw/master/out/com.by_syk.nanoiconpack.sample_v1.1.0.1.nightly(17012901).apk)

| APP | ICON |
| :---- | :----: |
| @Childish / Anomaly Icon Pack | ![Anomaly](art/ic_launcher_anomaly.png) |
| @sftmi / OMFG图标包 | ![OMFG](art/ic_launcher_omfg.png) |

![Anomaly_OMFG](art/screenshots_anomaly_and_omfg.png)


### 参考

- [IconShowcase](https://github.com/jahirfiquitiva/IconShowcase)
- [[GUIDE] Apex Launcher Theme Tutorial](https://forum.xda-developers.com/showthread.php?t=1649891)


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