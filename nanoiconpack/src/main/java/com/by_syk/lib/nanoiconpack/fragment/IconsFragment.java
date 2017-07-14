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

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.by_syk.lib.nanoiconpack.R;
import com.by_syk.lib.nanoiconpack.bean.IconBean;
import com.by_syk.lib.nanoiconpack.dialog.IconDialog;
import com.by_syk.lib.nanoiconpack.util.ExtraUtil;
import com.by_syk.lib.nanoiconpack.util.PkgUtil;
import com.by_syk.lib.nanoiconpack.util.adapter.IconAdapter;
import com.by_syk.lib.nanoiconpack.util.IconsGetter;
import com.by_syk.lib.sp.SP;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by By_syk on 2017-01-27.
 */

public class IconsFragment extends Fragment {
    private int pageId = 0;
    private IconsGetter iconsGetter;
    private int gridItemMode;

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
        iconsGetter = (IconsGetter) bundle.getSerializable("iconsGetter");
        gridItemMode = bundle.getInt("mode", IconAdapter.MODE_ICON);

        RecyclerView recyclerView = (RecyclerView) contentView.findViewById(R.id.recycler_view);

        int[] gridNumAndWidth = calculateGridNumAndWidth();
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), gridNumAndWidth[0]));

        iconAdapter = new IconAdapter(getContext(), gridNumAndWidth[1]);
        iconAdapter.setMode(gridItemMode);
        iconAdapter.setOnItemClickListener(new IconAdapter.OnItemClickListener() {
            @Override
            public void onClick(int pos, IconBean bean) {
                IconDialog.newInstance(bean, ExtraUtil.isFromLauncherPick(getActivity().getIntent()))
                        .show(getFragmentManager(), "iconDialog");
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

    public static IconsFragment newInstance(int id, IconsGetter iconsGetter, int mode) {
        IconsFragment fragment = new IconsFragment();

        Bundle bundle = new Bundle();
        bundle.putInt("pageId", id);
        bundle.putSerializable("iconsGetter", iconsGetter);
        bundle.putInt("mode", mode);
        fragment.setArguments(bundle);

        return fragment;
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
                return retainedFragment.getIconList(pageId);
            }

            if (!isAdded() || iconsGetter == null) {
                return new ArrayList<>();
            }

            clearIconsCache();

            try {
                return iconsGetter.getIcons(getContext());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new ArrayList<>();
        }

        @Override
        protected void onPostExecute(List<IconBean> list) {
            super.onPostExecute(list);

            retainedFragment.setIconList(pageId, list);

            ((AVLoadingIndicatorView) contentView.findViewById(R.id.view_loading)).hide();

            iconAdapter.refresh(list);

            if (onLoadDoneListener != null) {
                onLoadDoneListener.onLoadDone(pageId, list.size());
            }
        }

        /**
         * We use Glide to load icons in page list and Glide will cache them in disk. (See IconAdapter.java)
         * When the icon pack app is updated (some icons may be redrawn), we'd better clear cache.
         */
        private void clearIconsCache() {
            SP sp = new SP(getContext());
            String tag = "iconsCacheCleared-" + PkgUtil.getAppVer(getContext(), "%1$s(%2$s)");
            if (!sp.getBoolean(tag)) {
                sp.save(tag, true);
                Glide.get(getContext()).clearDiskCache();
            }
        }
    }
}
