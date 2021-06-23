package com.example.mposapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.example.projectdesign.R;

import java.util.ArrayList;
import java.util.List;

public class TransactionHistory extends AppCompatActivity {
    private Intent i;
    private  List<TransactionInfo> transactionInfoList;
    private TableRow tableRow;
    private TextView amount,ip,state,date;
    private TableLayout tableLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);
        tableLayout=findViewById(R.id.tableLayout);
        i = getIntent();
        transactionInfoList=(List<TransactionInfo>) i.getSerializableExtra("tr");
        for (int j=0;j<transactionInfoList.size();j++){
            tableRow = new TableRow(this);
            tableRow.setLayoutParams(new TableRow.LayoutParams( TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
            amount = new TextView(this);
            amount.setBackgroundColor(0xffffbb33);
            amount.setGravity(Gravity.CENTER);
            amount.setWidth(15);
            amount.setText(String.valueOf(transactionInfoList.get(j).amount));

            ip=new TextView(this);
            ip.setBackgroundColor(0xffffbb33);
            ip.setGravity(Gravity.CENTER);
            ip.setText(transactionInfoList.get(j).ip);

            date=new TextView(this);
            date.setBackgroundColor(0xffffbb33);
            date.setWidth(15);
            date.setGravity(Gravity.CENTER);
            date.setText(transactionInfoList.get(j).date);

            state=new TextView(this);
            state.setBackgroundColor(0xffffbb33);
            state.setWidth(20);
            state.setGravity(Gravity.CENTER);
            state.setText(transactionInfoList.get(j).state);

            if(transactionInfoList.get(j).state.equals("Rejected")){
                amount.setBackgroundColor(0xffff0000);
                ip.setBackgroundColor(0xffff0000);
                date.setBackgroundColor(0xffff0000);
                state.setBackgroundColor(0xffff0000);
            }

            TableRow.LayoutParams cellParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT);
            cellParams.weight = 2;
            cellParams.rightMargin = 10;
            cellParams.topMargin=10;
            amount.setLayoutParams(cellParams);
            ip.setLayoutParams(cellParams);
            date.setLayoutParams(cellParams);
            state.setLayoutParams(cellParams);

            tableRow.addView(amount);
            tableRow.addView(ip);
            tableRow.addView(date);
            tableRow.addView(state);

            tableLayout.addView(tableRow);

        }

    }



}