package com.gyh.fileindex.util;

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
}
