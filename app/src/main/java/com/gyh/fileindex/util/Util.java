package com.gyh.fileindex.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;

import java.text.DecimalFormat;

public class Util {
    public static final double GB = 1024 * 1024 * 1024;
    public static final double MB = 1024 * 1024;
    public static final double KB = 1024;

    public static String getNetFileSizeDescription(long size) {
        StringBuilder bytes = new StringBuilder();
        DecimalFormat format = new DecimalFormat("###.00");
        if (size >= GB) {
            double i = (size / GB);
            bytes.append(format.format(i)).append("GB");
        } else if (size >= MB) {
            double i = (size / MB);
            bytes.append(format.format(i)).append("MB");
        } else if (size >= KB) {
            double i = (size / KB);
            bytes.append(format.format(i)).append("KB");
        } else if (size <= 0) {
            bytes.append("0B");
        } else {
            bytes.append((int) size).append("B");
        }
        return bytes.toString();
    }

    public static void revealShow(final View view, boolean reveal) {
        if (reveal) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f);
            animator.setDuration(300); //ms
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    view.setVisibility(View.VISIBLE);
                }
            });
            animator.start();
        } else {

            ObjectAnimator animator = ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f);
            animator.setDuration(300); //ms
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    view.setVisibility(View.GONE);
                }
            });
            animator.start();
        }
    }
}
