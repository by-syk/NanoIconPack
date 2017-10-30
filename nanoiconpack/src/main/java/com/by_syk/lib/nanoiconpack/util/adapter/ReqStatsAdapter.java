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
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.by_syk.lib.nanoiconpack.R;
import com.by_syk.lib.nanoiconpack.bean.AppBean;
import com.by_syk.lib.nanoiconpack.util.ExtraUtil;
import com.by_syk.lib.nanoiconpack.util.PkgUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by By_syk on 2017-01-27.
 */

public class ReqStatsAdapter extends RecyclerView.Adapter<ReqStatsAdapter.IconViewHolder> {
    private Context context;

    private LayoutInflater layoutInflater;

    private List<AppBean> dataList = new ArrayList<>();

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onClick(int pos, AppBean bean);
    }

    public ReqStatsAdapter(Context context) {
        this.context = context;

        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public IconViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View contentView = layoutInflater.inflate(R.layout.item_req_stats, parent, false);
        return new IconViewHolder(contentView);
    }

    @Override
    public void onBindViewHolder(final IconViewHolder holder, int position) {
        AppBean bean = dataList.get(position);

        holder.viewTag.setBackgroundResource(bean.isMark() ? R.drawable.tag_redraw : 0);
//        holder.ivIcon.setImageDrawable(bean.getIcon());
        if (bean.getIcon() != null) {
            holder.ivIcon.setImageDrawable(bean.getIcon());
        } else {
            holder.ivIcon.setImageResource(0);
            if (!TextUtils.isEmpty(bean.getIconUrl())) {
                Glide.with(context)
                        .load(bean.getIconUrl())
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(holder.ivIcon);
            }
        }
        holder.tvApp.setText(bean.getLabel());
        holder.tvComponent.setText(PkgUtil.concatComponent(bean.getPkg(), bean.getLauncher()));
        if (bean.getReqTimes() >= 0) {
            holder.tvReqTimes.setText(ExtraUtil.renderReqTimes(bean.getReqTimes()));
        } else {
            holder.tvReqTimes.setText("");
        }
        if (bean.isHintMark()) {
            holder.viewHint.setVisibility(View.VISIBLE);
            holder.viewHint.setBackground(context.getDrawable(R.drawable.dot_green));
        } else if (bean.isHintUndoMark()) {
            holder.viewHint.setVisibility(View.VISIBLE);
            holder.viewHint.setBackground(context.getDrawable(R.drawable.dot_red));
        } else if (bean.isHintLost()) {
            holder.viewHint.setVisibility(View.VISIBLE);
            holder.viewHint.setBackground(context.getDrawable(R.drawable.dot_red));
        } else {
            holder.viewHint.setVisibility(View.GONE);
        }

        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = holder.getAdapterPosition();
                    onItemClickListener.onClick(pos, dataList.get(pos));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Nullable
    public AppBean getItem(int pos) {
        if (pos >= 0 && pos < dataList.size()) {
            return dataList.get(pos);
        }
        return null;
    }

    public void refresh(List<AppBean> dataList) {
        if (dataList == null) {
            return;
        }

        for (int i = 0, len = dataList.size(); i < len; ++i) {
            AppBean newBean = dataList.get(i);
            for (AppBean oldBean : this.dataList) {
                if (newBean.getPkg().equals(oldBean.getPkg())) {
                    newBean.setIcon(oldBean.getIcon());
                    newBean.setIconUrl(oldBean.getIconUrl());
                    break;
                }
            }
        }

        this.dataList.clear();
        this.dataList.addAll(dataList);
        notifyDataSetChanged();
    }

    public void remove(int pos) {
        if (pos < 0 || pos >= dataList.size()) {
            return;
        }
        dataList.remove(pos);
        notifyItemRemoved(pos);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    static class IconViewHolder extends RecyclerView.ViewHolder {
        View viewTag;
        ImageView ivIcon;
        TextView tvApp;
        TextView tvComponent;
        TextView tvReqTimes;
        View viewHint;

        IconViewHolder(View itemView) {
            super(itemView);

            viewTag = itemView.findViewById(R.id.view_tag);
            ivIcon = (ImageView) itemView.findViewById(R.id.iv_icon);
            tvApp = (TextView) itemView.findViewById(R.id.tv_app);
            tvComponent = (TextView) itemView.findViewById(R.id.tv_component);
            tvReqTimes = (TextView) itemView.findViewById(R.id.tv_req_times);
            viewHint = itemView.findViewById(R.id.view_hint);
        }
    }
}
