package com.gyh.fileindex.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.gyh.fileindex.QuickAdapter
import com.gyh.fileindex.R
import com.gyh.fileindex.SettingsActivity
import com.gyh.fileindex.api.Monitor
import com.gyh.fileindex.api.TabInfoData
import com.gyh.fileindex.bean.TabInfo
import com.gyh.fileindex.databinding.ActivityMainTabBinding
import com.gyh.fileindex.util.Util
import java.io.File


class NewMainTabActivity : AppCompatActivity(), Monitor {
    private val TAG = this.javaClass.simpleName
    private lateinit var quickAdapter: QuickAdapter<TabInfo>
    private lateinit var binding: ActivityMainTabBinding
    val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted: Map<String, Boolean> ->
            isGranted.entries.forEach { (k, v) ->
                Log.d(TAG, "$k: $v")
            }

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Shrine)
        super.onCreate(savedInstanceState)
        binding = ActivityMainTabBinding.inflate(layoutInflater)
        setContentView(binding.root)
        TabInfoData.data = listOf(
            TabInfo(
                arrayOf(".apk"),
                mutableListOf(),
                ContextCompat.getDrawable(this, R.mipmap.apk),
                0,
                TabInfoData.apk
            ),
            TabInfo(
                arrayOf(".zip", ".rar", ".7z"),
                mutableListOf(),
                ContextCompat.getDrawable(this, R.mipmap.zip),
                0,
                TabInfoData.zip
            ),
            TabInfo(
                arrayOf(".xls", ".xlsx"),
                mutableListOf(),
                ContextCompat.getDrawable(this, R.mipmap.excel),
                0,
                TabInfoData.excel
            ),
            TabInfo(
                arrayOf(".doc", ".docx"),
                mutableListOf(),
                ContextCompat.getDrawable(this, R.mipmap.word),
                0,
                TabInfoData.word
            ),
            TabInfo(
                arrayOf(".pdf"),
                mutableListOf(),
                ContextCompat.getDrawable(this, R.mipmap.pdf),
                0,
                TabInfoData.pdf
            ),
            TabInfo(
                arrayOf(".ppt", ".pptx"),
                mutableListOf(),
                ContextCompat.getDrawable(this, R.mipmap.ppt),
                0,
                TabInfoData.ppt
            )
        )
        //Util.permissionCheck(this)
        nmka()
        //Util.startFor("/storage/emulated/0", this)
        initCollapsingToolbar()
        initItemGrid()
        quickAdapter.notifyItemRangeRemoved(0, TabInfoData.data.size)
        Util.showMainDialog(this, binding.mainChart)
        val status = TabInfoData.scan()
        if (status == TabInfoData.Status.RUNNING) Toast.makeText(
            this,
            "正在扫描",
            Toast.LENGTH_SHORT
        ).show()
        TabInfoData.addListener(this)
    }

    fun nmka() {
        for (permission in arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE)) {
            when {
                ContextCompat.checkSelfPermission(this, permission)
                        == PackageManager.PERMISSION_GRANTED -> {
                    // You can use the API that requires the permission.
                    Log.d(TAG, "nmka: 已有权限 $permission")
                }
                shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                    Toast.makeText(this, "请授予权限", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    // You can directly ask for the permission.
                    // The registered ActivityResultCallback gets the result of this request.
                    Log.d(TAG, "nmka: 申请权限 $permission")
                    requestPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    )
                }
            }
        }
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

        val windowWidth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val metrics = windowManager.currentWindowMetrics
            // Gets all excluding insets
            val windowInsets = metrics.windowInsets
            val insets = windowInsets.getInsetsIgnoringVisibility(
                WindowInsets.Type.navigationBars()
                        or WindowInsets.Type.displayCutout()
            )
            insets.right + insets.left
        } else {
            val windowSize = Point()
            windowManager.defaultDisplay.getSize(windowSize)
            windowSize.x
        }
        collapsingToolbarImage.x = collapsingToolbarImage.x - windowWidth / 4
        collapsingToolbarLayout.scrimVisibleHeightTrigger =
            resources.getDimension(R.dimen.shrine_tall_toolbar_height).toInt() / 2
    }

    private fun initItemGrid() {
        binding.ProductGrid.setHasFixedSize(true)
        binding.ProductGrid.layoutManager = GridLayoutManager(this, 2)
        quickAdapter = object : QuickAdapter<TabInfo>(TabInfoData.data) {

            override fun getLayoutId(viewType: Int): Int {
                return R.layout.home_item_view
            }

            override fun convert(holder: VH, data: TabInfo, position: Int) {
                holder.getView<ImageView>(R.id.icon)?.setImageDrawable(data.icon)
                holder.getView<TextView>(R.id.text)?.text = data.text
                holder.getView<TextView>(R.id.count)?.text = "(${data.count})"
            }

            override fun convert(holder: VH, data: TabInfo, position: Int, payloads: List<*>) {
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
        binding.ProductGrid.adapter = quickAdapter
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null) {
            return
        }
        val uri: Uri? = data.data
        if (requestCode == 1 && uri != null) {
            contentResolver.takePersistableUriPermission(
                uri, data.flags and (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            );//关键是这里，这个就是保存这个目录的访问权限
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

    override fun updateResult(result: String) =
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show()

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
