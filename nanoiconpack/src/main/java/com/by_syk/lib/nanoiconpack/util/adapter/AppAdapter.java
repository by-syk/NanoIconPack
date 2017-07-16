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
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.by_syk.lib.nanoiconpack.R;
import com.by_syk.lib.nanoiconpack.bean.AppBean;
import com.by_syk.lib.nanoiconpack.util.ExtraUtil;
import com.by_syk.lib.nanoiconpack.util.PkgUtil;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by By_syk on 2017-01-27.
 */

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.IconViewHolder>
        implements FastScrollRecyclerView.SectionedAdapter,
        View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
    private LayoutInflater layoutInflater;

    private List<AppBean> dataList = new ArrayList<>();

    private boolean enableStatsModule = true;

    private int contextMenuActiveItemPos = -1;

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onReqIcon(int pos, AppBean bean);
        void onCopyCode(int pos, AppBean bean);
        void onSaveIcon(int pos, AppBean bean);
    }

    public AppAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);

        enableStatsModule = context.getResources().getBoolean(R.bool.enable_req_stats_module);
    }

    @Override
    public IconViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View contentView = layoutInflater.inflate(R.layout.item_app, parent, false);
        return new IconViewHolder(contentView);
    }

    @Override
    public void onBindViewHolder(final IconViewHolder holder, int position) {
        AppBean bean = dataList.get(position);

        holder.viewTag.setBackgroundResource(bean.isMark() ? R.drawable.tag_req : 0);
        holder.ivIcon.setImageDrawable(bean.getIcon());
//        if (bean.getIcon() != null) {
//            holder.ivIcon.setImageDrawable(bean.getIcon());
//        } else {
//            holder.ivIcon.setImageResource(android.R.drawable.sym_def_app_icon);
//        }
        holder.tvApp.setText(bean.getLabel());
        holder.tvComponent.setText(PkgUtil.concatComponent(bean.getPkg(), bean.getLauncher()));
        if (bean.getReqTimes() >= 0) {
            holder.tvReqTimes.setText(ExtraUtil.renderReqTimes(bean.getReqTimes()));
        } else {
            holder.tvReqTimes.setText("");
        }

        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = holder.getAdapterPosition();
                    if (enableStatsModule) {
                        onItemClickListener.onReqIcon(pos, dataList.get(pos));
                    } else {
                        onItemClickListener.onCopyCode(pos, dataList.get(pos));
                    }
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
//                    int pos = holder.getAdapterPosition();
//                    onItemClickListener.onLongClick(pos, dataList.get(pos));
//                    return true;
                    contextMenuActiveItemPos = holder.getAdapterPosition();
                    return false;
                }
            });
            // OnLongClickListener -> onCreateContextMenu -> onMenuItemClick
            holder.itemView.setOnCreateContextMenuListener(this);
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

    @Override
    public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
        contextMenu.setHeaderTitle(dataList.get(contextMenuActiveItemPos).getLabel());
        contextMenu.add(Menu.NONE, 0, Menu.NONE, R.string.menu_request_icon);
        contextMenu.add(Menu.NONE, 1, Menu.NONE, R.string.menu_copy_code);
        contextMenu.add(Menu.NONE, 2, Menu.NONE, R.string.menu_save_icon);
        contextMenu.getItem(0).setOnMenuItemClickListener(this);
        contextMenu.getItem(1).setOnMenuItemClickListener(this);
        contextMenu.getItem(2).setOnMenuItemClickListener(this);

        contextMenu.getItem(0).setVisible(enableStatsModule);
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        if (onItemClickListener == null) {
            return true;
        }
        switch (menuItem.getItemId()) {
            case 0:
                onItemClickListener.onReqIcon(contextMenuActiveItemPos,
                        dataList.get(contextMenuActiveItemPos));
                break;
            case 1:
                onItemClickListener.onCopyCode(contextMenuActiveItemPos,
                        dataList.get(contextMenuActiveItemPos));
                break;
            case 2:
                onItemClickListener.onSaveIcon(contextMenuActiveItemPos,
                        dataList.get(contextMenuActiveItemPos));
                break;
        }
        return true;
    }

    @Nullable
    public AppBean getItem(int pos) {
        if (pos >= 0 && pos < dataList.size()) {
            return dataList.get(pos);
        }
        return null;
    }

    public void refresh(List<AppBean> dataList) {
        if (dataList != null) {
            this.dataList.clear();
            this.dataList.addAll(dataList);

            notifyDataSetChanged();
        }
    }

//    public void updateTag(int pos) {
//        if (!copiedArr[pos]) {
//            copiedArr[pos] = true;
//            notifyItemChanged(pos);
//        }
//    }
//
//    public void clearTags() {
//        for (int i = 0, len = copiedArr.length; i < len; ++i) {
//            if (copiedArr[i]) {
//                copiedArr[i] = false;
//                notifyItemChanged(i);
//            }
//        }
//    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    static class IconViewHolder extends RecyclerView.ViewHolder {
        View viewTag;
        ImageView ivIcon;
        TextView tvApp;
        TextView tvComponent;
        TextView tvReqTimes;

        IconViewHolder(View itemView) {
            super(itemView);

            viewTag = itemView.findViewById(R.id.view_tag);
            ivIcon = (ImageView) itemView.findViewById(R.id.iv_icon);
            tvApp = (TextView) itemView.findViewById(R.id.tv_app);
            tvComponent = (TextView) itemView.findViewById(R.id.tv_component);
            tvReqTimes = (TextView) itemView.findViewById(R.id.tv_req_times);
        }
    }
}
