package com.by_syk.lib.nanoiconpack.util;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by By_syk on 2017-07-06.
 */

public class InstalledAppReader {
    private static InstalledAppReader instance;

    @NonNull
    private List<Bean> dataList = new ArrayList<>();

    private InstalledAppReader(@NonNull PackageManager packageManager) {
        init(packageManager);
    }

    private void init(@NonNull PackageManager packageManager) {
        try {
            Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
            mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> list = packageManager.queryIntentActivities(mainIntent, 0);
            for (ResolveInfo resolveInfo : list) {
                dataList.add(new Bean(resolveInfo.loadLabel(packageManager).toString(),
                        resolveInfo.activityInfo.packageName,
                        resolveInfo.activityInfo.name));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @NonNull
    public List<Bean> getDataList() {
        return dataList;
    }

    @NonNull
    public Set<String> getComponentSet() {
        Set<String> set = new HashSet<>(dataList.size());
        for (Bean bean : dataList) {
            set.add(bean.getPkg() + "/" + bean.getLauncher());
        }
        return set;
    }

    @NonNull
    public Map<String, String> getComponentLabelMap() {
        Map<String, String> map = new HashMap<>(dataList.size());
        for (Bean bean : dataList) {
            map.put(bean.getPkg() + "/" + bean.getLauncher(), bean.getLabel());
        }
        return map;
    }

    @Nullable
    public String getLabel(@NonNull String component) {
        for (Bean bean : dataList) {
            if (component.equals(bean.getPkg() + "/" + bean.getLauncher())) {
                return bean.getLabel();
            }
        }
        return null;
    }

    public static InstalledAppReader getInstance(@NonNull PackageManager packageManager) {
        if (instance == null) {
            synchronized (InstalledAppReader.class) {
                if (instance == null) {
                    instance = new InstalledAppReader(packageManager);
                }
            }
        }
        return instance;
    }

    public class Bean {
        @Nullable
        private String label;

        @NonNull
        private String pkg;

        @NonNull
        private String launcher;

        public Bean(@Nullable String label, @NonNull String pkg, @NonNull String launcher) {
            this.label = label;
            this.pkg = pkg;
            this.launcher = launcher;
        }

        @Nullable
        public String getLabel() {
            return label;
        }

        @NonNull
        public String getPkg() {
            return pkg;
        }

        @NonNull
        public String getLauncher() {
            return launcher;
        }
    }
}
