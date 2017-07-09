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

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.by_syk.lib.globaltoast.GlobalToast;
import com.by_syk.lib.nanoiconpack.R;
import com.by_syk.lib.nanoiconpack.bean.AppBean;
import com.by_syk.lib.nanoiconpack.bean.CoolApkApkDetailBean;
import com.by_syk.lib.nanoiconpack.bean.ReqTopBean;
import com.by_syk.lib.nanoiconpack.bean.ResResBean;
import com.by_syk.lib.nanoiconpack.dialog.ReqMenuDialog;
import com.by_syk.lib.nanoiconpack.util.AppfilterReader;
import com.by_syk.lib.nanoiconpack.util.ExtraUtil;
import com.by_syk.lib.nanoiconpack.util.PkgUtil;
import com.by_syk.lib.nanoiconpack.util.RetrofitHelper;
import com.by_syk.lib.nanoiconpack.util.impl.CoolApkServerService;
import com.by_syk.lib.nanoiconpack.util.impl.NanoServerService;
import com.by_syk.lib.nanoiconpack.util.adapter.ReqStatsAdapter;
import com.by_syk.lib.nanoiconpack.widget.DividerItemDecoration;
import com.by_syk.lib.sp.SP;
import com.coolapk.market.util.AuthUtils;
import com.simplecityapps.recyclerview_fastscroll.interfaces.OnFastScrollStateChangeListener;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;
import com.wang.avi.AVLoadingIndicatorView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
    private int filterType = 0;
    private int limitLevel = 0;

    private static final int[] LIMIT_NUM_ARR = {32, 64, 128};

    private static Handler handler = new Handler();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            filterType = savedInstanceState.getInt("filterType", 0);
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

        outState.putInt("filterType", filterType);
        outState.putInt("limitLevel", limitLevel);
    }

    private void init() {
        user = (new SP(getContext())).getString("user", null);

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
        swipeRefreshLayout.setColorSchemeColors(ExtraUtil.fetchColor(getContext(), R.attr.colorAccent));
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
                    GlobalToast.show(getContext(), bean1.isMark() ? R.string.toast_mark_undo_failed
                            : R.string.toast_mark_failed);
                    return;
                }
                reqStatsAdapter.remove(pos);
                if (lazyLoadTask == null) {
                    lazyLoadTask = new LazyLoadTask();
                    lazyLoadTask.execute(layoutManager.findFirstVisibleItemPosition(),
                            layoutManager.findLastVisibleItemPosition());
                }
                GlobalToast.show(getContext(),
                        bean1.isMark() ? R.string.toast_marked : R.string.toast_mark_undo);
            }
        });
        reqMenuDialog.show(getFragmentManager(), "reqMenuDialog");
    }

    private void updateData(int filterType, int limitLevel) {
        if (filterType == this.filterType && limitLevel == this.limitLevel) {
            return;
        }
        this.filterType = filterType;
        this.limitLevel = limitLevel;

        swipeRefreshLayout.setRefreshing(true);

        (new LoadAppsTask()).execute(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_req_stats, menu);

        menu.findItem(R.id.menu_filter).getSubMenu().getItem(filterType).setChecked(true);
        menu.findItem(R.id.menu_top).getSubMenu().getItem(limitLevel).setChecked(true);
        menu.findItem(R.id.menu_top).setVisible(filterType == 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_show_unmarked) {
            updateData(0, limitLevel);
            getActivity().invalidateOptionsMenu();
            return true;
        } else if (id == R.id.menu_show_marked) {
            updateData(1, limitLevel);
            getActivity().invalidateOptionsMenu();
            return true;
        } else if (id == R.id.menu_top_32) {
            item.setChecked(true);
            updateData(filterType, 0);
            return true;
        } else if (id == R.id.menu_top_64) {
            item.setChecked(true);
            updateData(filterType, 1);
            return true;
        } else if (id == R.id.menu_top_128) {
            item.setChecked(true);
            updateData(filterType, 2);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static ReqStatsFragment newInstance() {
        return new ReqStatsFragment();
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
                NanoServerService nanoServerService = RetrofitHelper.getInstance()
                        .getService(NanoServerService.class);
                Call<ResResBean<List<ReqTopBean>>> call;
                if (filterType == 1) {
                    call = nanoServerService.getReqTopMarked(getContext().getPackageName(), user);
                } else /*if (filterType == 0)*/ {
                    call = nanoServerService.getReqTop(getContext().getPackageName(),
                            user, LIMIT_NUM_ARR[limitLevel], true);
                }
                ResResBean<List<ReqTopBean>> resResBean = call.execute().body();
                if (resResBean == null || !resResBean.isStatusSuccess()
                        || resResBean.getResult() == null) {
                    return dataList;
                }
                List<ReqTopBean> reqTopBeanList = resResBean.getResult();
                for (ReqTopBean reqTopBean : reqTopBeanList) {
                    AppBean bean = new AppBean();
                    bean.setLabel(reqTopBean.getAppLabel());
                    bean.setPkg(reqTopBean.getPkg());
                    bean.setLauncher(reqTopBean.getLauncher());
                    bean.setReqTimes(reqTopBean.getReqTimes());
                    bean.setMark(reqTopBean.isMarked());
                    dataList.add(bean);
                }

                checkMatched(dataList);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return dataList;
        }

        @Override
        protected void onPostExecute(List<AppBean> list) {
            super.onPostExecute(list);

            retainedFragment.setReqTopList(list);

            ((AVLoadingIndicatorView) contentView.findViewById(R.id.view_loading)).hide();

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

        private void checkMatched(@NonNull List<AppBean> appList) {
            if (appList.isEmpty()) {
                return;
            }

            AppfilterReader reader = AppfilterReader.getInstance(getResources());
            Set<String> pkgSet = reader.getPkgSet();
            Set<String> pkgLauncherSet = reader.getComponentSet();
            for (AppBean appBean : appList) {
                String component = appBean.getPkg() + "/" + appBean.getLauncher();
                if (appBean.isMark()) {
                    if (!pkgLauncherSet.contains(component)) {
                        appBean.setHintUndoMark(true);
                    }
                } else {
                    if (pkgSet.contains(appBean.getPkg())) {
                        if (pkgLauncherSet.contains(component)) {
                            appBean.setHintMark(true);
                        } else {
                            appBean.setHintLost(true);
                        }
                    }
                }
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
                Drawable icon = PkgUtil.getIcon(packageManager, bean.getPkg());
                if (icon != null) {
                    bean.setIcon(icon);
                    publishProgress(i);
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
//                Call<ResResBean<String>> call = nanoServerService.getIconUrl(bean.getPkg());
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
            CoolApkServerService serverService = null;
            for (int i = pos[0]; i <= pos[1]; ++i) {
                if (isCancelled() || !isAdded()) {
                    return false;
                }
                AppBean bean = reqStatsAdapter.getItem(i);
                if (bean == null || bean.getIcon() != null || bean.getIconUrl() != null) {
                    continue;
                }
                if (serverService == null) {
                    serverService = RetrofitHelper.getInstance().init4Coolapk()
                            .getService4Coolapk(CoolApkServerService.class);
                }
                Call<CoolApkApkDetailBean> call = serverService.getCoolApkApkDetail(AuthUtils
                        .getAS(UUID.randomUUID().toString()), bean.getPkg());
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
}
