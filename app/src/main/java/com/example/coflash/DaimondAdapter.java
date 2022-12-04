package com.example.coflash;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.Nullable;

public class DaimondAdapter extends BaseAdapter implements View.OnClickListener {
    private LayoutInflater listlayoutInflater;

    private Context context;
    private int[] dAmount;
    private String[] dAddvalue;
    private String[] dPrice;
    private long diamonds;
    private boolean first_addvalue;
    private String uid;
    DatabaseReference myRef;

    public DaimondAdapter (Context context,int[] amount,String[] addvalue,String[] price, long diamonds, boolean first_addvalue, String uid){
        listlayoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.dAmount = amount;
        this.dAddvalue = addvalue;
        this.dPrice = price;
        this.diamonds = diamonds;
        this.first_addvalue = first_addvalue;
        this.uid = uid;
        System.out.println(this.first_addvalue);
    }

    @Override
    public int getCount() {
        return dAmount.length;
    }

    @Override
    public Object getItem(int position) {
        return dAmount[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder = new ViewHolder();
        context = parent.getContext();
        convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.diamond_list,null);
        viewHolder = new ViewHolder();
        viewHolder.diamond_amount = (TextView) convertView.findViewById(R.id.diamond_amount);
        viewHolder.diamond_addvalue = (TextView) convertView.findViewById(R.id.diamond_addvalue);
        viewHolder.btn_price = (Button) convertView.findViewById(R.id.btn_price);
        convertView.setTag(viewHolder);

        viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.diamond_amount.setText(String.valueOf(dAmount[position]));
        if(first_addvalue){
            viewHolder.diamond_addvalue.setText(dAddvalue[position]);
        }
        else{
            viewHolder.diamond_addvalue.setVisibility(View.GONE);
        }
        viewHolder.btn_price.setTag(R.id.btn_price, position);
        viewHolder.btn_price.setText(dPrice[position]);
        viewHolder.btn_price.setOnClickListener(this);
        return convertView;
    }
    @Override
    public void onClick(View view){
//        Toast.makeText(context, String.valueOf(dAmount[(int) view.getTag(R.id.btn_price)]), Toast.LENGTH_SHORT).show();
        try{
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            myRef = database.getReference("DB");
            int b = (int) view.getTag(R.id.btn_price);
            diamonds += dAmount[b];
            myRef.child("user").child(uid).child("diamond").setValue(diamonds);
            if(first_addvalue){
                String str = "花費 " + dPrice[b] + "\n獲得 " + dAmount[b]*2 + "顆鑽石";
                Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
                myRef.child("user").child(uid).child("first_addvalue").setValue(false);
            }
            else {
                String str = "花費 " + dPrice[b] + "\n獲得 " + dAmount[b] + "顆鑽石";
                Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
            }
            ((Activity)context).finish();
        }
        catch (Exception e){

        }
    }

    class ViewHolder{
        TextView diamond_amount, diamond_addvalue;
        Button btn_price;
    }
}
