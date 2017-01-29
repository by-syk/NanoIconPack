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
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.text.TextUtils;

import com.by_syk.lib.nanoiconpack.R;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by By_syk on 2017-01-23.
 */

public class ExtraUtil {
//    private static String readFile(InputStream inputStream, boolean keepNewLine) {
//        BufferedReader bufferedReader = null;
//        try {
//            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
//            StringBuilder stringBuilder = new StringBuilder();
//            String buffer;
//            while ((buffer = bufferedReader.readLine()) != null) {
//                stringBuilder.append(buffer);
//                if (keepNewLine) {
//                    stringBuilder.append("\n");
//                }
//            }
//            if (keepNewLine && stringBuilder.length() > 0) {
//                stringBuilder.setLength(stringBuilder.length() - 1);
//            }
//            return stringBuilder.toString();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (bufferedReader != null) {
//                try {
//                    bufferedReader.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        return null;
//    }

//    public static List<String> getAppFilterPkg(InputStream appFilterFileIs, String iconName) {
//        List<String> list = new ArrayList<>();
//        if (TextUtils.isEmpty(iconName)) {
//            return list;
//        }
//        String text = readFile(appFilterFileIs, false);
//        if (text == null) {
//            return list;
//        }
//
//        if (iconName.matches(".+?_\\d+")) {
//            iconName = iconName.substring(0, iconName.lastIndexOf('_'));
//        }
//
//        Pattern pattern = Pattern.compile("<item\\s+component=\"ComponentInfo\\{([^/]+?)/[^\\}]*?\\}\""
//                + "\\s+drawable=\"" + iconName + "(_\\d)?\"\\s*/>");
//        Matcher matcher = pattern.matcher(text);
//        while (matcher.find()) {
//            if (!list.contains(matcher.group(1))) {
//                list.add(matcher.group(1));
//            }
//        }
//        return list;
//    }

    public static List<String> getAppFilterPkg(Resources resources, String iconName) {
        List<String> list = new ArrayList<>();
        if (resources == null || TextUtils.isEmpty(iconName)) {
            return list;
        }

        if (iconName.matches(".+?_\\d+")) {
            iconName = iconName.substring(0, iconName.lastIndexOf('_'));
        }

        XmlResourceParser parser = resources.getXml(R.xml.appfilter);
        try {
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG) {
                    if ("item".equals(parser.getName())) {
                        String drawable = parser.getAttributeValue(null, "drawable");
                        if (drawable != null && drawable.matches(".+?_\\d+")) {
                            drawable = drawable.substring(0, drawable.lastIndexOf('_'));
                        }
                        if (iconName.equals(drawable)) {
                            String component = parser.getAttributeValue(null, "component");
                            if (component != null) {
                                Matcher matcher = Pattern.compile("ComponentInfo\\{([^/]+?)/.+?\\}")
                                        .matcher(component);
                                if (matcher.matches()) {
                                    list.add(matcher.group(1));
                                }
                            }
                        }
                    }
                }
                event = parser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static boolean isPkgInstalled(Context context, String pkgName) {
        if (context == null || pkgName == null) {
            return false;
        }

        try {
            context.getPackageManager().getPackageInfo(pkgName, 0);
            return true;
        } catch (Exception e) {
            //e.printStackTrace();
        }

        return false;
    }

    public static List<String> getInstalledPkgs(Context context) {
        List<String> pkgNameList = new ArrayList<>();
        if (context == null) {
            return pkgNameList;
        }

        try {
            List<PackageInfo> pkgList = context.getPackageManager().getInstalledPackages(0);
            if (pkgList != null) {
                for (PackageInfo packageInfo : pkgList) {
                    pkgNameList.add(packageInfo.packageName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pkgNameList;
    }

    public static boolean isFromLauncherPick(Intent intent) {
        if (intent == null) {
            return false;
        }

        String action = intent.getAction();
        return "com.novalauncher.THEME".equals(action) // Nova
                || "org.adw.launcher.icons.ACTION_PICK_ICON".equals(action) // ADW
                /*|| "com.phonemetra.turbo.launcher.icons.ACTION_PICK_ICON".equals(action) // Turbo
                || Intent.ACTION_PICK.equals(action)
                || Intent.ACTION_GET_CONTENT.equals(action)*/;
    }
}
