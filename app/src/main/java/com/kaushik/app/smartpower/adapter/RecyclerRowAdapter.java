package com.kaushik.app.smartpower.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kaushik.app.smartpower.R;
import com.kaushik.app.smartpower.SwitchActivity;

import java.util.ArrayList;

/**
 * Created by anant on 15/01/17.
 */

public class RecyclerRowAdapter extends RecyclerView.Adapter<RecyclerRowAdapter.ItemRowHolder> {
    private ArrayList<String> items = new ArrayList<>();
    private Context context;

    public RecyclerRowAdapter(Context con,ArrayList<String> items){
        context = con;
        this.items = items;
    }

    @Override
    public RecyclerRowAdapter.ItemRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.blecard, null);
        ItemRowHolder itemRowHolder = new ItemRowHolder(v);


        return itemRowHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerRowAdapter.ItemRowHolder holder, int position) {
        holder.textView.setText(items.get(position).trim());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ItemRowHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView textView;
        private CardView cardView;

        public ItemRowHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.textView);
            cardView = (CardView) itemView.findViewById(R.id.ble_card);

            cardView.setOnClickListener(this);
        }

        public void onClick(View v) {

        Intent intent = new Intent(context,SwitchActivity.class);
        context.startActivity(intent);


        }
    }
}
