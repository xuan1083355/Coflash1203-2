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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ClassifyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<String> Strings;
    private Context mContext;
    private int resource1;
    private int resource2;
    String[] str=new String[10];
    String myPos_classify = "";

    private OnItemClickListener mOnItemClickListener=null ;

    public ClassifyAdapter(Context context, List<String> Strings,int resource1,int resource2) {
        mContext=context;
        this.resource1 = resource1;
        this.resource2 = resource2;
        this.Strings = Strings;
    }
    public ClassifyAdapter(Context context, List<String> Strings,int resource1,int resource2, String pos_classify) {
        mContext=context;
        this.resource1 = resource1;
        this.resource2 = resource2;
        this.Strings = Strings;
        this.myPos_classify = pos_classify;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        RecyclerView.ViewHolder viewHolder;
        switch(viewType){
            case 1:
                itemView=LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.classify_cardview,parent,false);
                viewHolder = new OneViewHolder(itemView);
                return viewHolder;
            case 2:
                itemView=LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.tagsgroup_cardview,parent,false);
                viewHolder = new TwoViewHolder(itemView);
                return viewHolder;
            default:
                return null;
        }
    }

    @Override
    public int getItemViewType(int position){
        if(position<=6){
            return 1;
        }else{
            return 2;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if(holder instanceof OneViewHolder) {
            switch (position) {
                case 0:
                    if(myPos_classify.equals("食")){
                        ((OneViewHolder) holder).classify.setImageResource(R.drawable.food_classify_color);
                    }
                    else{
                        ((OneViewHolder) holder).classify.setImageResource(R.drawable.food_classify);
                    }
                    break;
                case 1:
                    if(myPos_classify.equals("衣")){
                        ((OneViewHolder) holder).classify.setImageResource(R.drawable.cloth_classify_color);
                    }
                    else{
                        ((OneViewHolder) holder).classify.setImageResource(R.drawable.cloth_classify);
                    }
                    break;
                case 2:
                    if(myPos_classify.equals("住")){
                        ((OneViewHolder) holder).classify.setImageResource(R.drawable.place_classify_color);
                    }
                    else{
                        ((OneViewHolder) holder).classify.setImageResource(R.drawable.place_classify);
                    }
                    break;
                case 3:
                    if(myPos_classify.equals("行")){
                        ((OneViewHolder) holder).classify.setImageResource(R.drawable.move_classify_color);
                    }
                    else{
                        ((OneViewHolder) holder).classify.setImageResource(R.drawable.move_classify);
                    }
                    break;
                case 4:
                    if(myPos_classify.equals("育")){
                        ((OneViewHolder) holder).classify.setImageResource(R.drawable.education_classify_color);
                    }
                    else{
                        ((OneViewHolder) holder).classify.setImageResource(R.drawable.education_classify);
                    }
                    break;
                case 5:
                    if(myPos_classify.equals("樂")){
                        ((OneViewHolder) holder).classify.setImageResource(R.drawable.entertainment_classify_color);
                    }
                    else{
                        ((OneViewHolder) holder).classify.setImageResource(R.drawable.entertainment_classify);
                    }
                    break;
                case 6:
                    if(myPos_classify.equals("其它")){
                        ((OneViewHolder) holder).classify.setImageResource(R.drawable.other_classify_color);
                    }
                    else{
                        ((OneViewHolder) holder).classify.setImageResource(R.drawable.other_classify);
                    }
                    break;
            }
            if (mOnItemClickListener != null) {
                ((OneViewHolder) holder).classify.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mOnItemClickListener.onClick(((OneViewHolder) holder).classify, position);
                    }
                });
            }
        }else{
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

    }

    @Override
    public int getItemCount() {
        return Strings.size();
    }

    public class OneViewHolder extends RecyclerView.ViewHolder{
        public ImageView classify;
        public OneViewHolder(@NonNull View itemView) {
            super(itemView);
            classify=itemView.findViewById(R.id.iv_recyclerview_imag);
        }
    }
    public class TwoViewHolder extends RecyclerView.ViewHolder{
        public Button tagsGroup;
        public TwoViewHolder(@NonNull View itemView) {
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
    /*interface OnChildClick{
        void onChildClick(PlusString plusString,int parentPosition);
    }*/
}


