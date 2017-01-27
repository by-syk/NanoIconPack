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

package com.by_syk.nanoiconpack.bean;

import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by By_syk on 2017-01-27.
 */

public class IconBean implements Serializable {
    private int id = 0;
    private @NonNull String name = "";
    private String label;

    public IconBean(int id, String name) {
        setId(id);
        setName(name);
    }

    public IconBean(int id, String name, String label) {
        this(id, name);
        setLabel(label);
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        if (name != null) {
            this.name = name;
        }
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public @NonNull String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }
}
