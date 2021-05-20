package com.example.practical01;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "TAG";
    Button SignUpBtn;
    TextView loginBtn;
    EditText fullNameSignup,emailSignup,pwdSignup,addressSignup;
    FirebaseAuth fauth;
    FirebaseFirestore  fstore;
    String userID;


    //Initialize the variable for Date Choose Picker
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private TextView birthdaySignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginBtn = (TextView)findViewById(R.id.Loginme);
        SignUpBtn = (Button) findViewById(R.id.resgisterBtn);
        fullNameSignup = findViewById(R.id.userNameSignup);
        emailSignup = findViewById(R.id.email);
        pwdSignup = findViewById(R.id.pwdSignup);
        birthdaySignup = (TextView)findViewById(R.id.birthday);
        addressSignup = findViewById(R.id.address);

        fauth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();


        //Date Choose picker
        birthdaySignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        MainActivity.this,
                        android.R.style.Theme_Holo_Dialog_MinWidth,
                        mDateSetListener,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

            }
        });
        //Date Choose Picker
        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;
                Log.d(TAG, "onDateSet: mm/dd/yyy:" + month + "/" + day + "/" + year);
                String birthday = month + "/" + day + "/" + year;
                birthdaySignup.setText(birthday);
            }
        };




        //Login Me TextView
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,login.class);
                startActivity(intent);
            }
        });

        //Signup Btn
        SignUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = emailSignup.getText().toString().trim();
                final String password = pwdSignup.getText().toString().trim();
                final String name = fullNameSignup.getText().toString().trim();
                final String address = addressSignup.getText().toString().trim();
                final String birthday = birthdaySignup.getText().toString().trim();



                //set the error if the fields are empty
                if(TextUtils.isEmpty(name)){
                    fullNameSignup.setError("FullName is Required");
                    return;
                }
                if(TextUtils.isEmpty(address)){
                    addressSignup.setError("Address is Required");
                }
                if(TextUtils.isEmpty(birthday)){
                    birthdaySignup.setError("Birthday is Required");
                }
                if(TextUtils.isEmpty(email)){
                    emailSignup.setError("Email is Required");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    pwdSignup.setError("Password is Required");
                    return;
                }




                //Register use in firebase
               fauth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                       if(task.isSuccessful()){
                           Toast.makeText(MainActivity.this, "Signup Succesfully", Toast.LENGTH_SHORT).show();
                           userID = fauth.getCurrentUser().getUid();
                           DocumentReference documentReference = fstore.collection("users").document(userID);

                           //create Hashmap object as user
                           Map<String,Object> user = new HashMap<>();
                           user.put("Name",name);
                           user.put("Email",email);
                           user.put("Password", password);
                           user.put("Birthday",birthday);
                           user.put("Address",address);

                           //check database
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                               @Override
                               public void onSuccess(Void aVoid) {
                                   Log.d(TAG, "onSuccess: user Profile is Created is for" + userID);
                               }
                           }).addOnFailureListener(new OnFailureListener() {
                               @Override
                               public void onFailure(@NonNull Exception e) {
                                   Log.d(TAG, "onFailure: " + e.toString());
                               }
                           });

                            //Intent
                           startActivity(new Intent(getApplicationContext(),login.class));

                       }else{
                           Toast.makeText(MainActivity.this, "Error" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                       }

                    }
                });
                //set the fields empty
                fullNameSignup.setText(null);
                emailSignup.setText(null);
                pwdSignup.setText(null);
                birthdaySignup.setText(null);
                addressSignup.setText(null);

            }



        });




    }
}