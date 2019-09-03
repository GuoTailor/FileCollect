package com.gyh.fileindex;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class QuickAdapter<T> extends RecyclerView.Adapter<QuickAdapter.VH>{
    private List<T> mDatas;
    private OnItemClickListener<T> onItemClickListener;

    public QuickAdapter(List<T> datas){
        this.mDatas = datas;
    }

    public abstract int getLayoutId(int viewType);

    @NotNull
    @Override
    public VH onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(parent.getContext()).inflate( getLayoutId(viewType), parent, false);
        return new VH(convertView);
    }

    /**
     * 设置RecyclerView某个的监听
     * @param onItemClickListener
     */
    public void setOnItemClickListener(OnItemClickListener<T> onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }


    @Override
    public void onBindViewHolder(@NotNull QuickAdapter.VH holder, int position) {
        convert(holder, mDatas.get(position), position);
    }



    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public abstract void convert(VH holder, T data, int position);


    public class VH extends RecyclerView.ViewHolder{
        private SparseArray<View> mViews;
        private View mConvertView;


        public VH(View v){
            super(v);
            mConvertView = v;
            mViews = new SparseArray<>();
            v.setOnClickListener(v1 -> {
                //Toast.makeText(mContext, "当前点击 "+ datas.get(getLayoutPosition()), Toast.LENGTH_SHORT).show();
                if(onItemClickListener != null){
                    onItemClickListener.onItemClick(v1, mDatas.get(getLayoutPosition()), getLayoutPosition());
                }
            });
        }

        public <V extends View> V getView(int id){
            View v = mViews.get(id);
            if(v == null){
                v = mConvertView.findViewById(id);
                mViews.put(id, v);
            }
            return (V)v;
        }


        public void setText(int id, String value){
            TextView view = getView(id);
            view.setText(value);
        }
    }

    //点击 RecyclerView 某条的监听
    public interface OnItemClickListener<K>{

        /**
         * 当RecyclerView某个被点击的时候回调
         * @param view 点击item的视图
         * @param data 点击得到的数据
         */
        void onItemClick(View view, K data, int index);

    }

}