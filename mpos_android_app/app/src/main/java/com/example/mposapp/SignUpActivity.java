package com.example.mposapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.projectdesign.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {
    EditText username,password,confirmPassword;
    Button signUpButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_page);
        username=findViewById(R.id.signUpUsername);
        password=findViewById(R.id.signUpPassword);
        confirmPassword=findViewById(R.id.signUpConfirmPassword);
        signUpButton=findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(confirmPassword.getText().toString().equals(password.getText().toString())){
                    try {
                        Service.getSignUp_instance()
                                .signUpMethod(new User(username.getText().toString(),password.getText().toString()))
                                .enqueue(new Callback<Void>() {
                                    @Override
                                    public void onResponse(Call<Void> call, Response<Void> response) {
                                        if(response.isSuccessful()){
                                            Toast.makeText(getBaseContext(), "Registered", Toast.LENGTH_LONG).show();
                                            final Handler handler=new Handler();
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Intent backToMainActivity=new Intent(SignUpActivity.this,MainActivity.class);
                                                    startActivity(backToMainActivity);
                                                    finish();
                                                }
                                            },1000);

                                        }
                                        else{
                                            Toast.makeText(getBaseContext(), "This username exists.Try another username", Toast.LENGTH_LONG).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<Void> call, Throwable t) {
                                        Toast.makeText(getBaseContext(), "Service Error", Toast.LENGTH_LONG).show();


                                    }
                                });

                    }catch (Exception e){
                        e.printStackTrace();
                        Toast.makeText(getBaseContext(), "error!!!", Toast.LENGTH_LONG).show();
                    }
                }
                else{
                    Toast.makeText(getBaseContext(), "Passwords should match", Toast.LENGTH_LONG).show();
                }

            }
        });
    }
}