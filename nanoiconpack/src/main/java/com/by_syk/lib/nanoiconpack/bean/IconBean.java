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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.by_syk.lib.nanoiconpack.util.ExtraUtil;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by By_syk on 2017-01-27.
 */

public class IconBean implements Serializable, Comparable<IconBean> {
    private int id = 0;

    @NonNull
    private String name = "";

    // extra
    @NonNull
    private String nameNoSeq = "";

    @Nullable
    private String label;

    // extra
    private String labelPinyin;

    @NonNull
    private Set<Component> components = new HashSet<>();

    // extra
    // Mark that the icon is the default one recorded in appfilter.xml.
    // If true, the var "recorded" must be true.
    private boolean def = false;

    public IconBean(int id, String name) {
        setId(id);
        setName(name);
    }

    public IconBean(int id, String name, String label, String labelPinyin) {
        this(id, name);
        setLabel(label);
        setLabelPinyin(labelPinyin);
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(@Nullable String name) {
        if (name != null) {
            this.name = name;
            this.nameNoSeq = ExtraUtil.purifyIconName(name);
        }
    }

    public void setLabel(@Nullable String label) {
        this.label = label;
    }

    public void setLabelPinyin(String labelPinyin) {
        this.labelPinyin = labelPinyin;
    }

    public boolean addComponent(@Nullable String pkg, @Nullable String launcher) {
        if (pkg == null || launcher == null) {
            return false;
        }
        components.add(new Component(pkg, launcher));
        return true;
    }

    public void setDef(boolean def) {
        this.def = def;
    }

    public int getId() {
        return id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getNameNoSeq() {
        return nameNoSeq;
    }

    @Nullable
    public String getLabel() {
        return label;
    }

    public String getLabelPinyin() {
        return labelPinyin;
    }

    @NonNull
    public Set<Component> getComponents() {
        return components;
    }

    public boolean containsInstalledComponent() {
        for (IconBean.Component component : components) {
            if (component.isInstalled()) {
                return true;
            }
        }
        return false;
    }

    public boolean isRecorded() {
        return components.size() > 0;
    }

    public boolean isDef() {
        return def;
    }

    @Override
    public int compareTo(@NonNull IconBean bean) {
        return this.getLabelPinyin().compareTo(bean.getLabelPinyin());
    }

    public class Component implements Serializable {
        @NonNull
        private String pkg;

        @NonNull
        private String launcher;

        // extra
        @Nullable
        private String label;

        // extra
        // Mark that app of the icon is installed.
        private boolean installed = false;

        Component(@NonNull String pkg, @NonNull String launcher) {
            this.pkg = pkg;
            this.launcher = launcher;
        }

        public void setLabel(@Nullable String label) {
            this.label = label;
        }

        public void setInstalled(boolean installed) {
            this.installed = installed;
        }

        @NonNull
        public String getPkg() {
            return pkg;
        }

        @NonNull
        public String getLauncher() {
            return launcher;
        }

        @Nullable
        public String getLabel() {
            return label;
        }

        public boolean isInstalled() {
            return installed;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Component)) {
                return false;
            }
            Component c2 = (Component) obj;
            return pkg.equals(c2.getPkg()) && launcher.equals(c2.getLauncher());
        }

        @Override
        public int hashCode() {
            return (pkg + "/" + launcher).hashCode();
        }
    }
}
