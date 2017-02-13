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

package com.by_syk.nanoiconpack.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppCodeGetter {
    private static Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        System.out.println("=== AppCodeGetter(v1.0.1) for NanoIconPack(v1.3.0) ===");
        
        String apkPath = getApkPath(args);
        System.out.println();
        System.out.println(getAppCode(apkPath));
    }
    
    private static String getApkPath(String[] args) {
        if (args != null && args.length > 0) {
            return args[0];
        }
        
        System.out.println("ApkPath:");
        String apkPath;
        if ((apkPath = scanner.nextLine()).isEmpty()) {
            return scanner.nextLine();
        }
        return apkPath;
    }
    
    private static String getAppCode(String apkPath) {
        if (apkPath == null || !(new File(apkPath).exists())) {
            return null;
        }
        
        StringBuilder sbAaptResult = new StringBuilder();
        
        Runtime runtime = Runtime.getRuntime();
        Process process = null;
        BufferedReader bufferedReader = null;
        try {
            process = runtime.exec("cmd.exe /c aapt dump badging \"" + apkPath + "\"");
            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream(), "utf-8"));
            String buffer;
            while ((buffer = bufferedReader.readLine()) != null) {
                sbAaptResult.append(buffer).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (process != null) {
                process.destroy();
                process = null;
            }
        }
        
        if (sbAaptResult.length() == 0) {
            return null;
        }
        
        String pkgName = "";
        String launcherActivity = "";
        String appName = "";
        String appNameEn = "";
        String iconName = "";
        
        Matcher matcher = Pattern.compile("package: name='(.+?)'").matcher(sbAaptResult.toString());
        if (matcher.find()) {
            pkgName = matcher.group(1);
        }
        matcher = Pattern.compile("launchable-activity: name='(.+?)'").matcher(sbAaptResult.toString());
        if (matcher.find()) {
            launcherActivity = matcher.group(1);
        }
        matcher = Pattern.compile("application-label:'(.+?)'").matcher(sbAaptResult.toString());
        if (matcher.find()) {
            appName = matcher.group(1);
            appNameEn = appName;
        }
        matcher = Pattern.compile("application-label-zh_CN:'(.+?)'").matcher(sbAaptResult.toString());
        if (matcher.find()) {
            appName = matcher.group(1);
        } else {
            matcher = Pattern.compile("application-label-zh:'(.+?)'").matcher(sbAaptResult.toString());
            if (matcher.find()) {
                appName = matcher.group(1);
            }
        }
        iconName = appName2drawableName(appName, appNameEn);
        
        return String.format("<!-- %1$s / %2$s -->\n<item component=\"ComponentInfo{%3$s/%4$s}\" drawable=\"%5$s\" />",
                appName, appNameEn, pkgName, launcherActivity, iconName);
    }
    
    private static String appName2drawableName(String label, String labelEn) {
        if (labelEn != null && labelEn.matches("[A-Za-z\\d ]+")) {
            Matcher matcher = Pattern.compile("([a-z])([A-Z])").matcher(labelEn);
            while (matcher.find()) {
                labelEn = labelEn.replaceFirst(matcher.group(0), matcher.group(1) + " " + matcher.group(2));
            }
            return labelEn.replaceAll(" ", "_").toLowerCase();
        } else if (label != null && label.matches("[A-Za-z\\d ]+")) {
            Matcher matcher = Pattern.compile("([a-z])([A-Z])").matcher(label);
            while (matcher.find()) {
                label = label.replaceFirst(matcher.group(0), matcher.group(1) + " " + matcher.group(2));
            }
            return label.replaceAll(" ", "_").toLowerCase();
        }
        return "";
    }
}
