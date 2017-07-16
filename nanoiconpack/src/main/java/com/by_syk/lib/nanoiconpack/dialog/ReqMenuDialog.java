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

package com.by_syk.lib.nanoiconpack.dialog;

import android.app.Dialog;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.by_syk.lib.globaltoast.GlobalToast;
import com.by_syk.lib.nanoiconpack.R;
import com.by_syk.lib.nanoiconpack.bean.AppBean;
import com.by_syk.lib.nanoiconpack.bean.CodeBean;
import com.by_syk.lib.nanoiconpack.bean.ResResBean;
import com.by_syk.lib.nanoiconpack.util.C;
import com.by_syk.lib.nanoiconpack.util.ExtraUtil;
import com.by_syk.lib.nanoiconpack.util.RetrofitHelper;
import com.by_syk.lib.nanoiconpack.util.impl.NanoServerService;
import com.by_syk.lib.sp.SP;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by By_syk on 2017-02-26.
 */

public class ReqMenuDialog extends BottomSheetDialogFragment implements View.OnClickListener {
    private View contentView;

    private int pos;
    private AppBean bean;

    private OnMarkDoneListener onMarkDoneListener;

    public interface OnMarkDoneListener {
        void onMarkDone(int pos, AppBean bean, boolean ok);
    }

    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        Bundle bundle = getArguments();
        pos = bundle.getInt("pos");
        bean = (AppBean) bundle.getSerializable("bean");

        contentView = View.inflate(getContext(), R.layout.fragment_req_menu, null);
        dialog.setContentView(contentView);

