package com.example.coflash;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.annotation.SuppressLint;

import java.util.ArrayList;
import java.util.List;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.ImageViewHolder> {
    private List<Myinfo.PlusString> mPlusString;
    private OnChildClick childClick;
    private int parentPosition;

    private String str;
    private int count=1;
    private int index=0;
    private List<String> tagslist=new ArrayList<String>();
    private int position_classify;//分類功能用

    private OnItemClickListener mOnItemClickListener=null ;

    public TagAdapter(List<Myinfo.PlusString> plusStrings, int parentPosition, OnChildClick childClick) {
        this.mPlusString = plusStrings;
        this.parentPosition = parentPosition;
        this.childClick = childClick;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ImageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.tag_cardview,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, @SuppressLint("RecyclerView") int position) {
        position_classify=position;
        int pposition=this.parentPosition;
        Myinfo.PlusString pluscurrent=mPlusString.get(pposition);
        if(pluscurrent.getTag()!=null) {    //0829
            str = pluscurrent.getTag().toString();

            for (String s : str.split("#")) {  //標籤字串以"/"切割
                if (!(s.equals("")) && !(s.equals(" ")) && index < 1) {
                    tagslist.add(s);//如何讓taglist數字正確
                }
            }
        }
        count = tagslist.size();
        //0823
        if(count>0) {
            holder.tag.setText(tagslist.get(index));
            index++;
        }
        if (mOnItemClickListener != null) {
            holder.tag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickListener.onClick(holder.tag, tagslist.get(position));
                    System.out.println("有成功點擊");
                    System.out.println("position"+position);
                    System.out.println("position_classify是"+position_classify);
                    System.out.println("tagslist是"+tagslist.get(position));
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return count;
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder{
        public TextView tag;
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            tag=itemView.findViewById(R.id.tag);
        }
    }
    // 設置點擊事件
    public void setOnItemClickListener(OnItemClickListener l) {
        this.mOnItemClickListener =  l;
    }
    // 點擊事件接口
    public interface OnItemClickListener {
        void onClick(View view,String tagtext);
    }
    interface OnChildClick{
        void onChildClick(Myinfo.PlusString plusString, int parentPosition);
    }
}

