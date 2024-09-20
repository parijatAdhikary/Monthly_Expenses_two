package com.example.monthlyexpenses;

import static com.example.monthlyexpenses.MainActivity.*;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class RecyclerViewDataAdapter extends RecyclerView.Adapter<RecyclerViewDataAdapter.RecyclerViewViewHolder> {
    private Context context;
    private ArrayList<NodeInfo> items;
    private String tableUniqueName;
    public RecyclerViewDataAdapter(Context context, ArrayList<NodeInfo> items, String tableUniqueName) {
        this.context = context;
        this.items = items;
        this.tableUniqueName = tableUniqueName;

    }

    @NonNull
    @Override
    public RecyclerViewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.node_layout,parent,false);
        return new RecyclerViewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewViewHolder holder, int position) {
        holder.textViewGot.setText("");
        holder.textViewGave.setText("");
        if(items.get(position).getT_type().equals(MainActivity.GAVE_MONEY))
            holder.textViewGave.setText(formatter.format(items.get(position).getT_amount()));
        else
            holder.textViewGot.setText(formatter.format(items.get(position).getT_amount()));
        if(items.get(position).getBalance()<0)
            holder.textViewDescription.setTextColor(context.getResources().getColor(R.color.red_color));
        else
            holder.textViewDescription.setTextColor(context.getResources().getColor(R.color.green_color));
        holder.textViewTitle.setText(items.get(position).getT_title());
        holder.textViewDescription.setText("Available Balance: "+formatter.format(items.get(position).getBalance()));
        holder.textViewDate.setText(getFormattedDate(items.get(position).getT_date()));
    }

    private String getFormattedDate(@NonNull String date) {
        String formattedDate=Integer.parseInt(date.substring(0,2))+"\n";
        switch (Integer.parseInt(date.substring(3,5))){
            case 1:formattedDate=formattedDate+"Jan";
            break;
            case 2:formattedDate=formattedDate+"Feb";
            break;
            case 3:formattedDate=formattedDate+"Mar";
            break;
            case 4:formattedDate=formattedDate+"Apr";
            break;
            case 5:formattedDate=formattedDate+"May";
            break;
            case 6:formattedDate=formattedDate+"Jun";
            break;
            case 7:formattedDate=formattedDate+"Jul";
            break;
            case 8:formattedDate=formattedDate+"Aug";
            break;
            case 9:formattedDate=formattedDate+"Sep";
            break;
            case 10:formattedDate=formattedDate+"Oct";
            break;
            case 11:formattedDate=formattedDate+"Nov";
            break;
            case 12:formattedDate=formattedDate+"Dec";
            break;
        }
        return formattedDate;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class RecyclerViewViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewGave,textViewGot,textViewDescription,textViewTitle,textViewDate;
        public RecyclerViewViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewGave=itemView.findViewById(R.id.textViewGave);
            textViewGot=itemView.findViewById(R.id.textViewGot);
            textViewDescription=itemView.findViewById(R.id.textViewDescription);
            textViewTitle=itemView.findViewById(R.id.textViewTitle);
            textViewDate=itemView.findViewById(R.id.textViewDate);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    int pos=getAdapterPosition();
                    String entryName=items.get(pos).getT_title();

                    if((getLayoutPosition()<items.size()-1)&&(showTableNo==totalTable)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context,R.style.AlertDialogCustom);
                        builder.setTitle("");
                        builder.setMessage("Do you want to DELETE or EDIT?");
                        builder.setPositiveButton("EDIT", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Bundle bundle=new Bundle();
                                bundle.putInt(T_ID, items.get(getLayoutPosition()).getT_id());
                                bundle.putInt(T_AMOUNT, items.get(getLayoutPosition()).getT_amount());
                                bundle.putInt(BALANCE, items.get(getLayoutPosition()).getBalance());

                                bundle.putString(T_TITLE, items.get(getLayoutPosition()).getT_title());
                                bundle.putString(T_DATE, items.get(getLayoutPosition()).getT_date());
                                bundle.putString(T_TYPE, items.get(getLayoutPosition()).getT_type());
                                bundle.putString(TRANSACTION_TYPE, UPDATE_TRANSACTION);
                                bundle.putString(TABLE_NAME, tableUniqueName);

                                Intent intent=new Intent(context, InputActivity.class);
                                intent.putExtra(BUNDLE_KEY,bundle);
                                context.startActivity(intent);
                                dialog.dismiss();
                            }
                        });
                        builder.setNegativeButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                AlertDialog.Builder builder1 = new AlertDialog.Builder(context,R.style.AlertDialogCustomReverse);
                                builder1.setTitle("Delete Confirmation");
                                builder1.setMessage("Are you sure you want to delete\n\""+entryName+"\" - this entry ?");
                                builder1.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        int transactionDiff= -items.get(pos).getT_amount();
                                        if(DB.updateTransaction(tableUniqueName,items.get(pos).getT_id(),"null",0,transactionDiff,2)) {
                                            if(DB.deleteData(tableUniqueName,items.get(pos).getT_id())) {
                                                refreshRecyclerView(true);
                                                Toast.makeText(context, "Data deleted successfully", Toast.LENGTH_SHORT).show();
                                            }
                                            else
                                                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                                        }
                                        else
                                            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                });
                                builder1.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                                AlertDialog alertDialog1 = builder1.create();
                                dialog.dismiss();
                                alertDialog1.show();
                            }
                        });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                    return false;
                }
            });
        }
    }
}
