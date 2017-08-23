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
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;

import com.github.promeg.pinyinhelper.Pinyin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by By_syk on 2017-01-23.
 */

public class ExtraUtil {
    private static Pattern codePattern = Pattern.compile("([a-z][A-Z])|([A-Za-z]\\d)|(\\d[A-Za-z])");

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

    public static boolean isFromLauncherPick(Intent intent) {
        if (intent == null) {
            return false;
        }

        String action = intent.getAction();
        return "com.novalauncher.THEME".equals(action) // Nova
                // Apex: No such funtion
                || "org.adw.launcher.icons.ACTION_PICK_ICON".equals(action) // ADW
                /*|| "com.phonemetra.turbo.launcher.icons.ACTION_PICK_ICON".equals(action) // Turbo
                || Intent.ACTION_PICK.equals(action)
                || Intent.ACTION_GET_CONTENT.equals(action)*/;
    }

//    /**
//     * 汉字串转拼音
//     * 保留非汉字；打头的第一个汉字若是多音字则按取拼音首字母不同的几个，其他只取一个音
//     *
//     * 设置 -> [shezhi]
//     * Google设置 -> [googleshezhi]
//     * 调色板 -> [diaoseban, tiaoseban]
//     * 相机 -> [xiangji]
//     *
//     * @param text
//     * @return
//     */
//    @NonNull
//    public static String[] getPinyinForSorting(@Nullable String text) {
//        if (TextUtils.isEmpty(text)) {
//            return new String[]{""};
//        }
//        text = text.toLowerCase();
//
//        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
//        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
//        /*
//         * WITHOUT_TONE：无音标 （zhong）
//         * WITH_TONE_NUMBER：1-4数字表示英标 （zhong4）
//         * WITH_TONE_MARK：直接用音标符（必须WITH_U_UNICODE否则异常） （zhòng）
//         */
//        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
//        /*
//         * WITH_V：用v表示ü （nv）
//         * WITH_U_AND_COLON：用"u:"表示ü （nu:）
//         * WITH_U_UNICODE：直接用ü （nü）
//         */
//        format.setVCharType(HanyuPinyinVCharType.WITH_V);
//
//        List<String> resultList = new ArrayList<>();
//        try {
//            char[] chArr = text.toCharArray();
//            String[] pyArr = PinyinHelper.toHanyuPinyinStringArray(chArr[0], format);
//            if (pyArr != null) {
//                resultList.addAll(Arrays.asList(pyArr));
//                Collections.sort(resultList);
//                for (int i = 1; i < resultList.size(); ++i) {
//                    if (resultList.get(i).charAt(0) == resultList.get(i - 1).charAt(0)) {
//                        resultList.remove(i);
//                        --i;
//                    }
//                }
//            } else {
//                resultList.add(String.valueOf(chArr[0]));
//            }
//            for (int i = 1, len = chArr.length; i < len; ++i) {
//                pyArr = PinyinHelper.toHanyuPinyinStringArray(chArr[i], format);
//                String append;
//                if (pyArr != null) {
//                    // 仅选取多音字的第一个音
//                    append = pyArr[0];
//                } else {
//                    append = String.valueOf(chArr[i]);
//                }
//                for (int j = 0, len1 = resultList.size(); j < len1; ++j) {
//                    resultList.set(j, resultList.get(j) + append);
//                }
//            }
//            return resultList.toArray(new String[resultList.size()]);
//        } catch (BadHanyuPinyinOutputFormatCombination e) {
//            e.printStackTrace();
//        }
//
//        return new String[]{text};
//    }

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
    public static String[] getPinyinForSorting(@Nullable String text) {
        if (TextUtils.isEmpty(text)) {
            return new String[]{""};
        }
        String pinyin = Pinyin.toPinyin(text, "").toLowerCase();
        return new String[]{pinyin};
    }

//    @NonNull
//    public static String[] getPinyinForSorting(String[] textArr) {
//        if (textArr == null) {
//            return new String[0];
//        }
//
//        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
//        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
//        /*
//         * WITHOUT_TONE：无音标 （zhong）
//         * WITH_TONE_NUMBER：1-4数字表示英标 （zhong4）
//         * WITH_TONE_MARK：直接用音标符（必须WITH_U_UNICODE否则异常） （zhòng）
//         */
//        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
//        /*
//         * WITH_V：用v表示ü （nv）
//         * WITH_U_AND_COLON：用"u:"表示ü （nu:）
//         * WITH_U_UNICODE：直接用ü （nü）
//         */
//        format.setVCharType(HanyuPinyinVCharType.WITH_V);
//
//        try {
//            String[] resultArr = new String[textArr.length];
//            for (int i = 0, len = textArr.length; i < len; ++i) {
//                String result = "";
//                for (char ch : textArr[i].toLowerCase().toCharArray()) {
//                    String[] pyArr = PinyinHelper.toHanyuPinyinStringArray(ch, format);
//                    if (pyArr != null) {
//                        // 仅选取多音字的第一个音
//                        result += pyArr[0];
//                    } else {
//                        result += ch;
//                    }
//                }
//                resultArr[i] = result;
//            }
//            return resultArr;
//        } catch (BadHanyuPinyinOutputFormatCombination e) {
//            e.printStackTrace();
//        }
//
//        return textArr;
//    }

