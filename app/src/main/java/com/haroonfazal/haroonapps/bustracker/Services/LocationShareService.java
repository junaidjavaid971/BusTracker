package com.haroonfazal.haroonapps.bustracker.Services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.haroonfazal.haroonapps.bustracker.Activities.DashboardActivity;
import com.haroonfazal.haroonapps.bustracker.R;

public class LocationShareService extends Service {
    LatLng latLngCurrent;
    DatabaseReference reference;
    FirebaseAuth auth;
    FirebaseUser user;

    public final int notificationID = 654321;

    FusedLocationProviderClient fusedLocationClient;
    LocationRequest locationRequest;
    NotificationManagerCompat nmc;
    Notification.Builder builder;
    LocationCallback locationCallback;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        reference = FirebaseDatabase.getInstance().getReference().child("Drivers");
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        initLocationCallback();
        createLocationRequest();
        startLocationUpdates();
        showNotifications();

        return START_STICKY;
    }

    private void showNotifications() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(getString(R.string.app_name), "001", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("This is description");
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);

            builder = new Notification.Builder(getApplicationContext(), notificationChannel.getId());
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setContentTitle("IOBM Bus Tracker");
            builder.setContentText("You are sharing your location.!");
            builder.setAutoCancel(false);
            builder.setSmallIcon(R.drawable.share_location)
                    .setPriority(Notification.PRIORITY_DEFAULT);

            nmc = NotificationManagerCompat.from(getApplicationContext());
            nmc.notify(notificationID, builder.build());
        } else {
            builder = new Notification.Builder(getApplicationContext());
            builder.setSmallIcon(R.mipmap.ic_launcher);

            builder.setContentTitle("IOBM Bus Tracker");

            builder.setContentText("You are sharing your location.!").
                    setPriority(Notification.PRIORITY_DEFAULT);

            nmc = NotificationManagerCompat.from(getApplicationContext());
            nmc.notify(notificationID, builder.build());
        }
        Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(pendingIntent);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.notify(notificationID, builder.build());
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    private void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    private void initLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    try {
                        latLngCurrent = new LatLng(location.getLatitude(), location.getLongitude());

                        reference.child(user.getUid()).child("lat").setValue(String.valueOf(latLngCurrent.latitude));
                        reference.child(user.getUid()).child("lng").setValue(String.valueOf(latLngCurrent.longitude))
                                .addOnCompleteListener(task -> {
                                    if (!task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(), "Could not share Location.", Toast.LENGTH_SHORT).show();
                                    }
                                });

                        Log.d("LocationService", latLngCurrent.latitude + " - " + latLngCurrent.longitude);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Only drivers can share their location", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
    }

    @Override
    public void onDestroy() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(notificationID);
    }
}
