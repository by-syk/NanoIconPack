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
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.by_syk.lib.nanoiconpack.dialog.ApplyDialog;
import com.by_syk.lib.nanoiconpack.fragment.AppsFragment;
import com.by_syk.lib.nanoiconpack.fragment.IconsFragment;

/**
 * Created by By_syk on 2016-07-16.
 */

public class MainActivity extends AppCompatActivity {
    private ViewPager viewPager;

    private BottomNavigationView bottomNavigationView;

    private int prevPagePos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
//        ((PagerTabStrip) findViewById(R.id.pager_tab_strip))
//                .setTabIndicatorColor(getResources().getColor(R.color.color_primary));

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        IconsPagerAdapter pagerAdapter = new IconsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation_view);
        bottomNavigationView.getMenu().getItem(2).setTitle(getString(R.string.nav_all,
                getResources().getStringArray(R.array.icons).length));

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                bottomNavigationView.getMenu().getItem(prevPagePos).setChecked(false);
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
                prevPagePos = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_lost) {
                    viewPager.setCurrentItem(0);
                } else if (id == R.id.nav_matched) {
                    viewPager.setCurrentItem(1);
                } else if (id == R.id.nav_all) {
                    viewPager.setCurrentItem(2);
                }
                return true;
            }
        });

        viewPager.setCurrentItem(1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_apply) {
            (new ApplyDialog()).show(getSupportFragmentManager(), "applyDialog");
            return true;
        } else if (id == R.id.menu_about) {
            item.setIntent(new Intent(this, AboutActivity.class));
            return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    class IconsPagerAdapter extends FragmentPagerAdapter {
//        private String[] titles;

        IconsPagerAdapter(FragmentManager fm) {
            super(fm);

//            titles = getResources().getStringArray(R.array.tabs);
//            titles[2] = String.format(Locale.US, titles[2],
//                    getResources().getStringArray(R.array.icons).length);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return AppsFragment.newInstance();
            }
            return IconsFragment.newInstance(position);
        }

//        @Override
//        public CharSequence getPageTitle(int position) {
//            return titles[position];
//        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
