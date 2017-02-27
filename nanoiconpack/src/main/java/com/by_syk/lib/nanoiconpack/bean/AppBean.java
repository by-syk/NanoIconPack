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

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;

/**
 * Created by By_syk on 2017-01-27.
 */

public class AppBean implements Serializable {
    @Nullable
    private Drawable icon;

    @Nullable
    private String iconUrl;

    @NonNull
    private String label = "";

    @NonNull
    private String labelPinyin = "";

    @NonNull
    private String pkgName = "";

    @NonNull
    private String launcherActivity = "";

    private int reqTimes = -1;

    private boolean mark = false;

    public AppBean() {}

    public AppBean(Drawable icon, String label, String labelPinyin, String pkgName, String launcherActivity) {
        setIcon(icon);
        setLabel(label);
        setLabelPinyin(labelPinyin);
        setPkgName(pkgName);
        setLauncherActivity(launcherActivity);
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public void setIconUrl(@NonNull String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public void setLabel(String label) {
        if (label != null) {
            this.label = label;
        }
    }

    public void setLabelPinyin(String labelPinyin) {
        if (labelPinyin != null) {
            this.labelPinyin = labelPinyin;
        }
    }

    public void setPkgName(String pkgName) {
        if (pkgName != null) {
            this.pkgName = pkgName;
        }
    }

    public void setLauncherActivity(String launcherActivity) {
        if (launcherActivity != null) {
            this.launcherActivity = launcherActivity;
        }
    }

    public void setReqTimes(int reqTimes) {
        this.reqTimes = reqTimes;
    }

    public void setMark(boolean mark) {
        this.mark = mark;
    }

    @Nullable
    public Drawable getIcon() {
        return icon;
    }

    @Nullable
    public String getIconUrl() {
        return iconUrl;
    }

    @NonNull
    public String getLabel() {
        return label;
    }

    public String getLabelPinyin() {
        return labelPinyin;
    }

    @NonNull
    public String getPkgName() {
        return pkgName;
    }

    @NonNull
    public String getLauncherActivity() {
        return launcherActivity;
    }

    public int getReqTimes() {
        return reqTimes;
    }

    public boolean isMark() {
        return mark;
    }
}
