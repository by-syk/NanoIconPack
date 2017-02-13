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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RelateReSort {
    private static Scanner scanner = new Scanner(System.in);
    
    public static void main(String[] args) {
        System.out.println("=== RelateReSort(v1.0.0) for NanoIconPack(v1.3.0) ===");
        
        List<Map<String, String>> itemList = getRelateItems();
        if (itemList == null) {
            System.out.println("error");
            return;
        }
        boolean withLetterIndex = withLetterIndex();
        sort(itemList);
        print(itemList, withLetterIndex);
    }

    private static List<Map<String, String>> getRelateItems() {
        List<Map<String, String>> dataList = new ArrayList<>();
        System.out.println("Array \"icons\"(\".\" to finish):");
        while (scanner.hasNext()) {
            String tmp = scanner.nextLine().trim();
            if (tmp.equals(".")) {
                break;
            }
            
            if (tmp.matches("<\\!--.+?-->")) {
                continue;
            }
            Map<String, String> map = new HashMap<>();
            map.put("icon", tmp);
            dataList.add(map);
        }
        
        System.out.println("Array \"icon_labels\"(\".\" to finish):");
        int index = 0;
        while (scanner.hasNext()) {
            String tmp = scanner.nextLine().trim();
            if (tmp.equals(".")) {
                break;
            }
            if (tmp.matches("<\\!--.+?-->")) {
                continue;
            }
            dataList.get(index++).put("label", tmp);
        }
        if (index < dataList.size()) {
            return null;
        }
        
        System.out.println("Array \"icon_labels\" in zh(\".\" to finish):");
        index = 0;
        while (scanner.hasNext()) {
            String tmp = scanner.nextLine().trim();
            if (tmp.equals(".")) {
                break;
            }
            if (tmp.matches("<\\!--.+?-->")) {
                continue;
            }
            dataList.get(index++).put("labelZh", tmp);
        }
        if (index < dataList.size()) {
            return null;
        }
        
        return dataList;
    }
    
    private static boolean withLetterIndex() {
        System.out.println("With letter index comment like \"<!-- A -->\", (Y/N)?");
        return scanner.next().toLowerCase().equals("y");
    }
    
    private static void sort(List<Map<String, String>> itemList) {
        Collections.sort(itemList, new Comparator<Map<String, String>>() {
            private Pattern pattern = Pattern.compile("<item>(.*?\\D)(\\d)</item>");
            
            @Override
            public int compare(Map<String, String> o1, Map<String, String> o2) {
                String str1 = o1.get("icon");
                String str2 = o2.get("icon");
                Matcher matcher = pattern.matcher(str1);
                if (matcher.find()) {
                    str1 = matcher.replaceAll("<item>" + matcher.group(1) + "0"
                            + matcher.group(2) + "</item>");
                }
                matcher = pattern.matcher(str2);
                if (matcher.find()) {
                    str2 = matcher.replaceAll("<item>" + matcher.group(1) + "0"
                            + matcher.group(2) + "</item>");
                }
                return str1.compareTo(str2);
            }
        });
    }
    
    private static void print(List<Map<String, String>> itemList, boolean withLetterIndex) {
        List<String> iconList = new ArrayList<>();
        List<String> labelList = new ArrayList<>();
        List<String> labelZhList = new ArrayList<>();
        if (withLetterIndex) {
            for (int i = 'A'; i <= 'Z'; ++i) {
                String indexComment = "<!-- " + (char) i + " -->";
                iconList.add(indexComment);
                labelList.add(indexComment);
                labelZhList.add(indexComment);
                for (Map<String, String> map : itemList) {
                    String tmp = map.get("icon");
                    if (tmp.split("<item>")[1].toUpperCase().charAt(0) != i) {
                        continue;
                    }
                    iconList.add(tmp);
                    labelList.add(map.get("label"));
                    labelZhList.add(map.get("labelZh"));
                }
            }
        } else {
            for (Map<String, String> map : itemList) {
                iconList.add(map.get("icon"));
                labelList.add(map.get("label"));
                labelZhList.add(map.get("labelZh"));
            }
        }
        
        System.out.println();
        System.out.println("New array \"icons\":");
        for (String item : iconList) {
            System.out.println(item);
        }
        
        System.out.println();
        System.out.println("New array \"icon_labels\":");
        for (String item : labelList) {
            System.out.println(item);
        }
        
        System.out.println();
        System.out.println("New array \"icon_labels\" in zh:");
        for (String item : labelZhList) {
            System.out.println(item);
        }
    }
}
