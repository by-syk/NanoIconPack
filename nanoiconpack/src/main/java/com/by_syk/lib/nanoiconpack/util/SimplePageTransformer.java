package com.by_syk.lib.nanoiconpack.util;

import android.support.annotation.IntDef;
import android.support.v4.view.ViewPager;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
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
    public static final int ANIM_FLIP = 4;

    @IntDef({ANIM_RANDOM, ANIM_DEFAULT, ANIM_ZOOM_OUT, ANIM_DEPTH, ANIM_FLIP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AnimType {}

    public SimplePageTransformer() {}

    public SimplePageTransformer(int animType) {
        if (animType == ANIM_RANDOM) {
            int[] animTypes = {ANIM_DEFAULT, ANIM_ZOOM_OUT, ANIM_DEPTH};
            this.animType = animTypes[(new Random()).nextInt(animTypes.length)];
        } else {
            this.animType = animType;
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
            case ANIM_FLIP:
                transformPageFlip(view, position);
                break;
            case ANIM_DEFAULT:
                // Do nothing
        }
    }

    private void transformPageZoomOut(View view, float position) {
        final float MIN_SCALE = 0.85f;
        final float MIN_ALPHA = 0.5f;

        int pageWidth = view.getWidth();
        int pageHeight = view.getHeight();

        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            view.setAlpha(0);
        } else if (position <= 1) { // [-1,1]
            // Modify the default slide transition to shrink the page as well
            float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
            float verMargin = pageHeight * (1 - scaleFactor) / 2;
            float horzMargin = pageWidth * (1 - scaleFactor) / 2;
            if (position < 0) {
                view.setTranslationX(horzMargin - verMargin / 2);
            } else {
                view.setTranslationX(-horzMargin + verMargin / 2);
            }

            // Scale the page down (between MIN_SCALE and 1)
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);

            // Fade the page relative to its size.
            view.setAlpha(MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA));
        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            view.setAlpha(0);
        }
    }

    private void transformPageDepth(View view, float position) {
        final float MIN_SCALE = 0.75f;

        int pageWidth = view.getWidth();

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
            view.setTranslationX(pageWidth * -position);

            // Scale the page down (between MIN_SCALE and 1)
            float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);
        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            view.setAlpha(0);
        }
    }

    private void transformPageFlip(View view, float position) {
        float percentage = 1 - Math.abs(position);
        view.setCameraDistance(12000);
        setVisibility(view, position);
        setTranslation(view);
        setSize(view, position, percentage);
        setRotation(view, position, percentage);
    }

    private void setVisibility(View view, float position) {
        if (position < 0.5 && position > -0.5) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.INVISIBLE);
        }
    }

    private void setTranslation(View view) {
        ViewPager viewPager = (ViewPager) view.getParent();
        int scroll = viewPager.getScrollX() - view.getLeft();
        view.setTranslationX(scroll);
    }

    private void setSize(View view, float position, float percentage) {
        if (percentage < 0.85f) {
            percentage = 0.85f;
        }
        view.setScaleX((position != 0 && position != 1) ? percentage : 1);
        view.setScaleY((position != 0 && position != 1) ? percentage : 1);
    }

    private void setRotation(View view, float position, float percentage) {
        if (position > 0) {
            view.setRotationY(-180 * (percentage + 1));
        } else {
            view.setRotationY(180 * (percentage + 1));
        }
    }
}