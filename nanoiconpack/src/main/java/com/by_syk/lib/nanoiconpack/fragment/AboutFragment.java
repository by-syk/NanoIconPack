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

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.text.TextUtils;

import com.by_syk.lib.nanoiconpack.R;
import com.by_syk.lib.nanoiconpack.dialog.QrcodeDialog;
import com.by_syk.lib.nanoiconpack.util.PkgUtil;
import com.by_syk.lib.text.AboutMsgRender;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by By_syk on 2017-02-17.
 */

public class AboutFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener {
    private static final String PREFERENCE_ICONS = "icons";
    private static final String PREFERENCE_ICONS_NOTE = "iconsNote";
    private static final String PREFERENCE_ICONS_AUTHOR = "iconsAuthor";
    private static final String PREFERENCE_ICONS_CONTACT = "iconsContact";
    private static final String PREFERENCE_ICONS_DONATE = "iconsDonate";
    private static final String PREFERENCE_ICONS_TODO_1 = "iconsTodo1";
    private static final String PREFERENCE_ICONS_COPYRIGHT = "iconsCopyright";
    private static final String PREFERENCE_APP = "app";
    private static final String PREFERENCE_APP_APP = "appApp";
    private static final String PREFERENCE_APP_TODO_1 = "appTodo1";
    private static final String PREFERENCE_APP_DASHBOARD = "appDashboard";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_about);

        init();
    }

    private void init() {
        PreferenceCategory preCatIcons = (PreferenceCategory) findPreference(PREFERENCE_ICONS);
        Preference preIconsNote = findPreference(PREFERENCE_ICONS_NOTE);
        Preference preIconsAuthor = findPreference(PREFERENCE_ICONS_AUTHOR);
        Preference preIconsContact = findPreference(PREFERENCE_ICONS_CONTACT);
        Preference preIconsDonate = findPreference(PREFERENCE_ICONS_DONATE);
        Preference preIconsTodo1 = findPreference(PREFERENCE_ICONS_TODO_1);
        Preference preIconsCopyright = findPreference(PREFERENCE_ICONS_COPYRIGHT);
        PreferenceCategory preCatApp = (PreferenceCategory) findPreference(PREFERENCE_APP);
        Preference preAppApp = findPreference(PREFERENCE_APP_APP);
        Preference preAppTodo1 = findPreference(PREFERENCE_APP_TODO_1);
        Preference preAppDashboard = findPreference(PREFERENCE_APP_DASHBOARD);

//        preIconsNote.setOnPreferenceClickListener(this);
        preIconsAuthor.setOnPreferenceClickListener(this);
        preIconsContact.setOnPreferenceClickListener(this);
        preIconsDonate.setOnPreferenceClickListener(this);
        preIconsTodo1.setOnPreferenceClickListener(this);
        preIconsCopyright.setOnPreferenceClickListener(this);
//        preAppApp.setOnPreferenceClickListener(this);
        preAppTodo1.setOnPreferenceClickListener(this);
        preAppDashboard.setOnPreferenceClickListener(this);

        preCatIcons.setTitle(getString(R.string.preference_category_icons,
                getResources().getStringArray(R.array.icons).length));

        String summary = AboutMsgRender.parseCode(getString(R.string.preference_icons_summary_author));
        if (!TextUtils.isEmpty(summary)) {
            preIconsAuthor.setSummary(summary);
        }
        summary = AboutMsgRender.parseCode(getString(R.string.preference_icons_summary_contact));
        if (!TextUtils.isEmpty(summary)) {
            preIconsContact.setSummary(summary);
        }
        summary = AboutMsgRender.parseCode(getString(R.string.preference_icons_summary_donate));
        if (!TextUtils.isEmpty(summary)) {
            preIconsDonate.setSummary(summary);
        }
        summary = AboutMsgRender.parseCode(getString(R.string.preference_icons_summary_todo_1));
        if (!TextUtils.isEmpty(summary)) {
            preIconsTodo1.setSummary(summary);
        }
        summary = AboutMsgRender.parseCode(getString(R.string.preference_icons_summary_copyright));
        if (!TextUtils.isEmpty(summary)) {
            preIconsCopyright.setSummary(summary);
        }
        summary = PkgUtil.getAppVer(getActivity());
        if (!TextUtils.isEmpty(summary)) {
            preAppApp.setSummary(summary);
        }
        summary = AboutMsgRender.parseCode(getString(R.string.preference_app_summary_todo_1));
        if (!TextUtils.isEmpty(summary)) {
            preAppTodo1.setSummary(summary);
        }
        summary = AboutMsgRender.parseCode(getString(R.string.preference_app_summary_dashboard));
        if (!TextUtils.isEmpty(summary)) {
            preAppDashboard.setSummary(summary);
        }

        if (preIconsNote.getSummary() == null || preIconsNote.getSummary().length() == 0) {
            preCatIcons.removePreference(preIconsNote);
        }
        if (preIconsAuthor.getSummary() == null || preIconsAuthor.getSummary().length() == 0) {
            preCatIcons.removePreference(preCatIcons);
        }
        if (preIconsContact.getSummary() == null || preIconsContact.getSummary().length() == 0) {
            preCatIcons.removePreference(preIconsContact);
        }
        if (preIconsDonate.getSummary() == null || preIconsDonate.getSummary().length() == 0) {
            preCatIcons.removePreference(preIconsDonate);
        }
        if (preIconsTodo1.getSummary() == null || preIconsTodo1.getSummary().length() == 0) {
            preCatIcons.removePreference(preIconsTodo1);
        }
        if (preIconsCopyright.getSummary() == null || preIconsCopyright.getSummary().length() == 0) {
            preCatIcons.removePreference(preIconsCopyright);
        }
        if (preAppApp.getSummary() == null || preAppApp.getSummary().length() == 0) {
            preCatApp.removePreference(preAppApp);
        }
        if (preAppTodo1.getSummary() == null || preAppTodo1.getSummary().length() == 0) {
            preCatApp.removePreference(preAppTodo1);
        }
        if (preAppDashboard.getSummary() == null || preAppDashboard.getSummary().length() == 0) {
            preCatApp.removePreference(preAppDashboard);
        }
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
            case PREFERENCE_ICONS_DONATE:
                executeCode(preference.getTitle().toString(),
                        getString(R.string.preference_icons_summary_donate));
                break;
            case PREFERENCE_ICONS_TODO_1:
                executeCode(preference.getTitle().toString(),
                        getString(R.string.preference_icons_summary_todo_1));
                break;
            case PREFERENCE_ICONS_COPYRIGHT:
                executeCode(preference.getTitle().toString(),
                        getString(R.string.preference_icons_summary_copyright));
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
        }
        return true;
    }

    private void executeCode(String title, String summary) {
        if (TextUtils.isEmpty(summary)) {
            return;
        }

        Matcher matcher = Pattern.compile("\\[.*?\\]\\(qrcode:(.*?)\\)").matcher(summary);
        if (matcher.find()) {
            QrcodeDialog.newInstance(title, matcher.group(1))
                    .show(getFragmentManager(), "qrcodeDialog");
        } else {
            AboutMsgRender.executeCode(getActivity(), summary);
        }
    }
}
