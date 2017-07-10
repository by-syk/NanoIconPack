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
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.ViewGroup;
import android.widget.EditText;

import com.by_syk.lib.nanoiconpack.R;

/**
 * Created by By_syk on 2017-01-01.
 */

public class UserDialog extends DialogFragment {
    private EditText etUser;

    private OnContinueListener onContinueListener;

    public interface OnContinueListener {
        void onContinue(@NonNull String user);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) getActivity().getLayoutInflater()
                .inflate(R.layout.dialog_key, null);
        etUser = (EditText) viewGroup.findViewById(R.id.et_user);

        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.dlg_title_set_key)
                .setView(viewGroup)
                .setPositiveButton(R.string.dlg_bt_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (onContinueListener != null) {
                            onContinueListener.onContinue(etUser.getText().toString().trim());
                        }
                    }
                })
                .create();
        alertDialog.setCanceledOnTouchOutside(false);

        return alertDialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);

        getActivity().finish();
    }

    public void setOnContinueListener(OnContinueListener onContinueListener) {
        this.onContinueListener = onContinueListener;
    }
}
