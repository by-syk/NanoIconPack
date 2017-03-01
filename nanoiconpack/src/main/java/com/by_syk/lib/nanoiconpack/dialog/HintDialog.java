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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

/**
 * Created by By_syk on 2017-02-15.
 */

public class HintDialog extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        Bundle bundle = getArguments();
        if (bundle != null) {
            String title = bundle.getString("title");
            String msg = bundle.getString("msg");
            String btText = bundle.getString("btText");
            if (!TextUtils.isEmpty(title)) {
                builder.setTitle(title);
            }
            builder.setMessage(msg != null ? msg : "");
            if (!TextUtils.isEmpty(btText)) {
                builder.setPositiveButton(btText, null);
            }
        }

        return builder.create();
    }

    public static HintDialog newInstance(String title, String msg, String btText) {
        HintDialog dialog = new HintDialog();

        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("msg", msg);
        bundle.putString("btText", btText);
        dialog.setArguments(bundle);

        return dialog;
    }
}
