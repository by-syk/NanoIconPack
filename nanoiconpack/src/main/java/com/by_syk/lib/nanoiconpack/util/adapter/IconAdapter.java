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
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.by_syk.lib.nanoiconpack.R;
import com.by_syk.lib.nanoiconpack.bean.IconBean;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by By_syk on 2017-01-27.
 */

public class IconAdapter extends RecyclerView.Adapter
        implements FastScrollRecyclerView.SectionedAdapter {
    private LayoutInflater layoutInflater;

    private RequestManager glideReqManager;

    private List<IconBean> dataList = new ArrayList<>();

    private int gridSize = -1;

    private int mode = MODE_ICON;
    public static final int MODE_ICON = 0;
    public static final int MODE_ICON_LABEL = 1;

    @IntDef({MODE_ICON, MODE_ICON_LABEL})
    @Retention(RetentionPolicy.SOURCE)
    public  @interface Mode {}

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onClick(int pos, IconBean bean);
    }

    public IconAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);

        glideReqManager = Glide.with(context);
    }

    public IconAdapter(Context context, int gridSize) {
        this(context);

        this.gridSize = gridSize;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View contentView;
        RecyclerView.ViewHolder viewHolder;
        if (viewType == MODE_ICON_LABEL) {
            contentView = layoutInflater.inflate(R.layout.item_icon_label, parent, false);
            viewHolder = new IconLabelViewHolder(contentView);
        } else {
            contentView = layoutInflater.inflate(R.layout.item_icon, parent, false);
            viewHolder = new IconViewHolder(contentView);
        }

        if (gridSize > 0) {
            ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
            layoutParams.width = gridSize;
            layoutParams.height = gridSize;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof IconLabelViewHolder) {
            IconLabelViewHolder viewHolder = (IconLabelViewHolder) holder;
//            viewHolder.ivIcon.setImageResource(dataList.get(position).getId());
            glideReqManager.load(dataList.get(position).getId())
                    .apply(new RequestOptions().fitCenter())
                    .into(viewHolder.ivIcon);
            viewHolder.tvLabel.setText(dataList.get(position).getLabel());
        } else {
            IconViewHolder viewHolder = (IconViewHolder) holder;
//            viewHolder.ivIcon.setImageResource(dataList.get(position).getId());
            glideReqManager.load(dataList.get(position).getId())
                    .apply(new RequestOptions().fitCenter())
                    .into(viewHolder.ivIcon);
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

    @Override
    public int getItemViewType(int position) {
        return mode;
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return dataList.get(position).getLabelPinyin().substring(0, 1).toUpperCase();
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void switchMode(int mode) {
        if (mode == this.mode) {
            return;
        }

        this.mode = mode;
        notifyDataSetChanged();
    }

    public void refresh(List<IconBean> dataList) {
        if (dataList != null) {
            this.dataList.clear();
            this.dataList.addAll(dataList);

            notifyDataSetChanged();
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    static class IconViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivIcon;

        IconViewHolder(View itemView) {
            super(itemView);

            ivIcon = (ImageView) itemView.findViewById(R.id.iv_icon);
        }
    }

    private static class IconLabelViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivIcon;
        private TextView tvLabel;

        IconLabelViewHolder(View itemView) {
            super(itemView);

            ivIcon = (ImageView) itemView.findViewById(R.id.iv_icon);
            tvLabel = (TextView) itemView.findViewById(R.id.tv_label);
        }
    }
}
