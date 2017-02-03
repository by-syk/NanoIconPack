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

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.by_syk.lib.nanoiconpack.R;
import com.by_syk.lib.nanoiconpack.bean.AppBean;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
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

    public static List<String> getInstalledPkgsWithLauncherActivity(Context context) {
        List<String> pkgNameList = new ArrayList<>();
        if (context == null) {
            return pkgNameList;
        }

        try {
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(mainIntent, 0);
            for (ResolveInfo resolveInfo : list) {
                pkgNameList.add(resolveInfo.activityInfo.packageName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pkgNameList;
    }

    public static String getLauncherActivity(Context context, String pkgName) {
        try {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(pkgName);
            if (intent != null) {
                return intent.getComponent().getClassName();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean isFromLauncherPick(Intent intent) {
        if (intent == null) {
            return false;
        }

        String action = intent.getAction();
        return "com.novalauncher.THEME".equals(action) // Nova
                // Apex: No such funtion
                || "org.adw.launcher.icons.ACTION_PICK_ICON".equals(action) // ADW
                // Aviate: No such funtion
                /*|| "com.phonemetra.turbo.launcher.icons.ACTION_PICK_ICON".equals(action) // Turbo
                || Intent.ACTION_PICK.equals(action)
                || Intent.ACTION_GET_CONTENT.equals(action)*/;
    }

    /**
     * 汉字串转拼音
     * 保留非汉字；打头的第一个汉字若是多音字则按取拼音首字母不同的几个，其他只取一个音
     *
     * 设置 -> [shezhi]
     * Google设置 -> [googleshezhi]
     * 调色板 -> [diaoseban, tiaoseban]
     * 相机 -> [xiangji]
     *
     * @param text
     * @return
     */
    @NonNull
    public static String[] getPinyinForSorting(String text) {
        if (TextUtils.isEmpty(text)) {
            return new String[]{""};
        }
        text = text.toLowerCase();

        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        /*
         * WITHOUT_TONE：无音标 （zhong）
         * WITH_TONE_NUMBER：1-4数字表示英标 （zhong4）
         * WITH_TONE_MARK：直接用音标符（必须WITH_U_UNICODE否则异常） （zhòng）
         */
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        /*
         * WITH_V：用v表示ü （nv）
         * WITH_U_AND_COLON：用"u:"表示ü （nu:）
         * WITH_U_UNICODE：直接用ü （nü）
         */
        format.setVCharType(HanyuPinyinVCharType.WITH_V);

        List<String> resultList = new ArrayList<>();
        try {
            char[] chArr = text.toCharArray();
            String[] pyArr = PinyinHelper.toHanyuPinyinStringArray(chArr[0], format);
            if (pyArr != null) {
                resultList.addAll(Arrays.asList(pyArr));
                Collections.sort(resultList);
                for (int i = 1; i < resultList.size(); ++i) {
                    if (resultList.get(i).charAt(0) == resultList.get(i - 1).charAt(0)) {
                        resultList.remove(i);
                        --i;
                    }
                }
            } else {
                resultList.add(String.valueOf(chArr[0]));
            }
            for (int i = 1, len = chArr.length; i < len; ++i) {
                pyArr = PinyinHelper.toHanyuPinyinStringArray(chArr[i], format);
                String append;
                if (pyArr != null) {
                    // 仅选取多音字的第一个音
                    append = pyArr[0];
                } else {
                    append = String.valueOf(chArr[i]);
                }
                for (int j = 0, len1 = resultList.size(); j < len1; ++j) {
                    resultList.set(j, resultList.get(j) + append);
                }
            }
            return resultList.toArray(new String[resultList.size()]);
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }

        return new String[]{text};
    }

    @NonNull
    public static String[] getPinyinForSorting(String[] textArr) {
        if (textArr == null) {
            return new String[0];
        }

        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        /*
         * WITHOUT_TONE：无音标 （zhong）
         * WITH_TONE_NUMBER：1-4数字表示英标 （zhong4）
         * WITH_TONE_MARK：直接用音标符（必须WITH_U_UNICODE否则异常） （zhòng）
         */
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        /*
         * WITH_V：用v表示ü （nv）
         * WITH_U_AND_COLON：用"u:"表示ü （nu:）
         * WITH_U_UNICODE：直接用ü （nü）
         */
        format.setVCharType(HanyuPinyinVCharType.WITH_V);

        try {
            String[] resultArr = new String[textArr.length];
            for (int i = 0, len = textArr.length; i < len; ++i) {
                String result = "";
                for (char ch : textArr[i].toLowerCase().toCharArray()) {
                    String[] pyArr = PinyinHelper.toHanyuPinyinStringArray(ch, format);
                    if (pyArr != null) {
                        // 仅选取多音字的第一个音
                        result += pyArr[0];
                    } else {
                        result += ch;
                    }
                }
                resultArr[i] = result;
            }
            return resultArr;
        } catch (BadHanyuPinyinOutputFormatCombination e) {
            e.printStackTrace();
        }

        return textArr;
    }

    /**
     * 复制文本到剪切板
     *
     * @param context
     * @param text
     */
    @TargetApi(11)
    public static void copy2Clipboard(Context context, String text) {
        if (context == null || text == null) {
            return;
        }

        if (C.SDK >= 11) {
            ClipboardManager clipboardManager = (ClipboardManager)
                    context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText(null, text);
            clipboardManager.setPrimaryClip(clipData);
        } else {
            android.text.ClipboardManager clipboardManager = (android.text.ClipboardManager)
                    context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.setText(text);
        }
    }

    public static String getAppLabelEn(Context context, AppBean appBean) {
        if (context == null || appBean == null) {
            return null;
        }

        String result = null;
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getPackageInfo(appBean.getPkgName(), 0)
                    .applicationInfo;

            Configuration configuration = new Configuration();
            // It's better, I think, to use Locale.ENGLISH
            // instead of Locale.ROOT (although I want to do).
            configuration.locale = Locale.ENGLISH;
            // The result is a value in disorder maybe if using:
            //     packageManager.getResourcesForApplication(PACKAGE_NAME)
            Resources resources = packageManager.getResourcesForApplication(applicationInfo);
            resources.updateConfiguration(configuration,
                    context.getResources().getDisplayMetrics());
            final int LABEL_RES = applicationInfo.labelRes;
            if (LABEL_RES != 0) {
                // If the localized label is not added, the default is returned.
                // NOTICE!!!If the default were empty, Resources$NotFoundException would be called.
                result = resources.getString(LABEL_RES);
            }

            /*
             * NOTICE!!!
             * We have to restore the locale.
             * On the one hand,
             * it will influence the label of Activity, etc..
             * On the other hand,
             * the got "resources" equals the one "this.getResources()" if the current .apk file
             * happens to be this APK Checker(com.by_syk.apkchecker).
             * We need to restore the locale, or the language of APK Checker will change to English.
             */
            configuration.locale = Locale.getDefault();
            resources.updateConfiguration(configuration,
                    context.getResources().getDisplayMetrics());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static boolean isSysApp(Context context, String pkgName) {
        if (context == null || TextUtils.isEmpty(pkgName)) {
            return false;
        }

        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getPackageInfo(pkgName, 0).applicationInfo;
            return (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @NonNull
    public static String appName2drawableName(String label, String labelEn) {
        if (!TextUtils.isEmpty(labelEn) && labelEn.matches("[A-Za-z\\d ]+")) {
            Matcher matcher = Pattern.compile("([a-z])([A-Z])").matcher(labelEn);
            if (matcher.find()) {
                labelEn = matcher.replaceAll(matcher.group(1) + " " + matcher.group(2));
            }
            return labelEn.replaceAll(" ", "_").toLowerCase();
        } else if (!TextUtils.isEmpty(label) && label.matches("[A-Za-z\\d ]+")) {
            Matcher matcher = Pattern.compile("([a-z])([A-Z])").matcher(label);
            if (matcher.find()) {
                label = matcher.replaceAll(matcher.group(1) + " " + matcher.group(2));
            }
            return label.replaceAll(" ", "_").toLowerCase();
        }
        return "";
    }
}
