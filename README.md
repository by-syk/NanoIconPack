# NanoIconPack / Nano图标包


Nano图标包是一个简单轻量的图标包模板，支持展示所有图标、过滤已安装图标、新原图标对比等功能。

![Nano](art/screenshot_nano_3.png)

您可以基于此进行二次开发，只需要装配图标、修改少量文件即可用于发布。

您还可以参考 [Jahir](https://github.com/jahirfiquitiva) 开发的更完备的图标包模板——[IconShowcase-Dashboard](https://github.com/jahirfiquitiva/IconShowcase-Dashboard)。


### 支持启动器

目前支持活跃的三个启动器：

| 启动器 | 图标 |
| :---- | :----: |
| Nova Launcher | ![Nova](art/ic_launcher_nova.png) |
| Apex Launcher | ![Apex](art/ic_launcher_apex.png) |
| Aviate | ![Aviate](art/ic_launcher_aviate.png) |

图标包应用到启动器、从启动器应用图标包均可。


### 二次开发

- 导入配置项目

从 GitHub 导入 NanoIconPack 项目到 Android Studio，修改包名（可在原包名`com.by_syk.nanoiconpack`后追加`.[author_of_icons]`）：
```
android {
    defaultConfig {
        applicationId "com.by_syk.nanoiconpack.[author_of_icons]"
    }
}
```

修改`/res/values/strings.xml`：
```
<string name="app_name">Nano Icon Pack</string>
<string name="copyright_desc">"Icon pack author: [@By_syk](copy:@By_syk)
Copyright &#169; 2017 By_syk. All rights reserved.
\nApp is based on Nano Icon Pack developed by [@By_syk](copy:@By_syk).
[https://github.com/by-syk/NanoIconPack](https://github.com/by-syk/NanoIconPack)
Copyright &#169; 2017 By_syk. All rights reserved."</string>
```

修改`/res/values/apex_config.xml`：
```
<string name="developer_name">By_syk</string>
```

修改APP图标`/res/mipmap/ic_launcher.png`。

- 装配图标

图标规则命名后复制到`/res/drawable-nodpi/`文件夹。

参考命名规则：小写字母+数字+`_`；不能数字打头；重名照`_1`添加后缀。

以“联系人”APP为例，命名`contacts.png`。

- 登记图标

`/res/values/icon_pack.xml`中添加：
```
<!-- File name (no suffix) list of all icons in /res/drawable-nodpi/ -->
<string-array name="icons">
    <item>contacts</item>
</string-array>
<!-- Corresponding app name list of all icons in /res/drawable-nodpi/ -->
<string-array name="icon_labels">
    <item>Contacts</item>
</string-array>
```

`/res/xml/drawable.xml`中添加：
```
<category title="All" />
<item drawable="contacts" />
```

`/res/xml/appfilter.xml`中添加：
```
<item
   component="ComponentInfo{com.android.contacts/com.android.contacts.activities.PeopleActivity}"
   drawable="contacts" />
```


### 基于NanoIconPack的APP

| APP | 图标 |
| :---- | :----: |
| Anomaly Icon Pack | ![Anomaly](art/ic_launcher_anomaly.png) |
| OMFG图标包 | ![OMFG](art/ic_launcher_omfg.png) |

![Anomaly](art/screenshot_anomaly_omfg.png)


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