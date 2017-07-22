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

package com.by_syk.lib.nanoiconpack;

import android.app.SearchManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;

import com.by_syk.lib.nanoiconpack.bean.IconBean;
import com.by_syk.lib.nanoiconpack.dialog.IconDialog;
import com.by_syk.lib.nanoiconpack.util.AllIconsGetter;
import com.by_syk.lib.nanoiconpack.util.adapter.IconAdapter;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by By_syk on 2017-07-15.
 */

public class SearchActivity extends AppCompatActivity {
    private SearchView searchView;

    private IconAdapter adapter;

    private List<IconBean> dataList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        init();

        (new LoadDataTask()).execute();
    }

    private void init() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        int[] gridNumAndWidth = calculateGridNumAndWidth();
        recyclerView.setLayoutManager(new GridLayoutManager(this, gridNumAndWidth[0]));

        adapter = new IconAdapter(this, gridNumAndWidth[1]);
        adapter.setMode(IconAdapter.MODE_ICON_LABEL);
        adapter.setOnItemClickListener(new IconAdapter.OnItemClickListener() {
            @Override
            public void onClick(int pos, IconBean bean) {
                searchView.clearFocus();
                IconDialog.newInstance(bean, false).show(getSupportFragmentManager(), "iconDialog");
            }
        });
        recyclerView.setAdapter(adapter);
    }

    private int[] calculateGridNumAndWidth() {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int totalWidth = displayMetrics.widthPixels;

        int minGridSize = getResources().getDimensionPixelSize(R.dimen.grid_size);
        int num = totalWidth / minGridSize;

        return new int[]{num, totalWidth / num};
    }

    private void initSearchView() {
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconified(false);
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                (new SearchTask()).execute(newText);
                return false;
            }
        });
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                finish();
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        initSearchView();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class LoadDataTask extends AsyncTask<String, Integer, List<IconBean>> {
        @Override
        protected List<IconBean> doInBackground(String... strings) {
            try {
                return (new AllIconsGetter()).getIcons(SearchActivity.this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return new ArrayList<>();
        }

        @Override
        protected void onPostExecute(List<IconBean> list) {
            super.onPostExecute(list);

            dataList = list;

            ((AVLoadingIndicatorView) findViewById(R.id.view_loading)).hide();
        }
    }

    private class SearchTask extends AsyncTask<String, Integer, List<IconBean>> {
        @Override
        protected List<IconBean> doInBackground(String... strings) {
            String keyword = strings[0];
            if (TextUtils.isEmpty(keyword)) {
                return new ArrayList<>();
            }

            List<IconBean> result = new ArrayList<>();

            for (IconBean bean : dataList) {
                boolean findComponent = false;
                for (IconBean.Component component : bean.getComponents()) {
                    if (component.getPkg().equals(keyword) || (component.getLabel() != null
                            && component.getLabel().contains(keyword))) {
                        findComponent = true;
                        result.add(bean);
                        break;
                    }
                }
                if (findComponent) {
                    continue;
                }
                if (bean.getLabel() != null && bean.getLabel().contains(keyword)) {
                    result.add(bean);
                } else if (bean.getName().contains(keyword)) {
                    result.add(bean);
                }
            }

            Collections.sort(result);
            return result;
        }

        @Override
        protected void onPostExecute(List<IconBean> list) {
            super.onPostExecute(list);

            adapter.refresh(list);
        }
    }
}
