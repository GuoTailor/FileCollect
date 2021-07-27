package com.gyh.fileindex.api;

import android.content.Context;
import android.os.AsyncTask;
import android.text.format.Formatter;
import android.util.Pair;
import android.widget.TextView;

import com.gyh.fileindex.R;
import com.gyh.fileindex.bean.FileInfo;
import com.gyh.fileindex.util.Util;

/**
 * @author Emmanuel
 *         on 12/5/2017, at 19:40.
 */

public class CountItemsOrAndSizeTask extends AsyncTask<Void, Pair<Integer, Long>, String> {

    private final Context context;
    private final TextView itemsText;
    private final FileInfo file;

    public CountItemsOrAndSizeTask(Context c, TextView itemsText, FileInfo f) {
        this.context = c;
        this.itemsText = itemsText;
        file = f;
    }

    @Override
    protected String doInBackground(Void[] params) {
        String items;
        long fileLength = file.getFile().length();

        if (file.getFile().isDirectory()) {
            final int folderLength = file.getFile().list().length;
            long folderSize;
            folderSize = Util.getTotalSizeOfFilesInDir(file.getFile());
            items = getText(folderLength, folderSize, false);
        } else {
            items = Formatter.formatFileSize(context, fileLength) + (" (" + fileLength + " "
                    + context.getResources().getQuantityString(R.plurals.bytes, (int) fileLength) //truncation is insignificant
                    + ")");
        }

        return items;
    }

    @Override
    protected void onProgressUpdate(Pair<Integer, Long>[] dataArr) {
        Pair<Integer, Long> data = dataArr[0];

        itemsText.setText(getText(data.first, data.second, true));
    }

    private String getText(int filesInFolder, long length, boolean loading) {
        String numOfItems = (filesInFolder != 0? filesInFolder + " ":"")
                + context.getResources().getQuantityString(R.plurals.items, filesInFolder) ;

        return numOfItems + "; " + (loading? ">":"") + Formatter.formatFileSize(context, length);
    }

    protected void onPostExecute(String items) {
        itemsText.setText(items);
    }
}
