package com.by_syk.lib.nanoiconpack.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.by_syk.lib.nanoiconpack.R;
import com.by_syk.lib.nanoiconpack.bean.DonateBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by By_syk on 2016-11-16.
 */

public class SponsorsDialog extends DialogFragment {
    private OnDonateListener onDonateListener;

    public interface OnDonateListener {
        void onDonate();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ArrayList<DonateBean> dataList = (ArrayList<DonateBean>) getArguments().getSerializable("data");

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.preference_support_title_sponsors)
                .setItems(parseData(dataList), null)
                .setPositiveButton(R.string.dlg_bt_donate_too, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (onDonateListener != null) {
                            onDonateListener.onDonate();
                        }
                    }
                })
                .create();
    }

    @NonNull
    private String[] parseData(ArrayList<DonateBean> dataList) {
        if (dataList == null) {
            return new String[0];
        }
        List<String> dataArr = new ArrayList<>();
        for (int i = 0, len = dataList.size(); i < len; ++i) {
            String sponsor = dataList.get(i).getDonator();
            if (!TextUtils.isEmpty(sponsor)) {
                if (!sponsor.contains("@")) {
                    sponsor = "@" + sponsor;
                }
                dataArr.add(sponsor);
            }
        }
        return dataArr.toArray(new String[dataArr.size()]);
    }

    public void setOnDonateListener(OnDonateListener onDonateListener) {
        this.onDonateListener = onDonateListener;
    }

    public static SponsorsDialog newInstance(ArrayList<DonateBean> dataList) {
        SponsorsDialog dialog = new SponsorsDialog();

        Bundle bundle = new Bundle();
        bundle.putSerializable("data", dataList);
        dialog.setArguments(bundle);

        return dialog;
    }
}
