 package com.example.mposapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.ui.AppBarConfiguration;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.projectdesign.R;
import com.google.android.material.navigation.NavigationView;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {

    private int userId;
    private NavigationView navigationView;
    private DrawerLayout navDrawer;
    private String token,password,id,paymentAmountStr;
    private Button enterAmount,threeLineBut;
    private EditText paymentAmount;
    private List<TransactionInfo> transactionInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_nav_drawer);
        navDrawer=findViewById(R.id.drawer_layout);
        enterAmount=findViewById(R.id.button2);
        paymentAmount=findViewById(R.id.paymentAmount);
        threeLineBut=findViewById(R.id.hamButton);
        navigationView=findViewById(R.id.nav_view);
        userId=(getIntent().getIntExtra("user_id",0));
        id=String.valueOf(userId);
        token="Bearer "+getIntent().getStringExtra("token");
        transactionInfos = new ArrayList<>();
        password=getIntent().getStringExtra("password");
        navigationView.bringToFront();

        threeLineBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navDrawer.openDrawer(GravityCompat.END);
                navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.nav_account:
                                Intent accountIntent=new Intent(getApplicationContext(),AccountActivity.class);
                                accountIntent.putExtra("id",id);
                                accountIntent.putExtra("token",token);
                                accountIntent.putExtra("password",password);
                                startActivity(accountIntent);
                                navDrawer.closeDrawer(GravityCompat.END);
                                return true;
                            case R.id.nav_history:
                                try {
                                    Service.TransactionInstance()
                                            .getTransaction(id,token)
                                            .enqueue(new Callback<List<TransactionInfo>>() {
                                                @Override
                                                public void onResponse(Call<List<TransactionInfo>> call, Response<List<TransactionInfo>> response) {
                                                    if (response.isSuccessful()){
                                                        transactionInfos.clear();
                                                        //Toast.makeText(getBaseContext(),response.body().get(0).state, Toast.LENGTH_LONG).show();
                                                        for (int i=0;i<response.body().size();i++){
                                                            transactionInfos.add(response.body().get(i));
                                                        }
                                                        Intent transactionHistoryIntent=new Intent(getApplicationContext(),TransactionHistory.class);
                                                        transactionHistoryIntent.putExtra("tr", (Serializable) transactionInfos);
                                                        startActivity(transactionHistoryIntent);

                                                    }

                                                }

                                                @Override
                                                public void onFailure(Call<List<TransactionInfo>> call, Throwable t) {
                                                    Toast.makeText(getBaseContext(), "service error", Toast.LENGTH_LONG).show();

                                                }
                                            });


                                }catch (Exception e){
                                    e.printStackTrace();
                                    Toast.makeText(getBaseContext(), "error!!!", Toast.LENGTH_LONG).show();

                                };
                                navDrawer.closeDrawer(GravityCompat.END);
                                return true;
                            case R.id.nav_log_out:
                                Intent intent=new Intent(PaymentActivity.this,MainActivity.class);
                                startActivity(intent);
                                finish();
                                navDrawer.closeDrawer(GravityCompat.END);
                                return true;

                        }
                        return true;
                    }
                });
            }
        });

        enterAmount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!paymentAmount.getText().toString().isEmpty()){
                    paymentAmountStr=paymentAmount.getText().toString();
                    Intent taptoPayIntent=new Intent(PaymentActivity.this,TapToPay.class);
                    taptoPayIntent.putExtra("token",token);
                    taptoPayIntent.putExtra("id",id);
                    taptoPayIntent.putExtra("paymentAmountStr",paymentAmountStr);
                    //taptoPayIntent.putExtra("tr", (Serializable) transactionInfos);
                    startActivity(taptoPayIntent);


                }
                else{
                    Toast.makeText(getBaseContext(), "Please enter a valid amount", Toast.LENGTH_LONG).show();
                }

            }
        });

    }
    public static String getDatetime() {
        Calendar c = Calendar .getInstance();
        System.out.println("Current time => "+c.getTime());
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy - HH:mm");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }

    public static String getDatetime2() {
        Calendar c = Calendar .getInstance();
        System.out.println("Current time => "+c.getTime());
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedDate = df.format(c.getTime());
        return formattedDate;
    }

    @Override
    public void onBackPressed() {

    }
}