package com.example.monthlyexpenses;

import static com.example.monthlyexpenses.MainActivity.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class InputActivity extends AppCompatActivity {
    private TextView textViewSave,textViewDisplayMsg;
    private EditText editTextDescription,editTextAmount;
    private ImageView imageViewBack;
    private String transactionType,tTitle,tDate,tType,tableName;
    private TextWatcher editTextAmountWatcher,editTextDescriptionWatcher;
    private int tID,tAmount,balance;

    @SuppressLint({"SetTextI18n", })
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);
        imageViewBack=findViewById(R.id.imageViewBack);
        editTextDescription=findViewById(R.id.editTextDescription);
        editTextAmount=findViewById(R.id.editTextAmount);
        textViewSave=findViewById(R.id.textViewSave);
        textViewDisplayMsg=findViewById(R.id.textViewDisplayMsg);
        Bundle bundle=Objects.requireNonNull(getIntent().getBundleExtra(BUNDLE_KEY));
        String parentTransactionType= bundle.getString(TRANSACTION_TYPE);
        //String transactionType=getIntent().getStringExtra(TRANSACTION_TYPE);
        if(Objects.requireNonNull(parentTransactionType).equalsIgnoreCase(UPDATE_TRANSACTION)){
            tID=bundle.getInt(T_ID);
            tAmount=bundle.getInt(T_AMOUNT);
            balance=bundle.getInt(BALANCE);
            tTitle=bundle.getString(T_TITLE);
            tDate=bundle.getString(T_DATE);
            tType=bundle.getString(T_TYPE);
            tableName=bundle.getString(TABLE_NAME);
            transactionType=tType;

            editTextDescription.setText(tTitle);
            if(tAmount<0)
                tAmount*=(-1);
            editTextAmount.setText(""+tAmount);

        }
        else
            transactionType=parentTransactionType;

        textViewSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String t_des=editTextDescription.getText().toString().trim();
                String t_amt=editTextAmount.getText().toString().trim();
                int t_amount=getAmount(t_amt);
                if(t_amount>0 && t_des.length()>0){
                    if(transactionType.equals(GAVE_MONEY))
                        t_amount=t_amount*(-1);
                    if(Objects.requireNonNull(parentTransactionType).equalsIgnoreCase(UPDATE_TRANSACTION)){
                        int transactionDiff=Math.abs(t_amount)-Math.abs(tAmount);
                        if(transactionType.equalsIgnoreCase(GAVE_MONEY))
                            transactionDiff*=(-1);
                        int updateField=2;
                        if((!t_des.equalsIgnoreCase(tTitle)) && Math.abs(t_amount)==Math.abs(tAmount))
                            updateField=0;
                        if(t_des.equalsIgnoreCase(tTitle) && Math.abs(t_amount)!=Math.abs(tAmount))
                            updateField=1;

                        if(DB.updateTransaction(tableName,tID,t_des,t_amount,transactionDiff,updateField)) {
                            refreshRecyclerView(true);
                            Toast.makeText(InputActivity.this, "Data updated successfully", Toast.LENGTH_SHORT).show();
                            InputActivity.this.finish();
                        }
                        else
                            Toast.makeText(InputActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        @SuppressLint("SimpleDateFormat") SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
                        Date date = new Date();
                        String current_date= formatter.format(date);
                        if(DB.addTransaction(t_des,current_date,transactionType,t_amount)) {
                            refreshRecyclerView(true);
                            Toast.makeText(InputActivity.this, "Data added successfully", Toast.LENGTH_SHORT).show();
                            InputActivity.this.finish();
                        }
                        else
                            Toast.makeText(InputActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });
        
        if(transactionType.equals(GOT_MONEY)){
            textViewDisplayMsg.setTextColor(getResources().getColor(R.color.green_color));
            ((ConstraintLayout)findViewById(R.id.layoutMSG)).setBackgroundColor(getResources().getColor(R.color.green_color_light));
            ((TextInputLayout)findViewById(R.id.textInputLayoutDes)).setHint("Got From");
            editTextAmount.setTextColor(getResources().getColor(R.color.green_color));
        }
        else {
            textViewDisplayMsg.setTextColor(getResources().getColor(R.color.red_color));
            ((ConstraintLayout)findViewById(R.id.layoutMSG)).setBackgroundColor(getResources().getColor(R.color.red_color_light));
            ((TextInputLayout)findViewById(R.id.textInputLayoutDes)).setHint("Paid To");
            editTextAmount.setTextColor(getResources().getColor(R.color.red_color));
        }

        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputActivity.this.finish();
            }
        });

        String amt=editTextAmount.getText().toString();
        String des=editTextDescription.getText().toString();
        textViewDisplayMsg.setText("You "+(transactionType.equals(GOT_MONEY)?" got ":" paid ")+" ₹"
                +(amt.length()>0?amt:"0")
                +(transactionType.equals(GOT_MONEY)?" from ":" to ")
                +(des.length()>0?des:"_"));
        editTextAmountWatcher=new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String rawData=editTextAmount.getText().toString().trim();
                if(rawData.length()==1){
                    if(rawData.equalsIgnoreCase("+")||rawData.equalsIgnoreCase("-")||rawData.equalsIgnoreCase("0")){
                        editTextAmount.removeTextChangedListener(editTextAmountWatcher);
                        editTextAmount.setText("");
                        editTextAmount.addTextChangedListener(editTextAmountWatcher);
                    }
                }
                else {
                    while (rawData.contains("++")||rawData.contains("+-")||rawData.contains("-+")||rawData.contains("--")||rawData.contains("-0")||rawData.contains("+0")||rawData.startsWith("+")||rawData.startsWith("-")||rawData.startsWith("0")){
                        rawData=rawData.replace("++","+");
                        rawData=rawData.replace("+-","-");
                        rawData=rawData.replace("-+","+");
                        rawData=rawData.replace("--","-");
                        rawData=rawData.replace("-0","-");
                        rawData=rawData.replace("+0","+");
                        if(("+".equalsIgnoreCase(rawData.charAt(0)+""))||("-".equalsIgnoreCase(rawData.charAt(0)+""))||("0".equalsIgnoreCase(rawData.charAt(0)+"")))
                            rawData=rawData.substring(1);
                    }
                    editTextAmount.removeTextChangedListener(editTextAmountWatcher);
                    editTextAmount.setText(rawData);
                    editTextAmount.setSelection(rawData.length());
                    editTextAmount.addTextChangedListener(editTextAmountWatcher);
                }
                String amt=rawData+"";
                String des=editTextDescription.getText().toString().trim();
                int finalAmount=getAmount(amt);
                textViewDisplayMsg.setText("You "+(transactionType.equals(GOT_MONEY)?" got ":" paid ")+" ₹"
                        +(amt.length()>0?formatter.format(finalAmount):"0")
                        +(transactionType.equals(GOT_MONEY)?" from ":" to ")
                        +(des.length()>0?des:"_"));

                if(finalAmount>0&&des.length()>0) {
                    if (transactionType.equals(GOT_MONEY))
                        textViewSave.setBackgroundColor(getResources().getColor(R.color.green_color));
                    else
                        textViewSave.setBackgroundColor(getResources().getColor(R.color.red_color));
                }
                else
                    textViewSave.setBackgroundColor(getResources().getColor(R.color.gray));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        editTextDescriptionWatcher=new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String amt=editTextAmount.getText().toString().trim();
                String des=editTextDescription.getText().toString();
                while (des.contains("\n\n")){
                    des=des.replace("\n\n","\n");
                }
                editTextDescription.removeTextChangedListener(editTextDescriptionWatcher);
                editTextDescription.setText(des);
                editTextDescription.setSelection(des.length());
                editTextDescription.addTextChangedListener(editTextDescriptionWatcher);
                int finalAmount=getAmount(amt);
                textViewDisplayMsg.setText("You "+(transactionType.equals(GOT_MONEY)?" got ":" paid ")+" ₹"
                        +(amt.length()>0?formatter.format(finalAmount):"0")
                        +(transactionType.equals(GOT_MONEY)?" from ":" to ")
                        +(des.length()>0?des:"_"));
                if(finalAmount>0&&des.length()>0) {
                    if (transactionType.equals(GOT_MONEY))
                        textViewSave.setBackgroundColor(getResources().getColor(R.color.green_color));
                    else
                        textViewSave.setBackgroundColor(getResources().getColor(R.color.red_color));
                }
                else
                    textViewSave.setBackgroundColor(getResources().getColor(R.color.gray));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        };

        editTextAmount.addTextChangedListener(editTextAmountWatcher);
        editTextDescription.addTextChangedListener(editTextDescriptionWatcher);
    }


    private static int getAmount(String equation){
        int sum=0;
        int num=0;
        String lastOperator="+";
        for( char ch : equation.toCharArray() ){
            String chString=""+ch;
            if(chString.equalsIgnoreCase("+")||chString.equalsIgnoreCase("-")) {
                if(lastOperator.equalsIgnoreCase("-"))
                    num*=(-1);
                sum+=num;
                lastOperator=chString;
                num=0;
            }
            else{
                num*=10;
                num+=Integer.parseInt(chString);
            }
        }
        if(lastOperator.equalsIgnoreCase("-"))
            num*=(-1);
        sum+=num;
        return sum;
    }
}