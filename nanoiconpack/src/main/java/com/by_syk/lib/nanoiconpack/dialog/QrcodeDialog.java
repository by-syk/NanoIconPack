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
import android.view.ViewGroup;
import android.widget.ImageView;

import com.by_syk.lib.nanoiconpack.R;

import net.glxn.qrgen.android.QRCode;

/**
 * Created by By_syk on 2017-02-04.
 */

public class QrcodeDialog extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) getActivity().getLayoutInflater()
                .inflate(R.layout.dialog_qrcode, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setView(viewGroup);

        Bundle bundle = getArguments();
        if (bundle != null) {
            String title = bundle.getString("title");
            String qrcodeUrl = bundle.getString("qrcodeUrl");
            if (!TextUtils.isEmpty(title)) {
                builder.setTitle(title);
            }
            if (!TextUtils.isEmpty(qrcodeUrl)) {
                int qrcodeSize = getResources().getDimensionPixelSize(R.dimen.qrcode_size);
                QRCode qrCode = QRCode.from(qrcodeUrl).withSize(qrcodeSize, qrcodeSize);
                ((ImageView) viewGroup.findViewById(R.id.iv_qrcode)).setImageBitmap(qrCode.bitmap());
            }
        }

        return builder.create();
    }

    public static QrcodeDialog newInstance(String title, String qrcodeUrl) {
        QrcodeDialog dialog = new QrcodeDialog();

        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("qrcodeUrl", qrcodeUrl);
        dialog.setArguments(bundle);

        return dialog;
    }
}
