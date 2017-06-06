/*
 * Copyright 2017 By_syk
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.by_syk.lib.nanoiconpack.util;

import android.os.Build;

/**
 * Created by By_syk on 2016-07-16.
 */

public class C {
    public static final int SDK = Build.VERSION.SDK_INT;

    public static final String LOG_TAG = "NANO_ICON_PACK";

    public static final String APP_CODE_COMPONENT = "<item component=\"ComponentInfo{%1$s/%2$s}\" drawable=\"%3$s\" />";
    public static final String APP_CODE_LABEL = "<!-- %1$s / %2$s -->";
    public static final String APP_CODE_BUILD = "<!-- Build: %1$s / %2$s -->";

    public static final String URL_NANO_SERVER = "http://by-syk.com:8081/nanoiconpack/";
//    public static final String URL_NANO_SERVER = "http://192.168.43.76:8082/nanoiconpack/";
    public static final String URL_COOLAPK_API = "https://api.coolapk.com/v6/";

//    public static final String REQ_REDRAW_PREFIX = "\uD83C\uDE38 ";
//    public static final String REQ_REDRAW_PREFIX = "\uD83D\uDE4F ";
//    public static final String REQ_REDRAW_PREFIX = "\uD83D\uDCCE ";
//    public static final String REQ_REDRAW_PREFIX = "\uD83D\uDC65 ";
    public static final String REQ_REDRAW_PREFIX = "\uD83D\uDC64 ";
//    public static final String ICON_ONE_SUFFIX = " ◎";
    public static final String ICON_ONE_SUFFIX = " ·";
}
