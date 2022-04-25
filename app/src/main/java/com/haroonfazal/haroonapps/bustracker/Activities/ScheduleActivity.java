package com.haroonfazal.haroonapps.bustracker.Activities;

import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.haroonfazal.haroonapps.bustracker.Models.Upload;
import com.haroonfazal.haroonapps.bustracker.R;
import com.squareup.picasso.Picasso;

public class ScheduleActivity extends AppCompatActivity {

    Toolbar toolbar;
    ImageView imageView;
    DatabaseReference databaseReference;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schedule_two);
        toolbar = findViewById(R.id.driverToolbar);
        toolbar.setTitle("Bus Schedule");
        setSupportActionBar(toolbar);

        imageView = (ImageView) findViewById(R.id.imageView);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("uploads").child("0");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Upload upload = dataSnapshot.getValue(Upload.class);
                    Picasso.get().load(upload.getImageName()).placeholder(R.drawable.placeholder).into(imageView);
                } else {
                    Toast.makeText(getApplicationContext(), "Schedule not found", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "" + databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });


    }


}
