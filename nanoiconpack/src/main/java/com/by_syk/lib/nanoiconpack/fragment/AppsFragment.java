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

package com.by_syk.lib.nanoiconpack.fragment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.XmlResourceParser;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.by_syk.lib.nanoiconpack.R;
import com.by_syk.lib.nanoiconpack.bean.AppBean;
import com.by_syk.lib.nanoiconpack.dialog.AppTapHintDialog;
import com.by_syk.lib.nanoiconpack.util.C;
import com.by_syk.lib.nanoiconpack.widget.DividerItemDecoration;
import com.by_syk.lib.nanoiconpack.util.ExtraUtil;
import com.by_syk.lib.nanoiconpack.util.PkgUtil;
import com.by_syk.lib.nanoiconpack.util.adapter.AppAdapter;
import com.by_syk.lib.storage.SP;
import com.by_syk.lib.toast.GlobalToast;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by By_syk on 2017-01-27.
 */

public class AppsFragment extends Fragment {
    private SP sp;

    private View contentView;

    private AppAdapter appAdapter;

    private SwipeRefreshLayout swipeRefreshLayout;

    private String appCodeSelected = "";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (contentView == null) {
            contentView = inflater.inflate(R.layout.fragment_apps, container, false);
            init();

            (new LoadAppsTask()).execute();
        }

        return contentView;
    }

    @Override
    public void onStart() {
        super.onStart();

        appCodeSelected = "";

        appAdapter.clearTags();
    }

    private void init() {
        sp = new SP(getActivity(), false);

        appAdapter = new AppAdapter(getActivity());
        appAdapter.setOnItemClickListener(new AppAdapter.OnItemClickListener() {
            @Override
            public void onClick(int pos, AppBean bean) {
                if (!sp.getBoolean("appTapHint")) {
                    (new AppTapHintDialog()).show(getActivity().getFragmentManager(), "appTapTintDialog");
                    return;
                }
                copyOrShareAppCode(bean, true);

                appAdapter.tag(pos);
            }

            @Override
            public void onLongClick(int pos, AppBean bean) {
                if (!sp.getBoolean("appTapHint")) {
                    (new AppTapHintDialog()).show(getActivity().getFragmentManager(), "hintDialog");
                    return;
                }
                copyOrShareAppCode(bean, false);

                appAdapter.tag(pos);
            }
        });

        RecyclerView recyclerView = (RecyclerView) contentView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(appAdapter);

        swipeRefreshLayout = (SwipeRefreshLayout) contentView.findViewById(R.id.swipe_refresh_layout);
        if (C.SDK >= 21) {
            swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.color_accent));
        } else {
            swipeRefreshLayout.setColorSchemeColors(getResources().getColor(android.R.color.holo_blue_light));
        }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                (new LoadAppsTask()).execute();
            }
        });
    }

    private void copyOrShareAppCode(AppBean bean, boolean toCopyOrShare) {
        if (bean == null) {
            return;
        }

        String label = bean.getLabel();
        String labelEn = PkgUtil.getAppLabelEn(getActivity(), bean);
        boolean isSysApp = PkgUtil.isSysApp(getActivity(), bean.getPkgName());
        String code = getString(isSysApp ? R.string.app_component_1 : R.string.app_component,
                Build.BRAND, Build.MODEL, label, labelEn,
                bean.getPkgName(), bean.getLauncherActivity(),
                ExtraUtil.appName2drawableName(label, labelEn));
        if (!appCodeSelected.contains(code)) {
            appCodeSelected += (appCodeSelected.length() > 0 ? "\n\n" : "") + code;
        }

        if (toCopyOrShare) {
            ExtraUtil.copy2Clipboard(getActivity(), appCodeSelected);
            GlobalToast.showToast(getActivity(), getString(R.string.toast_code_copied,
                    appCodeSelected.split("\n\n").length));
        } else {
            ExtraUtil.shareText(getActivity(), appCodeSelected, getString(R.string.send_code));
        }
    }

    private class LoadAppsTask extends AsyncTask<String, Integer, List<AppBean>> {
        @Override
        protected List<AppBean> doInBackground(String... strings) {
            List<AppBean> dataList = new ArrayList<>();
            if (getActivity() == null) {
                return dataList;
            }

            PackageManager packageManager = getActivity().getPackageManager();
            try {
                Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
                mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                List<ResolveInfo> list = packageManager.queryIntentActivities(mainIntent, 0);
                for (ResolveInfo resolveInfo : list) {
                    String label = resolveInfo.loadLabel(packageManager).toString();
                    for (String labelPinyin : ExtraUtil.getPinyinForSorting(label)) {
                        dataList.add(new AppBean(resolveInfo.loadIcon(packageManager),
                                label, labelPinyin,
                                resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.name));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!isAdded() || dataList.isEmpty()) {
                return dataList;
            }

            removeMatched(dataList);

            Collections.sort(dataList, new Comparator<AppBean>() {
                @Override
                public int compare(AppBean bean1, AppBean bean2) {
//                    return bean1.getLabel().compareTo(bean2.getLabel());
                    return bean1.getLabelPinyin().compareTo(bean2.getLabelPinyin());
                }
            });

            return dataList;
        }

        @Override
        protected void onPostExecute(List<AppBean> list) {
            super.onPostExecute(list);

            contentView.findViewById(R.id.pb_loading).setVisibility(View.GONE);

            appAdapter.refresh(list);

            swipeRefreshLayout.setRefreshing(false);

            appCodeSelected = "";
        }

        private void removeMatched(@NonNull List<AppBean> appList) {
            try {
                XmlResourceParser parser = getResources().getXml(R.xml.appfilter);
                int event = parser.getEventType();
                while (event != XmlPullParser.END_DOCUMENT) {
                    if (event == XmlPullParser.START_TAG) {
                        if ("item".equals(parser.getName())) {
                            String component = parser.getAttributeValue(0);
                            if (component != null) {
                                Matcher matcher = Pattern.compile("ComponentInfo\\{([^/]+?)/.+?\\}")
                                        .matcher(component);
                                if (matcher.matches()) {
                                    for (AppBean bean : appList) {
                                        if (bean.getPkgName().equals(matcher.group(1))) {
                                            appList.remove(bean);
                                            break;
                                        }
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
        }
    }

    public static AppsFragment newInstance() {
        return new AppsFragment();
    }
}
