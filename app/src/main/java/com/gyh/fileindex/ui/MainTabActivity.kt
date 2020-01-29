package com.gyh.fileindex.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.gyh.fileindex.QuickAdapter
import com.gyh.fileindex.R
import com.gyh.fileindex.api.Monitor
import com.gyh.fileindex.api.TabInfoData
import com.gyh.fileindex.bean.TabInfo
import com.gyh.fileindex.util.Util
import kotlinx.android.synthetic.main.activity_main_tab.*
import java.io.File

class MainTabActivity : AppCompatActivity(), Monitor {

    private lateinit var quickAdapter: QuickAdapter<TabInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
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
        initRecyclerView()
        quickAdapter.notifyItemRangeRemoved(0, TabInfoData.data.size)
        permissionCheck()
        val status = TabInfoData.scan()
        if (status == TabInfoData.Status.RUNNING) Toast.makeText(
            this,
            "正在扫描",
            Toast.LENGTH_SHORT
        ).show()
        TabInfoData.addListener(this)
    }

    private fun initRecyclerView() {
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
        //recyclerTabView.adapter = quickAdapter
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

    override fun updateResult(result: String) =
        Toast.makeText(this, result, Toast.LENGTH_SHORT).show()

    override fun isCare(file: File) = true

    private var mNoPermissionIndex = 0
    private val PERMISSION_REQUEST_CODE = 1
    private val permissionManifest = arrayOf(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    private val noPermissionTip = intArrayOf(
        R.string.no_read_phone_state_permission,
        R.string.no_write_external_storage_permission,
        R.string.no_read_external_storage_permission
    )

    private fun permissionCheck() {
        var permissionCheck = PackageManager.PERMISSION_GRANTED
        var permission: String
        for (i in permissionManifest.indices) {
            permission = permissionManifest[i]
            mNoPermissionIndex = i
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionCheck = PackageManager.PERMISSION_DENIED
            }
        }
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= 23) {
                ActivityCompat.requestPermissions(this, permissionManifest, PERMISSION_REQUEST_CODE)
            } else {
                showNoPermissionTip(getString(noPermissionTip[mNoPermissionIndex]))
                finish()
            }
        }
    }

    private fun showNoPermissionTip(tip: String) {
        Toast.makeText(this, tip, Toast.LENGTH_LONG).show()
    }
}
