package com.gyh.fileindex;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class QuickAdapter<T> extends RecyclerView.Adapter<QuickAdapter.VH> {
    private List<T> mDatas;
    private OnItemClickListener<T> onItemClickListener;

    public QuickAdapter(List<T> datas) {
        this.mDatas = datas;
    }

    public List<T> getmDatas() {
        return mDatas;
    }

    public void setmDatas(List<T> mDatas) {
        this.mDatas = mDatas;
    }

    public abstract int getLayoutId(int viewType);

    @NotNull
    @Override
    public VH onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(parent.getContext()).inflate(getLayoutId(viewType), parent, false);
        return new VH(convertView);
    }

    /**
     * 设置RecyclerView某个的监听
     */
    public void setOnItemClickListener(OnItemClickListener<T> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }


    @Override
    public void onBindViewHolder(@NotNull QuickAdapter.VH holder, int position) {
        convert(holder, mDatas.get(position), position);
    }

    @Override
    public void onBindViewHolder(@NotNull QuickAdapter.VH holder, final int position, List payloads) {
        if (payloads.isEmpty()) {//payloads为空 即不是调用notifyItemChanged(position,payloads)方法执行的
            //在这里进行初始化item全部控件
            convert(holder, mDatas.get(position), position);
        } else {//payloads不为空 即调用notifyItemChanged(position,payloads)方法后执行的
            //在这里可以获取payloads中的数据  进行局部刷新
            //假设是int类型
            convert(holder, mDatas.get(position), position, payloads);
        }
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public abstract void convert(VH holder, T data, int position);

    public void convert(VH holder, T data, int position, List type) {
    }


    public class VH extends RecyclerView.ViewHolder {
        private SparseArray<View> mViews;
        private View mConvertView;


        VH(View v) {
            super(v);
            mConvertView = v;
            mViews = new SparseArray<>();
            v.setOnClickListener(v1 -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v1, getLayoutPosition());
                }
            });
        }

        public <V extends View> V getView(int id) {
            View v = mViews.get(id);
            if (v == null) {
                v = mConvertView.findViewById(id);
                mViews.put(id, v);
            }
            return (V) v;
        }

    }

    //点击 RecyclerView 某条的监听
    public interface OnItemClickListener<K> {

        /**
         * 当RecyclerView某个被点击的时候回调
         *
         * @param view 点击item的视图
         */
        void onItemClick(View view, int index);

    }

}