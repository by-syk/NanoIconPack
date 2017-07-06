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

public class AppBean implements Serializable, Comparable<AppBean> {
    @Nullable
    private Drawable icon;

    @Nullable
    private String iconUrl;

    @NonNull
    private String label = "";

    // extra
    @NonNull
    private String labelPinyin = "";

    @NonNull
    private String pkg = "";

    @NonNull
    private String launcher = "";

    // extra
    private int reqTimes = -1;

    // extra
    private boolean mark = false;

    // extra
    // If true, it shows an app(pkg + launcher) is recorded in appfilter.xml but NOT marked
    private boolean hintMark = false;

    // extra
    // If true, it shows an app(pkg + launcher) is marked but NOT recorded in appfilter.xml
    private boolean hintUndoMark = false;

    // extra
    // If true, it shows the app's pkg is recorded in appfilter.xml but its launcher not
    private boolean hintLost = false;

    public AppBean() {}

    public AppBean(Drawable icon, String label, String labelPinyin, String pkg, String launcher) {
        setIcon(icon);
        setLabel(label);
        setLabelPinyin(labelPinyin);
        setPkg(pkg);
        setLauncher(launcher);
    }

    public void setIcon(@Nullable Drawable icon) {
        this.icon = icon;
    }

    public void setIconUrl(@Nullable String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public void setLabel(String label) {
        if (label != null) {
            this.label = label;
        }
    }

    public void setLabelPinyin(@Nullable String labelPinyin) {
        if (labelPinyin != null) {
            this.labelPinyin = labelPinyin;
        }
    }

    public void setPkg(@Nullable String pkg) {
        if (pkg != null) {
            this.pkg = pkg;
        }
    }

    public void setLauncher(@Nullable String launcher) {
        if (launcher != null) {
            this.launcher = launcher;
        }
    }

    public void setReqTimes(int reqTimes) {
        this.reqTimes = reqTimes;
    }

    public void setMark(boolean mark) {
        this.mark = mark;
    }

    public void setHintMark(boolean hintMark) {
        this.hintMark = hintMark;
    }

    public void setHintUndoMark(boolean hintUndoMark) {
        this.hintUndoMark = hintUndoMark;
    }

    public void setHintLost(boolean hintLost) {
        this.hintLost = hintLost;
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

    @NonNull
    public String getLabelPinyin() {
        return labelPinyin;
    }

    @NonNull
    public String getPkg() {
        return pkg;
    }

    @NonNull
    public String getLauncher() {
        return launcher;
    }

    public int getReqTimes() {
        return reqTimes;
    }

    public boolean isMark() {
        return mark;
    }

    public boolean isHintMark() {
        return hintMark;
    }

    public boolean isHintUndoMark() {
        return hintUndoMark;
    }

    public boolean isHintLost() {
        return hintLost;
    }

    @Override
    public int compareTo(@NonNull AppBean bean) {
        return this.getLabelPinyin().compareTo(bean.getLabelPinyin());
    }
}
