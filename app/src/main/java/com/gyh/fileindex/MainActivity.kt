package com.gyh.fileindex

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.*

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var mData: MutableList<ApkInfo>
    private lateinit var context: Context
    private lateinit var quickAdapter: QuickAdapter<ApkInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        this.context = this
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        if (!permissionCheck()) {
            if (Build.VERSION.SDK_INT >= 23) {
                ActivityCompat.requestPermissions(this, permissionManifest, PERMISSION_REQUEST_CODE)
            } else {
                showNoPermissionTip(getString(noPermissionTip[mNoPermissionIndex]))
                finish()
            }
        }
        mData = LinkedList()
        quickAdapter = object : QuickAdapter<ApkInfo>(mData) {

            override fun getLayoutId(viewType: Int): Int {
                return R.layout.apk_item
            }

            override fun convert(holder: VH, data: ApkInfo, position: Int) {
                holder.getView<TextView>(R.id.appName)?.text = data.appName
                holder.getView<ImageView>(R.id.img)?.setImageDrawable(data.icon)
                holder.getView<TextView>(R.id.time)?.text = data.date
                holder.getView<TextView>(R.id.version)?.text = "版本: " + data.version
                holder.getView<TextView>(R.id.size)?.text = data.size
            }
        }
        quickAdapter.setOnItemClickListener { view, data, index ->
            AlertDialog.Builder(context)
                .setTitle("你确定要删除这个apk吗?")
                .setIcon(data.icon)
                .setMessage(data.path)
                .setPositiveButton("删除") { dialog, _ ->
                    //File(data.path).delete()
                    Log.d("DoLog", data.path + " > " + index)
                    dialog.dismiss()
                    mData.remove(data)
                    quickAdapter.notifyItemRemoved(index)
                }
                .setNegativeButton("取消") { dialog, _ ->
                    Log.d("DoLog", data.path)
                    dialog.dismiss()
                }
                .create()
                .show()
        }
        val layoutManager = LinearLayoutManager(this)
        //设置布局管理器
        recyclerView.layoutManager = layoutManager
        //设置为垂直布局，这也是默认的
        layoutManager.orientation = RecyclerView.VERTICAL
        //设置Adapter
        recyclerView.adapter = quickAdapter
        //设置分隔线
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        //设置增加或删除条目的动画
        recyclerView.itemAnimator = DefaultItemAnimator()
        scan()
    }

    /**
     * 扫描apk文件
     */
    private fun scan() {
        val scan = ApkScan({
            mData.add(it)
            quickAdapter.notifyItemInserted(mData.size - 1)
        }, { Toast.makeText(this, it + mData.size, Toast.LENGTH_SHORT).show() }, this)
        scan.execute(".apk")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d("Main", ">$requestCode")
        for ((index, value) in permissions.withIndex()) {
            if (value == Manifest.permission.READ_EXTERNAL_STORAGE && grantResults[index] == 0) {
                scan()
            }
            Log.d("Main", value + " > " + grantResults[index])
        }
    }

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

    private fun permissionCheck(): Boolean {
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
        return permissionCheck == PackageManager.PERMISSION_GRANTED
    }

    private fun showNoPermissionTip(tip: String) {
        Toast.makeText(this, tip, Toast.LENGTH_LONG).show()
    }

}
