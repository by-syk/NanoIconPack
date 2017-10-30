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
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.by_syk.lib.nanoiconpack.dialog.ApplyDialog;
import com.by_syk.lib.nanoiconpack.fragment.AppsFragment;
import com.by_syk.lib.nanoiconpack.fragment.IconsFragment;
import com.by_syk.lib.nanoiconpack.util.AllIconsGetter;
import com.by_syk.lib.nanoiconpack.util.ExtraUtil;
import com.by_syk.lib.nanoiconpack.util.MatchedIconsGetter;
import com.by_syk.lib.nanoiconpack.util.PkgUtil;
import com.by_syk.lib.nanoiconpack.util.SimplePageTransformer;
import com.by_syk.lib.sp.SP;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

/**
 * Created by By_syk on 2016-07-16.
 */

public class MainActivity extends AppCompatActivity
        implements IconsFragment.OnLoadDoneListener, AppsFragment.OnLoadDoneListener {
    private SP sp;

    private ViewPager viewPager;

    private BottomNavigationView bottomNavigationView;

    private boolean enableStatsModule = true;

    private int prevPagePos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        sp = new SP(this);

        enableStatsModule = getResources().getBoolean(R.bool.enable_req_stats_module);

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation_view);

        viewPager.setOffscreenPageLimit(3); // Keep all 3 pages alive.
        viewPager.setPageTransformer(true, new SimplePageTransformer(getResources()
                .getInteger(R.integer.home_page_transform_anim)));
        viewPager.setAdapter(new IconsPagerAdapter(getSupportFragmentManager()));
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
            private long lastTapTime = 0;

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_lost) {
                    viewPager.setCurrentItem(0);
                    prepareReqPrompt();
                } else if (id == R.id.nav_matched) {
                    viewPager.setCurrentItem(1);
                } else if (id == R.id.nav_all) {
                    viewPager.setCurrentItem(2);
                }
                if (!enableStatsModule) {
                    return true;
                }
                if (id == R.id.nav_lost) {
                    if (System.currentTimeMillis() - lastTapTime < 400) {
                        enterConsole();
                        lastTapTime = 0;
                    } else {
                        lastTapTime = System.currentTimeMillis();
                    }
                } else {
                    lastTapTime = 0;
                }
                return true;
            }
        });

        // Set the default page to show.
        // 0: Lost, 1: Matched 2. All
        viewPager.setCurrentItem(1);
    }

    public void prepareReqPrompt() {
        if (sp.getBoolean("hintReq")) {
            return;
        }
        sp.save("hintReq", true);

        bottomNavigationView.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isDestroyed()) {
                    showReqPrompt();
                }
            }
        }, 800);
    }

    public void showReqPrompt() {
        (new MaterialTapTargetPrompt.Builder(this))
                .setTarget(bottomNavigationView.findViewById(R.id.nav_lost))
                .setPrimaryText(getString(R.string.prompt_req))
                .setSecondaryText(getString(R.string.prompt_req_desc))
                .setBackgroundColour(getResources().getColor(R.color.color_primary))
                .setAutoDismiss(false)
                .setCaptureTouchEventOutsidePrompt(true)
                .setAnimationInterpolator(new FastOutSlowInInterpolator())
                .show();
    }

    private void enterConsole() {
        if (!ExtraUtil.isNetworkConnected(this)) {
            return;
        }

        (new Handler()).postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(MainActivity.this, ReqStatsActivity.class));
            }
        }, 100);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // If latest icons is provided, show the entrance menu item.
        if (getResources().getStringArray(R.array.latest_icons).length > 0) {
            menu.findItem(R.id.menu_whats_new).setVisible(true);
            if (menu.findItem(R.id.menu_apply).getIcon() == null) {
                if (!sp.getBoolean("hideLatest" + PkgUtil.getAppVer(this, "%1$s"))) {
                    menu.findItem(R.id.menu_whats_new).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                    menu.findItem(R.id.menu_search).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
                }
            }
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_whats_new) {
            sp.save("hideLatest" + PkgUtil.getAppVer(this, "%1$s"), true);
            item.setIntent(new Intent(this, WhatsNewActivity.class));
            return super.onOptionsItemSelected(item);
        } else if (id == R.id.menu_search) {
            item.setIntent(new Intent(this, SearchActivity.class));
            return super.onOptionsItemSelected(item);
        } else if (id == R.id.menu_apply) {
            (new ApplyDialog()).show(getSupportFragmentManager(), "applyDialog");
            return true;
        } else if (id == R.id.menu_about) {
            item.setIntent(new Intent(this, AboutActivity.class));
            return super.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLoadDone(int pageId, int sum) {
        MenuItem menuItem = bottomNavigationView.getMenu().getItem(pageId);
        switch (pageId) {
            case 0:
                menuItem.setTitle(getString(R.string.nav_lost) + "(" + sum + ")");
                break;
            case 1:
                menuItem.setTitle(getString(R.string.nav_matched) + "(" + sum + ")");
                break;
            case 2:
                menuItem.setTitle(getString(R.string.nav_all) + "(" + sum + ")");
                break;
        }
    }

    class IconsPagerAdapter extends FragmentPagerAdapter {
        IconsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return AppsFragment.newInstance(position);
                case 1:
                    return IconsFragment.newInstance(position, new MatchedIconsGetter(),
                            getResources().getInteger(R.integer.home_grid_item_mode));
                case 2:
                    return IconsFragment.newInstance(position, new AllIconsGetter(),
                            getResources().getInteger(R.integer.home_grid_item_mode));
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
