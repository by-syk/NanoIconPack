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
import android.text.TextUtils;

import com.by_syk.lib.nanoiconpack.R;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by By_syk on 2017-03-04.
 */

public class AppfilterReader {
    private static AppfilterReader instance;

    @NonNull
    private List<Bean> dataList = new ArrayList<>();

    private static Pattern componentPattern = Pattern.compile("ComponentInfo\\{([^/]+?)/(.+?)\\}");

    private AppfilterReader(@NonNull Resources resources) {
        init(resources);
    }

    private boolean init(@NonNull Resources resources) {
        try {
            XmlResourceParser parser = resources.getXml(R.xml.appfilter);
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG) {
                    if (!"item".equals(parser.getName())) {
                        event = parser.next();
                        continue;
                    }
                    String drawable = parser.getAttributeValue(null, "drawable");
                    if (TextUtils.isEmpty(drawable)) {
                        event = parser.next();
                        continue;
                    }
                    String component = parser.getAttributeValue(null, "component");
                    if (TextUtils.isEmpty(component)) {
                        event = parser.next();
                        continue;
                    }
                    Matcher matcher = componentPattern.matcher(component);
                    if (!matcher.matches()) {
                        event = parser.next();
                        continue;
                    }
                    dataList.add(new Bean(matcher.group(1), matcher.group(2), drawable));
                }
                event = parser.next();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @NonNull
    public List<Bean> getDataList() {
        return dataList;
    }

    @NonNull
    public Set<String> getPkgSet() {
        Set<String> pkgSet = new HashSet<>(dataList.size());
        for (Bean bean : dataList) {
            pkgSet.add(bean.pkg);
        }
        return pkgSet;
    }

    @NonNull
    public Set<String> getComponentSet() {
        Set<String> pkgLauncherSet = new HashSet<>(dataList.size());
        for (Bean bean : dataList) {
            pkgLauncherSet.add(bean.pkg + "/" + bean.launcher);
        }
        return pkgLauncherSet;
    }

//    @NonNull
//    public List<Bean> findByDrawable(@Nullable String drawable) {
//        if (TextUtils.isEmpty(drawable)) {
//            return new ArrayList<>();
//        }
//
//        List<Bean> list = new ArrayList<>();
//        String drawableNoSeq = ExtraUtil.purifyIconName(drawable);
//        for (Bean bean : dataList) {
//            if (bean.drawableNoSeq.equals(drawableNoSeq)) {
//                list.add(bean);
//            }
//        }
//        return list;
//    }

    public static AppfilterReader getInstance(@NonNull Resources resources) {
        if (instance == null) {
            synchronized (AppfilterReader.class) {
                if (instance == null) {
                    instance = new AppfilterReader(resources);
                }
            }
        }

        return instance;
    }

    public class Bean {
        @NonNull
        private String pkg;

        @NonNull
        private String launcher;

        @NonNull
        private String drawable;

        // extra
        @NonNull
        private String drawableNoSeq;

        Bean(@NonNull String pkg, @NonNull String launcher, @NonNull String drawable) {
            this.pkg = pkg;
            this.launcher = launcher;
            this.drawable = drawable;
            this.drawableNoSeq = ExtraUtil.purifyIconName(drawable);
        }

        @NonNull
        public String getPkg() {
            return pkg;
        }

        @NonNull
        public String getLauncher() {
            return launcher;
        }

        @NonNull
        public String getDrawable() {
            return drawable;
        }

        @NonNull
        public String getDrawableNoSeq() {
            return drawableNoSeq;
        }
    }
}
