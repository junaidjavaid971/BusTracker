package com.haroonfazal.haroonapps.bustracker.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.haroonfazal.haroonapps.bustracker.Models.Driver;
import com.haroonfazal.haroonapps.bustracker.R;

import java.util.Objects;

public class DriverRegistrationActivity extends AppCompatActivity {

    EditText editTextDriverName;
    EditText editTextDriverEmail;
    EditText editTextDriverPassword;
    EditText editTextDriverBus;
    Toolbar toolbar;


    FirebaseAuth auth;
    ProgressDialog dialog;
    FirebaseUser user;

    DatabaseReference databaseReference;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_registration);
        init();
        auth = FirebaseAuth.getInstance();
        dialog = new ProgressDialog(this);
    }

    private void init() {
        editTextDriverName = findViewById(R.id.tv_enterDriverName);
        editTextDriverEmail = findViewById(R.id.tv_enterDriverEmail);
        editTextDriverPassword = findViewById(R.id.tv_enterDriverPassword);
        editTextDriverBus = findViewById(R.id.tv_enterBusNumber);
        toolbar = findViewById(R.id.driverToolbar);
    }

    public void registerDriver(View v) {
        dialog.setTitle("Creating account");
        dialog.setMessage("Please wait");
        dialog.show();

        final String name = editTextDriverName.getText().toString();
        final String email = editTextDriverEmail.getText().toString();
        final String password = editTextDriverPassword.getText().toString();


        if (name.equals("") && email.equals("") && password.equals("")) {
            Toast.makeText(getApplicationContext(), "Please enter correct details", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        } else {
            doAllStuff();
        }
    }


    public void doAllStuff() {
        auth.createUserWithEmailAndPassword(editTextDriverEmail.getText().toString(),
                editTextDriverPassword.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            Driver driver = new Driver(editTextDriverName.getText().toString(),
                                    editTextDriverEmail.getText().toString(),
                                    editTextDriverPassword.getText().toString(),
                                    editTextDriverBus.getText().toString(),
                                    "33.652037", "73.156598");

                            user = auth.getCurrentUser();
                            databaseReference = FirebaseDatabase.getInstance().
                                    getReference().child("Drivers").child(user.getUid());

                            databaseReference.setValue(driver)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {
                                                dialog.dismiss();
                                                Toast.makeText(DriverRegistrationActivity.this,
                                                        "Account created successfully", Toast.LENGTH_SHORT).show();
                                                finish();
                                                Intent myIntent = new Intent(DriverRegistrationActivity.this, DashboardActivity.class);
                                                myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(myIntent);
                                            } else {
                                                Toast.makeText(DriverRegistrationActivity.this,
                                                        "Could not register driver", Toast.LENGTH_LONG).show();
                                                dialog.dismiss();
                                            }
                                        }
                                    });


                        } else {
                            Toast.makeText(DriverRegistrationActivity.this,
                                    "Could not register. " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    }
                });
    }
}
