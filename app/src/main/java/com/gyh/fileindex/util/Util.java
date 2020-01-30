package com.gyh.fileindex.util;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.SpannableString;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.gyh.fileindex.R;
import com.gyh.fileindex.api.CountItemsOrAndSizeTask;
import com.gyh.fileindex.api.GenerateHashesTask;
import com.gyh.fileindex.api.LoadFolderSpaceDataTask;
import com.gyh.fileindex.bean.ApkInfo;
import com.gyh.fileindex.bean.FileInfo;

import java.io.File;
import java.io.FileFilter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static androidx.core.content.ContextCompat.getColor;

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

    private static int mNoPermissionIndex = 0;
    private static int PERMISSION_REQUEST_CODE = 1;
    private static String[] permissionManifest = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private static int[] noPermissionTip = {
            R.string.no_read_phone_state_permission,
            R.string.no_write_external_storage_permission,
            R.string.no_read_external_storage_permission
    };

    public static void permissionCheck(Activity activity) {
        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        String permission;
        for (int i = 0; i < permissionManifest.length; i++) {
            permission = permissionManifest[i];
            mNoPermissionIndex = i;
            if (ContextCompat.checkSelfPermission(activity, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionCheck = PackageManager.PERMISSION_DENIED;
            }
        }
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= 23) {
                ActivityCompat.requestPermissions(activity, permissionManifest, PERMISSION_REQUEST_CODE);
            } else {
                showNoPermissionTip(activity, activity.getString(noPermissionTip[mNoPermissionIndex]));
                activity.finish();
            }
        }
    }

    public static int getNumberOfCPUCores() {
        int cores;
        try {
            cores = new File("/sys/devices/system/cpu/").listFiles(CPU_FILTER).length;
        } catch (SecurityException e) {
            cores = -1;
        } catch (NullPointerException e) {
            cores = -2;
        }
        return cores;
    }

    private static final FileFilter CPU_FILTER = pathname -> {
        String path = pathname.getName();
        //regex is slow, so checking char by char.
        if (path.startsWith("cpu")) {
            for (int i = 3; i < path.length(); i++) {
                if (path.charAt(i) < '0' || path.charAt(i) > '9') {
                    return false;
                }
            }
            return true;
        }
        return false;
    };

    private static void showNoPermissionTip(Context context, String tip) {
        Toast.makeText(context, tip, Toast.LENGTH_LONG).show();
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
     * 获取文件夹大小
     *
     * @param file File实例
     * @return long
     */
    public static long getTotalSizeOfFilesInDir(File file) {
        if (file.isFile())
            return file.length();
        final File[] children = file.listFiles();
        long total = 0;
        if (children != null)
            for (final File child : children)
                total += getTotalSizeOfFilesInDir(child);
        return total;
    }

    public static long[] getSpaces(File hFile) {
        long totalSpace = hFile.getTotalSpace();
        long freeSpace = hFile.getUsableSpace();
        long fileSize;

        if (hFile.isDirectory()) {
            fileSize = getTotalSizeOfFilesInDir(hFile);
        } else {
            fileSize = hFile.length();
        }
        return new long[]{totalSpace, freeSpace, fileSize};
    }


    /**
     * 显示apk详情弹窗
     *
     * @param baseFile apkinfo
     * @param activity activity
     */
    public static void showPropertiesDialog(final ApkInfo baseFile, Activity activity) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(activity);
        builder.title(activity.getString(R.string.properties));
        if (baseFile.getIcon() != null) {
            builder.icon(baseFile.getIcon());
        }
        View v = activity.getLayoutInflater().inflate(R.layout.properties_dialog, null);
        TextView itemsText = v.findViewById(R.id.t7);
        int accentColor = activity.getResources().getColor(R.color.zt, null);

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

    /**
     * 显示文件详情弹窗
     *
     * @param baseFile apkinfo
     * @param activity activity
     */
    public static void showPropertiesDialog(final FileInfo baseFile, Activity activity) {
        final ThreadManager executor = ThreadManager.getInstance();

        final String date = baseFile.getDate();

        MaterialDialog.Builder builder = new MaterialDialog.Builder(activity);
        builder.title(activity.getString(R.string.properties));

        View v = activity.getLayoutInflater().inflate(R.layout.properties_dialog_file_info, null);
        TextView itemsText = v.findViewById(R.id.t7);
        int accentColor = activity.getResources().getColor(R.color.zt, null);

        /*View setup*/
        {
            TextView mNameTitle = v.findViewById(R.id.title_name);
            mNameTitle.setTextColor(accentColor);

            TextView mDateTitle = v.findViewById(R.id.title_date);
            mDateTitle.setTextColor(accentColor);

            TextView mSizeTitle = v.findViewById(R.id.title_size);
            mSizeTitle.setTextColor(accentColor);

            TextView mLocationTitle = v.findViewById(R.id.title_location);
            mLocationTitle.setTextColor(accentColor);

            TextView md5Title = v.findViewById(R.id.title_md5);
            md5Title.setTextColor(accentColor);

            TextView sha256Title = v.findViewById(R.id.title_sha256);
            sha256Title.setTextColor(accentColor);

            ((TextView) v.findViewById(R.id.t5)).setText(baseFile.getName());
            ((TextView) v.findViewById(R.id.t6)).setText(baseFile.getPath());
            itemsText.setText(baseFile.getSize());
            ((TextView) v.findViewById(R.id.t8)).setText(date);

            LinearLayout mNameLinearLayout = v.findViewById(R.id.properties_dialog_name);
            LinearLayout mLocationLinearLayout = v.findViewById(R.id.properties_dialog_location);
            LinearLayout mSizeLinearLayout = v.findViewById(R.id.properties_dialog_size);
            LinearLayout mDateLinearLayout = v.findViewById(R.id.properties_dialog_date);

            // setting click listeners for long press
            mNameLinearLayout.setOnLongClickListener(v1 -> {
                copyToClipboard(activity, baseFile.getName());
                Toast.makeText(activity, activity.getString(R.string.name) + " " +
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
                copyToClipboard(activity, date);
                Toast.makeText(activity, activity.getString(R.string.date) + " " +
                        activity.getString(R.string.properties_copied_clipboard), Toast.LENGTH_SHORT).show();
                return false;
            });
        }

        CountItemsOrAndSizeTask countItemsOrAndSizeTask = new CountItemsOrAndSizeTask(activity, itemsText, baseFile);
        countItemsOrAndSizeTask.executeOnExecutor(executor.getExecutorService());


        GenerateHashesTask hashGen = new GenerateHashesTask(baseFile, activity, v);
        hashGen.executeOnExecutor(executor.getExecutorService());

        /*Chart creation and data loading*/
        {
            boolean isRightToLeft = false;
            PieChart chart = v.findViewById(R.id.chart);

            chart.setTouchEnabled(false);
            chart.setDrawEntryLabels(false);
            chart.setDescription(null);
            chart.setNoDataText(activity.getString(R.string.loading));
            chart.setRotationAngle(0f);
            chart.setHoleColor(Color.TRANSPARENT);

            chart.getLegend().setEnabled(true);
            chart.getLegend().setForm(Legend.LegendForm.CIRCLE);
            chart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
            chart.getLegend().setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            chart.animateY(1000);
            LoadFolderSpaceDataTask loadFolderSpaceDataTask = new LoadFolderSpaceDataTask(activity, chart, baseFile);
            loadFolderSpaceDataTask.executeOnExecutor(executor.getExecutorService());

            chart.invalidate();
        }


        builder.customView(v, true);
        builder.positiveText(activity.getString(R.string.ok));
        builder.positiveColor(accentColor);
        builder.dismissListener(dialog -> {
            countItemsOrAndSizeTask.cancel(true);
            hashGen.cancel(true);
        });

        MaterialDialog materialDialog = builder.build();
        materialDialog.show();
        materialDialog.getActionButton(DialogAction.NEGATIVE).setEnabled(false);
    }

    /**
     * 显示内存空余详情
     *
     * @param activity activity
     */
    public static void showMainDialog(Activity activity, PieChart chart) {
        chart.setTouchEnabled(false);
        chart.setDrawEntryLabels(false);
        chart.setDescription(null);
        chart.setNoDataText(activity.getString(R.string.loading));
        chart.setRotationAngle(0f);
        chart.setHoleColor(Color.TRANSPARENT);

        chart.getLegend().setEnabled(true);
        chart.getLegend().setForm(Legend.LegendForm.CIRCLE);
        chart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        chart.getLegend().setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        chart.animateY(1000);

        chart.invalidate();
        File file = Environment.getExternalStorageDirectory();
        final long totalSpace = file.getTotalSpace();
        long freeSpace = file.getFreeSpace();
        long usedByOther = totalSpace - freeSpace;
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(usedByOther, activity.getString(R.string.used_by_others)));
        entries.add(new PieEntry(freeSpace, activity.getString(R.string.free)));

        PieDataSet set = new PieDataSet(entries, null);
        set.setColors(getColor(activity, R.color.piechart_blue),
                getColor(activity, R.color.piechart_green));
        set.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        set.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        set.setSliceSpace(5f);
        set.setAutomaticallyDisableSliceSpacing(true);
        set.setValueLinePart2Length(0.5f);
        set.setValueTextSize(12f);
        set.setSelectionShift(1f);

        PieData pieData = new PieData(set);
        pieData.setValueFormatter(new SizeFormatter(activity));

        chart.setCenterText(new SpannableString(activity.getString(R.string.total) + "\n" +
                Formatter.formatFileSize(activity, totalSpace)));
        chart.setData(pieData);

        chart.notifyDataSetChanged();
        chart.invalidate();
    }

    public static class SizeFormatter extends ValueFormatter {

        private Context context;

        public SizeFormatter(Context c) {
            context = c;
        }

        @Override
        public String getFormattedValue(float value) {
            return Formatter.formatFileSize(context, (long) value);
        }
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


    /**
     * 调用系统应用打开图片
     *
     * @param context context
     * @param file    file
     */
    public static void openFile(File file, Context context) {
        if (!file.exists()) {
            //如果文件不存在
            Toast.makeText(context, "打开失败，原因：文件已经被移动或者删除", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //设置intent的Action属性
        intent.setAction(Intent.ACTION_VIEW);
        Uri uri;
        // 支持Android7.0，Android 7.0以后，用了Content Uri 替换了原本的File Uri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = getUri(context, intent, file);
        } else {
            uri = Uri.fromFile(file);
        }

        //获取文件file的MIME类型
        String type = getMIMEType(file);
        //设置intent的data和Type属性。
        intent.setDataAndType(uri, type);
        //跳转
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            Log.e("FileUtil", e.getMessage(), e);
            Toast.makeText(context, "找不到打开此文件的应用！", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 获取对应文件的Uri
     *
     * @param intent 相应的Intent
     * @param file   文件对象
     * @return
     */
    private static Uri getUri(Context context, Intent intent, File file) {
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //判断版本是否在7.0以上
            uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    /**
     * 根据文件后缀回去MIME类型
     *
     * @param file file
     * @return string
     */
    private static String getMIMEType(File file) {
        String type = "*/*";
        String fName = file.getName();

        //获取后缀名前的分隔符"."在fName中的位置。
        int dotIndex = fName.lastIndexOf(".");
        if (dotIndex < 0) {
            return type;
        }

        /* 获取文件的后缀名*/
        String end = fName.substring(dotIndex).toLowerCase();
        if ("".equals(end)) {
            return type;
        }

        //在MIME和文件类型的匹配表中找到对应的MIME类型。
        for (String[] strings : MIME_MapTable) {
            if (end.equals(strings[0])) {
                type = strings[1];
                break;
            }
        }
        return type;
    }

    /**
     * 根据后缀获取文件MIME
     *
     * @param index 文件后缀
     * @return 文件的MIME类型
     */
    public static String getMIME(String index) {
        for (String[] s : MIME_MapTable) {
            if (s[0].equals(index)) {
                return s[1];
            }
        }
        return "*/*";
    }

    private static final String[][] MIME_MapTable = {
            // {后缀名，MIME类型}
            {".aac", "audio/aac"},
            {".abw", "application/x-abiword"},
            {".apk", "application/vnd.android.package-archive"},
            {".asf", "video/x-ms-asf"},
            {".arc", "application/x-freearc"},
            {".avi", "video/x-msvideo"},
            {".azw", "application/vnd.amazon.ebook"},
            {".bin", "application/octet-stream"},
            {".bmp", "image/bmp"},
            {".bz", "application/x-bzip"},
            {".bz2", "application/x-bzip2"},
            {".csh", "application/x-csh"},
            {".css", "text/css"},
            {".csv", "text/csv"},
            {".c", "text/plain"},
            {".class", "application/octet-stream"},
            {".conf", "text/plain"},
            {".cpp", "text/plain"},
            {".doc", "application/msword"},
            {".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"},
            {".eot", "application/vnd.ms-fontobject"},
            {".exe", "application/octet-stream"},
            {".epub", "application/epub+zip"},
            {".gif", "image/gif"},
            {".gtar", "application/x-gtar"},
            {".gz", "application/x-gzip"},
            {".h", "text/plain"},
            {".htm", "text/html"},
            {".html", "text/html"},
            {".ico", "image/vnd.microsoft.icon"},
            {".ics", "text/calendar"},
            {".jar", "application/java-archive"},
            {".java", "text/plain"},
            {".jpeg", "image/jpeg"},
            {".jpg", "image/jpeg"},
            {".js", "text/javascript"},
            {".json", "application/json"},
            {".jsonld", "application/ld+json"},
            {".log", "text/plain"},
            {".midi", "audio/x-midi"},
            {".mid", "audio/midi"},
            {".mjs", "text/javascript"},
            {".mp3", "audio/mpeg"},
            {".m3u", "audio/x-mpegurl"},
            {".m4a", "audio/mp4a-latm"},
            {".m4b", "audio/mp4a-latm"},
            {".m4p", "audio/mp4a-latm"},
            {".m4u", "video/vnd.mpegurl"},
            {".m4v", "video/x-m4v"},
            {".mov", "video/quicktime"},
            {".mp2", "audio/x-mpeg"},
            {".mp3", "audio/x-mpeg"},
            {".mp4", "video/mp4"},
            {".mpc", "application/vnd.mpohun.certificate"},
            {".mpe", "video/mpeg"},
            {".mpg", "video/mpeg"},
            {".mpeg", "video/mpeg"},
            {".mpg4", "video/mp4"},
            {".mpga", "audio/mpeg"},
            {".msg", "application/vnd.ms-outlook"},
            {".mpkg", "application/vnd.apple.installer+xml"},
            {".ogg", "audio/ogg"},
            {".odp", "application/vnd.oasis.opendocument.presentation"},
            {".ods", "application/vnd.oasis.opendocument.spreadsheet"},
            {".odt", "application/vnd.oasis.opendocument.text"},
            {".oga", "audio/ogg"},
            {".ogv", "video/ogg"},
            {".ogx", "application/ogg"},
            {".otf", "font/otf"},
            {".png", "image/png"},
            {".pdf", "application/pdf"},
            {".pps", "application/vnd.ms-powerpoint"},
            {".ppt", "application/vnd.ms-powerpoint"},
            {".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"},
            {".prop", "text/plain"},
            {".rar", "application/x-rar-compressed"},
            {".rc", "text/plain"},
            {".rmvb", "audio/x-pn-realaudio"},
            {".rtf", "application/rtf"},
            {".sh", "application/x-sh"},
            {".svg", "image/svg+xml"},
            {".swf", "application/x-shockwave-flash"},
            {".tar", "application/x-tar"},
            {".tgz", "application/x-compressed"},
            {".tif", "image/tiff"},
            {".tiff", "image/tiff"},
            {".ttf", "font/ttf"},
            {".txt", "text/plain"},
            {".vsd", "application/vnd.visio"},
            {".wav", "audio/wav"},
            {".weba", "audio/webm"},
            {".webm", "video/webm"},
            {".webp", "image/webp"},
            {".wma", "audio/x-ms-wma"},
            {".wps", "application/vnd.ms-works"},
            {".woff", "font/woff"},
            {".woff2", "font/woff2"},
            {".xhtml", "application/xhtml+xml"},
            {".xls", "application/vnd.ms-excel"},
            {".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"},
            {".xml", "text/xml"},
            {".xul", "application/vnd.mozilla.xul+xml"},
            {".z", "application/x-compress"},
            {".zip", "application/zip"},
            {".rar", "application/x-rar-compressed"},
            {".3gp", "video/3gpp"},
            {".3g2", "video/3gpp2"},
            {".7z", "application/x-7z-compressed"},
            {"", "*/*"},
    };

}
