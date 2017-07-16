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

package com.by_syk.lib.nanoiconpack.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Created by By_syk on 2017-03-31.
 */

public class CodeBean {
    @SerializedName("label")
    private String appLabel;

    @SerializedName("labelEn")
    private String appLabelEn;

    @SerializedName("pkg")
    private String pkg;

    @SerializedName("launcher")
    private String launcherActivity;

    public String getAppLabel() {
        return appLabel;
    }

    public String getAppLabelEn() {
        return appLabelEn;
    }

    public String getPkg() {
        return pkg;
    }

    public String getLauncherActivity() {
        return launcherActivity;
    }
}
