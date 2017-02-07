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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.by_syk.lib.nanoiconpack.R;
import com.by_syk.lib.nanoiconpack.bean.IconBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by By_syk on 2017-01-27.
 */

public class IconAdapter extends RecyclerView.Adapter<IconAdapter.IconViewHolder>
        /*implements SectionTitleProvider*/ {
    private LayoutInflater layoutInflater;

    private List<IconBean> dataList = new ArrayList<>();

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onClick(int pos, IconBean bean);
        void onLongClick(int pos, IconBean bean);
    }

    public IconAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public IconViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View contentView = layoutInflater.inflate(R.layout.item_icon, parent, false);
        return new IconViewHolder(contentView);
    }

    @Override
    public void onBindViewHolder(IconViewHolder holder, int position) {
        holder.ivIcon.setImageResource(dataList.get(position).getId());

        if (onItemClickListener != null) {
            final int INDEX = position;
            holder.ivIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onItemClickListener.onClick(INDEX, dataList.get(INDEX));
                }
            });
            holder.ivIcon.setOnLongClickListener(new View.OnLongClickListener() {
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

//    @Override
//    public String getSectionTitle(int position) {
//        return dataList.get(position).getLabelPinyin().substring(0, 1).toUpperCase();
//    }

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
}
