package com.example.mposapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DownloadManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.example.projectdesign.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        Button log_in_but=findViewById(R.id.loginbutton);
        EditText signUp=findViewById(R.id.signUp);
        EditText username=findViewById(R.id.username);
        EditText password=findViewById(R.id.password);
        final int[] userId = new int[1];
        final String[] token = new String[1];
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signUpIntent=new Intent(MainActivity.this,SignUpActivity.class);
                startActivity(signUpIntent);
                finish();
            }
        });
        log_in_but.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!username.getText().toString().isEmpty() && !password.getText().toString().isEmpty()) {
                    try {
                        Service.getInstance()
                                .authenticate(new User(username.getText().toString(), password.getText().toString()))
                                .enqueue(new Callback<UserResponse>() {
                                    @Override
                                    public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                                        if (response.isSuccessful()) {
                                            Log.d("USER_RESPONSE", String.valueOf(response.body().token));
                                            //Log.d("USER_RESPONSE", String.valueOf(response.body().userId));
                                            userId[0] =response.body().userId;
                                            token[0] =response.body().token;
                                            Toast.makeText(getBaseContext(),"Username and password are correct", Toast.LENGTH_LONG).show();
                                            Intent readWriteIntent=new Intent(MainActivity.this,PaymentActivity.class);
                                            readWriteIntent.putExtra("user_id",userId[0]);
                                            readWriteIntent.putExtra("token",token[0]);
                                            readWriteIntent.putExtra("password",password.getText().toString());
                                            startActivity(readWriteIntent);
                                            finish();
                                        } else {
                                            Toast.makeText(getBaseContext(), "Wrong username or password", Toast.LENGTH_LONG).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<UserResponse> call, Throwable t) {
                                        Toast.makeText(getBaseContext(), "Service error!!!", Toast.LENGTH_LONG).show();
                                    }
                                });
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getBaseContext(), "error!!!", Toast.LENGTH_LONG).show();
                    }

                } else {
                    Toast.makeText(getBaseContext(), "Fill the necessary fields.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }



}
