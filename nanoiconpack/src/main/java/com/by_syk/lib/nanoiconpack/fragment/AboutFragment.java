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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.TextUtils;

import com.by_syk.lib.nanoiconpack.R;
import com.by_syk.lib.nanoiconpack.ReqStatsActivity;
import com.by_syk.lib.nanoiconpack.bean.DonateBean;
import com.by_syk.lib.nanoiconpack.bean.ResResBean;
import com.by_syk.lib.nanoiconpack.dialog.QrcodeDialog;
import com.by_syk.lib.nanoiconpack.dialog.SponsorsDialog;
import com.by_syk.lib.nanoiconpack.util.ExtraUtil;
import com.by_syk.lib.nanoiconpack.util.PkgUtil;
import com.by_syk.lib.aboutmsgrender.AboutMsgRender;
import com.by_syk.lib.nanoiconpack.util.RetrofitHelper;
import com.by_syk.lib.nanoiconpack.util.impl.NanoServerService;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by By_syk on 2017-02-17.
 */

public class AboutFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {
    private Preference prefSupportDonate;
    private Preference prefSupportSponsors;

    private ArrayList<DonateBean> sponsorList = new ArrayList<>();

    private static Pattern sponsorPattern = Pattern.compile("\\[.*?\\]\\(usr:(.*?)\\)");
    private static Pattern codePattern = Pattern.compile("\\[.*?\\]\\((.+?):(.*?)\\)");

