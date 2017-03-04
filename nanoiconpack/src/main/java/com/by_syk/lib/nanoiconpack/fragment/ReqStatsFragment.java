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

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.by_syk.lib.nanoiconpack.R;
import com.by_syk.lib.nanoiconpack.bean.AppBean;
import com.by_syk.lib.nanoiconpack.bean.CoolApkApkDetailBean;
import com.by_syk.lib.nanoiconpack.bean.ResResBean;
import com.by_syk.lib.nanoiconpack.dialog.ReqMenuDialog;
import com.by_syk.lib.nanoiconpack.util.C;
import com.by_syk.lib.nanoiconpack.util.ExtraUtil;
import com.by_syk.lib.nanoiconpack.util.RetrofitHelper;
import com.by_syk.lib.nanoiconpack.util.impl.NanoServerService;
import com.by_syk.lib.nanoiconpack.util.adapter.ReqStatsAdapter;
import com.by_syk.lib.nanoiconpack.widget.DividerItemDecoration;
import com.by_syk.lib.storage.SP;
import com.by_syk.lib.toast.GlobalToast;
import com.coolapk.market.util.AuthUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.simplecityapps.recyclerview_fastscroll.interfaces.OnFastScrollStateChangeListener;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;

/**
 * Created by By_syk on 2017-01-27.
 */

public class ReqStatsFragment extends Fragment {
    private View contentView;

    private LinearLayoutManager layoutManager;
    private ReqStatsAdapter reqStatsAdapter;

    private SwipeRefreshLayout swipeRefreshLayout;

    private LazyLoadTask lazyLoadTask;

    private RetainedFragment retainedFragment;

    private String user;
    private boolean toFilter = true;
    private int limitLevel = 0;

    private static final int[] LIMIT_NUM_ARR = {32, 64, 128};

    private static Handler handler = new Handler();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            toFilter = savedInstanceState.getBoolean("toFilter", true);
            limitLevel = savedInstanceState.getInt("limitLevel", 0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (contentView == null) {
            contentView = inflater.inflate(R.layout.fragment_apps, container, false);
            init();

            (new LoadAppsTask()).execute(false);
        }

        return contentView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("toFilter", toFilter);
        outState.putInt("limitLevel", limitLevel);
    }

    private void init() {
        user = (new SP(getContext(), false)).getString("user", null);

        initAdapter();
        initRecycler();
        initSwipeRefresh();
    }

    private void initAdapter() {
        reqStatsAdapter = new ReqStatsAdapter(getContext());
        reqStatsAdapter.setOnItemClickListener(new ReqStatsAdapter.OnItemClickListener() {
            @Override
            public void onClick(int pos, AppBean bean) {
                showReqMenu(pos, bean);
            }
        });
    }

