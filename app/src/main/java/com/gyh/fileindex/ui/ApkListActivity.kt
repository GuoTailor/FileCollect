package com.gyh.fileindex.ui

import android.app.Activity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.gyh.fileindex.QuickAdapter
import com.gyh.fileindex.R
import com.gyh.fileindex.api.AsynchronizedTask
import com.gyh.fileindex.util.ThreadManager
import com.gyh.fileindex.api.Monitor
import com.gyh.fileindex.api.TabInfoData
import com.gyh.fileindex.bean.ApkInfo
import com.gyh.fileindex.util.Util
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class ApkListActivity : BaseActivity<ApkInfo>(), Monitor {
    override var data = ArrayList<ApkInfo>()
    private var converting: AsynchronizedTask<ApkInfo>? = null

    override fun updateProgress(vararg files: File) {
        for (file in files) {
            val apkInfo = ApkInfo(file)
            data.add(apkInfo)
            data.sortWith(Comparator { o1, o2 ->
                val i = o1.appName.compareTo(o2.appName)
                if (i == 0) {
                    o1.version.compareTo(o2.version)
                } else {
                    i
                }
            })
            val index = data.indexOf(apkInfo)
            quickAdapter.notifyItemInserted(index)
        }
    }

    override fun updateResult(result: String) {
        fab.clearAnimation()
    }

    override fun showPropertiesDialog(baseFile: ApkInfo, activity: Activity) {
        Util.showPropertiesDialog(baseFile, this)
    }

    override fun isCare(file: File) = tabInfo.exitSuffix(file.name)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TabInfoData.addListener(this)
        converting = AsynchronizedTask({
            data.add(it[0])
            val index = data.indexOf(it[0])
            quickAdapter.notifyItemInserted(index)
        }, ::updateResult, { task, _ ->
            val mData = ArrayList(tabInfo.fileInfos)
            for (file in mData) {
                task.updateProgress(ApkInfo(file))
            }
        })
        converting?.executeOnExecutor(ThreadManager.getInstance().executorService)
    }

    override fun getLayoutId(viewType: Int): Int {
        return R.layout.apk_item
    }

    override fun convert(holder: QuickAdapter<ApkInfo>.VH, data: ApkInfo, position: Int) {
        holder.getView<TextView>(R.id.time)?.text = data.date
        holder.getView<TextView>(R.id.size)?.text = data.size
        holder.getView<TextView>(R.id.appName)?.text = data.appName
        holder.getView<ImageView>(R.id.img)?.setImageDrawable(data.icon)
        holder.getView<TextView>(R.id.version)?.text = "版本: " + data.version
        holder.getView<TextView>(R.id.currentVersion)?.text = data.currentVersion
    }

    override fun onDestroy() {
        super.onDestroy()
        TabInfoData.removeListener(this)
        converting?.cancel(true)
    }
}