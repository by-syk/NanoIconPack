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

package com.by_syk.lib.nanoiconpack;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.by_syk.lib.nanoiconpack.dialog.UserDialog;
import com.by_syk.lib.nanoiconpack.fragment.ReqStatsFragment;
import com.by_syk.lib.sp.SP;

/**
 * Created by By_syk on 2017-02-24.
 */

public class ReqStatsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_req_stats);

        init();
    }

    private void init() {
        if ((new SP(this)).contains("user")) {
            showFragment();
        } else {
            signIn();
        }
    }

    private void signIn() {
        UserDialog userDialog = new UserDialog();
        userDialog.setOnContinueListener(new UserDialog.OnContinueListener() {
            @Override
            public void onContinue(@NonNull String user) {
                if (user.isEmpty()) {
                    signIn();
                    return;
                }
                (new SP(ReqStatsActivity.this)).save("user", user);
                showFragment();
            }
        });
        userDialog.show(getSupportFragmentManager(), "userDialog");
    }

    private void showFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        ReqStatsFragment reqStatsFragment = (ReqStatsFragment) fragmentManager.findFragmentByTag("reqStatsFragment");
        if (reqStatsFragment != null) {
            fragmentManager.beginTransaction().show(reqStatsFragment).commit();
        } else {
            fragmentManager.beginTransaction().add(R.id.fragment_content,
                    ReqStatsFragment.newInstance(), "reqStatsFragment").commit();
        }
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.fragment_content, ReqStatsFragment.newInstance(), "reqStatsFragment")
//                .commit();
    }

//    @TargetApi(25)
//    private void enableShortcut() {
//        if (C.SDK < 25 || sp.getBoolean("shortcutEnabled")) {
//            return;
//        }
//        sp.save("shortcutEnabled", true);
//
//        ShortcutInfo shortcut = new ShortcutInfo.Builder(this, "reqStats")
//                .setShortLabel(getString(R.string.shortcut_req_stats))
//                .setLongLabel(getString(R.string.shortcut_req_stats))
//                .setIcon(Icon.createWithResource(this, R.drawable.ic_shortcut_stats))
//                .setIntent(new Intent(this, ReqStatsActivity.class))
//                .build();
//
//        ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);
//        shortcutManager.setDynamicShortcuts(Arrays.asList(shortcut));
//    }
}
