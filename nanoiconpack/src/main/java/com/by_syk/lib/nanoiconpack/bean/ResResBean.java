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
 * Created by By_syk on 2017-02-24.
 */

public class ResResBean<T> {
    // Make sure Gson works well after Proguard:
    // + Using @SerializedName()
    // + -keep class com.by_syk.lib.nanoiconpack.bean.ResResBean { private *; }
    @SerializedName("status")
    private int status = -1;

    @SerializedName("msg")
    private String msg;

    @SerializedName("result")
    private T result;

    public static final int STATUS_SUCCESS = 0;
    public static final int STATUS_EXISTED = 4;
    public static final int STATUS_NO_SUCH = 5;

    public int getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public T getResult() {
        return result;
    }

    public boolean isStatusSuccess() {
        return status == STATUS_SUCCESS;
    }
}
