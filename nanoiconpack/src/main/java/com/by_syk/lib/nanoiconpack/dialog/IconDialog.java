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

import android.Manifest;
import android.animation.LayoutTransition;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import com.by_syk.lib.nanoiconpack.R;
import com.by_syk.lib.nanoiconpack.bean.IconBean;
import com.by_syk.lib.nanoiconpack.util.C;
import com.by_syk.lib.nanoiconpack.util.ExtraUtil;
import com.by_syk.lib.nanoiconpack.util.PkgUtil;
import com.by_syk.lib.toast.GlobalToast;

import java.util.List;

/**
 * Created by By_syk on 2017-01-27.
 */

public class IconDialog extends DialogFragment {
    private ViewGroup viewContent;
    private View iconGridView;
    private View iconViewSmall;

    private IconBean iconBean;

    private boolean isAppInstalled = true;

    private boolean isExecuted = false;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        viewContent = (ViewGroup) getActivity().getLayoutInflater()
                .inflate(R.layout.dialog_icon, null);
        viewContent.setLayoutTransition(new LayoutTransition());

        iconViewSmall = viewContent.findViewById(R.id.small_icon_view);
        iconViewSmall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                viewContent.removeView(iconViewSmall);
                iconGridView.setVisibility(View.INVISIBLE);
            }
        });
        viewContent.removeView(iconViewSmall);

        iconGridView = viewContent.findViewById(R.id.icon_grid);

        ImageView ivIcon = (ImageView) viewContent.findViewById(R.id.iv_icon);
        ivIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (iconGridView.getVisibility() == View.VISIBLE) {
                    iconGridView.setVisibility(View.INVISIBLE);
                } else {
                    iconGridView.setVisibility(View.VISIBLE);
                }
                if (!isAppInstalled) {
                    return;
                }
                if (iconViewSmall == null) {
                    (new ExtractRawIconTask()).execute();
                } else {
                    if (viewContent.getChildCount() == 2) {
                        viewContent.removeView(iconViewSmall);
                    } else {
                        viewContent.addView(iconViewSmall);
                    }
                }
            }
        });
        ivIcon.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                saveIcon();
                return true;
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(viewContent);

        Bundle bundle = getArguments();
        if (bundle != null) {
            iconBean = (IconBean) bundle.getSerializable("bean");
            if (iconBean != null) {
                builder.setTitle(iconBean.getLabel() != null ? iconBean.getLabel() : iconBean.getName());
//                ivIcon.setImageResource(iconBean.getId());
                int hdIconId = getResources().getIdentifier(iconBean.getName(), "mipmap",
                        getActivity().getPackageName());
                if (hdIconId != 0) {
                    ivIcon.setImageResource(hdIconId);
                } else {
                    ivIcon.setImageResource(iconBean.getId());
                }
            }
            if (bundle.getBoolean("pick")) {
                builder.setPositiveButton(R.string.dlg_bt_pick, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        returnPickIcon();
                    }
                });
            }
        }

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!isExecuted) {
            isExecuted = true;

            (new ExtractRawIconTask()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                    "extractRawIconTask");

            // 浮入浮出动画
            Window window = getDialog().getWindow();
            if (window != null) {
                window.setWindowAnimations(android.R.style.Animation_InputMethod);
            }
        }
    }

    @TargetApi(23)
    private void saveIcon() {
        if (C.SDK >= 23 && getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            return;
        }

        boolean ok = ExtraUtil.saveIcon(getActivity(), iconBean);
        GlobalToast.showToast(getActivity(), ok ? R.string.toast_icon_saved
                : R.string.toast_icon_not_saved);
    }

    private void returnPickIcon() {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeResource(getResources(), iconBean.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent();
        if (bitmap != null) {
            intent.putExtra("icon", bitmap);
            intent.putExtra("android.intent.extra.shortcut.ICON_RESOURCE", iconBean.getId());
            intent.setData(Uri.parse("android.resource://" + getActivity().getPackageName()
                    + "/" + String.valueOf(iconBean.getId())));
            getActivity().setResult(Activity.RESULT_OK, intent);
        } else {
            getActivity().setResult(Activity.RESULT_CANCELED, intent);
        }
        getActivity().finish();
    }

    class ExtractRawIconTask extends AsyncTask<String, Integer, Drawable> {
        @Override
        protected Drawable doInBackground(String... strings) {
            if (!isAdded()) {
                return null;
            }

            List<String> matchedPkgList = ExtraUtil.getAppFilterPkg(getResources(), iconBean.getName());
            for (String pkgName : matchedPkgList) {
                if (PkgUtil.isPkgInstalled(getActivity(), pkgName)) {
                    PackageManager packageManager = getActivity().getPackageManager();
                    try {
                        PackageInfo packageInfo = packageManager.getPackageInfo(pkgName, 0);
                        return packageInfo.applicationInfo.loadIcon(packageManager);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            super.onPostExecute(drawable);

            if (!isAdded()) {
                return;
            }

            if (drawable == null) {
                isAppInstalled = false;
                return;
            }

            if (viewContent.getChildCount() > 1) {
                return;
            }

            ((ImageView) iconViewSmall.findViewById(R.id.iv_icon_small)).setImageDrawable(drawable);
            viewContent.addView(iconViewSmall);
            iconGridView.setVisibility(View.VISIBLE);
        }
    }

    public static IconDialog newInstance(IconBean bean, boolean isPick) {
        IconDialog dialog = new IconDialog();

        Bundle bundle = new Bundle();
        bundle.putSerializable("bean", bean);
        bundle.putBoolean("pick", isPick);
        dialog.setArguments(bundle);

        return dialog;
    }
}
