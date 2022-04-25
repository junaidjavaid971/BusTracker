package com.haroonfazal.haroonapps.bustracker.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.haroonfazal.haroonapps.bustracker.R;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    EditText editTextUserEmail;
    EditText editTextUserPassword;
    Button btnLogin;

    FirebaseAuth auth;
    ProgressDialog dialog;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init();
        auth = FirebaseAuth.getInstance();
        dialog = new ProgressDialog(this);
    }

    private void init() {
        editTextUserEmail = findViewById(R.id.tv_enterEmail);
        editTextUserPassword = findViewById(R.id.tv_enterPassword);
        btnLogin = findViewById(R.id.btnlogin);

    }

    public void login(View v) {

        dialog.setMessage("Logging in. Please wait.");
        dialog.show();

        if (editTextUserEmail.getText().toString().equals("") || editTextUserPassword.getText().toString().equals("")) {
            Toast.makeText(getApplicationContext(), "Blank fields not allowed.", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        } else {
            String newEmail = editTextUserEmail.getText().toString();

            auth.signInWithEmailAndPassword(newEmail, editTextUserPassword.getText().toString())
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                dialog.dismiss();
                                Intent loginIntent = new Intent(LoginActivity.this, NavigationActivity.class);
                                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(loginIntent);
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "Wrong email/password combination. Try again.", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }
                    });
        }
    }

}
