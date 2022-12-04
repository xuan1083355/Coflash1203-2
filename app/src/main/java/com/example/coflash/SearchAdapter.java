package com.example.coflash;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import android.content.Intent;
import android.widget.Button;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<String> Strings;
    private Context mContext;
    private int resource;
    String[] str=new String[10];

    private OnItemClickListener mOnItemClickListener=null ;

    public SearchAdapter(Context context, List<String> Strings,int resource) {
        mContext=context;
        this.resource = resource;
        this.Strings = Strings;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        RecyclerView.ViewHolder viewHolder;
        itemView=LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_cardview,parent,false);
        viewHolder = new TwoViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        ((TwoViewHolder) holder).tagsGroup.setText("#"+Strings.get(position));

        if (mOnItemClickListener != null) {
            ((TwoViewHolder) holder).tagsGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickListener.onClick(((TwoViewHolder) holder).tagsGroup, position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return Strings.size();
    }

    public class TwoViewHolder extends RecyclerView.ViewHolder{
        public Button tagsGroup;
        public TwoViewHolder(@NonNull View itemView) {
            super(itemView);
            tagsGroup=itemView.findViewById(R.id.tagsGroup);
            //tagsGroup.setBackgroundColor(Color.GREEN);
            //tagsGroup.getBackground().setColorFilter(0xFF00FF00, android.graphics.PorterDuff.Mode.MULTIPLY );
            //tagsGroup.setBackgroundColor(Color.parseColor("#FF0000"));
        }
    }
    // 設置點擊事件
    public void setOnItemClickListener(OnItemClickListener l) {
        this.mOnItemClickListener =  l;
    }
    // 點擊事件接口
    public interface OnItemClickListener {
        void onClick(View view,int position);
    }
    /*interface OnChildClick{
        void onChildClick(PlusString plusString,int parentPosition);
    }*/
}



