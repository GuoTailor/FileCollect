package com.gyh.fileindex.ui

import android.Manifest
import android.annotation.TargetApi
import android.content.Intent
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.gyh.fileindex.QuickAdapter
import com.gyh.fileindex.R
import com.gyh.fileindex.SettingsActivity
import com.gyh.fileindex.api.Monitor
import com.gyh.fileindex.api.TabInfoData
import com.gyh.fileindex.bean.TabInfo
import com.gyh.fileindex.util.ThreadManager
import com.gyh.fileindex.util.Util
import kotlinx.android.synthetic.main.activity_main_tab.*
import java.io.File

class NewMainTabActivity : AppCompatActivity(), Monitor {
    private lateinit var quickAdapter: QuickAdapter<TabInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Shrine)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_tab)
        TabInfoData.data = listOf(
            TabInfo(
                arrayOf(".apk"),
                mutableListOf(),
                getDrawable(R.mipmap.apk),
                0,
                TabInfoData.apk
            ),
            TabInfo(
                arrayOf(".zip", ".rar", ".7z"),
                mutableListOf(),
                getDrawable(R.mipmap.zip),
                0,
                TabInfoData.zip
            ),
            TabInfo(
                arrayOf(".xls", ".xlsx"),
                mutableListOf(),
                getDrawable(R.mipmap.excel),
                0,
                TabInfoData.excel
            ),
            TabInfo(
                arrayOf(".doc", ".docx"),
                mutableListOf(),
                getDrawable(R.mipmap.word),
                0,
                TabInfoData.word
            ),
            TabInfo(
                arrayOf(".pdf"),
                mutableListOf(),
                getDrawable(R.mipmap.pdf),
                0,
                TabInfoData.pdf
            ),
            TabInfo(
                arrayOf(".ppt", ".pptx"),
                mutableListOf(),
                getDrawable(R.mipmap.ppt),
                0,
                TabInfoData.ppt
            )
        )
        Util.permissionCheck(this)
        initCollapsingToolbar()
        initItemGrid()
        quickAdapter.notifyItemRangeRemoved(0, TabInfoData.data.size)
        Util.showMainDialog(this, mainChart)
        val status = TabInfoData.scan()
        if (status == TabInfoData.Status.RUNNING) Toast.makeText(
            this,
            "正在扫描",
            Toast.LENGTH_SHORT
        ).show()
        TabInfoData.addListener(this)
    }

    private fun initCollapsingToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.AppBar)
        val collapsingToolbarImage = findViewById<View>(R.id.CollapsingToolbarImage)
        setSupportActionBar(toolbar)
        val collapsingToolbarLayout =
            findViewById<CollapsingToolbarLayout>(R.id.CollapsingToolbarLayout)
        collapsingToolbarLayout.title = toolbar.title
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.TextAppearance_Shrine_Logo)
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.TextAppearance_Shrine_Logo)
        val windowSize = Point()
        windowManager.defaultDisplay.getSize(windowSize)
        val windowWidth = windowSize.x
        collapsingToolbarImage.x = collapsingToolbarImage.x - windowWidth / 4
        collapsingToolbarLayout.scrimVisibleHeightTrigger =
            resources.getDimension(R.dimen.shrine_tall_toolbar_height).toInt() / 2
    }

    private fun initItemGrid() {
        ProductGrid.setHasFixedSize(true)
        ProductGrid.layoutManager = GridLayoutManager(this, 2)
        quickAdapter = object : QuickAdapter<TabInfo>(TabInfoData.data) {

            override fun getLayoutId(viewType: Int): Int {
                return R.layout.home_item_view
            }

            override fun convert(holder: VH, data: TabInfo, position: Int) {
                holder.getView<ImageView>(R.id.icon)?.setImageDrawable(data.icon)
                holder.getView<TextView>(R.id.text)?.text = data.text
                holder.getView<TextView>(R.id.count)?.text = "(${data.count})"
            }

            override fun convert(
                holder: VH,
                data: TabInfo,
                position: Int,
                payloads: List<*>
            ) {
                for (type in payloads)
                    when (type) {
                        0 -> holder.getView<ImageView>(R.id.icon)?.setImageDrawable(data.icon)
                        1 -> holder.getView<TextView>(R.id.text)?.text = data.text
                        2 -> holder.getView<TextView>(R.id.count)?.text = "(${data.count})"
                    }
            }
        }
        quickAdapter.setOnItemClickListener { _, position ->
            when (position) {
                0 -> {
                    val intent = Intent(this, ApkListActivity::class.java)
                    intent.putExtra(TabInfoData.tag, TabInfoData.data[position].text)
                    startActivity(intent)
                }
                else -> {
                    val intent = Intent(this, FileListActivity::class.java)
                    intent.putExtra(TabInfoData.tag, TabInfoData.data[position].text)
                    startActivity(intent)
                }
            }
        }
        ProductGrid.adapter = quickAdapter
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d("Main", ">$requestCode")
        for ((index, value) in permissions.withIndex()) {
            if (value == Manifest.permission.READ_EXTERNAL_STORAGE && grantResults[index] == 0) {
                val status = TabInfoData.scan()
                if (status == TabInfoData.Status.RUNNING) Toast.makeText(
                    this,
                    "正在扫描",
                    Toast.LENGTH_SHORT
                ).show()
            }
            Log.d("Main", value + " > " + grantResults[index])
        }
    }

    override fun updateProgress(vararg files: File) {
        TabInfoData.data.forEachIndexed { index, it ->
            if (it.exitSuffix(files[0].name)) {
                quickAdapter.notifyItemChanged(index, 2)
            }
        }
    }

    override fun isCare(file: File) = true

    override fun updateResult(result: String) = Toast.makeText(this, result, Toast.LENGTH_SHORT).show()

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.shrine_toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.ShrineToolbarFilterIcon) {
            startActivity(Intent(this, SettingsActivity::class.java))
            return true
        }
        return false
    }

}
