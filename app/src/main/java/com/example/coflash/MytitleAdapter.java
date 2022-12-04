package com.example.coflash;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import android.content.Intent;
import android.widget.Button;
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


public class MytitleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private int[] title_num;
    private Context mContext;
    private int resource1;
    private int resource2;
    private String uid;
    private DatabaseReference myRef;

    private OnItemClickListener mOnItemClickListener=null ;

    public MytitleAdapter(Context context, int[] title_num,int resource1,int resource2) {
        mContext=context;
        this.resource1 = resource1;
        this.resource2 = resource2;
        this.title_num = title_num;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();//取得使用者資料
        if (user != null) {
            uid = user.getUid();//取得uid
        }
        //連線資料庫
        FirebaseDatabase database = FirebaseDatabase.getInstance();//連線數據庫
        myRef = database.getReference("DB");
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        RecyclerView.ViewHolder viewHolder;
        switch(viewType){
            case 1:
                itemView=LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.title_text_cardview,parent,false);
                viewHolder = new OneViewHolder(itemView);
                return viewHolder;
            case 2:
                itemView=LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.title_button_cardview,parent,false);
                viewHolder = new TwoViewHolder(itemView);
                return viewHolder;
            default:
                return null;
        }
    }

    @Override
    public int getItemViewType(int position){
        int index=position;
        if(title_num[index]==2){
            return 2;
        }else{
            return 1;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if(holder instanceof OneViewHolder) {
            myRef.child("user").child(uid).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(Task<DataSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException());//查詢使用者經驗
                    } else {
                        switch (position) {
                            case 0:
                                if((String.valueOf(task.getResult().child("alltitle").child("1").child("初心者").getValue())).equals("true")){
                                    ((OneViewHolder) holder).title_status.setText("已領取");
                                }
                                ((OneViewHolder) holder).title_name.setText("初心者");
                                break;
                            case 1:
                                ((OneViewHolder) holder).title_status.setText("經驗須達50");
                                if((String.valueOf(task.getResult().child("alltitle").child("2").child("勇者").getValue())).equals("true")){
                                    ((OneViewHolder) holder).title_status.setText("已領取");
                                }
                                ((OneViewHolder) holder).title_name.setText("勇者");
                                break;
                            case 2:
                                ((OneViewHolder) holder).title_status.setText("經驗須達150");
                                if((String.valueOf(task.getResult().child("alltitle").child("3").child("見習探險家").getValue())).equals("true")){
                                    ((OneViewHolder) holder).title_status.setText("已領取");
                                }
                                ((OneViewHolder) holder).title_name.setText("見習探險家");
                                break;
                            case 3:
                                ((OneViewHolder) holder).title_status.setText("經驗須達500");
                                if((String.valueOf(task.getResult().child("alltitle").child("4").child("大使").getValue())).equals("true")){
                                    ((OneViewHolder) holder).title_status.setText("已領取");
                                }
                                ((OneViewHolder) holder).title_name.setText("大使");
                                break;
                            case 4:
                                ((OneViewHolder) holder).title_status.setText("經驗須達1000");
                                if((String.valueOf(task.getResult().child("alltitle").child("5").child("冒險王").getValue())).equals("true")){
                                    ((OneViewHolder) holder).title_status.setText("已領取");
                                }
                                ((OneViewHolder) holder).title_name.setText("冒險王");
                                break;
                        }
                    }
                }
            });
        }else{
            if (mOnItemClickListener != null) {
                switch (position) {
                    case 1:
                        ((TwoViewHolder) holder).title_name.setText("勇者");
                        break;
                    case 2:
                        ((TwoViewHolder) holder).title_name.setText("見習探險家");
                        break;
                    case 3:
                        ((TwoViewHolder) holder).title_name.setText("大使");
                        break;
                    case 4:
                        ((TwoViewHolder) holder).title_name.setText("冒險王");
                        break;
                }
                ((TwoViewHolder) holder).title_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        switch (position) {
                            case 1:
                                myRef.child("user").child(uid).child("alltitle").child("2").child("勇者").setValue("true");
                                break;
                            case 2:
                                myRef.child("user").child(uid).child("alltitle").child("3").child("見習探險家").setValue("true");
                                break;
                            case 3:
                                myRef.child("user").child(uid).child("alltitle").child("4").child("大使").setValue("true");
                                break;
                            case 4:
                                myRef.child("user").child(uid).child("alltitle").child("5").child("冒險王").setValue("true");
                                break;
                        }
                        mOnItemClickListener.onClick(((TwoViewHolder) holder).title_button, position);
                    }
                });
            }
            /*
            ((TwoViewHolder) holder).tagsGroup.setText("#"+Strings.get(position));
            if (mOnItemClickListener != null) {
                ((TwoViewHolder) holder).tagsGroup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mOnItemClickListener.onClick(((TwoViewHolder) holder).tagsGroup, position);
                    }
                });
            }*/
        }

    }

    @Override
    public int getItemCount() {
        return title_num.length;
    }

    public class OneViewHolder extends RecyclerView.ViewHolder{
        public TextView title_status,title_name;
        public OneViewHolder(@NonNull View itemView) {
            super(itemView);
            title_status=itemView.findViewById(R.id.title_status);
            title_name=itemView.findViewById(R.id.title_name);
        }
    }
    public class TwoViewHolder extends RecyclerView.ViewHolder{
        public Button title_button;
        public TextView title_name;
        public TwoViewHolder(@NonNull View itemView) {
            super(itemView);
            title_button=itemView.findViewById(R.id.title_button);
            title_name=itemView.findViewById(R.id.title_name);
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


