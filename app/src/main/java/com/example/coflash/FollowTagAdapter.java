package com.example.coflash;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import android.content.Intent;
import android.widget.Toast;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class FollowTagAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<String> Strings;
    private List<Integer> tag_num;
    private Context mContext;
    private String type;
    private int count;
    private int resource1;
    private int resource2;
    private String uid;
    private DatabaseReference myRef2,myRef;
    private int love_count=0;

    private OnItemClickListener mOnItemClickListener=null ;

    public FollowTagAdapter(Context context,String type,List<Integer> tag_num,List<String> Strings,int count,int resource1,int resource2) {
        mContext=context;
        this.type=type;//辨別要顯示貼文數量的介面，HotLabels
        this.tag_num=tag_num;
        this.count=count;
        this.resource1 = resource1;
        this.resource2 = resource2;
        this.Strings = Strings;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();//取得使用者資料
        if (user != null) {
            uid = user.getUid();//取得uid
        }
        //連線資料庫
        FirebaseDatabase database = FirebaseDatabase.getInstance();//連線數據庫
        myRef = database.getReference("DB");
        myRef2 = myRef.child("push");

        myRef.child("user").child(uid).child("tag_love").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    love_count++;
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        RecyclerView.ViewHolder viewHolder;
        switch(viewType){
            case 1:
                itemView=LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.tagtext_cardview,parent,false);
                viewHolder = new OneViewHolder(itemView);
                return viewHolder;
            case 2:
                itemView=LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.show_follow_cardview,parent,false);
                viewHolder = new TwoViewHolder(itemView);
                return viewHolder;
            default:
                return null;
        }
    }

    @Override
    public int getItemViewType(int position){
        if(position!=count){
            return 1;
        }else{
            return 2;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if(holder instanceof OneViewHolder) {
            if(type.equals("Tag")){
                ((OneViewHolder) holder).tag_num.setVisibility(View.INVISIBLE);//隱藏TextView
            }
            else if(type.equals("All")){
                ((OneViewHolder) holder).tag_num.setVisibility(View.INVISIBLE);//隱藏TextView
                ((OneViewHolder) holder).star.setVisibility(View.GONE);
            }
            else {
                ((OneViewHolder) holder).tag_num.setText("被使用"+tag_num.get(position)+"次");
                ((OneViewHolder) holder).star.setVisibility(View.GONE);
                ((OneViewHolder) holder).tv_rank.setVisibility(View.VISIBLE);
                ((OneViewHolder) holder).tv_rank.setText(String.valueOf(position+1));
            }
            ((OneViewHolder) holder).follow.setText(Strings.get(position));
            if (mOnItemClickListener != null) {
                ((OneViewHolder) holder).follow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mOnItemClickListener.onClick(((OneViewHolder) holder).follow, position);
                    }
                });
            }
            //我的最愛
            myRef.child("user").child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(Task<DataSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException());
                    } else {
                        if (task.getResult().child("tag_love").child(Strings.get(position)).getValue() != null) {//代表之前這個uid加入了這個tag到我的最愛
                            Glide.with(mContext).load(R.drawable.star_love_color).into(((OneViewHolder) holder).star);
                        } else {//未加入
                            Glide.with(mContext).load(R.drawable.star_love).into(((OneViewHolder) holder).star);
                        }
                    }
                }
            });
            ((OneViewHolder) holder).star.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    myRef.child("user").child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                        @Override
                        public void onComplete(Task<DataSnapshot> task) {
                            if (!task.isSuccessful()) {
                                Log.e("firebase", "Error getting data", task.getException());
                            } else {
                                if (task.getResult().child("tag_love").child(Strings.get(position)).getValue() != null) {//代表之前這個uid加入了這個tag到我的最愛
                                    Glide.with(mContext).load(R.drawable.star_love).into(((OneViewHolder) holder).star);
                                    myRef.child("user").child(uid).child("tag_love").child(Strings.get(position)).removeValue();//移除紀錄
                                    love_count--;
                                } else {//未收藏
                                    System.out.println("love_count收藏數量"+love_count);
                                    if (love_count < 5) {
                                        Glide.with(mContext).load(R.drawable.star_love_color).into(((OneViewHolder) holder).star);
                                        myRef.child("user").child(uid).child("tag_love").child(Strings.get(position)).setValue("0");//存進資料庫
                                        myRef.child("user").child(uid).child("tag").child(Strings.get(position)).setValue("0");//存進資料庫
                                        love_count++;
                                    }else{
                                        Toast.makeText(mContext, "最多只能收藏五個標籤", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    });
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return Strings.size();
    }

    public class OneViewHolder extends RecyclerView.ViewHolder{
        public TextView follow;
        public ImageView star;
        private TextView tag_num, tv_rank;
        public OneViewHolder(@NonNull View itemView) {
            super(itemView);
            follow=itemView.findViewById(R.id.follow);
            star=itemView.findViewById(R.id.star);
            tag_num=itemView.findViewById(R.id.tag_num);
            tv_rank=itemView.findViewById(R.id.tv_rank);
        }
    }
    public class TwoViewHolder extends RecyclerView.ViewHolder{
        public TextView followtext;
        public TwoViewHolder(@NonNull View itemView) {
            super(itemView);
            followtext=itemView.findViewById(R.id.followtext);
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


