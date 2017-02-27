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

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.by_syk.lib.nanoiconpack.R;
import com.by_syk.lib.nanoiconpack.bean.IconBean;
import com.by_syk.lib.nanoiconpack.dialog.IconDialog;
import com.by_syk.lib.nanoiconpack.dialog.IconTapHintDialog;
import com.by_syk.lib.nanoiconpack.util.C;
import com.by_syk.lib.nanoiconpack.util.ExtraUtil;
import com.by_syk.lib.nanoiconpack.util.PkgUtil;
import com.by_syk.lib.nanoiconpack.util.adapter.IconAdapter;
import com.by_syk.lib.storage.SP;
import com.by_syk.lib.toast.GlobalToast;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by By_syk on 2017-01-27.
 */

public class IconsFragment extends Fragment {
    private int pageId = 0;
    private boolean filterUnmatched = false;

    private SP sp;

    private View contentView;

    private IconAdapter iconAdapter;

    private RetainedFragment retainedFragment;

    private OnLoadDoneListener onLoadDoneListener;

    public interface OnLoadDoneListener {
        void onLoadDone(int pageId, int sum);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof OnLoadDoneListener) {
            onLoadDoneListener = (OnLoadDoneListener) activity;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (contentView == null) {
            contentView = inflater.inflate(R.layout.fragment_icons, container, false);
            init();

//            (new LoadIconsTask()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
//                    "loadIconsTask" + pageId);
            (new LoadIconsTask()).execute();
        }

        return contentView;
    }

    private void init() {
        Bundle bundle = getArguments();
        pageId = bundle.getInt("pageId");
        filterUnmatched = bundle.getBoolean("filterUnmatched");

        sp = new SP(getContext(), false);

        RecyclerView recyclerView = (RecyclerView) contentView.findViewById(R.id.recycler_view);

        int[] gridNumAndWidth = calculateGridNumAndWidth();
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), gridNumAndWidth[0]));

        iconAdapter = new IconAdapter(getContext(), gridNumAndWidth[1]);
        iconAdapter.setOnItemClickListener(new IconAdapter.OnItemClickListener() {
            @Override
            public void onClick(int pos, IconBean bean) {
                if (!sp.getBoolean("iconTapHint")) {
                    (new IconTapHintDialog()).show(getFragmentManager(), "iconTapHintDialog");
                    return;
                }
                IconDialog.newInstance(bean, ExtraUtil.isFromLauncherPick(getActivity().getIntent()))
                        .show(getFragmentManager(), "iconDialog");
            }

            @Override
            public void onLongClick(int pos, IconBean bean) {
                if (!sp.getBoolean("iconTapHint")) {
                    (new IconTapHintDialog()).show(getFragmentManager(), "iconTapHintDialog");
                    return;
                }
                saveIcon(bean);
            }
        });
        recyclerView.setAdapter(iconAdapter);
    }

    private int[] calculateGridNumAndWidth() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int totalWidth = displayMetrics.widthPixels;

        int minGridSize = getResources().getDimensionPixelSize(R.dimen.grid_size);
        int num = totalWidth / minGridSize;

        return new int[]{num, totalWidth / num};
    }

    @TargetApi(23)
    private void saveIcon(IconBean bean) {
        if (C.SDK >= 23 && getContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            return;
        }

        boolean ok = ExtraUtil.saveIcon(getContext(), bean);
        GlobalToast.showToast(getContext(), ok ? R.string.toast_icon_saved
                : R.string.toast_icon_not_saved);
    }

    private class LoadIconsTask extends AsyncTask<String, Integer, List<IconBean>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            retainedFragment = RetainedFragment.initRetainedFragment(getFragmentManager(), "icon");
        }

        @Override
        protected List<IconBean> doInBackground(String... strings) {
            if (retainedFragment.isIconListSaved(pageId)) {
                Log.d(C.LOG_TAG, "LoadIconsTask - retainedFragment.getIconList(pageId)");
                return retainedFragment.getIconList(pageId);
            }

            if (!isAdded()) {
                return new ArrayList<>();
            }
            Resources resources = getResources();
            if (resources == null) {
                return new ArrayList<>();
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
                labelPinyins = Arrays.copyOf(labels, labels.length);
            }

            Pattern pattern = Pattern.compile("(?<=\\D|^)\\d(?=\\D|$)");
            for (int i = 0, len = labelPinyins.length; i < len; ++i) { // 优化100以内数值逻辑排序
                Matcher matcher = pattern.matcher(labelPinyins[i]);
                if (matcher.find()) {
                    labelPinyins[i] = matcher.replaceAll("0" + matcher.group(0));
                }
            }

            List<IconBean> dataList = new ArrayList<>();
            for (int i = 0, len = names.length; i < len; ++i) {
                if (!isAdded()) {
                    return new ArrayList<>();
                }
                int id = resources.getIdentifier(names[i], "drawable",
                        getContext().getPackageName());
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

            if (filterUnmatched) {
                try {
                    dataList = filterUnmatched(dataList);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return dataList;
        }

        @Override
        protected void onPostExecute(List<IconBean> list) {
            super.onPostExecute(list);

            retainedFragment.setIconList(pageId, list);

            contentView.findViewById(R.id.view_loading).setVisibility(View.GONE);

            iconAdapter.refresh(list);

            if (onLoadDoneListener != null) {
                onLoadDoneListener.onLoadDone(pageId, list.size());
            }
        }

        private List<IconBean> filterUnmatched(@NonNull List<IconBean> dataList) throws Exception {
            List<String> installedIconList = new ArrayList<>();
//            List<String> installedPkgList = PkgUtil.getInstalledPkgs(getContext());
            List<String> installedPkgList = PkgUtil.getInstalledPkgsWithLauncherActivity(getContext());
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

            List<IconBean> installedDataList = new ArrayList<>();
            for (IconBean bean : dataList) {
                for (String icon : installedIconList) {
                    if (icon.equals(bean.getName())) {
                        installedDataList.add(bean);
                        break;
                    }
                }
            }

            return installedDataList;
        }
    }

    public static IconsFragment newInstance(int id, boolean filterUnmatched) {
        IconsFragment fragment = new IconsFragment();

        Bundle bundle = new Bundle();
        bundle.putInt("pageId", id);
        bundle.putBoolean("filterUnmatched", filterUnmatched);
        fragment.setArguments(bundle);

        return fragment;
    }
}
