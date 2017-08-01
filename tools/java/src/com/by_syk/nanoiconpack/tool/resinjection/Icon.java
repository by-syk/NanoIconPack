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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class Icon {
    // item != null && item.isFile() == true
    private List<File> files = new ArrayList<>();
    
    private String name;
    
    private String appName;
    
    // 为 null 则与 appName 一致
    private String appNameEn;
    
    // item like: com.google.android.calendar/com.android.calendar.AllInOneActivity
    private Set<String> components = new HashSet<>();
    
    public void setFiles(String path) {
        files.clear();
        if (path == null || path.isEmpty()) {
            return;
        }
        if (path.startsWith("\"") && path.endsWith("\"")) {
            path = path.substring(1, path.length() - 1);
        }
        File file = new File(path);
        if (!file.isFile()) {
            return;
        }
        files.add(file);
        for (int i = 0; i < 20; ++i) {
            File tmp = new File(file.getParent(),
                    file.getName().replace(".png", "_" + i + ".png"));
            if (tmp.exists()) {
                files.add(tmp);
            }
        }
    }
    
    public void setName(String name) {
        if (name == null) {
            return;
        }
        name = name.trim();
        if (name.isEmpty()) {
            return;
        }
        this.name = name;
    }
    
    public void setAppName(String appName) {
        if (appName == null) {
            return;
        }
        appName = appName.trim();
        if (appName.isEmpty()) {
            return;
        }
        this.appName = appName;
    }
    
    public void setAppNameEn(String appNameEn) {
        if (appNameEn == null) {
            return;
        }
        appNameEn = appNameEn.trim();
        if (appNameEn.isEmpty()) {
            return;
        }
        this.appNameEn = appNameEn;
    }
    
    public void addComponent(String component) {
        if (component == null || component.isEmpty()) {
            return;
        }
        components.add(component);
    }
    
    public void setComponents(Set<String> components) {
        this.components.clear();
        if (components != null) {
            this.components.addAll(components);
        }
    }
    
    public List<File> getFiles() {
        return files;
    }
    
    public String getIconFileName() {
        if (files.size() > 0) {
            return files.get(0).getName().replace(".png", "");
        }
        return null;
    }

    public String getName() {
        return name;
    }
    
    public String getAppName() {
        return appName;
    }
    
    public String getAppNameEn() {
        return appNameEn;
    }
        
    public File getTargetCopyFile(File resDir, int index, boolean isHd) {
        if (resDir == null || !resDir.isDirectory()) {
            return null;
        }
        if (name == null) {
            return null;
        }
        if (isHd) {
            return new File(resDir, "mipmap-nodpi/" + (index > 0 ? name + "_" + index : name) + ".png");
        }
        return new File(resDir, "drawable-nodpi/" + (index > 0 ? name + "_" + index : name) + ".png");
    }
    
    public Set<String> getComponents() {
        return components;
    }
    
    public boolean foundAltIconFiles() {
        return files.size() > 1;
    }
    
    public void removeAltIconFiles() {
        if (files.size() > 1) {
            File file = files.get(0);
            files.clear();
            files.add(file);
        }
    }
    
    public boolean isMetaValid() {
        return name != null && appName != null && !components.isEmpty();
    }
    
    public boolean isValid() {
        return !files.isEmpty() && isMetaValid();
    }
    
    public String generateIconCode(boolean feed) {
        if (name == null) {
            return "";
        }
        
        String code = "";
        for (int i = 0, len = files.size(); i < len; ++i) {
            code += "<item>" + (i > 0 ? name + "_" + i : name) + "</item>"
                  + "\n        ";
        }
        if (!feed) {
            code = code.substring(0, code.length() - 9);
        }
        return code;
    }

    public String generateIconLabelCode(boolean feed) {
        if (appNameEn == null) {
            return "";
        }
        
        String name = appNameEn.replaceAll("'", "\\\\'");
        String code = "";
        for (int i = 0, len = files.size(); i < len; ++i) {
            code += "<item>" + name + "</item>"
                  + "\n        ";
        }
        if (!feed) {
            code = code.substring(0, code.length() - 9);
        }
        return code;
    }

    public String generateIconLabelZhCode(boolean feed) {
        if (appName == null) {
            return "";
        }
        
        String name = appName.replaceAll("'", "\\\\'");
        String code = "";
        for (int i = 0, len = files.size(); i < len; ++i) {
            code += "<item>" + name + "</item>"
                  + "\n        ";
        }
        if (!feed) {
            code = code.substring(0, code.length() - 9);
        }
        return code;
    }
    
    public String generateDrawableCode(boolean feed) {
        if (name == null) {
            return "";
        }
        
        String code = "";
        for (int i = 0, len = files.size(); i < len; ++i) {
            code += "<item drawable=\"" + (i > 0 ? name + "_" + i : name) + "\" />"
                  + "\n    ";
        }
        if (!feed) {
            code = code.substring(0, code.length() - 5);
        }
        return code;
    }

    public String generateAppfilterCode(boolean feed) {
        if (name == null || components.isEmpty()) {
            return "";
        }
        
        String code = "";
        for (String component : components) {
            code += "<item"
                  + "\n        component=\"ComponentInfo{" + component + "}\""
                  + "\n        drawable=\"" + name + "\" />"
                  + "\n    ";
        }
        if (feed) {
            code += "\n    ";
        } else {
            code = code.substring(0, code.length() - 5);
        }
        return code;
    }
    
    @Override
    public String toString() {
        String code = "";
        for (File file : files) {
            code += "<!-- " + file.getPath() + "-->\n";
        }
        code += "<!-- " + appName + "/" + appNameEn + "-->";
        for (String component : components) {
            code += "\n<item"
                  + "\n    component=\"ComponentInfo{" + component + "}\""
                  + "\n    drawable=\"" + name + "\" />";
        }
        return code;
    }
}
