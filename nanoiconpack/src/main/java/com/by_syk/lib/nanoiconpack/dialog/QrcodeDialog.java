package com.by_syk.lib.nanoiconpack.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.by_syk.lib.nanoiconpack.R;

/**
 * Created by By_syk on 2017-02-04.
 */

public class QrcodeDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) getActivity().getLayoutInflater()
                .inflate(R.layout.dialog_qrcode, null);

        int qrcodeId = getResources().getIdentifier(getString(R.string.donate_qrcode_img), "drawable",
                getActivity().getPackageName());
        if (qrcodeId != 0) {
            ((ImageView) viewGroup.findViewById(R.id.iv_qrcode)).setImageResource(qrcodeId);
        }

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dlg_title_donate)
                .setView(viewGroup)
                .create();
    }
}
