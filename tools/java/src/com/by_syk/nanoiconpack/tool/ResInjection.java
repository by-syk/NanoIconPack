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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by By_syk on 2017-02-11.
 */

public class ResInjection {
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== ResInjection(v1.0.0) for NanoIconPack(v1.3.0) ===");
        String projectDir = getProjectDir();
        System.out.println("Config: prjectDir = " + projectDir);
        while (true) {
            System.out.println();
            String resPath = getResPath(projectDir);
            appendIcon(resPath);
        }
    }
    
    private static String getProjectDir() {
        String projectDir = "E:/Android/CoreProjects/NanoIconPack/";
        File configFile = new File((new File(System.getProperty("java.class.path"))).getParentFile(),
                "nanoiconpacktool.properties");
        if (configFile.exists()) {
            String configText = readFile(configFile);
            Matcher matcher = Pattern.compile("^projectDir\\s*=\\s*(.+)").matcher(configText);
            if (matcher.find()) {
                projectDir = matcher.group(1);
            }
        }
        return projectDir;
    }

    private static String getResPath(String projectDir) {
        List<String> projectPathList = new ArrayList<>();
        for (File dir : (new File(projectDir)).listFiles()) {
            if ((new File(dir, "build.gradle")).exists()) {
                projectPathList.add(dir.getPath());
            }
        }

        String hint = "";
        Collections.sort(projectPathList);
        for (int i = 0, len = projectPathList.size(); i < len; ++i) {
            hint += i + ". " + (new File(projectPathList.get(i))).getName() + "  ";
        }
        System.out.println("Choose module:");
        System.out.println(hint);

        return (new File(new File(projectPathList.get(scanner.nextInt())),
                "src/main/res")).getPath();
    }

    private static void appendIcon(String resPath) {
        String iconName = "";
        String appName = "";
        String appNameEn = "";
        String componentInfo = "";

        System.out.println("One by one or a whole codes?\n0. One by one  1. Whole");
        int which = scanner.nextInt();
        if (which == 0) {
            System.out.print("IconName(\"\\D[\\da-z_]*\"): ");
            iconName = scanner.next("\\D[\\da-z_]*");
            System.out.print("AppName: ");
            scanner.nextLine();
            appName = scanner.nextLine();
            System.out.print("AppNameEn(\".\" to use AppName): ");
            appNameEn = scanner.nextLine();
            if (appNameEn.equals(".")) {
                appNameEn = appName;
            }
            System.out.print("ComponentInfo([pkgName]/[launcherActivity], \".\" to skip): ");
            componentInfo = scanner.nextLine();
            if (componentInfo.equals(".")) {
                componentInfo = "";
            }
        } else if (which == 1) {
            System.out.println("Codes(\".\" to finish):");
            String input = "";
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.equals(".")) {
                    break;
                }
                if (line.endsWith(".")) {
                    input += line.substring(0, line.length() - 1);
                    break;
                }
                input += line;
            }
            Matcher matcher = Pattern.compile("<\\!-- (.+?) / (.*?) -->" +
                    "<item component=\"ComponentInfo\\{(.+?)\\}\"" +
                    " drawable=\"(.+?)\" />").matcher(input);
            if (matcher.matches()) {
                iconName = matcher.group(4);
                appName = matcher.group(1);
                appNameEn = matcher.group(2);
                if (appNameEn.isEmpty() || appNameEn.equals("null")) {
                    appNameEn = appName;
                }
                componentInfo = matcher.group(3);
            }
        }
        if (iconName.isEmpty() || appName.isEmpty() || appNameEn.isEmpty()) {
            System.out.println("error");
            return;
        }

        boolean ok = appendIcon2IconPackXmlFile(resPath, iconName, appName, appNameEn);
        System.out.println("values/icon_pack.xml: " + ok);
        if (!ok) {
            return;
        }
        ok = appendIcon2DrawableXmlFile(resPath, iconName);
        System.out.println("xml/drawable.xml: " + ok);
        if (!ok) {
            return;
        }
        if (!componentInfo.isEmpty()) {
            ok = appendIcon2AppFilterXmlFile(resPath, componentInfo, iconName);
            System.out.println("xml/appfilter.xml: " + ok);
        }
    }

    private static boolean appendIcon2IconPackXmlFile(String resPath, String iconName, String appName, String appNameEn) {
        File xmlFile = new File(new File(resPath), "values/icon_pack.xml");

        String text1 = readFile(xmlFile);
        int index = text1.indexOf("<string-array name=\"icons\"");
        String text2 = text1.substring(index);
        text1 = text1.substring(0, index);
        index = text2.indexOf("<string-array name=\"icon_labels\"", 19);
        String text3 = text2.substring(index);
        text2 = text2.substring(0, index);

        boolean ok = false;
        boolean existed = false;
        Matcher matcher = Pattern.compile("(?:<\\!--)?<item>(.+?)</item>(-->)?(?:<\\!-- ok -->)?").matcher(text2);
        while (matcher.find()) {
            boolean isLineCommented = matcher.group(2) != null;
            index = iconName.compareTo(matcher.group(1));
            if (index < 0) {
                index = text2.indexOf(matcher.group(0));
                int letterIndex = text2.indexOf("<!-- " + matcher.group(1).substring(0, 1).toUpperCase() + " -->");
                if (letterIndex == index - 19 && iconName.charAt(0) != matcher.group(1).charAt(0)) {
                    index = text2.indexOf("<!-- " + Character.toUpperCase((char) (iconName.charAt(0) + 1)) + " -->");
                }
                String tmp = text2.substring(index);
                text2 = text2.substring(0, index);
                text2 += "<item>" + iconName + "</item>\n        ";
                text2 += tmp;
                ok = true;
                break;
            } else if (index == 0 && !isLineCommented) {
                ok = true;
                existed = true;
                break;
            }
        }
        if (existed) {
            return false;
        }
        if (!ok) {
            index = text2.indexOf("<!-- " + Character.toUpperCase((char) (iconName.charAt(0) + 1))
                    + " -->") - 8;
            if (index < 0) {
                index = text2.indexOf("    </string-array>");
            }
            String tmp = text2.substring(index);
            text2 = text2.substring(0, index);
            text2 += "        <item>" + iconName + "</item>\n";
            text2 += tmp;
        }

        int insertLineIndex = -1;
        for (String line : text2.split("\n")) {
            ++insertLineIndex;
            if (line.contains("<item>" + iconName + "</item>")) {
                break;
            }
        }
        String[] text3Arr = text3.split("\n", -1);
        text3 = "";
        for (int i = 0, len = text3Arr.length; i < len; ++i) {
            if (i == insertLineIndex) {
                text3 += "        <item>" + appNameEn + "</item>\n";
            }
            text3 += text3Arr[i] + "\n";
        }
        text3 = text3.substring(0, text3.length() - 1);

        ok = saveFile(text1 + text2 + text3, xmlFile);
        if (ok) {
            return appendIcon2IconPackXmlFileZh(resPath, appName, insertLineIndex);
        }
        return false;
    }

    private static boolean appendIcon2IconPackXmlFileZh(String resPath, String appName, int insertLineIndex) {
        File xmlFile = new File(new File(resPath), "values-zh/icon_pack.xml");
        if (!xmlFile.exists()) {
            return true;
        }

        String text1 = readFile(xmlFile);
        int index = text1.indexOf("<string-array name=\"icon_labels\"");
        String text2 = text1.substring(index);
        text1 = text1.substring(0, index);
        String[] text2Arr = text2.split("\n", -1);
        text2 = "";
        for (int i = 0, len = text2Arr.length; i < len; ++i) {
            if (i == insertLineIndex) {
                text2 += "        <item>" + appName + "</item>\n";
            }
            text2 += text2Arr[i] + "\n";
        }
        text2 = text2.substring(0, text2.length() - 1);

        return saveFile(text1 + text2, xmlFile);
    }

    private static boolean appendIcon2DrawableXmlFile(String resPath, String iconName) {
        File xmlFile = new File(new File(resPath), "xml/drawable.xml");

        String text1 = readFile(xmlFile);
        int index = text1.indexOf("<category title=\"All");
        String text2 = text1.substring(index);
        text1 = text1.substring(0, index);
        index = text2.indexOf("<category title=", 16);
        String text3 = "";
        if (index >= 0) {
            text3 = text2.substring(index);
            text2 = text2.substring(0, index);
        }

        boolean ok = false;
        boolean existed = false;
        int itemNum = 0;
        Matcher matcher = Pattern.compile("(?:<\\!--)?<item drawable=\"(.+?)\"\\s*/>(-->)?").matcher(text2);
        while (matcher.find()) {
            boolean isLineCommented = matcher.group(2) != null;
            if (!isLineCommented) {
                ++itemNum;
            }
            if (!ok) {
                index = iconName.compareTo(matcher.group(1));
                if (index < 0) {
                    index = text2.indexOf(matcher.group(0));
                    String tmp = text2.substring(index);
                    text2 = text2.substring(0, index);
                    text2 += "<item drawable=\"" + iconName + "\" />\n    ";
                    text2 += tmp;
                    ++itemNum;
                    ok = true;
                } else if (index == 0 && !isLineCommented) {
                    ok = true;
                    existed = true;
                }
            }
        }
        if (existed) {
            return false;
        }
        if (!ok) {
            index = text2.lastIndexOf("<item drawable=");
            if (index >= 0) {
                index = text2.indexOf('\n', index);
            } else {
                index = text2.indexOf('\n');
            }
            String tmp = text2.substring(index);
            text2 = text2.substring(0, index);
            text2 += "\n    <item drawable=\"" + iconName + "\" />";
            text2 += tmp;
            ++itemNum;
        }

        matcher = Pattern.compile("\"All\\((\\d+)\\)\"").matcher(text2);
        if (matcher.find()) {
            text2 = matcher.replaceAll("\"All(" + itemNum + ")\"");
        }
        return saveFile(text1 + text2 + text3, xmlFile);
    }

    private static boolean appendIcon2AppFilterXmlFile(String resPath, String componentInfo, String iconName) {
        File xmlFile = new File(new File(resPath), "xml/appfilter.xml");

        String text = readFile(xmlFile);

        if (Pattern.compile("(?!<\\!--)<item\n?\\s+component=\"ComponentInfo\\{" + componentInfo).matcher(text).find()) {
            return false;
        }

        boolean ok = false;
        Matcher matcher = Pattern.compile("(?:<\\!--)?<item\n?\\s+component=\"ComponentInfo\\{.+?\\}\"" +
                "\n?\\s+drawable=\"(.+?)\"\\s*/>(?:-->)?").matcher(text);
        while (matcher.find()) {
            int compValue = iconName.compareTo(matcher.group(1));
            if (compValue <= 0) {
                int index = text.indexOf(matcher.group(0));
                String tmp = text.substring(index);
                text = text.substring(0, index);
                text += "<item\n        component=\"ComponentInfo{" + componentInfo + "}\""
                        + "\n        drawable=\"" + iconName + "\" />"
                        + (compValue == 0 ? "\n    " : "\n\n    ");
                text += tmp;
                ok = true;
                break;
            }
        }
        if (!ok) {
            text = text.replace("</resources>", "    <item"
                    + "\n        component=\"ComponentInfo{" + componentInfo + "}\""
                    + "\n        drawable=\"" + iconName + "\" />\n\n</resources>");
        }

        return saveFile(text, xmlFile);
    }

    private static String readFile(File file) {
        if (file == null) {
            return null;
        }

        StringBuilder sbData = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
            String buffer;
            while ((buffer = bufferedReader.readLine()) != null) {
                sbData.append(buffer).append("\n");
            }
            if (sbData.length() > 0) {
                sbData.setLength(sbData.length() - 1);
            }

            return sbData.toString();
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
        }

        return null;
    }

    private static boolean saveFile(String text, File saveFile) {
        if (text == null || saveFile == null) {
            return false;
        }

        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(saveFile);
            outputStream.write(text.getBytes("UTF-8"));
            return true;
        } catch (IOException e) {
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

        return false;
    }
}
