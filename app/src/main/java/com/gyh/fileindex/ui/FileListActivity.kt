package com.gyh.fileindex.ui

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.gyh.fileindex.QuickAdapter
import com.gyh.fileindex.R
import com.gyh.fileindex.api.AsynchronizedTask
import com.gyh.fileindex.api.Monitor
import com.gyh.fileindex.api.SortAdapter
import com.gyh.fileindex.api.TabInfoData
import com.gyh.fileindex.bean.FileInfo
import com.gyh.fileindex.util.ThreadManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.io.File

class FileListActivity : BaseActivity<FileInfo>(), Monitor {
    override var data = ArrayList<FileInfo>()
    override lateinit var sortAdapter :SortAdapter<FileInfo>
    private var converting: AsynchronizedTask<FileInfo>? = null

    override fun getLayoutId(viewType: Int): Int {
        return R.layout.file_item
    }

    override fun convert(holder: QuickAdapter<FileInfo>.VH, data: FileInfo, position: Int) {
        holder.getView<TextView>(R.id.fileName)?.text = data.name
        holder.getView<ImageView>(R.id.img)?.setImageDrawable(data.icon ?: tabInfo.icon)
        holder.getView<TextView>(R.id.time)?.text = data.date
        holder.getView<TextView>(R.id.size)?.text = data.size
    }

    override fun updateProgress(vararg files: File) {
        for (file in files) {
            val fileInfo = FileInfo(file)
            data.add(fileInfo)
            sortAdapter.sort(data)
            val index = data.indexOf(fileInfo)
            quickAdapter.notifyItemInserted(index)
        }
    }

    override fun updateResult(result: String) {
        fab.clearAnimation()
        recyclerView.scrollToPosition(0)
    }

    override fun sort() = sortAdapter.sort(data)

    override fun isCare(file: File) = tabInfo.exitSuffix(file.name)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TabInfoData.addListener(this)
        sortAdapter = SortAdapter(this, tabInfo.text)
        converting = AsynchronizedTask({
            data.add(it[0])
            sortAdapter.sort(data)
            val index = data.indexOf(it[0])
            quickAdapter.notifyItemInserted(index)
        }, ::updateResult, {
            ArrayList(tabInfo.fileInfos).forEach {
                updateProgress(FileInfo(it))
            }
        })
        converting?.executeOnExecutor(ThreadManager.getInstance().executorService)
    }

    override fun onDestroy() {
        super.onDestroy()
        TabInfoData.removeListener(this)
        converting?.cancel(true)
    }

}