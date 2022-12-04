package com.example.coflash;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.coflash.ui.main.Upload;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PopupWindowAdapter extends RecyclerView.Adapter<PopupWindowAdapter.ImageViewHolder>{
    private Context mContext;
    private List<String> tags;
    private int count=1;
    private int index=0;
    String[] str=new String[10];

    private OnItemClickListener mOnItemClickListener=null ;

    public PopupWindowAdapter(Context context,List<String> tags){
        mContext=context;
        this.tags=tags;
    }

    @NonNull
    @Override
    public PopupWindowAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView=LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tagsgroup_cardview,parent,false);
        ImageViewHolder viewHolder = new ImageViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PopupWindowAdapter.ImageViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.tagsGroup.setText(str[position]);
        count = tags.size();
        if(count>0) {
            holder.tagsGroup.setText(tags.get(index));
            index++;
        }
        if (mOnItemClickListener != null) {
            holder.tagsGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickListener.onClick(holder.tagsGroup, tags.get(position));//不確定
                    System.out.println("有成功點擊");
                }
            });
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
    @Override
    public int getItemCount() {
        return tags.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder{
        public Button tagsGroup;
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            tagsGroup=itemView.findViewById(R.id.tagsGroup);

        }
    }

}
