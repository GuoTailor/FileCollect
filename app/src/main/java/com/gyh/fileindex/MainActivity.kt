package com.gyh.fileindex

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.*
import com.afollestad.materialdialogs.MaterialDialog
import com.gyh.fileindex.appbar.AppBar
import com.gyh.fileindex.util.Util
import com.yanzhenjie.recyclerview.*

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.io.File
import java.util.*
import kotlin.Comparator
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    lateinit var mData: MutableList<ApkInfo>
    private lateinit var context: Context
    private lateinit var quickAdapter: QuickAdapter<ApkInfo>
    private lateinit var apkScan: ApkScan
    private lateinit var appbar: AppBar
    private var search: Boolean = false
    private lateinit var btnAnim: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        this.context = this
        fab.setOnClickListener { view ->
            if (search) return@setOnClickListener
            val previousSize = mData.size
            if (apkScan.status == AsyncTask.Status.RUNNING) {
                Snackbar.make(view, "正在扫描", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
                return@setOnClickListener
            }
            mData.clear()
            quickAdapter.notifyItemRangeRemoved(0, previousSize)
            scan()
        }

        btnAnim = AnimationUtils.loadAnimation(this, R.anim.rotate)
        permissionCheck()
        appbar = AppBar(this) { queue ->
            if (queue.isNotEmpty()) {
                val queueList = mData.filter { it.appName.contains(queue) }
                quickAdapter.setmDatas(queueList)
                quickAdapter.notifyItemRangeRemoved(0, mData.size)
                search = true
            }
        }
        fab_bg.setBackgroundResource(R.drawable.fab_shadow_black)


        fab_bg.setOnClickListener { view -> if (appbar.searchView.isEnabled) appbar.searchView.hideSearchView() }

        setSupportActionBar(appbar.toolbar)
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
                holder.getView<TextView>(R.id.currentVersion)?.text = data.currentVersion
                holder.getView<TextView>(R.id.size)?.text = data.size
            }
        }
        initAdapter()
        initRecyclerView()
    }

    private fun initRecyclerView() {
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
        recyclerView.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && fab.visibility == VISIBLE) {
                    fab.hide()
                } else if (dy < 0 && fab.visibility != VISIBLE) {
                    fab.show()
                }
            }
        })
    }

    private fun initAdapter() {
        val swipeMenuCreator =
            SwipeMenuCreator { swipeLeftMenu, swipeRightMenu, position ->
                val width = resources.getDimensionPixelSize(R.dimen.dp_70)
                // 1. MATCH_PARENT 自适应高度，保持和Item一样高;
                // 2. 指定具体的高，比如80;
                // 3. WRAP_CONTENT，自身高度，不推荐;
                val height = ViewGroup.LayoutParams.MATCH_PARENT
                // 添加右侧的，如果不添加，则右侧不会出现菜单。
                val deleteItem =
                    SwipeMenuItem(context).setBackground(R.drawable.selector_green)
                        .setText("安装")
                        .setTextColor(Color.WHITE)
                        .setWidth(width)
                        .setHeight(height)
                swipeRightMenu.addMenuItem(deleteItem)// 添加菜单到右侧。

                val addItem =
                    SwipeMenuItem(context).setBackground(R.drawable.selector_red)
                        .setText("删除")
                        .setTextColor(Color.WHITE)
                        .setWidth(width)
                        .setHeight(height)
                swipeRightMenu.addMenuItem(addItem) // 添加菜单到右侧。
            }
        // 菜单点击监听。

        val mMenuItemClickListener =
            OnItemMenuClickListener { menuBridge, position ->
                menuBridge.closeMenu()

                val direction = menuBridge.direction // 左侧还是右侧菜单。
                val menuPosition = menuBridge.position // 菜单在RecyclerView的Item中的Position。

                if (direction == SwipeRecyclerView.RIGHT_DIRECTION) {
                    when (menuPosition) {
                        0 -> {
                            Util.install(File(mData[position].path), this)
                        }
                        1 -> {
                            val apkInfo = mData[position]
                            File(apkInfo.path).delete()
                            Log.d("DoLog", "删除：" + apkInfo.path)
                            mData.remove(apkInfo)
                            quickAdapter.notifyItemRemoved(position)
                        }
                    }
                }
            }
        recyclerView.setSwipeMenuCreator(swipeMenuCreator)
        recyclerView.setOnItemMenuClickListener(mMenuItemClickListener)
        recyclerView.setOnItemLongClickListener { view, position ->
            val data = mData[position]
            Util.showPropertiesDialog(data, this)
        }
    }

    /**
     * 扫描apk文件
     */
    private fun scan() {
        apkScan = ApkScan({
            mData.add(it)
            mData.sortWith(Comparator { o1, o2 ->
                val i = o1.appName.compareTo(o2.appName)
                if (i == 0) {
                    o1.version.compareTo(o2.version)
                } else {
                    i
                }
            })
            quickAdapter.notifyItemInserted(mData.indexOf(it))
        }, {
            Toast.makeText(this, it + mData.size, Toast.LENGTH_SHORT).show()
            fab.clearAnimation()
        }, this)
        apkScan.execute(".apk")
        fab.startAnimation(btnAnim)
    }

    override fun onBackPressed() {
        if (search) {
            quickAdapter.setmDatas(mData)
            quickAdapter.notifyItemRangeRemoved(0, mData.size)
            search = false
        } else {
            finish()
            exitProcess(0)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.activity_extra, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val s = menu.findItem(R.id.view)
        appbar.setTitle(R.string.app_name)
        s.title = resources.getString(R.string.gridview)
        //s.title = resources.getString(R.string.listview)
        menu.findItem(R.id.search).isVisible = true
        menu.findItem(R.id.sort).isVisible = true
        menu.findItem(R.id.view).isVisible = true

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.search -> {
                appbar.searchView.revealSearchView()
                true
            }
            R.id.exit -> {
                finish()
                true
            }
            R.id.sort -> {
                val sort = resources.getStringArray(R.array.sortby)
                val current = 1
                val a = MaterialDialog.Builder(this)
                a.items(*sort).itemsCallbackSingleChoice(
                    if (current > 3) current - 4 else current
                ) { dialog, view, which, text -> true }
                a.negativeText(R.string.ascending)
                a.positiveText(R.string.descending)
                a.onNegative { dialog, which ->
                    Toast.makeText(this, "升序" + dialog.selectedIndex, Toast.LENGTH_SHORT).show()
                }
                a.onPositive { dialog, which ->
                    Toast.makeText(this, "降序" + dialog.selectedIndex, Toast.LENGTH_SHORT).show()
                }
                a.title(R.string.sortby)
                a.build().show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Shows a view that goes from white at it's lowest part to transparent a the top.
     * It covers the fragment.
     */
    fun showSmokeScreen() {
        Util.revealShow(fab_bg, true)
    }

    fun hideSmokeScreen() {
        Util.revealShow(fab_bg, false)
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
        if(permissionCheck != PackageManager.PERMISSION_GRANTED) {
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
