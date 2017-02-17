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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

import com.by_syk.lib.nanoiconpack.dialog.ApplyDialog;
import com.by_syk.lib.nanoiconpack.fragment.AppsFragment;
import com.by_syk.lib.nanoiconpack.fragment.IconsFragment;

import java.util.Locale;

/**
 * Created by By_syk on 2016-07-16.
 */

public class MainActivity extends FragmentActivity {
    private ViewPager viewPager;

    private IconsPagerAdapter pagerAdapter;

    private boolean isFromLauncherPick = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
//        if (C.SDK >= 21) {
//            ((PagerTabStrip) findViewById(R.id.pager_tab_strip))
//                    .setTabIndicatorColor(getResources().getColor(R.color.color_primary));
//        }

        viewPager = (ViewPager) findViewById(R.id.view_pager);

        pagerAdapter = new IconsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        viewPager.setCurrentItem(1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        if (isFromLauncherPick) {
            menu.getItem(0).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_apply) {
            (new ApplyDialog()).show(getFragmentManager(), "applyDialog");
            return true;
        } else if (id == R.id.menu_about) {
            item.setIntent(new Intent(this, AboutActivity.class));
            return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    class IconsPagerAdapter extends FragmentPagerAdapter {
        private String[] titles;

        IconsPagerAdapter(FragmentManager fm) {
            super(fm);

            titles = getResources().getStringArray(R.array.tabs);
            titles[2] = String.format(Locale.US, titles[2],
                    getResources().getStringArray(R.array.icons).length);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return AppsFragment.newInstance();
            }
            return IconsFragment.newInstance(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

        @Override
        public int getCount() {
            return titles.length;
        }
    }
}
