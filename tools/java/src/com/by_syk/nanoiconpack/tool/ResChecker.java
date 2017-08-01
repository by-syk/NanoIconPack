package com.by_syk.nanoiconpack.tool;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.by_syk.nanoiconpack.tool.util.FileUtil;

public class ResChecker {
    public static void main(String[] args) {
//        checkAppFilter("E:/Android/CoreProjects/OwIconPack/app/src/main/res/xml");
        checkAppFilter2("E:/Android/CoreProjects/NanoIconPack/cardicons/src/main/res");
//        checkIconFiles("E:/Android/CoreProjects/NanoIconPack/cardicons/src/main/res");
        System.out.println("Done");
    }
    
    private static void checkAppFilter(String xmlDirPath) {
        String appFilterText = FileUtil.readFile(new File(new File(xmlDirPath), "appfilter.xml"));
        String drawableText = FileUtil.readFile(new File(new File(xmlDirPath), "drawable.xml"));
        
        Matcher matcher = Pattern.compile("drawable=\"(.+?)\"").matcher(appFilterText);
        while (matcher.find()) {
            if (!drawableText.contains("\"" + matcher.group(1) + "\"")) {
                System.out.println("Lost: " + matcher.group(1));
            }
        }
    }
    
    private static void checkAppFilter2(String resDirPath) {
        String appFilterText = FileUtil.readFile(new File(new File(resDirPath), "/xml/appfilter.xml"));

        File drawableDir = new File(resDirPath, "drawable-nodpi");
        for (File iconFile : drawableDir.listFiles()) {
            if (iconFile.getName().contains("_alt")) {
                continue;
            }
            if (!appFilterText.contains("drawable=\"" + iconFile.getName().replace(".png", "") + "\"")) {
                System.out.println(iconFile.getName());
            }
        }
    }
    
    private static void checkIconFiles(String resDirPath) {
        String drawableText = FileUtil.readFile(new File(new File(resDirPath), "xml/drawable.xml"));
        
        File drawableDir = new File(resDirPath, "drawable-nodpi");
        for (File iconFile : drawableDir.listFiles()) {
            if (!drawableText.contains("\"" + iconFile.getName().replace(".png", "") + "\"")) {
                System.out.println(iconFile.getName());
            }
        }
    }
}
