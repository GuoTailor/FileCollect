package com.gyh.fileindex.api;

import android.app.Activity;
import android.os.AsyncTask;
import android.text.SpannableString;
import android.text.format.Formatter;
import android.view.View;

import androidx.core.util.Pair;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.gyh.fileindex.R;
import com.gyh.fileindex.bean.FileInfo;
import com.gyh.fileindex.util.Util;

import java.util.ArrayList;
import java.util.List;

import static androidx.core.content.ContextCompat.getColor;


/**
 * Loads data for chart in FileUtils.showPropertiesDialog()
 *
 * @author Emmanuel Messulam<emmanuelbendavid@gmail.com>
 *         on 12/5/2017, at 00:07.
 */

public class LoadFolderSpaceDataTask extends AsyncTask<Void, Long, Pair<String, List<PieEntry>>> {

    private static int[] COLORS;
    private static String[] LEGENDS;

    private final Activity context;
    private final PieChart chart;
    private final FileInfo file;

    public LoadFolderSpaceDataTask(Activity c, PieChart chart, FileInfo f) {
        context = c;
        this.chart = chart;
        file = f;
        LEGENDS = new String[]{context.getString(R.string.size), context.getString(R.string.used_by_others), context.getString(R.string.free)};
        COLORS = new int[]{getColor(c, R.color.piechart_red), getColor(c, R.color.piechart_blue),
                getColor(c, R.color.piechart_green)};
    }

    @Override
    protected Pair<String, List<PieEntry>> doInBackground(Void... params) {
        long[] dataArray =  Util.getSpaces(file.getFile().length());

        if (dataArray[0] != -1 && dataArray[0] != 0) {
            long totalSpace = dataArray[0];

            List<PieEntry> entries = createEntriesFromArray(dataArray, false);

            return new Pair<>(Formatter.formatFileSize(context, totalSpace), entries);
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Long[] dataArray) {
        if (dataArray[0] != -1 && dataArray[0] != 0) {
            long totalSpace = dataArray[0];

            List<PieEntry> entries = createEntriesFromArray(
                    new long[]{dataArray[0], dataArray[1], dataArray[2]},
                    true);

            updateChart(Formatter.formatFileSize(context, totalSpace), entries);

            chart.notifyDataSetChanged();
            chart.invalidate();
        }
    }

    @Override
    protected void onPostExecute(Pair<String, List<PieEntry>> data) {
        if(data == null) {
            chart.setVisibility(View.GONE);
            return;
        }

        updateChart(data.first, data.second);

        chart.notifyDataSetChanged();
        chart.invalidate();
    }

    private List<PieEntry> createEntriesFromArray(long[] dataArray, boolean loading) {
        long usedByFolder = dataArray[2],
                usedByOther = dataArray[0] - dataArray[1] - dataArray[2],
                freeSpace = dataArray[1];

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(usedByFolder, LEGENDS[0], loading? ">":null));
        entries.add(new PieEntry(usedByOther, LEGENDS[1], loading? "<":null));
        entries.add(new PieEntry(freeSpace, LEGENDS[2]));

        return entries;
    }

    private void updateChart(String totalSpace, List<PieEntry> entries) {

        PieDataSet set = new PieDataSet(entries, null);
        set.setColors(COLORS);
        set.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        set.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        set.setSliceSpace(5f);
        set.setAutomaticallyDisableSliceSpacing(true);
        set.setValueLinePart2Length(1.05f);
        set.setSelectionShift(0f);

        PieData pieData = new PieData(set);
        pieData.setValueFormatter(new Util.SizeFormatter(context));

        chart.setCenterText(new SpannableString(context.getString(R.string.total) + "\n" + totalSpace));
        chart.setData(pieData);
    }

}
