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
import java.util.Arrays;

public class tagsGroupAdapter extends RecyclerView.Adapter<tagsGroupAdapter.ImageViewHolder>{
    private Context mContext;
    private List<String> tags;
    private int count=0;
    String[] str=new String[10];
    private OnItemClickListener mOnItemClickListener=null ;

    public tagsGroupAdapter(Context context,List<String> tags){
        mContext=context;
        this.tags=tags;
        int strIndex=0;
        for(int i=0;i<tags.size();i++){
            if(tags.get(i).indexOf("#")>-1) {
                for (String s : tags.get(i).split("#")) {
                    if(!(s.equals("")) && !(s.equals(" ")) && !(Arrays.asList(str).contains(s))) {
                        str[(strIndex + 1) % 10] = s;
                        strIndex++;
                    }
                }
            }else{
                if(!(tags.get(i).equals("")) && !(tags.get(i).equals(" ")) && !(Arrays.asList(str).contains(tags.get(i)))) {
                    str[(strIndex + 1) % 10] = tags.get(i);
                    strIndex++;
                }
            }
        }
    }

    @NonNull
    @Override
    public tagsGroupAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView=LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tagsgroup_cardview,parent,false);
        //return new tagsGroupAdapter.ImageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.tagsgroup_cardview,parent,false));  原本的
        ImageViewHolder viewHolder = new ImageViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull tagsGroupAdapter.ImageViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.tagsGroup.setText(str[position]);
        if(mOnItemClickListener!=null) {
            holder.tagsGroup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnItemClickListener.onClick(holder.tagsGroup,position);
                }
            });
        }
    }


    @Override
    public int getItemCount() {
        return str.length;  //10
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder{
        public Button tagsGroup;
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            tagsGroup=itemView.findViewById(R.id.tagsGroup);

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
    public String[] getStr(){
        return str;
    }


}
