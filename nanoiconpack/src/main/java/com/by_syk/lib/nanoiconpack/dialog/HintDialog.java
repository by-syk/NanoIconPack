package com.by_syk.lib.nanoiconpack.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;

import com.by_syk.lib.nanoiconpack.R;
import com.by_syk.lib.storage.SP;

/**
 * Created by By_syk on 2017-02-15.
 */

public class HintDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setMessage(R.string.icon_tap_desc)
                .setPositiveButton(R.string.dlg_bt_got_it, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        (new SP(getActivity(), false)).save("iconTapHint", true);
                    }
                });

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
