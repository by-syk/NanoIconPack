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

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.by_syk.lib.nanoiconpack.R;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by By_syk on 2017-03-04.
 */

public class AppFilterReader {
    private static AppFilterReader reader;

    @NonNull
    private List<Bean> dataList = new ArrayList<>();

    private AppFilterReader() {}

    public boolean isInited() {
        return !dataList.isEmpty();
    }

    public boolean init(Resources resources) {
        dataList.clear();

        if (resources == null) {
            return false;
        }

        XmlResourceParser parser = resources.getXml(R.xml.appfilter);
        try {
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG) {
                    if (!"item".equals(parser.getName())) {
                        event = parser.next();
                        continue;
                    }
                    Bean bean = new Bean();
                    bean.drawable = parser.getAttributeValue(null, "drawable");
                    if (TextUtils.isEmpty(bean.drawable)) {
                        event = parser.next();
                        continue;
                    }
                    if (bean.drawable.matches(".+?_\\d+")) {
                        bean.drawableNoSeq = bean.drawable.substring(0, bean.drawable.lastIndexOf('_'));
                    } else {
                        bean.drawableNoSeq = bean.drawable;
                    }
                    String component = parser.getAttributeValue(null, "component");
                    if (component == null) {
                        event = parser.next();
                        continue;
                    }
                    Matcher matcher = Pattern.compile("ComponentInfo\\{([^/]+?)/(.+?)\\}").matcher(component);
                    if (matcher.matches()) {
                        bean.pkg = matcher.group(1);
                        bean.launcher = matcher.group(2);
                    }
                    dataList.add(bean);
                }
                event = parser.next();
            }
            return isInited();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<Bean> findByDrawable(String drawable) {
        List<Bean> list = new ArrayList<>();
        if (!isInited() || TextUtils.isEmpty(drawable)) {
            return list;
        }

        String drawableNoSeq = drawable;
        if (drawable.matches(".+?_\\d+")) {
            drawableNoSeq = drawable.substring(0, drawable.lastIndexOf('_'));
        }

        for (Bean bean : dataList) {
            if (bean.drawableNoSeq.equals(drawableNoSeq)) {
                list.add(bean);
            }
        }

        return list;
    }

    public static AppFilterReader getInstance() {
        if (reader == null) {
            reader = new AppFilterReader();
        }

        return reader;
    }

    public class Bean {
        @Nullable
        public String pkg;

        @Nullable
        public String launcher;

        @NonNull
        public String drawable;

        @NonNull
        public String drawableNoSeq;
    }
}