    @NonNull
    public static String[] getPinyinForSorting(String[] textArr) {
        if (textArr == null) {
            return new String[0];
        }

        String[] resultArr = new String[textArr.length];
        for (int i = 0, len = textArr.length; i < len; ++i) {
            resultArr[i] = Pinyin.toPinyin(textArr[i], "").toLowerCase();
        }

        return resultArr;
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

//    @NonNull
//    public static String appName2drawableName(String label, String labelEn) {
//        if (labelEn != null && labelEn.matches("[A-Za-z\\d ]+")) {
//            Matcher matcher = Pattern.compile("([a-z])([A-Z])").matcher(labelEn);
//            while (matcher.find()) {
//                labelEn = labelEn.replaceFirst(matcher.group(0), matcher.group(1) + " " + matcher.group(2));
//            }
//            return labelEn.replaceAll(" ", "_").toLowerCase();
//        } else if (label != null && label.matches("[A-Za-z\\d ]+")) {
//            Matcher matcher = Pattern.compile("([a-z])([A-Z])").matcher(label);
//            while (matcher.find()) {
//                label = label.replaceFirst(matcher.group(0), matcher.group(1) + " " + matcher.group(2));
//            }
//            return label.replaceAll(" ", "_").toLowerCase();
//        }
//        return "";
//    }

    @NonNull
    public static String codeAppName(String name) {
        if (name == null) {
            return "";
        }
        name = name.trim();
        if (name.length() == 0) {
            return "";
        }
        // Not "[A-Za-z][A-Za-z\\d'\\+-\\. _]*"
        if (!name.matches("[A-Za-z][A-Za-z\\d'\\+\\-\\. _]*")) {
            return "";
        }
        Matcher matcher = codePattern.matcher(name);
        while (matcher.find()) {
            name = name.replace(matcher.group(0), matcher.group(0).substring(0, 1) + '_'
                    + matcher.group(0).substring(1, 2));
        }
        return name.toLowerCase()
                .replaceAll("'", "")
                .replaceAll("\\+", "_plus")
                .replaceAll("-|\\.| ", "_")
                .replaceAll("_{2,}", "_");
    }

    public static boolean saveIcon(Context context, Drawable drawable, String name) {
        if (context == null || drawable == null || TextUtils.isEmpty(name)) {
            return false;
        }

        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        if (bitmap == null) {
            return false;
        }

        // Create a path where we will place our picture
        // in the user's public pictures directory.
        File picDir = new File(Environment.getExternalStoragePublicDirectory(Environment
                .DIRECTORY_PICTURES), "Icons");
        // Make sure the Pictures directory exists.
        picDir.mkdirs();
        File targetFile = new File(picDir, "ic_" + name + "_"
                + bitmap.getByteCount() + ".png");

        boolean result = false;
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(targetFile);
            result = bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (!result) {
            targetFile.delete();
            return false;
        }

        record2Gallery(context, targetFile, false);
        return true;
    }

    /**
     * 记录新增图片文件到媒体库，这样可迅速在系统图库看到
     *
     * @param context
     * @param newlyPicFile
     * @return
     */
    private static boolean record2Gallery(Context context, File newlyPicFile, boolean allInDir) {
        if (context == null || newlyPicFile == null || !newlyPicFile.exists()) {
            return false;
        }

        Log.d(C.LOG_TAG, "record2Gallery(): " + newlyPicFile + ", " + allInDir);

        if (C.SDK >= 19) {
            String[] filePaths;
            if (allInDir) {
                filePaths = newlyPicFile.getParentFile().list();
            } else {
                filePaths = new String[]{newlyPicFile.getPath()};
            }
            MediaScannerConnection.scanFile(context, filePaths, null, null);
        } else {
            if (allInDir) {
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                        Uri.fromFile(newlyPicFile.getParentFile())));
            } else {
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        Uri.fromFile(newlyPicFile)));
            }
        }

        return true;
    }

    public static void shareText(Context context, String content, String hint) {
        if (context == null || TextUtils.isEmpty(content)) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, content);
        try {
            if (TextUtils.isEmpty(hint)) {
                context.startActivity(intent);
            } else {
                context.startActivity(Intent.createChooser(intent, hint));
            }
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static boolean isNetworkConnected(Context context, boolean isWifiOnly) {
        if (context == null) {
            return false;
        }

        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager == null) {
            return false;
        }

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return false;
        }

        boolean isConnected = networkInfo.isAvailable();
        if (isWifiOnly) {
            isConnected &= networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
        }

        return isConnected;
    }

    public static boolean isNetworkConnected(Context context) {
        return isNetworkConnected(context, false);
    }

    public static String getDeviceId(Context context) {
        String androidId = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        if (!TextUtils.isEmpty(androidId)) {
            String serial = Build.SERIAL;
            if (!"unknown".equalsIgnoreCase(serial)) {
                return androidId + serial;
            }
            return androidId;
        }

        File file = new File(context.getFilesDir(), "deviceId");
        file.mkdir();
        File[] files = file.listFiles();
        if (files.length > 0) {
            return files[0].getName();
        }
        String id = UUID.randomUUID().toString();
        (new File(file, id)).mkdir();
        return id;
    }

    public static void gotoMarket(Context context, String pkgName, boolean viaBrowser) {
        if (context == null || TextUtils.isEmpty(pkgName)) {
            return;
        }

        // https://play.google.com/store/apps/details?id=%s
        final String LINK = String.format((viaBrowser
                ? "http://www.coolapk.com/apk/%s"
                : "market://details?id=%s"), pkgName);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(LINK));

        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();

            if (!viaBrowser) {
                gotoMarket(context, pkgName, true);
            }
        }
    }

    @NonNull
    public static String renderReqTimes(int reqTimes) {
        if (reqTimes < 0) {
            reqTimes = 0;
        }
//        return (reqTimes > 1 ? C.REQ_REDRAW_SUFFIX_TWO : C.REQ_REDRAW_SUFFIX_ONE) + reqTimes;
        return C.REQ_REDRAW_PREFIX + reqTimes;
    }

