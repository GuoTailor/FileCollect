package com.gyh.fileindex.api

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.gyh.fileindex.R
import com.gyh.fileindex.bean.FileInfo
import com.gyh.fileindex.util.ThreadManager

open class SortAdapter<T : FileInfo>(val context: Context, fileKey: String) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(fileKey, Context.MODE_PRIVATE)
    var sort = sharedPreferences.getString("sort", context.getString(R.string.sortName))
    var order = sharedPreferences.getString("order", context.getString(R.string.ascending))

    open fun compare(): Comparator<T> {
        return when (sort) {
            context.getString(R.string.sortName) -> Comparator { o1, o2 ->
                o1.name.trimStart().compareTo(o2.name.trimStart())
            }
            context.getString(R.string.lastModified) -> Comparator { o1, o2 ->
                o1.date.trimStart().compareTo(o2.date.trimStart())
            }
            context.getString(R.string.sortSize) -> Comparator { o1, o2 ->
                o1.intSize.compareTo(o2.intSize)
            }
            context.getString(R.string.path) -> Comparator { o1, o2 ->
                o1.path.trimStart().compareTo(o2.path.trimStart())
            }
            else -> throw IllegalStateException("不支持的排序方法: $sort")
        }
    }

    fun Int.orderBy(): Int {
        return when (order) {
            context.getString(R.string.ascending) -> this
            context.getString(R.string.descending) -> -this
            else -> throw IllegalStateException("不支持的排序方法: $order")
        }
    }

    fun sort(data: ArrayList<T>) {
        data.sortWith { o1, o2 ->
            val result = compare().compare(o1, o2).orderBy()
            if (result == 0) {
                o1.date.compareTo(o2.date)
            } else {
                result
            }
        }
    }

    fun getCurrentSortIndex(): Int {
        return when (sort) {
            context.getString(R.string.sortName) -> 0
            context.getString(R.string.lastModified) -> 1
            context.getString(R.string.sortSize) -> 2
            context.getString(R.string.path) -> 3
            else -> -1
        }
    }

    @SuppressLint("ApplySharedPref")
    fun setOrder(sort: String, order: String) {
        this.sort = sort
        this.order = order
        ThreadManager.getInstance().execute {
            sharedPreferences.edit().putString("sort", sort).putString("order", order).commit()
        }
    }

}