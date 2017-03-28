package com.by_syk.lib.nanoiconpack.util;

import android.support.v4.view.ViewPager;
import android.view.View;

import java.util.Random;

/**
 * Created by By_syk on 2016-11-04.
 */

public class SimplePageTransformer implements ViewPager.PageTransformer {
    private int animType = ANIM_ZOOM_OUT;

    public static final int ANIM_RANDOM = 0;
    public static final int ANIM_DEFAULT = 1;
    public static final int ANIM_ZOOM_OUT = 2;
    public static final int ANIM_DEPTH = 3;

    public SimplePageTransformer() {}

    public SimplePageTransformer(int animType) {
        if (animType == ANIM_RANDOM) {
            int[] anim_types = {ANIM_DEFAULT ,ANIM_ZOOM_OUT, ANIM_DEPTH};
            this.animType = anim_types[(new Random()).nextInt(anim_types.length)];
        }
    }

    public void transformPage(View view, float position) {
        switch (animType) {
            case ANIM_ZOOM_OUT:
                transformPageZoomOut(view, position);
                break;
            case ANIM_DEPTH:
                transformPageDepth(view, position);
                break;
            case ANIM_DEFAULT:
                // Do nothing
        }
    }

    private void transformPageZoomOut(View view, float position) {
        final float MIN_SCALE = 0.85f;
        final float MIN_ALPHA = 0.5f;

        int page_width = view.getWidth();
        int page_height = view.getHeight();

        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            view.setAlpha(0);
        } else if (position <= 1) { // [-1,1]
            // Modify the default slide transition to shrink the page as well
            float scale_factor = Math.max(MIN_SCALE, 1 - Math.abs(position));
            float vert_margin = page_height * (1 - scale_factor) / 2;
            float horz_margin = page_width * (1 - scale_factor) / 2;
            if (position < 0) {
                view.setTranslationX(horz_margin - vert_margin / 2);
            } else {
                view.setTranslationX(-horz_margin + vert_margin / 2);
            }

            // Scale the page down (between MIN_SCALE and 1)
            view.setScaleX(scale_factor);
            view.setScaleY(scale_factor);

            // Fade the page relative to its size.
            view.setAlpha(MIN_ALPHA + (scale_factor - MIN_SCALE)
                    / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            view.setAlpha(0);
        }
    }

    private void transformPageDepth(View view, float position) {
        final float MIN_SCALE = 0.75f;

        int page_width = view.getWidth();

        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            view.setAlpha(0);
        } else if (position <= 0) { // [-1,0]
            // Use the default slide transition when moving to the left page
            view.setAlpha(1);
            view.setTranslationX(0);
            view.setScaleX(1);
            view.setScaleY(1);
        } else if (position <= 1) { // (0,1]
            // Fade the page out.
            view.setAlpha(1 - position);

            // Counteract the default slide transition
            view.setTranslationX(page_width * -position);

            // Scale the page down (between MIN_SCALE and 1)
            float scale_factor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
            view.setScaleX(scale_factor);
            view.setScaleY(scale_factor);
        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            view.setAlpha(0);
        }
    }
}