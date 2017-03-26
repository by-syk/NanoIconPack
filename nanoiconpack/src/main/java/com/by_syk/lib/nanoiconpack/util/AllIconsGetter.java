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

import android.content.Context;
import android.support.annotation.NonNull;

import com.by_syk.lib.nanoiconpack.bean.IconBean;

import java.io.Serializable;
import java.util.List;

/**
 * Created by By_syk on 2017-03-26.
 */

public class AllIconsGetter extends IconsGetter implements Serializable {
    @Override
    public List<IconBean> getIcons(@NonNull Context context) throws Exception {
        return getAllIcons(context);
    }
}
