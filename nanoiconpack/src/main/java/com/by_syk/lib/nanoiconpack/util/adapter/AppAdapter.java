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

package com.by_syk.lib.nanoiconpack.util.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.andraskindler.quickscroll.Scrollable;
import com.by_syk.lib.nanoiconpack.R;
import com.by_syk.lib.nanoiconpack.bean.AppBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by By_syk on 2017-01-27.
 */

public class AppAdapter extends BaseAdapter implements Scrollable {
    private LayoutInflater layoutInflater;

    private List<AppBean> dataList = new ArrayList<>();

    public AppAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public AppBean getItem(int i) {
        return dataList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        /*
         * 使用ViewHolder模式来避免没有必要的调用findViewById()：因为太多的findViewById也会影响性能
         * ViewHolder模式通过getView()方法返回的视图的标签(Tag)中存储一个数据结构，
         * 这个数据结构包含了指向我们要绑定数据的视图的引用，从而避免每次调用getView()的时候调用findViewById()
         */
        ViewHolder viewHolder;

        // 重用缓存convertView传递给getView()方法来避免填充不必要的视图
        if (convertView == null) {
            /* 避免这样使用：
             *     layoutInflater.inflate(R.layout.list_item, null);
             * 查看
             *     https://possiblemobile.com/2013/05/layout-inflation-as-intended/
             */
            convertView = layoutInflater.inflate(R.layout.item_app, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
            viewHolder.tvApp = (TextView) convertView.findViewById(R.id.tv_app);
            viewHolder.tvComponent = (TextView) convertView.findViewById(R.id.tv_component);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        AppBean bean = dataList.get(position);
        String component = bean.getPkgName() + "/";
        if (bean.getLauncherActivity().startsWith(bean.getPkgName())) {
            component += bean.getLauncherActivity().substring(bean.getPkgName().length());
        } else {
            component += bean.getLauncherActivity();
        }

        viewHolder.ivIcon.setImageDrawable(bean.getIcon());
        viewHolder.tvApp.setText(bean.getLabel());
        viewHolder.tvComponent.setText(component);

        return convertView;
    }

    @Override
    public String getIndicatorForPosition(int childPosition, int groupPosition) {
        return dataList.get(childPosition).getLabelPinyin().substring(0, 1).toUpperCase();
    }

    @Override
    public int getScrollPosition(int childPosition, int groupPosition) {
        return childPosition;
    }

    public void refresh(List<AppBean> dataList) {
        if (dataList != null) {
            this.dataList.clear();
            this.dataList.addAll(dataList);

            notifyDataSetChanged();
        }
    }

    private static class ViewHolder {
        ImageView ivIcon;
        TextView tvApp;
        TextView tvComponent;
    }
}
