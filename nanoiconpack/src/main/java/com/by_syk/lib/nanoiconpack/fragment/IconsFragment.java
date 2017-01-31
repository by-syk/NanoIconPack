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

import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.by_syk.lib.nanoiconpack.R;
import com.by_syk.lib.nanoiconpack.bean.IconBean;
import com.by_syk.lib.nanoiconpack.util.C;
import com.by_syk.lib.nanoiconpack.util.ExtraUtil;
import com.by_syk.lib.nanoiconpack.util.adapter.IconAdapter;

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

public class IconsFragment extends Fragment {
    private int pageId = 1;

    private View contentView;

    private IconAdapter iconAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (contentView == null) {
            contentView = inflater.inflate(R.layout.fragment_icons, container, false);
            init();

            (new LoadIconsTask()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                    "loadIconsTask" + pageId);
        }

        return contentView;
    }

    private void init() {
        pageId = getArguments().getInt("pageId", 1);

        RecyclerView recyclerView = (RecyclerView) contentView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), getColumns()));

        iconAdapter = new IconAdapter(getActivity());
        iconAdapter.setOnItemClickListener(new IconAdapter.OnItemClickListener() {
            @Override
            public void onClick(int pos, IconBean bean) {
                IconDialog.newInstance(bean, ExtraUtil.isFromLauncherPick(getActivity().getIntent()))
                        .show(getActivity().getFragmentManager(), "iconDialog");
            }
        });
        recyclerView.setAdapter(iconAdapter);

//        RecyclerFastScroller fastScroller = (RecyclerFastScroller)
//                contentView.findViewById(R.id.fast_scroller);
//        fastScroller.attachRecyclerView(recyclerView);
    }

    private int getColumns() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int totalWidth = displayMetrics.widthPixels;
        totalWidth = totalWidth - 2 * getResources()
                .getDimensionPixelSize(R.dimen.icon_layout_horz_margin);

        int gridWidth = getResources().getDimensionPixelSize(R.dimen.grid_size);

        return totalWidth / gridWidth;
    }

    private class LoadIconsTask extends AsyncTask<String, Integer, List<IconBean>> {
        @Override
        protected List<IconBean> doInBackground(String... strings) {
            List<IconBean> dataList = new ArrayList<>();

            Resources resources = getResources();
            if (resources == null) {
                return dataList;
            }

            String[] names = resources.getStringArray(R.array.icons);
            String[] labels = resources.getStringArray(R.array.icon_labels);
            String[] labelPinyins;
            if (labels.length > 0) {
                labelPinyins = ExtraUtil.getPinyinForSorting(labels);
            } else { // No app name list provided, use icon name list instead.
                labels = new String[names.length];
                for (int i = 0, len = names.length; i < len; ++i) {
                    labels[i] = names[i].replaceAll("_", " ");
                }
                labelPinyins = labels;
            }

            for (int i = 0, len = names.length; i < len; ++i) {
                int id = getResources().getIdentifier(names[i], "drawable",
                        getActivity().getPackageName());
                dataList.add(new IconBean(id, names[i], labels[i], labelPinyins[i]));
            }
            Collections.sort(dataList, new Comparator<IconBean>() {
                @Override
                public int compare(IconBean bean1, IconBean bean2) {
//                    return bean1.getName().compareTo(bean2.getName());
//                    return bean1.getLabel().compareTo(bean2.getLabel());
                    return bean1.getLabelPinyin().compareTo(bean2.getLabelPinyin());
                }
            });

            if (pageId == 1) {
                dataList = filterMatched(dataList);
            }

            return dataList;
        }

        @Override
        protected void onPostExecute(List<IconBean> list) {
            super.onPostExecute(list);

            contentView.findViewById(R.id.pb_loading).setVisibility(View.GONE);

            iconAdapter.refresh(list);
        }

        private List<IconBean> filterMatched(@NonNull List<IconBean> dataList) {
            long start = System.currentTimeMillis();

            List<String> installedIconList = new ArrayList<>();
//            List<String> installedPkgList = ExtraUtil.getInstalledPkgs(getActivity());
            List<String> installedPkgList = ExtraUtil.getInstalledPkgsWithLauncherActivity(getActivity());
            XmlResourceParser parser = getResources().getXml(R.xml.appfilter);
            try {
                int event = parser.getEventType();
                while (event != XmlPullParser.END_DOCUMENT) {
                    if (event == XmlPullParser.START_TAG) {
                        if ("item".equals(parser.getName())) {
                            String component = parser.getAttributeValue(0);
                            if (component != null) {
                                Matcher matcher = Pattern.compile("ComponentInfo\\{([^/]+?)/.+?\\}")
                                        .matcher(component);
                                if (matcher.matches()) {
                                    String pkgName = matcher.group(1);
                                    String drawable = parser.getAttributeValue(1);
                                    for (String installedPkg : installedPkgList) {
                                        if (installedPkg.equals(pkgName)) {
                                            installedIconList.add(drawable);
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

            List<IconBean> installedDataList = new ArrayList<>();
            for (IconBean bean : dataList) {
                for (String icon : installedIconList) {
                    if (icon.equals(bean.getName())) {
                        installedDataList.add(bean);
                        break;
                    }
                }
            }

            Log.d(C.LOG_TAG, "Cost(filterMatched): " + (System.currentTimeMillis() - start) + "ms");

            return installedDataList;
        }
    }

    public static IconsFragment newInstance(int id) {
        IconsFragment fragment = new IconsFragment();

        Bundle bundle = new Bundle();
        bundle.putInt("pageId", id);
        fragment.setArguments(bundle);

        return fragment;
    }
}
