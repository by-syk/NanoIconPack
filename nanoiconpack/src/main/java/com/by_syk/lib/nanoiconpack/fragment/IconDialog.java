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

package com.by_syk.lib.nanoiconpack.fragment;

import android.animation.LayoutTransition;
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
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.by_syk.lib.nanoiconpack.R;
import com.by_syk.lib.nanoiconpack.bean.IconBean;
import com.by_syk.lib.nanoiconpack.util.ExtraUtil;

import java.util.List;

/**
 * Created by By_syk on 2017-01-27.
 */

public class IconDialog extends DialogFragment {
    private LinearLayout viewContent;
    private View iconLineView;
    private ImageView ivIconSmall;

    private IconBean iconBean;

    private boolean isInstalled = true;

    private boolean isExecuted = false;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) getActivity().getLayoutInflater().inflate(R.layout.dialog_icon, null);

        viewContent = (LinearLayout) viewGroup.findViewById(R.id.root_view);
        viewContent.setLayoutTransition(new LayoutTransition());

        iconLineView = viewGroup.findViewById(R.id.icon_line);

        ImageView ivIcon = (ImageView) viewGroup.findViewById(R.id.iv_icon);
        ivIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (iconLineView.getVisibility() == View.VISIBLE) {
                    iconLineView.setVisibility(View.INVISIBLE);
                } else {
                    iconLineView.setVisibility(View.VISIBLE);
                }
                if (!isInstalled) {
                    return;
                }
                if (ivIconSmall == null) {
                    (new ExtractRawIconTask()).execute();
                } else {
                    if (viewContent.getChildCount() == 2) {
                        viewContent.removeView(ivIconSmall);
                    } else {
                        viewContent.addView(ivIconSmall);
                    }
                }
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setView(viewGroup);

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

            (new ExtractRawIconTask()).execute();

            // 浮入浮出动画
            Window window = getDialog().getWindow();
            if (window != null) {
                window.setWindowAnimations(android.R.style.Animation_InputMethod);
            }
        }
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
            List<String> matchedPkgList = ExtraUtil.getAppFilterPkg(getResources(), iconBean.getName());
            for (String pkgName : matchedPkgList) {
                if (ExtraUtil.isPkgInstalled(getActivity(), pkgName)) {
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
                isInstalled = false;
                return;
            }

            ivIconSmall = new ImageView(getActivity());
            int iconSmallSize = getResources().getDimensionPixelSize(R.dimen.dlg_small_icon_size);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(iconSmallSize, iconSmallSize);
            ivIconSmall.setLayoutParams(layoutParams);
            ivIconSmall.setScaleType(ImageView.ScaleType.FIT_CENTER);
            ivIconSmall.setImageDrawable(drawable);
            TypedValue typedValue = new TypedValue();
            getActivity().getTheme().resolveAttribute(R.attr.left_lean_icon_frame, typedValue, true);
            ivIconSmall.setBackgroundResource(typedValue.resourceId);
            ivIconSmall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewContent.removeView(ivIconSmall);
                    iconLineView.setVisibility(View.INVISIBLE);
                }
            });
            viewContent.addView(ivIconSmall);
            iconLineView.setVisibility(View.VISIBLE);
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
