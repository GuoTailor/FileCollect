package com.gyh.fileindex.ui

import android.app.Activity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.gyh.fileindex.QuickAdapter
import com.gyh.fileindex.R
import com.gyh.fileindex.api.AsynchTask
import com.gyh.fileindex.api.Monitor
import com.gyh.fileindex.api.SortAdapter
import com.gyh.fileindex.api.TabInfoData
import com.gyh.fileindex.bean.ApkInfo
import com.gyh.fileindex.bean.HybridFile
import com.gyh.fileindex.util.Util

class ApkListActivity : BaseActivity<ApkInfo>(), Monitor {
    override var data = ArrayList<ApkInfo>()
    private var converting: AsynchTask<ApkInfo>? = null
    override lateinit var sortAdapter: SortAdapter<ApkInfo>

    override fun updateProgress(vararg files: HybridFile) {
        for (file in files) {
            val apkInfo = ApkInfo(file)
            data.add(apkInfo)
            sortAdapter.sort(data)
            val index = data.indexOf(apkInfo)
            quickAdapter.notifyItemInserted(index)
        }
    }

    override fun sort() = sortAdapter.sort(data)

    override fun updateResult(result: String) {
        binding.fab.clearAnimation()
        recyclerView.scrollToPosition(0)
    }

    override fun showPropertiesDialog(baseFile: ApkInfo, activity: Activity) {
        Util.showPropertiesDialog(baseFile, this)
    }

    override fun isCare(file: HybridFile): Boolean =
        tabInfo.exitSuffix(file.name() ?: "") || (file.name() ?: "").contains(".apk", true)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TabInfoData.addListener(this)
        sortAdapter = object : SortAdapter<ApkInfo>(this, tabInfo.text) {
            override fun compare(): Comparator<ApkInfo> {
                return if (sort.equals(context.getString(R.string.sortName))) {
                    Comparator { o1, o2 ->
                        o1.appName.trimStart().compareTo(o2.appName.trimStart())
                    }
                } else super.compare()
            }
        }
        converting = AsynchTask({
            data.add(it[0])
            sortAdapter.sort(data)
            val index = data.indexOf(it[0])
            quickAdapter.notifyItemInserted(index)
        }, ::updateResult, {
            ArrayList(tabInfo.fileInfos).forEach {
                updateProgress(ApkInfo(it))
            }
        })
        converting?.execute()
    }

    override fun getLayoutId(viewType: Int): Int {
        return R.layout.apk_item
    }

    override fun convert(holder: QuickAdapter<ApkInfo>.VH, data: ApkInfo, position: Int) {
        holder.getView<TextView>(R.id.time)?.text = data.date
        holder.getView<TextView>(R.id.size)?.text = data.size
        holder.getView<TextView>(R.id.appName)?.text = data.appName
        holder.getView<ImageView>(R.id.img)?.setImageDrawable(data.icon)
        holder.getView<TextView>(R.id.version)?.text = "版本: ${data.version}"
        holder.getView<TextView>(R.id.currentVersion)?.text = data.currentVersion
    }

    override fun onDestroy() {
        super.onDestroy()
        TabInfoData.removeListener(this)
        converting?.cancel(true)
    }
}