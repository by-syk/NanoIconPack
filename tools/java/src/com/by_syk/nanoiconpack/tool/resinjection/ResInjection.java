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

package com.by_syk.nanoiconpack.tool.resinjection;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.by_syk.nanoiconpack.tool.util.FileUtil;
import com.by_syk.nanoiconpack.tool.util.Utils;

/**
 * TODO 对APP名含特殊符号的处理
 * TODO 追书神器 英语？
 * 
 * Created by By_syk on 2017-02-11.
 */

class ResInjection {
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("=== ResInjection(v1.2.0) for NanoIconPack(v3.0.0) ===");
        System.out.println();
        System.out.println("      Copyright (c) 2017 By_syk. All rights reserved.");
        System.out.println();
        String projectDir = ResInjection.getProjectDir(args);
        File resDir = ResInjection.getResPath(projectDir);
        while (true) {
            System.out.println();
            appendIcon(resDir);
        }
    }
    
    public static String getProjectDir(String[] args) {
        if (args != null && args.length > 0) {
            return args[0];
        }
        
        System.out.println("> ProjectDir:");
        String projectDir;
        if ((projectDir = scanner.nextLine()).isEmpty()) {
            return scanner.nextLine();
        }
        return projectDir;
    }

    public static File getResPath(String projectDir) {
        List<File> projectDirList = new ArrayList<>();
        for (File dir : (new File(projectDir)).listFiles()) {
            if ((new File(dir, "build.gradle")).exists()) {
                projectDirList.add(dir);
            }
        }

        String hint = "";
        Collections.sort(projectDirList);
        for (int i = 0, len = projectDirList.size(); i < len; ++i) {
            hint += i + ". " + projectDirList.get(i).getName() + "  ";
        }
        System.out.println("> Choose module:");
        System.out.println(hint);

        return new File(projectDirList.get(scanner.nextInt()), "src/main/res");
    }

    public static void appendIcon(File resDir) {
        Icon icon = new Icon();
        
        System.out.println("> IconPath(\".\" to skip):");
        String tmp = scanner.nextLine();
        if (tmp.isEmpty()) {
            tmp = scanner.nextLine();
        }
        if (isInputEnd(tmp)) {
            tmp = "";
        }
        icon.setFiles(tmp);
        tmp = icon.getIconFileName();
        if (tmp != null) { // 复制文件名到剪切板，便于接下来快速查询
            Utils.copy2Clipboard(tmp);
        }
        if (icon.foundAltIconFiles()) {
            System.out.println("> Include alt?\n0. No  1. Yes");
            int which = scanner.nextInt();
            if (which != 1) {
                icon.removeAltIconFiles();
            }
        }
        
        System.out.println("> Manually or automatically?\n0. Manually  1. Automatically");
        int which = scanner.nextInt();
        if (which == 0) { // 手动录入
            System.out.println("IconName(\"\\D[\\da-z_]*\"):");
            icon.setName(scanner.next("\\D[\\da-z_]*"));
            System.out.println("AppName:");
            tmp = scanner.nextLine();
            if (tmp.isEmpty()) {
                tmp = scanner.nextLine();
            }
            icon.setAppName(tmp);
            System.out.println("AppNameEn(\".\" to use AppName):");
            tmp = scanner.nextLine();
            if (!isInputEnd(tmp)) {
                icon.setAppNameEn(tmp);
            }
            System.out.println("ComponentInfo([pkgName]/[launcherActivity], \".\" to finish):");
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (isInputEnd(line)) {
                    break;
                }
                icon.addComponent(line);
            }
        } else if (which == 1) { // 快速录入
            System.out.println("> Codes(\".\" to finish):");
            String input = ""; // No '\n'
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (isInputEnd(line)) {
                    break;
                }
                if (line.endsWith(".")) {
                    input += line.substring(0, line.length() - 1);
                    break;
                }
                input += line;
            }
            Matcher matcher = Pattern.compile("<\\!-- (.+?) / (.*?) -->"
                    + "<item component=\"ComponentInfo\\{(.+?)\\}\""
                    + " drawable=\"(.+?)\" />").matcher(input);
            if (matcher.find()) {
                icon.setName(matcher.group(4));
                icon.setAppName(matcher.group(1));
                tmp = matcher.group(2);
                if (!tmp.isEmpty() && !tmp.equals("null")) {
                    icon.setAppNameEn(tmp);
                }
                icon.addComponent(matcher.group(3));
                // 连续添加
                matcher = Pattern.compile("ComponentInfo\\{(.+?)\\}").matcher(input);
                while (matcher.find()) {
                    icon.addComponent(matcher.group(1));
                }
            }
        }
        if (!icon.isMetaValid()) {
            System.out.println("error");
            return;
        }

        boolean ok = copyIconFile(resDir, icon);
        String outText = "drawable-nodpi/" + icon.getName() + ".png";
        System.out.printf("%s: %s\n", outText, (ok ? (icon.getFiles().size() > 1
                ? "TRUE(" + icon.getFiles().size() + ")" : "TRUE") : "FALSE"));
        if (!ok) {
            return;
        }
        ok = appendIcon2IconPackXmlFile(resDir, icon);
        System.out.printf("%-" + outText.length() + "s: %s\n", "values/icon_pack.xml",
                ok ? (icon.getFiles().size() > 1 ? "TRUE(" + icon.getFiles().size() + ")" : "TRUE") : "FALSE");
        if (!ok) {
            return;
        }
        ok = appendIcon2DrawableXmlFile(resDir, icon);
        System.out.printf("%-" + outText.length() + "s: %s\n", "xml/drawable.xml",
                ok ? (icon.getFiles().size() > 1 ? "TRUE(" + icon.getFiles().size() + ")" : "TRUE") : "FALSE");
        if (!ok) {
            return;
        }
        if (!icon.getComponents().isEmpty()) {
            ok = appendIcon2AppFilterXmlFile(resDir, icon);
            System.out.printf("%-" + outText.length() + "s: %s\n",
                    "xml/appfilter.xml", ok ? "TRUE" : "FALSE");
        }
    }

    public static boolean copyIconFile(File resDir, Icon icon) {
        if (icon == null || icon.getFiles().isEmpty()) {
            return false;
        }
        for (int i = 0, len = icon.getFiles().size(); i < len; ++i) {
            File iconFile = icon.getFiles().get(i);
            File tarIconFile = icon.getTargetCopyFile(resDir, i, false);
            boolean ok = FileUtil.copyFile(iconFile, tarIconFile);
            if (ok) {
                iconFile.delete();
            } else {
                tarIconFile.delete();
                return false;
            }
        }
        return true;
    }

    public static boolean appendIcon2IconPackXmlFile(File resDir, Icon icon) {
        if (resDir == null || !resDir.isDirectory()) {
            return false;
        }
        if (icon == null || !icon.isMetaValid()) {
            return false;
        }
        
        File xmlFile = new File(resDir, "values/icon_pack.xml");

        String text1 = FileUtil.readFile(xmlFile);
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
            index = icon.getName().compareTo(matcher.group(1));
            if (index < 0) {
                index = text2.indexOf(matcher.group(0));
                int letterIndex = text2.indexOf("<!-- " + matcher.group(1).substring(0, 1).toUpperCase() + " -->");
                if (letterIndex == index - 19 && icon.getName().charAt(0) != matcher.group(1).charAt(0)) {
                    index = text2.indexOf("<!-- " + Character.toUpperCase((char) (icon.getName().charAt(0) + 1)) + " -->");
                }
                String tmp = text2.substring(index);
                text2 = text2.substring(0, index);
                text2 += icon.generateIconCode(true);
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
            index = text2.indexOf("<!-- " + Character.toUpperCase((char) (icon.getName().charAt(0) + 1))
                    + " -->") - 8;
            if (index < 0) {
                index = text2.indexOf("    </string-array>");
            }
            String tmp = text2.substring(index);
            text2 = text2.substring(0, index);
            text2 += "        " + icon.generateIconCode(false) + "\n";
            text2 += tmp;
        }

        int insertLineIndex = -1;
        String insertCode = icon.generateIconCode(false).split("\n")[0];
        for (String line : text2.split("\n")) {
            ++insertLineIndex;
            if (line.contains(insertCode)) {
                break;
            }
        }
        String[] text3Arr = text3.split("\n", -1);
        text3 = "";
        for (int i = 0, len = text3Arr.length; i < len; ++i) {
            if (i == insertLineIndex) {
                text3 += "        " + icon.generateIconLabelCode(false) + "\n";
            }
            text3 += text3Arr[i] + "\n";
        }
        text3 = text3.substring(0, text3.length() - 1);

        ok = FileUtil.saveFile(text1 + text2 + text3, xmlFile);
        if (ok) {
            return appendIcon2IconPackXmlFileZh(resDir, icon, insertLineIndex);
        }
        return false;
    }

    private static boolean appendIcon2IconPackXmlFileZh(File resDir, Icon icon, int insertLineIndex) {
        if (resDir == null || !resDir.isDirectory()) {
            return false;
        }
        if (icon == null || !icon.isMetaValid()) {
            return false;
        }
        
        File xmlFile = new File(resDir, "values-zh/icon_pack.xml");
        if (!xmlFile.exists()) {
            return true;
        }

        String text1 = FileUtil.readFile(xmlFile);
        int index = text1.indexOf("<string-array name=\"icon_labels\"");
        String text2 = text1.substring(index);
        text1 = text1.substring(0, index);
        String[] text2Arr = text2.split("\n", -1);
        text2 = "";
        for (int i = 0, len = text2Arr.length; i < len; ++i) {
            if (i == insertLineIndex) {
                text2 += "        " + icon.generateIconLabelZhCode(false) + "\n";
            }
            text2 += text2Arr[i] + "\n";
        }
        text2 = text2.substring(0, text2.length() - 1);

        return FileUtil.saveFile(text1 + text2, xmlFile);
    }

    public static boolean appendIcon2DrawableXmlFile(File resDir, Icon icon) {
        if (resDir == null || !resDir.isDirectory()) {
            return false;
        }
        if (icon == null || icon.getName() == null) {
            return false;
        }
        
        File xmlFile = new File(resDir, "xml/drawable.xml");

        String text1 = FileUtil.readFile(xmlFile);
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
                index = icon.getName().compareTo(matcher.group(1));
                if (index < 0) {
                    index = text2.indexOf(matcher.group(0));
                    String tmp = text2.substring(index);
                    text2 = text2.substring(0, index);
                    text2 += icon.generateDrawableCode(true);
                    text2 += tmp;
                    itemNum += icon.getFiles().size();
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
            text2 += "\n    " + icon.generateDrawableCode(false);
            text2 += tmp;
            itemNum += icon.getFiles().size();
        }

        matcher = Pattern.compile("\"All\\((\\d+)\\)\"").matcher(text2);
        if (matcher.find()) {
            text2 = matcher.replaceAll("\"All(" + itemNum + ")\"");
        }
        return FileUtil.saveFile(text1 + text2 + text3, xmlFile);
    }

    public static boolean appendIcon2AppFilterXmlFile(File resDir, Icon icon) {
        if (resDir == null || !resDir.isDirectory()) {
            return false;
        }
        if (icon == null || !icon.isMetaValid()) {
            return false;
        }
        
        File xmlFile = new File(resDir, "xml/appfilter.xml");

        String text = FileUtil.readFile(xmlFile);

        for (String component : icon.getComponents()) {
            if (Pattern.compile("(?!<\\!--)<item\n?\\s+component=\"ComponentInfo\\{" + component + "\\}").matcher(text).find()) {
                return false;
            }
        }

        boolean ok = false;
        Matcher matcher = Pattern.compile("(?:<\\!--)?<item\n?\\s+component=\"ComponentInfo\\{.+?\\}\"" +
                "\n?\\s+drawable=\"(.+?)\"\\s*/>(?:-->)?").matcher(text);
        while (matcher.find()) {
            int compValue = icon.getName().compareTo(matcher.group(1));
            if (compValue <= 0) {
                int index = text.indexOf(matcher.group(0));
                String tmp = text.substring(index);
                text = text.substring(0, index);
                text += icon.generateAppfilterCode(true);
                text += tmp;
                ok = true;
                break;
            }
        }
        if (!ok) {
            String tmp = "    " + icon.generateAppfilterCode(false) + "\n";
            text = text.replace("</resources>", tmp + "\n</resources>");
        }

        return FileUtil.saveFile(text, xmlFile);
    }
    
    private static boolean isInputEnd(String line) {
        return Objects.equals(line, ".") || Objects.equals(line, "。");
    }
}
