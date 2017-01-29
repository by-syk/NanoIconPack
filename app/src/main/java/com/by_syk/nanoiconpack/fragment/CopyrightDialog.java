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

package com.by_syk.nanoiconpack.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.by_syk.lib.text.AboutMsgRender;
import com.by_syk.nanoiconpack.R;

/**
 * Created by By_syk on 2017-01-27.
 */

public class CopyrightDialog extends DialogFragment {
    private boolean isExecuted = false;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        SpannableString ssMsg = AboutMsgRender.render(getActivity(), getString(R.string.copyright_desc)
                + "\n\n" + getString(R.string.copyright_base_desc));

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.app_name)
                .setMessage(ssMsg)
                .setPositiveButton(R.string.dlg_bt_ok, null)
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!isExecuted) {
            isExecuted = true;

            // 使内容中的链接可以被点击
            TextView tvMessage = (TextView) getDialog().findViewById(android.R.id.message);
            if (tvMessage != null) {
                tvMessage.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
    }
}
