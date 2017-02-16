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
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.by_syk.lib.nanoiconpack.R;
import com.by_syk.lib.nanoiconpack.bean.AppBean;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by By_syk on 2017-01-27.
 */

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.IconViewHolder>
    implements FastScrollRecyclerView.SectionedAdapter {
    private LayoutInflater layoutInflater;

    private List<AppBean> dataList = new ArrayList<>();
    private boolean[] tags = new boolean[0];

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onClick(int pos, AppBean bean);
        void onLongClick(int pos, AppBean bean);
    }

    public AppAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public IconViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View contentView = layoutInflater.inflate(R.layout.item_app, parent, false);
        return new IconViewHolder(contentView);
    }

    @Override
    public void onBindViewHolder(IconViewHolder holder, int position) {
        AppBean bean = dataList.get(position);
        String component = bean.getPkgName() + "/";
        if (bean.getLauncherActivity().startsWith(bean.getPkgName())) {
            component += bean.getLauncherActivity().substring(bean.getPkgName().length());
        } else {
            component += bean.getLauncherActivity();
        }

        holder.viewTag.setVisibility(tags[position] ? View.VISIBLE : View.GONE);
        holder.ivIcon.setImageDrawable(bean.getIcon());
        holder.tvApp.setText(bean.getLabel());
        holder.tvComponent.setText(component);

        if (onItemClickListener != null) {
            final int INDEX = position;
            holder.viewRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onClick(INDEX, dataList.get(INDEX));
                }
            });
            holder.viewRoot.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    onItemClickListener.onLongClick(INDEX, dataList.get(INDEX));
                    return true;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return dataList.get(position).getLabelPinyin().substring(0, 1).toUpperCase();
    }

    public void refresh(List<AppBean> dataList) {
        if (dataList != null) {
            this.dataList.clear();
            this.dataList.addAll(dataList);
            tags = new boolean[dataList.size()];

            notifyDataSetChanged();
        }
    }

    public void tag(int pos) {
        if (!tags[pos]) {
            tags[pos] = true;
            notifyItemChanged(pos);
        }
    }

    public void clearTags() {
        for (int i = 0, len = tags.length; i < len; ++i) {
            if (tags[i]) {
                tags[i] = false;
                notifyItemChanged(i);
            }
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    static class IconViewHolder extends RecyclerView.ViewHolder {
        View viewRoot;
        View viewTag;
        ImageView ivIcon;
        TextView tvApp;
        TextView tvComponent;

        IconViewHolder(View itemView) {
            super(itemView);

            viewRoot = itemView;
            viewTag = itemView.findViewById(R.id.view_tag);
            ivIcon = (ImageView) itemView.findViewById(R.id.iv_icon);
            tvApp = (TextView) itemView.findViewById(R.id.tv_app);
            tvComponent = (TextView) itemView.findViewById(R.id.tv_component);
        }
    }
}
