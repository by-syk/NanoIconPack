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
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

import com.by_syk.lib.nanoiconpack.R;
import com.by_syk.lib.nanoiconpack.bean.IconBean;
import com.by_syk.lib.nanoiconpack.util.C;
import com.by_syk.lib.nanoiconpack.util.ExtraUtil;
import com.by_syk.lib.nanoiconpack.util.InstalledAppReader;
import com.by_syk.lib.nanoiconpack.util.PkgUtil;
import com.by_syk.lib.globaltoast.GlobalToast;
import com.by_syk.lib.texttag.TextTag;

/**
 * Created by By_syk on 2017-01-27.
 */

public class IconDialog extends DialogFragment {
    private ImageView ivIcon;
    private View iconGridView;
    private View iconViewSmall;
    private View viewActionSave;
    private View viewActionSend2Home;
    private View viewActionChoose;

    private IconBean iconBean;

    private boolean isAppInstalled = true;

    private boolean isExecuted = false;

    private static boolean promptActionSave = true;
    private static boolean promptActionSend2Home = true;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View viewContent = getActivity().getLayoutInflater().inflate(R.layout.dialog_icon, null);

        initView(viewContent);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setView(viewContent);

        Bundle bundle = getArguments();
        if (bundle != null) {
            iconBean = (IconBean) bundle.getSerializable("bean");
            if (iconBean != null) {
                builder.setTitle(getTitle(iconBean));
//                ivIcon.setImageResource(iconBean.getId());
                int hdIconId = getResources().getIdentifier(iconBean.getName(), "mipmap",
                        getContext().getPackageName());
                ivIcon.setImageResource(hdIconId != 0 ? hdIconId : iconBean.getId());
                viewActionSave.setVisibility(iconBean.getId() != 0 || hdIconId != 0
                        ? View.VISIBLE : View.GONE);
                viewActionSend2Home.setVisibility(iconBean.containsInstalledComponent()
                        ? View.VISIBLE : View.GONE);
            }
            if (bundle.getBoolean("pick")) {
                viewActionSave.setVisibility(View.GONE);
                viewActionSend2Home.setVisibility(View.GONE);
                viewActionChoose.setVisibility(View.VISIBLE);
            }
        }

        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (iconBean == null) {
            dismiss();
            return;
        }

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

    private void initView(View viewContent) {
        iconViewSmall = viewContent.findViewById(R.id.small_icon_view);
        iconViewSmall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                iconViewSmall.setVisibility(View.GONE);
                iconGridView.setVisibility(View.INVISIBLE);
            }
        });

        iconGridView = viewContent.findViewById(R.id.icon_grid);

        ivIcon = (ImageView) viewContent.findViewById(R.id.iv_icon);
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
                    if (iconViewSmall.getVisibility() == View.VISIBLE) {
                        iconViewSmall.setVisibility(View.GONE);
                    } else {
                        iconViewSmall.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        viewActionSave = viewContent.findViewById(R.id.iv_save);
        viewActionSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (promptActionSave) {
                    promptActionSave = false;
                    GlobalToast.showLong(getContext(), R.string.toast_tap_save_icon);
                } else {
                    saveIcon();
                }
            }
        });

        viewActionSend2Home = viewContent.findViewById(R.id.iv_send_to_home);
        viewActionSend2Home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (promptActionSend2Home) {
                    promptActionSend2Home = false;
                    GlobalToast.showLong(getContext(), R.string.toast_tap_send_to_home);
                } else {
                    sendIcon();
                }
            }
        });

        viewActionChoose = viewContent.findViewById(R.id.iv_choose);
        viewActionChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnPickIcon();
            }
        });
    }

    private SpannableString getTitle(@NonNull IconBean bean) {
        TextTag.Builder builder = new TextTag.Builder()
                .text(iconBean.getLabel() != null ? iconBean.getLabel() : iconBean.getName())
                .bgColor(Color.GRAY);
        if (!bean.isRecorded()) {
            builder.tag(getString(R.string.icon_tag_undefined));
        } else if (!bean.isDef()) {
            builder.tag(getString(R.string.icon_tag_alternative));
        }
        return builder.build().render();
    }

    @TargetApi(23)
    private void saveIcon() {
        if (C.SDK >= 23 && getContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            return;
        }

        int iconId = getResources().getIdentifier(iconBean.getName(), "mipmap",
                getContext().getPackageName());
        if (iconId == 0) {
            iconId = iconBean.getId();
        }
        boolean ok = ExtraUtil.saveIcon(getContext(), getResources().getDrawable(iconId),
                iconBean.getName());
        if (ok) {
            ((ImageView) viewActionSave).getDrawable().mutate()
                    .setTint(ContextCompat.getColor(getContext(), R.color.positive));
        }
        GlobalToast.show(getContext(), ok ? R.string.toast_icon_saved
                : R.string.toast_icon_save_failed);
    }

    private void sendIcon() {
        IconBean.Component targetComponent = null;
        for (IconBean.Component component : iconBean.getComponents()) { // TODO
            if (component.isInstalled()) {
                targetComponent = component;
                break;
            }
        }
        boolean ok = false;
        if (targetComponent != null) {
            String label = targetComponent.getLabel();
            if (label == null) {
                label = iconBean.getLabel();
            }
            if (label == null) {
                label = iconBean.getName();
            }
            ok = ExtraUtil.sendIcon2HomeScreen(getContext(), iconBean.getId(), label,
                    targetComponent.getPkg(), targetComponent.getLauncher());
        }
        // Not .getDrawable().setTint()
        ((ImageView) viewActionSend2Home).getDrawable().mutate().setTint(ContextCompat
                .getColor(getContext(), ok ? R.color.positive : R.color.negative));
        GlobalToast.showLong(getContext(),
                ok ? R.string.toast_sent_to_home : R.string.toast_failed_send_to_home);
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
            intent.setData(Uri.parse("android.resource://" + getContext().getPackageName()
                    + "/" + String.valueOf(iconBean.getId())));
            getActivity().setResult(Activity.RESULT_OK, intent);
        } else {
            getActivity().setResult(Activity.RESULT_CANCELED, intent);
        }
        getActivity().finish();
    }

    class ExtractRawIconTask extends AsyncTask<String, String, Drawable> {
        @Override
        protected Drawable doInBackground(String... strings) {
            if (!isAdded()) {
                return null;
            }
            PackageManager packageManager = getContext().getPackageManager();
            for (IconBean.Component component : iconBean.getComponents()) {
                if (!component.isInstalled()) {
                    continue;
                }
                Drawable icon = PkgUtil.getIcon(packageManager,
                        component.getPkg(), component.getLauncher());
                if (icon != null) {
                    return icon;
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

            ((ImageView) iconViewSmall.findViewById(R.id.iv_icon_small)).setImageDrawable(drawable);
            iconViewSmall.postDelayed(new Runnable() {
                @Override
                public void run() {
                    iconGridView.setVisibility(View.VISIBLE);
                    iconViewSmall.setVisibility(View.VISIBLE);
                }
            }, 100);
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
