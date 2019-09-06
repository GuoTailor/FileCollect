package com.gyh.fileindex.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.gyh.fileindex.ApkInfo;
import com.gyh.fileindex.R;

import java.io.File;
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

    /**
     * 安装apk
     */
    public static void install(File file, Context mContext) {
        Log.e("install", "" + file.length());
        //安装
        Intent install = new Intent(Intent.ACTION_VIEW);
        //7.0获取存储文件的uri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri uri = FileProvider.getUriForFile(mContext, "com.gyh.fileindex.fileprovider", file);
            install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //赋予临时权限
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            //设置dataAndType
            install.setDataAndType(uri, "application/vnd.android.package-archive");
        } else {
            install.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        }
        mContext.startActivity(install);
    }

    public static void showPropertiesDialog(final ApkInfo baseFile,
                                             Activity activity) {

        MaterialDialog.Builder builder = new MaterialDialog.Builder(activity);
        builder.title(activity.getString(R.string.properties));
        if (baseFile.getIcon() != null) {
            builder.icon(baseFile.getIcon());
        }

        View v = activity.getLayoutInflater().inflate(R.layout.properties_dialog, null);
        TextView itemsText = v.findViewById(R.id.t7);
        int accentColor = activity.getResources().getColor(R.color.colorPrimary, null);

        {
            TextView mNameTitle = v.findViewById(R.id.title_name);
            mNameTitle.setTextColor(accentColor);

            TextView mDateTitle = v.findViewById(R.id.title_date);
            mDateTitle.setTextColor(accentColor);

            TextView mAppNameTitle = v.findViewById(R.id.title_app_name);
            mAppNameTitle.setTextColor(accentColor);

            TextView mSizeTitle = v.findViewById(R.id.title_size);
            mSizeTitle.setTextColor(accentColor);

            TextView mLocationTitle = v.findViewById(R.id.title_location);
            mLocationTitle.setTextColor(accentColor);

            TextView mVersionTitle = v.findViewById(R.id.title_version);
            mVersionTitle.setTextColor(accentColor);

            TextView mCurrentVersionTitle = v.findViewById(R.id.title_current_version);
            mCurrentVersionTitle.setTextColor(accentColor);

            ((TextView) v.findViewById(R.id.t5)).setText(baseFile.getPackageName());
            ((TextView) v.findViewById(R.id.t4)).setText(baseFile.getAppName());
            ((TextView) v.findViewById(R.id.t6)).setText(baseFile.getPath());
            itemsText.setText(baseFile.getSize());
            ((TextView) v.findViewById(R.id.t8)).setText(baseFile.getDate());
            ((TextView) v.findViewById(R.id.t9)).setText(baseFile.getVersion());
            ((TextView) v.findViewById(R.id.t10)).setText(baseFile.getCurrentVersion());

            LinearLayout mNameLinearLayout = v.findViewById(R.id.properties_dialog_name);
            LinearLayout mAppNameLinearLayout = v.findViewById(R.id.properties_dialog_app_name);
            LinearLayout mLocationLinearLayout = v.findViewById(R.id.properties_dialog_location);
            LinearLayout mSizeLinearLayout = v.findViewById(R.id.properties_dialog_size);
            LinearLayout mDateLinearLayout = v.findViewById(R.id.properties_dialog_date);
            LinearLayout mVersionLayout = v.findViewById(R.id.properties_dialog_version);
            LinearLayout mCurrentVersionLayout = v.findViewById(R.id.properties_dialog_current_version);

            // setting click listeners for long press
            mNameLinearLayout.setOnLongClickListener(v1 -> {
                copyToClipboard(activity, baseFile.getPackageName());
                Toast.makeText(activity, activity.getString(R.string.name) + " " +
                        activity.getString(R.string.properties_copied_clipboard), Toast.LENGTH_SHORT).show();
                return false;
            });
            mAppNameLinearLayout.setOnLongClickListener(v1 -> {
                copyToClipboard(activity, baseFile.getAppName());
                Toast.makeText(activity, activity.getString(R.string.apk_name) + " " +
                        activity.getString(R.string.properties_copied_clipboard), Toast.LENGTH_SHORT).show();
                return false;
            });
            mLocationLinearLayout.setOnLongClickListener(v12 -> {
                copyToClipboard(activity, baseFile.getPath());
                Toast.makeText(activity, activity.getString(R.string.location) + " " +
                        activity.getString(R.string.properties_copied_clipboard), Toast.LENGTH_SHORT).show();
                return false;
            });
            mSizeLinearLayout.setOnLongClickListener(v13 -> {
                copyToClipboard(activity, baseFile.getSize());
                Toast.makeText(activity, activity.getString(R.string.size) + " " +
                        activity.getString(R.string.properties_copied_clipboard), Toast.LENGTH_SHORT).show();
                return false;
            });
            mDateLinearLayout.setOnLongClickListener(v14 -> {
                copyToClipboard(activity, baseFile.getDate());
                Toast.makeText(activity, activity.getString(R.string.date) + " " +
                        activity.getString(R.string.properties_copied_clipboard), Toast.LENGTH_SHORT).show();
                return false;
            });
            mCurrentVersionLayout.setOnLongClickListener(v15 -> {
                copyToClipboard(activity, baseFile.getCurrentVersion());
                Toast.makeText(activity, activity.getString(R.string.current_version) + " " +
                        activity.getString(R.string.properties_copied_clipboard), Toast.LENGTH_SHORT).show();
                return false;
            });
            mVersionLayout.setOnLongClickListener(v16 -> {
                copyToClipboard(activity, baseFile.getVersion());
                Toast.makeText(activity, activity.getString(R.string.version) + " " +
                        activity.getString(R.string.properties_copied_clipboard), Toast.LENGTH_SHORT).show();
                return false;
            });
        }


        builder.customView(v, true);
        builder.positiveText(activity.getString(R.string.ok));
        builder.positiveColor(accentColor);

        MaterialDialog materialDialog = builder.build();
        materialDialog.show();
        materialDialog.getActionButton(DialogAction.NEGATIVE).setEnabled(false);

    }

    public static boolean copyToClipboard(Context context, String text) {
        try {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context
                    .getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData
                    .newPlainText(context.getString(R.string.clipboard_path_copy), text);
            clipboard.setPrimaryClip(clip);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
