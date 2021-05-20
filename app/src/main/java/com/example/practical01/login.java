package com.example.practical01;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class login extends AppCompatActivity {
    EditText loginEmail,loginPassword;
    Button loginBtn;
    TextView signUpBtn;
    FirebaseAuth fauth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signUpBtn  = findViewById(R.id.Signup);
        loginEmail = findViewById(R.id.userEmail);
        loginPassword   = findViewById(R.id.loginpwd);
        loginBtn   = findViewById(R.id.loginBtn);

        fauth = FirebaseAuth.getInstance();


        //Signup TextView
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(login.this,MainActivity.class);
                startActivity(intent);
            }
        });

        //login Button
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = loginEmail.getText().toString().trim();
                String password = loginPassword.getText().toString().trim();


                if(TextUtils.isEmpty(email)){
                    loginEmail.setError("Email is Required");
                    return;
                }
                else if(TextUtils.isEmpty(password)){
                    loginPassword.setError("Password is Required");
                    return;
                }



                //Autheticate user
                fauth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                      if(task.isSuccessful()){
                          Toast.makeText(login.this, "Logged in Succesfully", Toast.LENGTH_SHORT).show();
                          startActivity(new Intent(getApplicationContext(),user_profile.class));

                      }else {
                          Toast.makeText(login.this, "Error" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                      }

                    }
                });

                //Epmty EditText
                loginEmail.setText(null);
                loginPassword.setText(null);
            }
        });

    }
}