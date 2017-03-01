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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.SparseArray;

import com.by_syk.lib.nanoiconpack.bean.AppBean;
import com.by_syk.lib.nanoiconpack.bean.IconBean;

import java.util.List;

/**
 * Created by By_syk on 2017-02-18.
 */

public class RetainedFragment extends Fragment {
    private SparseArray<List<IconBean>> iconListArray = new SparseArray<>();
    private List<AppBean> appList = null;
    private List<AppBean> reqTopList = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

    public void setIconList(int pageId, List<IconBean> iconList) {
        iconListArray.put(pageId, iconList);
    }

    public void setAppList(List<AppBean> appList) {
        this.appList = appList;
    }

    public void setReqTopList(List<AppBean> reqTopList) {
        this.reqTopList = reqTopList;
    }

    public List<IconBean> getIconList(int pageId) {
        return iconListArray.get(pageId);
    }

    public List<AppBean> getAppList() {
        return appList;
    }

    public List<AppBean> getReqTopList() {
        return reqTopList;
    }

    public boolean isIconListSaved(int pageId) {
        List<IconBean> dataList = iconListArray.get(pageId);
        return dataList != null && !dataList.isEmpty();
    }

    public boolean isAppListSaved() {
        return appList != null && !appList.isEmpty();
    }

    public boolean isReqTopListSaved() {
        return reqTopList != null && !reqTopList.isEmpty();
    }

    @NonNull
    public static RetainedFragment initRetainedFragment(@NonNull FragmentManager fragmentManager,
                                                        @NonNull String tag) {
        RetainedFragment fragment = (RetainedFragment) fragmentManager.findFragmentByTag(tag);
        if (fragment == null) {
            fragment = new RetainedFragment();
            fragmentManager.beginTransaction().add(fragment, tag).commit();
        }
        return fragment;
    }
}
