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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.by_syk.lib.toast.GlobalToast;
import com.by_syk.lib.nanoiconpack.R;
import com.by_syk.lib.nanoiconpack.util.ExtraUtil;

/**
 * Created by By_syk on 2017-01-27.
 */

public class ApplyDialog extends DialogFragment {
    private String[] launcherNames;
    private String[] launcherPkgs;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        launcherNames = getResources().getStringArray(R.array.launchers);
        launcherPkgs = new String[launcherNames.length];
        for (int i = 0, len = launcherNames.length; i < len; ++i) {
            String[] paras = launcherNames[i].split("\\|");
            launcherNames[i] = paras[0];
            launcherPkgs[i] = paras[1];
        }

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dlg_title_apply)
                .setItems(launcherNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        apply(i);
                    }
                })
                .create();
    }

    private void apply(int pos) {
        if (!ExtraUtil.isPkgInstalled(getActivity(), launcherPkgs[pos])) {
            GlobalToast.showToast(getActivity(), getString(R.string.toast_not_installed, launcherNames[pos]));
            return;
        }

        switch (pos) {
            case 0:
                apply2Nova();
                break;
            case 1:
                apply2Apex();
                break;
            case 2:
                apply2Adw();
                break;
            case 3:
                apply2Aviate();
                break;
        }
    }

    private void apply2Nova() {
        Intent intent = new Intent("com.teslacoilsw.launcher.APPLY_ICON_THEME");
        intent.setPackage("com.teslacoilsw.launcher");
        intent.putExtra("com.teslacoilsw.launcher.extra.ICON_THEME_TYPE", "GO");
        intent.putExtra("com.teslacoilsw.launcher.extra.ICON_THEME_PACKAGE",
                getActivity().getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            getActivity().startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void apply2Apex() {
        Intent intent = new Intent("com.anddoes.launcher.SET_THEME");
        intent.putExtra("com.anddoes.launcher.THEME_PACKAGE_NAME", getActivity().getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            getActivity().startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void apply2Adw() {
        Intent intent = new Intent("org.adw.launcher.SET_THEME");
        intent.putExtra("org.adw.launcher.theme.NAME", getActivity().getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getActivity().startActivity(intent);
    }

    private void apply2Aviate() {
        Intent intent = new Intent("com.tul.aviate.SET_THEME");
        intent.setPackage("com.tul.aviate");
        intent.putExtra("THEME_PACKAGE", getActivity().getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getActivity().startActivity(intent);
    }
}
