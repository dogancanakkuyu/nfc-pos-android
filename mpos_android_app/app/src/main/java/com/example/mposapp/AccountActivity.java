package com.example.mposapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.projectdesign.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        EditText changePassword=findViewById(R.id.changepassword);
        String id=getIntent().getStringExtra("id");
        String token=getIntent().getStringExtra("token");
        String password=getIntent().getStringExtra("password");
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(AccountActivity.this);
                LayoutInflater layoutInflaterAndroid = LayoutInflater.from(AccountActivity.this);
                View view2 = layoutInflaterAndroid.inflate(R.layout.changepasswordlayout, null);
                builder.setView(view2);
                //builder.setCancelable(false);
                final AlertDialog alertDialog = builder.create();
                alertDialog.show();

                EditText oldPassword=alertDialog.findViewById(R.id.oldpassword);
                EditText newPassword=alertDialog.findViewById(R.id.newpassword);
                EditText confirmPassword=alertDialog.findViewById(R.id.confirm_new_password);
                Button changePasswordButton=alertDialog.findViewById(R.id.changepassword_button);

                changePasswordButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if(!confirmPassword.getText().toString().equals(newPassword.getText().toString())){
                            Toast.makeText(getBaseContext(),"Two passwords don't match",Toast.LENGTH_LONG).show();
                        }
                        else if(!oldPassword.getText().toString().equals(password)){
                            Toast.makeText(getBaseContext(),"Please enter correct current password",Toast.LENGTH_LONG).show();
                        }
                        else{
                            try{
                                Service.getPassword_instance()
                                        .changePassword(id,new PasswordInfo(oldPassword.getText().toString(),newPassword.getText().toString()),token)
                                        .enqueue(new Callback<Void>() {
                                            @Override
                                            public void onResponse(Call<Void> call, Response<Void> response) {
                                                if(response.isSuccessful()){
                                                    Toast.makeText(getBaseContext(), "Password changed", Toast.LENGTH_LONG).show();
                                                    alertDialog.dismiss();

                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<Void> call, Throwable t) {
                                                Toast.makeText(getBaseContext(), "service error", Toast.LENGTH_LONG).show();


                                            }
                                        });
                            }catch (Exception e){
                                e.printStackTrace();
                                Toast.makeText(getBaseContext(), "error!!!", Toast.LENGTH_LONG).show();
                            }
                        }


                    }
                });

            }
        });
    }
}