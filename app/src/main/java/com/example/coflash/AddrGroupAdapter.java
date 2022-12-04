package com.example.coflash;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AddrGroupAdapter extends RecyclerView.Adapter<AddrGroupAdapter.ImageViewHolder>{
    private Context mContext;
    private List<AddressString> tags;
    private AddressString mAddr;
    private String str;
    private AddrGroupAdapter.OnItemClickListener mOnItemClickListener=null ;

    public AddrGroupAdapter(Context context,List<AddressString> tags){
        mContext=context;
        this.tags=tags;

    }

    @NonNull
    @Override
    public AddrGroupAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tag_cardview,parent,false);
        //return new tagsGroupAdapter.ImageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.tagsgroup_cardview,parent,false));  原本的
        AddrGroupAdapter.ImageViewHolder viewHolder = new AddrGroupAdapter.ImageViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, @SuppressLint("RecyclerView")int position) {
        mAddr=tags.get(position);
        holder.tag.setText(mAddr.getTitle());
        if(mOnItemClickListener!=null) {
            holder.tag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickListener.onClick(holder.tag,position);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return tags.size();  //10
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder{
        public Button tag;
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            tag=itemView.findViewById(R.id.tag);

        }
    }

    // 設置點擊事件
    public void setOnItemClickListener(AddrGroupAdapter.OnItemClickListener l) {
        this.mOnItemClickListener =  l;
    }
    // 點擊事件接口
    public interface OnItemClickListener {
        void onClick(View view,int position);
    }

}