        ((TextView) contentView.findViewById(R.id.tv_title)).setText(bean.getLabel());
        if (bean.isMark()) {
            contentView.findViewById(R.id.view_menu_mark).setVisibility(View.GONE);
            contentView.findViewById(R.id.view_menu_undo_mark).setOnClickListener(this);
            contentView.findViewById(R.id.view_hint_undo_mark)
                    .setVisibility(bean.isHintUndoMark() ? View.VISIBLE : View.GONE);
        } else {
            contentView.findViewById(R.id.view_menu_undo_mark).setVisibility(View.GONE);
            contentView.findViewById(R.id.view_menu_mark).setOnClickListener(this);
            contentView.findViewById(R.id.view_hint_mark)
                    .setVisibility(bean.isHintMark() ? View.VISIBLE : View.GONE);
        }
        contentView.findViewById(R.id.view_hint_lost)
                .setVisibility(bean.isHintLost() ? View.VISIBLE : View.GONE);
        contentView.findViewById(R.id.view_menu_goto_market).setOnClickListener(this);
        contentView.findViewById(R.id.view_menu_copy_code).setOnClickListener(this);

        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior
                .from((View) contentView.getParent());
        if (bean.isHintLost()) {
            // In landscape, STATE_EXPANDED doesn't make sheet expanded.
            // Maybe it's a bug. So do this to fix it.
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                contentView.measure(0, 0);
                bottomSheetBehavior.setPeekHeight(contentView.getMeasuredHeight());
            }
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        } else {
            bottomSheetBehavior.setPeekHeight(getResources()
                    .getDimensionPixelSize(R.dimen.req_bottom_menu_height));
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // To avoid crashing
        dismiss();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.view_menu_mark) {
            if (contentView.findViewById(R.id.pb_marking).getVisibility() != View.VISIBLE) {
                mark();
            }
        } else if (id == R.id.view_menu_undo_mark) {
            if (contentView.findViewById(R.id.pb_marking).getVisibility() != View.VISIBLE) {
                undoMark();
            }
        } else if (id == R.id.view_menu_goto_market) {
            ExtraUtil.gotoMarket(getContext(), bean.getPkg(), false);
            dismiss();
        } else if (id == R.id.view_menu_copy_code) {
            copyCode();
        }
    }

    private void mark() {
        contentView.findViewById(R.id.pb_marking).setVisibility(View.VISIBLE);

        String user = (new SP(getContext())).getString("user");

        NanoServerService nanoServerService = RetrofitHelper.getInstance()
                .getService(NanoServerService.class);
        Call<ResResBean> call = nanoServerService.filterPkg(getContext().getPackageName(),
                user, bean.getPkg(), bean.getLauncher());
        call.enqueue(new Callback<ResResBean>() {
            @Override
            public void onResponse(Call<ResResBean> call, Response<ResResBean> response) {
                ResResBean resResBean = response.body();
                if (resResBean != null && (resResBean.getStatus() == ResResBean.STATUS_SUCCESS
                        || resResBean.getStatus() == ResResBean.STATUS_EXISTED)) {
                    bean.setMark(true);
                    if (onMarkDoneListener != null) {
                        onMarkDoneListener.onMarkDone(pos, bean, true);
                    }
                } else if (onMarkDoneListener != null) {
                    onMarkDoneListener.onMarkDone(pos, bean, false);
                }
                dismiss();
            }

            @Override
            public void onFailure(Call<ResResBean> call, Throwable t) {
                if (onMarkDoneListener != null) {
                    onMarkDoneListener.onMarkDone(pos, bean, false);
                }
                dismiss();
            }
        });
    }

    private void undoMark() {
        contentView.findViewById(R.id.pb_undo_marking).setVisibility(View.VISIBLE);

        String user = (new SP(getContext())).getString("user");

        NanoServerService nanoServerService = RetrofitHelper.getInstance()
                .getService(NanoServerService.class);
        Call<ResResBean> call = nanoServerService.undoFilterPkg(getContext().getPackageName(),
                user, bean.getPkg(), bean.getLauncher());
        call.enqueue(new Callback<ResResBean>() {
            @Override
            public void onResponse(Call<ResResBean> call, Response<ResResBean> response) {
                ResResBean resResBean = response.body();
                if (resResBean != null && (resResBean.getStatus() == ResResBean.STATUS_SUCCESS
                        || resResBean.getStatus() == ResResBean.STATUS_NO_SUCH)) {
                    bean.setMark(false);
                    if (onMarkDoneListener != null) {
                        onMarkDoneListener.onMarkDone(pos, bean, true);
                    }
                } else if (onMarkDoneListener != null) {
                    onMarkDoneListener.onMarkDone(pos, bean, false);
                }
                dismiss();
            }

            @Override
            public void onFailure(Call<ResResBean> call, Throwable t) {
                if (onMarkDoneListener != null) {
                    onMarkDoneListener.onMarkDone(pos, bean, false);
                }
                dismiss();
            }
        });
    }

    private void copyCode() {
        contentView.findViewById(R.id.pb_copy_code).setVisibility(View.VISIBLE);

        NanoServerService nanoServerService = RetrofitHelper.getInstance()
                .getService(NanoServerService.class);
        Call<ResResBean<List<CodeBean>>> call = nanoServerService.getCode(bean.getPkg(), bean.getLauncher());
        call.enqueue(new Callback<ResResBean<List<CodeBean>>>() {
            @Override
            public void onResponse(Call<ResResBean<List<CodeBean>>> call, Response<ResResBean<List<CodeBean>>> response) {
                ResResBean<List<CodeBean>> resResBean = response.body();
                if (resResBean.isStatusSuccess()) {
                    String codes = packageCodes(resResBean.getResult());
                    if (!TextUtils.isEmpty(codes)) {
                        ExtraUtil.copy2Clipboard(getContext(), codes);
                        GlobalToast.show(getContext(), R.string.toast_code_copied);
                        dismiss();
                        return;
                    }
                }
                GlobalToast.show(getContext(), R.string.toast_code_copy_failed);
                dismiss();
            }

            @Override
            public void onFailure(Call<ResResBean<List<CodeBean>>> call, Throwable t) {
                GlobalToast.show(getContext(), R.string.toast_code_copy_failed);
                dismiss();
            }
        });
    }

    private String packageCodes(@NonNull List<CodeBean> codeBeanList) {
        String codes = "";
        for (CodeBean codeBean : codeBeanList) {
            String code1 = String.format(Locale.US, C.APP_CODE_LABEL,
                    codeBean.getAppLabel(),
                    codeBean.getAppLabelEn());
            String icon = ExtraUtil.codeAppName(codeBean.getAppLabelEn());
            if (icon.isEmpty()) {
                icon = ExtraUtil.codeAppName(codeBean.getAppLabel());
            }
            String code2 = String.format(Locale.US, C.APP_CODE_COMPONENT,
                    codeBean.getPkg(),
                    codeBean.getLauncherActivity(),
                    icon);
            int index = codes.indexOf(code2);
            if (index >= 0) {
                codes = codes.substring(0, index) + code1 + "\n" + codes.substring(index);
            } else {
                codes += code1 + "\n" + code2 + "\n\n";
            }
        }
        if (!codes.isEmpty()) {
            codes = codes.substring(0, codes.length() - 2);
        }

        return codes;
    }

    public void setOnMarkDoneListener(OnMarkDoneListener onMarkDoneListener) {
        this.onMarkDoneListener = onMarkDoneListener;
    }

    public static ReqMenuDialog newInstance(int pos, @NonNull AppBean bean) {
        ReqMenuDialog dialog = new ReqMenuDialog();

        Bundle bundle = new Bundle();
        bundle.putInt("pos", pos);
        bundle.putSerializable("bean", bean);
        dialog.setArguments(bundle);

        return dialog;
    }
}