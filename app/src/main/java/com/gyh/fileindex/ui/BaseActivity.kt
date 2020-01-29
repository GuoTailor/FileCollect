package com.gyh.fileindex.ui

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.google.android.material.snackbar.Snackbar
import com.gyh.fileindex.QuickAdapter
import com.gyh.fileindex.R
import com.gyh.fileindex.appbar.AppBar
import com.gyh.fileindex.appbar.SmokeScreen
import com.gyh.fileindex.util.Util
import com.gyh.fileindex.api.TabInfoData
import com.gyh.fileindex.bean.FileInfo
import com.gyh.fileindex.bean.TabInfo
import com.yanzhenjie.recyclerview.OnItemMenuClickListener
import com.yanzhenjie.recyclerview.SwipeMenuCreator
import com.yanzhenjie.recyclerview.SwipeMenuItem
import com.yanzhenjie.recyclerview.SwipeRecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.io.File

abstract class BaseActivity<T : FileInfo> : AppCompatActivity(), SmokeScreen {
    lateinit var tabInfo: TabInfo
    abstract var data: ArrayList<T>
    open lateinit var context: Context
    open lateinit var quickAdapter: QuickAdapter<T>
    open lateinit var appbar: AppBar
    open var search: Boolean = false
    open lateinit var btnAnim: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fab.setOnClickListener { view ->
            if (search) return@setOnClickListener
            val previousSize = data.size
            if (TabInfoData.scan() == TabInfoData.Status.RUNNING) {
                Snackbar.make(view, "正在扫描", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
                return@setOnClickListener
            }
            data.clear()
            fab.startAnimation(btnAnim)
            quickAdapter.notifyItemRangeRemoved(0, previousSize)
        }
        context = this
        btnAnim = AnimationUtils.loadAnimation(this, R.anim.rotate)
        appbar = AppBar(this, this) { queue ->
            if (queue.isNotEmpty()) {
                val queueList = data.filter { it.name.contains(queue) }
                quickAdapter.setmDatas(queueList)
                quickAdapter.notifyItemRangeRemoved(0, data.size)
                search = true
            }
        }
        fab_bg.setBackgroundResource(R.drawable.fab_shadow_black)
        fab_bg.setOnClickListener { view -> if (appbar.searchView.isEnabled) appbar.searchView.hideSearchView() }
        appbar.toolbar.setElevation(0.0f)
        setSupportActionBar(appbar.toolbar)
        val tag = intent.getStringExtra(TabInfoData.tag)
        tabInfo = TabInfoData.getTabInfo(tag)
        quickAdapter = object : QuickAdapter<T>(data) {

            override fun getLayoutId(viewType: Int): Int {
                return this@BaseActivity.getLayoutId(viewType)
            }

            override fun convert(holder: VH, data: T, position: Int) {
                this@BaseActivity.convert(holder, data, position)
            }

            override fun convert(holder: VH, data: T, position: Int, payloads: List<*>) {
                this@BaseActivity.convert(holder, data, position, payloads)
            }
        }
        initAdapter()
        initRecyclerView()
    }

    /**
     * 注册图形Layout
     */
    abstract fun getLayoutId(viewType: Int): Int

    /**
     * 注册图形组件
     */
    abstract fun convert(holder: QuickAdapter<T>.VH, data: T, position: Int)

    open fun convert(holder: QuickAdapter<T>.VH, data: T, position: Int, payloads: List<*>) {}


    /**
     * Shows a view that goes from white at it's lowest part to transparent a the top.
     * It covers the fragment.
     */
    override fun showSmokeScreen() {
        Util.revealShow(fab_bg, true)
    }

    override fun hideSmokeScreen() {
        Util.revealShow(fab_bg, false)
    }

    open fun initAdapter() {
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
                        .setText("打开")
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
                            Util.openFile(File(data[position].path), this)
                        }
                        1 -> {
                            val fileInfo = data[position]
                            File(fileInfo.path).delete()
                            Log.d("DoLog", "删除：" + fileInfo.path)
                            data.remove(fileInfo)
                            quickAdapter.notifyItemRemoved(position)
                        }
                    }
                }
            }
        recyclerView.setSwipeMenuCreator(swipeMenuCreator)
        recyclerView.setOnItemMenuClickListener(mMenuItemClickListener)
        recyclerView.setOnItemLongClickListener { view, position ->
            val mdata = data[position]
            showPropertiesDialog(mdata, this)
        }
    }

    open fun showPropertiesDialog(baseFile: T, activity: Activity) {
        Util.showPropertiesDialog(baseFile, this)
    }

    open fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(this)
        //设置布局管理器
        recyclerView.layoutManager = layoutManager
        //设置为垂直布局，这也是默认的
        layoutManager.orientation = RecyclerView.VERTICAL
        //设置Adapter
        recyclerView.adapter = quickAdapter
        //设置增加或删除条目的动画
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 && fab.visibility == View.VISIBLE) {
                    fab.hide()
                } else if (dy < 0 && fab.visibility != View.VISIBLE) {
                    fab.show()
                }
            }
        })
    }

    override fun onBackPressed() {
        if (search) {
            quickAdapter.setmDatas(data)
            quickAdapter.notifyItemRangeRemoved(0, data.size)
            search = false
        } else {
            finish()
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
            R.id.home -> {
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

}