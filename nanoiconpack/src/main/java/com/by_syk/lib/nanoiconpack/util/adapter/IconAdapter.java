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

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.by_syk.lib.nanoiconpack.R;
import com.by_syk.lib.nanoiconpack.bean.IconBean;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by By_syk on 2017-01-27.
 */

public class IconAdapter extends RecyclerView.Adapter<IconAdapter.IconViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter {
    private LayoutInflater layoutInflater;

//    private RequestManager requestManager;

    private int gridSIze = -1;

    private List<IconBean> dataList = new ArrayList<>();

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onClick(int pos, IconBean bean);
        void onLongClick(int pos, IconBean bean);
    }

    public IconAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);

//        requestManager = Glide.with(context);
    }

    public IconAdapter(Context context, int gridSIze) {
        this(context);

        this.gridSIze = gridSIze;
    }

    @Override
    public IconViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View contentView = layoutInflater.inflate(R.layout.item_icon, parent, false);

        if (gridSIze > 0) {
            ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
            layoutParams.width = gridSIze;
            layoutParams.height = gridSIze;
        }

        return new IconViewHolder(contentView);
    }

    @Override
    public void onBindViewHolder(final IconViewHolder holder, int position) {
        holder.ivIcon.setImageResource(dataList.get(position).getId());
//        requestManager.load(dataList.get(position).getId()).into(holder.ivIcon);

        if (onItemClickListener != null) {
            holder.ivIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = holder.getAdapterPosition();
                    onItemClickListener.onClick(pos, dataList.get(pos));
                }
            });
            holder.ivIcon.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    int pos = holder.getAdapterPosition();
                    onItemClickListener.onLongClick(pos, dataList.get(pos));
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
        // TODO Try highlighting index icon here?

//        IconBean bean = dataList.get(position);
//        String indexStr = bean.getLabelPinyin().substring(0, 1).toUpperCase();
//        String character = bean.getLabel();
//        if (!TextUtils.isEmpty(character)) {
//            character = character.substring(0, 1).toUpperCase();
//            if (!character.equals(indexStr)) {
//                indexStr = character + " " + indexStr;
//            }
//        }
//        return indexStr;

        return dataList.get(position).getLabelPinyin().substring(0, 1).toUpperCase();
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
}