    private void initRecycler() {
        layoutManager = new LinearLayoutManager(getContext());

        FastScrollRecyclerView recyclerView = (FastScrollRecyclerView) contentView.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL));
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (lazyLoadTask == null) {
                        lazyLoadTask = new LazyLoadTask();
                        lazyLoadTask.execute(layoutManager.findFirstVisibleItemPosition(),
                                layoutManager.findLastVisibleItemPosition());
                    }
                } else if (lazyLoadTask != null) {
                    lazyLoadTask.cancel(true);
                    lazyLoadTask = null;
                }
            }
        });
        recyclerView.setStateChangeListener(new OnFastScrollStateChangeListener() {
            @Override
            public void onFastScrollStart() {
                if (lazyLoadTask != null) {
                    lazyLoadTask.cancel(true);
                    lazyLoadTask = null;
                }
            }

            @Override
            public void onFastScrollStop() {
                if (lazyLoadTask == null) {
                    lazyLoadTask = new LazyLoadTask();
                    lazyLoadTask.execute(layoutManager.findFirstVisibleItemPosition(),
                            layoutManager.findLastVisibleItemPosition());
                }
            }
        });

        recyclerView.setAdapter(reqStatsAdapter);
    }

    private void initSwipeRefresh() {
        swipeRefreshLayout = (SwipeRefreshLayout) contentView.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.color_accent));
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                (new LoadAppsTask()).execute(true);
            }
        });
    }

    private void showReqMenu(int pos, AppBean bean) {
        ReqMenuDialog reqMenuDialog = ReqMenuDialog.newInstance(pos, bean);
        reqMenuDialog.setOnMarkDoneListener(new ReqMenuDialog.OnMarkDoneListener() {
            @Override
            public void onMarkDone(int pos, AppBean bean1, boolean ok) {
                if (!ok) {
                    GlobalToast.showToast(getContext(), bean1.isMark() ? R.string.toast_mark_undo_failed
                            : R.string.toast_mark_failed);
                    return;
                }
                if (bean1.isMark()) {
                    if (toFilter) {
                        reqStatsAdapter.remove(pos);
                        if (lazyLoadTask == null) {
                            lazyLoadTask = new LazyLoadTask();
                            lazyLoadTask.execute(layoutManager.findFirstVisibleItemPosition(),
                                    layoutManager.findLastVisibleItemPosition());
                        }
                    } else {
                        reqStatsAdapter.notifyItemChanged(pos);
                    }
                    GlobalToast.showToast(getContext(), R.string.toast_marked);
                } else {
                    reqStatsAdapter.notifyItemChanged(pos);
                    GlobalToast.showToast(getContext(), R.string.toast_mark_undo);
                }
            }
        });
        reqMenuDialog.show(getFragmentManager(), "reqMenuDialog");
    }

    private void updateData(boolean toFilter, int limitLevel) {
        if (toFilter == this.toFilter && limitLevel == this.limitLevel) {
            return;
        }
        this.toFilter = toFilter;
        this.limitLevel = limitLevel;

        swipeRefreshLayout.setRefreshing(true);

        (new LoadAppsTask()).execute(true);
    }

    private class LoadAppsTask extends AsyncTask<Boolean, Integer, List<AppBean>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            retainedFragment = RetainedFragment.initRetainedFragment(getFragmentManager(), "req");
        }

        @Override
        protected List<AppBean> doInBackground(Boolean... booleans) {
            boolean forceRefresh = booleans.length > 0 && booleans[0];
            if (!forceRefresh && retainedFragment.isReqTopListSaved()) {
                return retainedFragment.getReqTopList();
            }

            if (TextUtils.isEmpty(user)) {
                return new ArrayList<>();
            }

            List<AppBean> dataList = new ArrayList<>();

            try {
                NanoServerService nanoServerService = RetrofitHelper.getInstance().getRetrofit()
                        .create(NanoServerService.class);
                Call<ResResBean<JsonArray>> call = nanoServerService.getReqTop(getContext().getPackageName(),
                        user, LIMIT_NUM_ARR[limitLevel], toFilter);
                ResResBean<JsonArray> resResBean = call.execute().body();
                if (resResBean == null || !resResBean.isStatusSuccess()
                        || resResBean.getResult() == null) {
                    return dataList;
                }
                JsonArray ja = resResBean.getResult();
                for (int i = 0, len = ja.size(); i < len; ++i) {
                    JsonObject jo = ja.get(i).getAsJsonObject();
                    AppBean bean = new AppBean();
                    bean.setLabel(jo.get("label").getAsString());
                    bean.setPkgName(jo.get("pkg").getAsString());
                    bean.setReqTimes(jo.get("sum").getAsInt());
                    bean.setMark(jo.get("filter").getAsInt() == 1);
                    dataList.add(bean);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return dataList;
        }

        @Override
        protected void onPostExecute(List<AppBean> list) {
            super.onPostExecute(list);

            retainedFragment.setReqTopList(list);

            contentView.findViewById(R.id.view_loading).setVisibility(View.GONE);

            reqStatsAdapter.refresh(list);

            swipeRefreshLayout.setRefreshing(false);

            if (getUserVisibleHint()) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!isAdded() || lazyLoadTask != null) {
                            return;
                        }
                        lazyLoadTask = new LazyLoadTask();
                        lazyLoadTask.execute(layoutManager.findFirstVisibleItemPosition(),
                                layoutManager.findLastVisibleItemPosition());
                    }
                }, 400);
            }
        }
    }

    private class LazyLoadTask extends AsyncTask<Integer, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(Integer... pos) {
            if (!isAdded() || pos == null || pos.length < 2) {
                return false;
            }

            PackageManager packageManager = getContext().getPackageManager();
            for (int i = pos[0]; i <= pos[1]; ++i) {
                if (isCancelled() || !isAdded()) {
                    return false;
                }
                AppBean bean = reqStatsAdapter.getItem(i);
                if (bean == null || bean.getIcon() != null || bean.getIconUrl() != null) {
                    continue;
                }
                try {
                    PackageInfo packageInfo = packageManager.getPackageInfo(bean.getPkgName(), 0);
                    bean.setIcon(packageInfo.applicationInfo.loadIcon(packageManager));
                    publishProgress(i);
                } catch (Exception e) {
                    Log.d(C.LOG_TAG, bean.getPkgName() + " is not installed.");
                }
            }

            if (!ExtraUtil.isNetworkConnected(getContext())) {
                return false;
            }
//            NanoServerService nanoServerService = null;
//            for (int i = pos[0]; i <= pos[1]; ++i) {
//                if (isCancelled() || !isAdded()) {
//                    return false;
//                }
//                AppBean bean = reqStatsAdapter.getItem(i);
//                if (bean == null || bean.getIcon() != null || bean.getIconUrl() != null) {
//                    continue;
//                }
//                if (nanoServerService == null) {
//                    nanoServerService = RetrofitHelper.getInstance().getRetrofit()
//                            .create(NanoServerService.class);
//                }
//                Call<ResResBean<String>> call = nanoServerService.getIconUrl(bean.getPkgName());
//                try {
//                    ResResBean<String> resResBean = call.execute().body();
//                    if (resResBean != null && resResBean.isStatusSuccess()) {
//                        bean.setIconUrl(resResBean.getResult());
//                        publishProgress(i);
//                        continue;
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                bean.setIconUrl("");
//            }
            NanoServerService nanoServerService = null;
            for (int i = pos[0]; i <= pos[1]; ++i) {
                if (isCancelled() || !isAdded()) {
                    return false;
                }
                AppBean bean = reqStatsAdapter.getItem(i);
                if (bean == null || bean.getIcon() != null || bean.getIconUrl() != null) {
                    continue;
                }
                if (nanoServerService == null) {
                    nanoServerService = RetrofitHelper.getInstance().init4Coolapk()
                            .getRetrofit4Coolapk().create(NanoServerService.class);
                }
                Call<CoolApkApkDetailBean> call = nanoServerService.getCoolApkApkDetail(AuthUtils
                        .getAS(UUID.randomUUID().toString()), bean.getPkgName());
                try {
                    CoolApkApkDetailBean apkDetailBean = call.execute().body();
                    if (apkDetailBean != null && apkDetailBean.getData() != null) {
                        bean.setIconUrl(apkDetailBean.getData().iconUrl);
                        publishProgress(i);
                        continue;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bean.setIconUrl("");
            }

            return false;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            reqStatsAdapter.notifyItemChanged(values[0]);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            lazyLoadTask = null;
        }
    }

    public static ReqStatsFragment newInstance() {
        return new ReqStatsFragment();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_req_stats, menu);

        menu.getItem(0).setChecked(!toFilter);
        menu.getItem(1).getSubMenu().getItem(limitLevel).setChecked(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_include_filter) {
            updateData(item.isChecked(), limitLevel);
            item.setChecked(!item.isChecked());
            return true;
        } else if (id == R.id.menu_top_32) {
            item.setChecked(true);
            updateData(toFilter, 0);
            return true;
        } else if (id == R.id.menu_top_64) {
            item.setChecked(true);
            updateData(toFilter, 1);
            return true;
        } else if (id == R.id.menu_top_128) {
            item.setChecked(true);
            updateData(toFilter, 2);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