    private static final String PREFERENCE_ICONS = "icons";
    private static final String PREFERENCE_ICONS_NOTE = "iconsNote";
    private static final String PREFERENCE_ICONS_AUTHOR = "iconsAuthor";
    private static final String PREFERENCE_ICONS_CONTACT = "iconsContact";
    private static final String PREFERENCE_ICONS_TODO_1 = "iconsTodo1";
    private static final String PREFERENCE_ICONS_COPYRIGHT = "iconsCopyright";
    private static final String PREFERENCE_SUPPORT = "support";
    private static final String PREFERENCE_SUPPORT_DONATE = "supportDonate";
    private static final String PREFERENCE_SUPPORT_TODO_1 = "supportTodo1";
    private static final String PREFERENCE_SUPPORT_SPONSORS = "supportSponsors";
    private static final String PREFERENCE_APP = "app";
    private static final String PREFERENCE_APP_APP = "appApp";
    private static final String PREFERENCE_APP_TODO_1 = "appTodo1";
    private static final String PREFERENCE_APP_DASHBOARD = "appDashboard";
    private static final String PREFERENCE_DEV = "dev";
    private static final String PREFERENCE_DEV_STATS = "devStats";
    private static final String PREFERENCE_DEV_QUERY = "devQuery";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_about);

        init();

        loadSponsors();
    }

    private void init() {
        PreferenceCategory prefCatIcons = (PreferenceCategory) findPreference(PREFERENCE_ICONS);
        Preference prefIconsNote = findPreference(PREFERENCE_ICONS_NOTE);
        Preference prefIconsAuthor = findPreference(PREFERENCE_ICONS_AUTHOR);
        Preference prefIconsContact = findPreference(PREFERENCE_ICONS_CONTACT);
        Preference prefIconsTodo1 = findPreference(PREFERENCE_ICONS_TODO_1);
        Preference prefIconsCopyright = findPreference(PREFERENCE_ICONS_COPYRIGHT);
        PreferenceCategory prefCatSupport = (PreferenceCategory) findPreference(PREFERENCE_SUPPORT);
        prefSupportDonate = findPreference(PREFERENCE_SUPPORT_DONATE);
        Preference prefSupportTodo1 = findPreference(PREFERENCE_SUPPORT_TODO_1);
        prefSupportSponsors = findPreference(PREFERENCE_SUPPORT_SPONSORS);
        PreferenceCategory prefCatApp = (PreferenceCategory) findPreference(PREFERENCE_APP);
        Preference prefAppApp = findPreference(PREFERENCE_APP_APP);
        Preference prefAppTodo1 = findPreference(PREFERENCE_APP_TODO_1);
        Preference prefAppDashboard = findPreference(PREFERENCE_APP_DASHBOARD);
        PreferenceCategory prefCatDev = (PreferenceCategory) findPreference(PREFERENCE_DEV);
        Preference prefDevStats = findPreference(PREFERENCE_DEV_STATS);
        Preference prefDevQuery = findPreference(PREFERENCE_DEV_QUERY);

//        prefIconsNote.setOnPreferenceClickListener(this);
        prefIconsAuthor.setOnPreferenceClickListener(this);
        prefIconsContact.setOnPreferenceClickListener(this);
        prefIconsTodo1.setOnPreferenceClickListener(this);
        prefIconsCopyright.setOnPreferenceClickListener(this);
        prefSupportDonate.setOnPreferenceClickListener(this);
        prefSupportTodo1.setOnPreferenceClickListener(this);
        prefSupportSponsors.setOnPreferenceClickListener(this);
        prefAppApp.setOnPreferenceClickListener(this);
        prefAppTodo1.setOnPreferenceClickListener(this);
        prefAppDashboard.setOnPreferenceClickListener(this);
        prefDevStats.setOnPreferenceClickListener(this);
        prefDevQuery.setOnPreferenceClickListener(this);

        prefCatIcons.setTitle(getString(R.string.preference_category_icons,
                getResources().getStringArray(R.array.icons).length));
        prefAppDashboard.setTitle(getString(R.string.preference_app_title_dashboard,
                getString(R.string.lib_ver)));

        String summary = AboutMsgRender.parseCode(getString(R.string.preference_icons_summary_author));
        if (!TextUtils.isEmpty(summary)) {
            prefIconsAuthor.setSummary(summary);
        }
        summary = AboutMsgRender.parseCode(getString(R.string.preference_icons_summary_contact));
        if (!TextUtils.isEmpty(summary)) {
            prefIconsContact.setSummary(summary);
        }
        summary = AboutMsgRender.parseCode(getString(R.string.preference_icons_summary_todo_1));
        if (!TextUtils.isEmpty(summary)) {
            prefIconsTodo1.setSummary(summary);
        }
        summary = AboutMsgRender.parseCode(getString(R.string.preference_icons_summary_copyright));
        if (!TextUtils.isEmpty(summary)) {
            prefIconsCopyright.setSummary(summary);
        }
        summary = AboutMsgRender.parseCode(getString(R.string.preference_support_summary_donate));
        if (!TextUtils.isEmpty(summary)) {
            prefSupportDonate.setSummary(summary);
        }
        summary = AboutMsgRender.parseCode(getString(R.string.preference_support_summary_todo_1));
        if (!TextUtils.isEmpty(summary)) {
            prefSupportTodo1.setSummary(summary);
        }
        summary = AboutMsgRender.parseCode(getString(R.string.preference_support_summary_sponsors));
        if (!TextUtils.isEmpty(summary)) {
            prefSupportSponsors.setSummary(summary);
        }
        summary = PkgUtil.getAppVer(getContext(), getString(R.string.preference_app_summary_app));
        if (!TextUtils.isEmpty(summary)) {
            prefAppApp.setSummary(summary);
        }
        summary = AboutMsgRender.parseCode(getString(R.string.preference_app_summary_todo_1));
        if (!TextUtils.isEmpty(summary)) {
            prefAppTodo1.setSummary(summary);
        }
        summary = AboutMsgRender.parseCode(getString(R.string.preference_app_summary_dashboard));
        if (!TextUtils.isEmpty(summary)) {
            prefAppDashboard.setSummary(summary);
        }
        summary = AboutMsgRender.parseCode(getString(R.string.preference_dev_summary_query));
        if (!TextUtils.isEmpty(summary)) {
            prefDevQuery.setSummary(summary);
        }

        prefSupportSponsors.setVisible(false);

        if (prefIconsNote.getSummary() == null || prefIconsNote.getSummary().length() == 0) {
            prefCatIcons.removePreference(prefIconsNote);
        }
        if (prefIconsAuthor.getSummary() == null || prefIconsAuthor.getSummary().length() == 0) {
            prefCatIcons.removePreference(prefIconsAuthor);
        }
        if (prefIconsContact.getSummary() == null || prefIconsContact.getSummary().length() == 0) {
            prefCatIcons.removePreference(prefIconsContact);
        }
        if (prefIconsTodo1.getSummary() == null || prefIconsTodo1.getSummary().length() == 0) {
            prefCatIcons.removePreference(prefIconsTodo1);
        }
        if (prefIconsCopyright.getSummary() == null || prefIconsCopyright.getSummary().length() == 0) {
            prefCatIcons.removePreference(prefIconsCopyright);
        }
        if (prefSupportDonate.getSummary() == null || prefSupportDonate.getSummary().length() == 0) {
            prefCatSupport.removePreference(prefSupportDonate);
        }
        if (prefSupportTodo1.getSummary() == null || prefSupportTodo1.getSummary().length() == 0) {
            prefCatSupport.removePreference(prefSupportTodo1);
        }
        if (prefAppApp.getSummary() == null || prefAppApp.getSummary().length() == 0) {
            prefCatApp.removePreference(prefAppApp);
        }
        if (prefAppTodo1.getSummary() == null || prefAppTodo1.getSummary().length() == 0) {
            prefCatApp.removePreference(prefAppTodo1);
        }
        if (prefAppDashboard.getSummary() == null || prefAppDashboard.getSummary().length() == 0) {
            prefCatApp.removePreference(prefAppDashboard);
        }
        if (!getResources().getBoolean(R.bool.enable_req_stats_module)) {
            prefCatDev.removePreference(prefDevStats);
        }
    }

    private void loadSponsors() {
        if (!ExtraUtil.isNetworkConnected(getContext())) {
            return;
        }

        String summarySponsors = getString(R.string.preference_support_summary_sponsors);
        if (TextUtils.isEmpty(summarySponsors)) {
            return;
        }
        Matcher matcher = sponsorPattern.matcher(summarySponsors);
        if (!matcher.find()) {
            return;
        }
        String key = matcher.group(1);

        NanoServerService service = RetrofitHelper.getInstance().getService(NanoServerService.class);
        Call<ResResBean<List<DonateBean>>> call = service.getDonates(getContext().getPackageName(), key);
        call.enqueue(new Callback<ResResBean<List<DonateBean>>>() {
            @Override
            public void onResponse(Call<ResResBean<List<DonateBean>>> call, Response<ResResBean<List<DonateBean>>> response) {
                ResResBean<List<DonateBean>> resResBean = response.body();
                if (resResBean == null || !resResBean.isStatusSuccess()) {
                    return;
                }
                if (!isAdded()) {
                    return;
                }
                List<DonateBean> sponsorList = resResBean.getResult();
                if (sponsorList != null && !sponsorList.isEmpty()) {
                    AboutFragment.this.sponsorList.addAll(sponsorList);
                    prefSupportSponsors.setVisible(true);
                }
            }

            @Override
            public void onFailure(Call<ResResBean<List<DonateBean>>> call, Throwable t) {}
        });
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
//            case PREFERENCE_ICONS_NOTE:
//                break;
            case PREFERENCE_ICONS_AUTHOR:
                executeCode(preference.getTitle().toString(),
                        getString(R.string.preference_icons_summary_author));
                break;
            case PREFERENCE_ICONS_CONTACT:
                executeCode(preference.getTitle().toString(),
                        getString(R.string.preference_icons_summary_contact));
                break;
            case PREFERENCE_ICONS_TODO_1:
                executeCode(preference.getTitle().toString(),
                        getString(R.string.preference_icons_summary_todo_1));
                break;
            case PREFERENCE_ICONS_COPYRIGHT:
                executeCode(preference.getTitle().toString(),
                        getString(R.string.preference_icons_summary_copyright));
                break;
            case PREFERENCE_SUPPORT_DONATE:
                executeCode(preference.getTitle().toString(),
                        getString(R.string.preference_support_summary_donate));
                break;
            case PREFERENCE_SUPPORT_TODO_1:
                executeCode(preference.getTitle().toString(),
                        getString(R.string.preference_support_summary_todo_1));
                break;
            case PREFERENCE_SUPPORT_SPONSORS:
                showDonate();
                break;
//            case PREFERENCE_APP_APP:
//                executeCode(preference.getTitle().toString(), preference.getSummary().toString());
//                break;
            case PREFERENCE_APP_TODO_1:
                executeCode(preference.getTitle().toString(),
                        getString(R.string.preference_app_summary_todo_1));
                break;
            case PREFERENCE_APP_DASHBOARD:
                executeCode(preference.getTitle().toString(),
                        getString(R.string.preference_app_summary_dashboard));
                break;
            case PREFERENCE_DEV_STATS:
                enterStats();
                break;
            case PREFERENCE_DEV_QUERY:
                executeCode(preference.getTitle().toString(),
                        getString(R.string.preference_dev_summary_query));
                break;
        }
        return true;
    }

    private void showDonate() {
        SponsorsDialog dialog = SponsorsDialog.newInstance(sponsorList);
        dialog.setOnDonateListener(new SponsorsDialog.OnDonateListener() {
            @Override
            public void onDonate() {
                if (!prefSupportDonate.wasDetached()) {
                    prefSupportDonate.performClick();
                }
            }
        });
        dialog.show(getFragmentManager(), "sponsorsDialog");
    }

    private void enterStats() {
        startActivity(new Intent(getContext(), ReqStatsActivity.class));
    }

    private void executeCode(String title, String summary) {
        if (TextUtils.isEmpty(summary)) {
            return;
        }

        Matcher matcher = codePattern.matcher(summary);
        if (matcher.find()) {
            switch (matcher.group(1)) {
                case "qrcode":
                case "wechat":
                    QrcodeDialog.newInstance(title, matcher.group(2))
                            .show(getFragmentManager(), "qrcodeDialog");
                    break;
                default:
                    AboutMsgRender.executeCode(getActivity(), summary);
            }
        } else {
            AboutMsgRender.executeCode(getActivity(), summary);
        }
    }
}
