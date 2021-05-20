package com.example.practical01;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class user_profile extends AppCompatActivity {
    public static final String TAG = "TAG";
    private static final int GALLERY_INTENT_CODE = 1023 ;
    EditText fullNameProfile,emailProfile,addressProfile,passwordProfile;
    Button saveBtn;
    FirebaseAuth fauth;
    FirebaseFirestore fstore;
    FirebaseUser user;
    StorageReference storageReference;
    String userId;
    ImageView profileImage;

    //Initialize the variable for Date Choose Picker
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private TextView birthdayProfile;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Intent data = getIntent();

        //Resource identify
        fullNameProfile = findViewById(R.id.edituserName);
        birthdayProfile = findViewById(R.id.editBirthday);
        addressProfile = findViewById(R.id.editAddress);
        emailProfile = findViewById(R.id.editEmail);
        passwordProfile = findViewById(R.id.changePwd);
        saveBtn = findViewById(R.id.profileSave);
        profileImage = findViewById(R.id.profileImage);

        fauth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        user = fauth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();

        //getcurrent userId for Read Data
        userId = fauth.getCurrentUser().getUid();


        //Date Choose picker
        birthdayProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        user_profile.this,
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
                birthdayProfile.setText(birthday);
            }
        };


        //get the image from fire base
        StorageReference profileRef = storageReference.child("users/" + fauth.getCurrentUser().getUid() + "/profile_image.png");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profileImage);
            }
        });



        //to open the image gallery app/ photos app
        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGalleryIntent, 1000);
            }
        });


        //Read data
        DocumentReference documentReference = fstore.collection("users").document(userId);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                fullNameProfile.setText(documentSnapshot.getString("Name"));
                birthdayProfile.setText(documentSnapshot.getString("Birthday"));
                addressProfile.setText(documentSnapshot.getString("Address"));
                emailProfile.setText(documentSnapshot.getString("Email"));
                passwordProfile.setText(documentSnapshot.getString("Password"));
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fullNameProfile.getText().toString().isEmpty() || birthdayProfile.getText().toString().isEmpty() || addressProfile.getText().toString().isEmpty()  || passwordProfile.getText().toString().isEmpty()) {
                    Toast.makeText(user_profile.this, "Fill All the Fields", Toast.LENGTH_SHORT).show();
                    return;
                }




                //update Password with auhtentication
                final String password = passwordProfile.getText().toString();
                user.updatePassword(password).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DocumentReference documentRe = fstore.collection("users").document(user.getUid());

                        //update all fields
                        Map<String, Object> edited = new HashMap<>();

                        edited.put("Name", fullNameProfile.getText().toString());
                        edited.put("Password", passwordProfile.getText().toString());
                        edited.put("Birthday", birthdayProfile.getText().toString());
                        edited.put("Address", addressProfile.getText().toString());

                        documentRe.update(edited).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(user_profile.this, "Profile is Updated", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getApplicationContext(), login.class));
                                finish();
                            }
                        });
                        Toast.makeText(user_profile.this, "Password is changed", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(user_profile.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @androidx.annotation.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1000){
            if(resultCode == Activity.RESULT_OK){
                Uri imageUri = data.getData();

                //profileImage.setImageURI(imageUri);

                uploadImageToFirebase(imageUri);


            }
        }

    }

    //upload the image to firebase
    private void uploadImageToFirebase(Uri imageUri) {
        // uplaod image to firebase storage
        final StorageReference fileRef = storageReference.child("users/" + fauth.getCurrentUser().getUid() + "/profile_image.png");
        fileRef.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(profileImage);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Failed.", Toast.LENGTH_SHORT).show();
            }
        });

    }
}