//    // TODO
//    @Nullable
//    public static String getImgMD5(@Nullable Bitmap bitmap) {
//        if (bitmap == null) {
//            return null;
//        }
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
//        byte[] bytes = baos.toByteArray();
//        try {
//            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
//            messageDigest.update(bytes);
//            return (new BigInteger(1, messageDigest.digest())).toString(16);
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    public static int fetchColor(@NonNull Context context, int attrId) {
        TypedValue typedValue = new TypedValue();
        TypedArray a = context.obtainStyledAttributes(typedValue.data,
                new int[] { attrId });
        int color = a.getColor(0, 0);
        a.recycle();
        return color;
    }

    public static boolean sendIcon2HomeScreen(Context context, int iconId, String appName,
                                              String pkgName, String launcherName) {
        if (context == null || iconId == 0 || TextUtils.isEmpty(appName)
                || TextUtils.isEmpty(pkgName) || TextUtils.isEmpty(launcherName)) {
            return false;
        }

        Intent shortcutIntent = new Intent(Intent.ACTION_VIEW);
        shortcutIntent.setClassName(pkgName, launcherName);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        final String ACTION_ADD_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT";

        Intent addIntent = new Intent();
        addIntent.setAction(ACTION_ADD_SHORTCUT);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, appName);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                Intent.ShortcutIconResource.fromContext(context, iconId));
        addIntent.putExtra("duplicate", false);
        context.sendBroadcast(addIntent);

        return true;
    }

    @NonNull
    public static String purifyIconName(String iconName) {
        if (TextUtils.isEmpty(iconName)) {
            return "";
        }
        if (iconName.matches(".+?_\\d+")) {
            return iconName.substring(0, iconName.lastIndexOf('_'));
        }
        return iconName;
    }
}
