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
import android.content.res.Resources;
import android.support.annotation.NonNull;

import com.by_syk.lib.nanoiconpack.R;
import com.by_syk.lib.nanoiconpack.bean.IconBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by By_syk on 2017-03-26.
 */

public abstract class IconsGetter implements Serializable {
    public abstract List<IconBean> getIcons(@NonNull Context context) throws Exception;

    protected List<IconBean> getAllIcons(@NonNull Context context) throws Exception {
        Resources resources = context.getResources();
        String[] names = resources.getStringArray(R.array.icons);
        String[] labels = resources.getStringArray(R.array.icon_labels);
        String[] labelPinyins;
        if (labels.length > 0) {
            labelPinyins = ExtraUtil.getPinyinForSorting(labels);
        } else { // No app name list provided, use icon name list instead.
            labels = new String[names.length];
            for (int i = 0, len = names.length; i < len; ++i) {
                labels[i] = names[i].replaceAll("_", " ");
            }
            labelPinyins = Arrays.copyOf(labels, labels.length);
        }

        Pattern pattern = Pattern.compile("(?<=\\D|^)\\d(?=\\D|$)");
        for (int i = 0, len = labelPinyins.length; i < len; ++i) { // 优化100以内数值逻辑排序
            Matcher matcher = pattern.matcher(labelPinyins[i]);
            if (matcher.find()) {
                labelPinyins[i] = matcher.replaceAll("0" + matcher.group(0));
            }
        }

        List<IconBean> dataList = new ArrayList<>();
        for (int i = 0, len = names.length; i < len; ++i) {
            int id = resources.getIdentifier(names[i], "drawable",
                    context.getPackageName());
            dataList.add(new IconBean(id, names[i], labels[i], labelPinyins[i]));
        }
        Collections.sort(dataList, new Comparator<IconBean>() {
            @Override
            public int compare(IconBean bean1, IconBean bean2) {
//                return bean1.getName().compareTo(bean2.getName());
//                return bean1.getLabel().compareTo(bean2.getLabel());
                return bean1.getLabelPinyin().compareTo(bean2.getLabelPinyin());
            }
        });

        return dataList;
    }
}
