package com.haroonfazal.haroonapps.bustracker.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.maps.android.PolyUtil;
import com.haroonfazal.haroonapps.bustracker.Models.UserLocation;
import com.haroonfazal.haroonapps.bustracker.R;
import com.haroonfazal.haroonapps.bustracker.Services.LocationShareService;
import com.haroonfazal.haroonapps.bustracker.Utils.CarDetailBottomSheetDialogFragment;
import com.haroonfazal.haroonapps.bustracker.Utils.CustomBottomSheetDialogFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback
        , GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks
        , LocationListener, GoogleMap.OnMarkerClickListener, ResultCallback {

    private static final int REQUEST_LOCATION = 1215412;
    LocationManager locationManager;
    GoogleMap mMap;
    GoogleApiClient client;
    LocationRequest request;
    LatLng latLngCurrentuserLocation;
    FirebaseAuth auth;
    HashMap<String, Marker> hashMap;

    boolean driver_profile = false;

    boolean user_profile = false;
    LatLng updateLatLng, nearestLatlng;
    DatabaseReference referenceDrivers, referenceUsers, scheduleReference;

    TextView textName, textEmail;
    RequestQueue requestQueue;
    Double distance = 0.0;

    ArrayList<UserLocation> stopList24 = new ArrayList<UserLocation>();
    ArrayList<UserLocation> stopListNNazimabad = new ArrayList<UserLocation>();

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_navigation);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        auth = FirebaseAuth.getInstance();
        requestQueue = Volley.newRequestQueue(this);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);

        textName = header.findViewById(R.id.title_text);
        textEmail = header.findViewById(R.id.email_text);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        referenceDrivers = FirebaseDatabase.getInstance().getReference().child("Drivers");
        referenceUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        scheduleReference = FirebaseDatabase.getInstance().getReference().child("uploads").child("0");
        hashMap = new HashMap<>();

        referenceDrivers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                FirebaseUser user = auth.getCurrentUser();
                if (dataSnapshot.child(user.getUid()).child("lat").exists()) {
                    driver_profile = true;
                    String driver_name = dataSnapshot.child(user.getUid()).child("name").getValue(String.class);
                    String driver_email = dataSnapshot.child(user.getUid()).child("email").getValue(String.class);
                    textName.setText(driver_name);
                    textEmail.setText(driver_email);

                    navigationView.getMenu().clear();
                    navigationView.inflateMenu(R.menu.driver_menu);

                } else {
                    user_profile = true;

                    referenceUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            FirebaseUser user1 = auth.getCurrentUser();
                            String user_name = dataSnapshot.child(user1.getUid()).child("name").getValue(String.class);
                            String user_email = dataSnapshot.child(user1.getUid()).child("email").getValue(String.class);
                            textName.setText(user_name);
                            textEmail.setText(user_email);
                            FirebaseMessaging.getInstance().subscribeToTopic("news");

                            navigationView.getMenu().clear();
                            navigationView.inflateMenu(R.menu.user_menu);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        referenceDrivers.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String lat = dataSnapshot.child("lat").getValue(String.class);
                    String lng = dataSnapshot.child("lng").getValue(String.class);
                    String vehicle_number = dataSnapshot.child("vehiclenumber").getValue(String.class);
                    LatLng latlng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

                    double d1 = CalculationByDistance(latLngCurrentuserLocation, latlng);
                    if (distance == 0.0) {
                        distance = d1;
                    } else if (d1 < distance) {
                        distance = d1;
                        nearestLatlng = latlng;
                    }

                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.title(name);
                    markerOptions.snippet("Van number: " + vehicle_number);
                    markerOptions.position(latlng);
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.mynewbusicon));

                    Marker myMarker = mMap.addMarker(markerOptions);

                    hashMap.put(myMarker.getTitle(), myMarker);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                try {
                    String name = dataSnapshot.child("name").getValue().toString();
                    String lat = dataSnapshot.child("lat").getValue().toString();
                    String lng = dataSnapshot.child("lng").getValue().toString();

                    updateLatLng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

                    final Marker marker = hashMap.get(name);

                    if (marker != null) {
                        marker.setPosition(updateLatLng);
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        new Handler().postDelayed(() -> {


        }, 50);
    }

    private void includeStops() {
        stopList24.add(new UserLocation(24.948273, 67.097166));
        stopList24.add(new UserLocation(24.943751, 67.077981));
        stopList24.add(new UserLocation(24.941191, 67.080394));
        stopList24.add(new UserLocation(24.939791, 67.075096));
        stopList24.add(new UserLocation(24.941375, 67.069573));
        stopList24.add(new UserLocation(24.938295, 67.066127));
        stopList24.add(new UserLocation(24.941338, 67.070615));
        stopList24.add(new UserLocation(24.812891, 67.117721));
        stopList24.add(new UserLocation(24.812880, 67.117385));

        stopListNNazimabad.add(new UserLocation(24.91018, 67.02384));
        stopListNNazimabad.add(new UserLocation(24.54357, 67.0134));
        stopListNNazimabad.add(new UserLocation(24.90926, 67.02784));
        stopListNNazimabad.add(new UserLocation(24.92289, 67.02772));
        stopListNNazimabad.add(new UserLocation(24.90426, 67.02788));
        stopListNNazimabad.add(new UserLocation(24.9133, 67.0270));
        stopListNNazimabad.add(new UserLocation(24.9178, 67.0284));
        stopListNNazimabad.add(new UserLocation(24.9257, 67.0231));
        stopListNNazimabad.add(new UserLocation(24.91855, 67.02701));
        stopListNNazimabad.add(new UserLocation(24.90164, 67.02708));

        addMarkersOnStops();
    }

    private void addMarkersOnStops() {
        for (UserLocation userLoc : stopList24) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(userLoc.latitude, userLoc.longitude)).icon(bitmapFromVector(this, R.drawable.ic_bus_stop)).visible(true));
        }

        for (UserLocation userLoc : stopListNNazimabad) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(userLoc.latitude, userLoc.longitude)).icon(bitmapFromVector(this, R.drawable.ic_bus_stop)).visible(true));
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle));
        mMap.setOnMarkerClickListener(this);

        includeStops();

        client = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addOnConnectionFailedListener(this)
                .addConnectionCallbacks(this)
                .build();

        client.connect();

        showTimeBottomSheet();
    }

    private void showTimeBottomSheet() {
        double distance = CalculationByDistance(latLngCurrentuserLocation, nearestLatlng);
        DecimalFormat df = new DecimalFormat("#.##");
        String dist = df.format(distance);

        Toast.makeText(getApplicationContext(), dist + " KM far.", Toast.LENGTH_SHORT).show();

        //      marker.setSnippet(dist + " KM far.");

        StringBuilder sb;
        Object[] dataTransfer = new Object[5];

        sb = new StringBuilder();
        sb.append("https://maps.googleapis.com/maps/api/directions/json?");
        sb.append("origin=").append(nearestLatlng.latitude).append(",").append(nearestLatlng.longitude);
        sb.append("&destination=").append(latLngCurrentuserLocation.latitude).append(",").append(latLngCurrentuserLocation.longitude);
        sb.append("&key=" + "AIzaSyAvCJARyieO_JjDsyIqA2dNbxbHxf8XV8g");

        DirectionAsync getDirectionsData = new DirectionAsync(getApplicationContext());
        dataTransfer[0] = sb.toString();
        dataTransfer[1] = new LatLng(nearestLatlng.latitude, nearestLatlng.longitude);
        dataTransfer[2] = new LatLng(latLngCurrentuserLocation.latitude, latLngCurrentuserLocation.longitude);

        getDirectionsData.execute(dataTransfer);
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        //       if (marker.getTitle() == null) return false;

        //       new CarDetailBottomSheetDialogFragment(marker).show(getSupportFragmentManager(), "Dialog");
        new CustomBottomSheetDialogFragment(marker).show(getSupportFragmentManager(), "Dialog");
        LatLng marker_Pos = marker.getPosition();
        return true;
    }

    private double CalculationByDistance(LatLng start, LatLng end) {
        int Radius = 6371;//radius of earth in Km
        double lat1 = start.latitude;
        double lat2 = end.latitude;
        double lon1 = start.longitude;
        double lon2 = end.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.parseInt(newFormat.format(valueResult));
        double meter = valueResult % 1000;
        int meterInDec = Integer.parseInt(newFormat.format(meter));

        return meter;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (driver_profile) {
            if (id == R.id.nav_signout) {
                if (auth != null) {
                    auth.signOut();
                    finish();
                    Intent myIntent = new Intent(NavigationActivity.this, MainActivity.class);
                    startActivity(myIntent);
                }

            } else if (id == R.id.nav_share_Location) {
                if (isServiceRunning(getApplicationContext(), LocationShareService.class)) {
                    Toast.makeText(getApplicationContext(), "You are already sharing your location.", Toast.LENGTH_SHORT).show();
                } else if (driver_profile) {
                    Intent myIntent = new Intent(NavigationActivity.this, LocationShareService.class);
                    startService(myIntent);
                } else {
                    Toast.makeText(getApplicationContext(), "Only driver can share location", Toast.LENGTH_SHORT).show();
                }

            } else if (id == R.id.nav_stop_Location) {

                Intent myIntent2 = new Intent(NavigationActivity.this, LocationShareService.class);
                stopService(myIntent2);
            }
        } else {
            if (id == R.id.nav_signout_user) {
                if (auth != null) {
                    auth.signOut();
                    finish();
                    Intent myIntent = new Intent(NavigationActivity.this, MainActivity.class);
                    myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(myIntent);
                }
            }
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void openDialog() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(NavigationActivity.this);

        LinearLayout container = new LinearLayout(NavigationActivity.this);
        container.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(50, 0, 50, 100);
        final EditText inputTitle = new EditText(NavigationActivity.this);
        inputTitle.setLayoutParams(lp);
        inputTitle.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);
        inputTitle.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        inputTitle.setLines(1);
        inputTitle.setHint("Enter title");
        inputTitle.setMaxLines(1);
        //   input.setText(lastDateValue);

        final EditText inputBody = new EditText(NavigationActivity.this);
        inputBody.setLayoutParams(lp);
        inputBody.setGravity(android.view.Gravity.TOP | android.view.Gravity.START);
        inputBody.setHint("Enter message");
        inputBody.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        inputBody.setLines(1);
        inputBody.setMaxLines(1);


        container.addView(inputTitle, lp);
        container.addView(inputBody, lp);

        alert.setMessage("Enter your notification details");
        alert.setTitle("Send Notifications");
        alert.setView(container);

        alert.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                String title_entered = inputTitle.getText().toString();
                String body_entered = inputBody.getText().toString();
                try {
                    sendFcm(title_entered, body_entered);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert.show();
    }

    private void sendFcm(String title, String body) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("to", "/topics/news");

        JSONObject notificationObject = new JSONObject();
        notificationObject.put("title", title);
        notificationObject.put("body", body);
        jsonObject.put("notification", notificationObject);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, "https://fcm.googleapis.com/fcm/send", jsonObject,
                response -> Log.d("response", response.toString()), new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "error in response=" + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> header = new HashMap<>();
                header.put("content-type", "application/json");
                header.put("authorization", "key=AIzaSyB9PCayi2q5kN7R0bS8l7Ykk5YbQyfG2Fw");
                return header;
            }
        };

        requestQueue.add(jsonObjectRequest);


    }


    public boolean isServiceRunning(Context c, Class<?> serviceClass) {
        ActivityManager activityManager = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);


        List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);


        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            if (runningServiceInfo.service.getClassName().equals(serviceClass.getName())) {
                return true;
            }
        }

        return false;


    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @SuppressLint("RestrictedApi")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        request = new LocationRequest().create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setInterval(5000);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(request);
        builder.setAlwaysShow(true);
        PendingResult result =
                LocationServices.SettingsApi.checkLocationSettings(
                        client,
                        builder.build()
                );
        result.setResultCallback(this);  // dialog for location

        LocationServices.FusedLocationApi.requestLocationUpdates(client, request, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        LocationServices.FusedLocationApi.removeLocationUpdates(client, this);

        if (location == null) {
            Toast.makeText(getApplicationContext(), "Could not find location", Toast.LENGTH_SHORT).show();
        } else {
            latLngCurrentuserLocation = new LatLng(location.getLatitude(), location.getLongitude());

            mMap.addMarker(new MarkerOptions().position(latLngCurrentuserLocation).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))).setVisible(true);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngCurrentuserLocation, 15));
        }
    }

    @Override
    public void onResult(@NonNull Result result) {
        final Status status = result.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                // NO need to show the dialog;

                break;

            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                //  GPS turned off, Show the user a dialog
                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().

                    status.startResolutionForResult(NavigationActivity.this, 202);

                } catch (IntentSender.SendIntentException e) {

                    //failed to show dialog
                }
                break;


            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                // Location settings are unavailable so not possible to show any dialog now
                break;
        }
    }

    private void OnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("Yes", (dialog, which) -> startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))).setNegativeButton("No", (dialog, which) -> dialog.cancel());
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void getLocation() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            OnGPS();
        }
        if (ActivityCompat.checkSelfPermission(
                NavigationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                NavigationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                NavigationActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION}, REQUEST_LOCATION);
        } else {
            Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (locationGPS != null) {
                double lat = locationGPS.getLatitude();
                double longi = locationGPS.getLongitude();
                String latitude = String.valueOf(lat);
                String longitude = String.valueOf(longi);
                Log.d("Location", "Latitude: " + latitude + "\n" + "Longitude: " + longitude);
                Toast.makeText(this, "Your Location: " + "\n" + "Latitude: " + latitude + "\n" + "Longitude: " + longitude, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Unable to find location.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private BitmapDescriptor bitmapFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    class DirectionAsync extends AsyncTask<Object, String, String> {

        HttpURLConnection httpURLConnection = null;
        String data = "";
        InputStream inputStream = null;

        String myurl;
        LatLng startLatLng, endLatLng;

        Context c;
        Marker marker;

        public DirectionAsync(Context c) {
            this.c = c;
        }

        @Override
        protected String doInBackground(Object... params) {
            myurl = (String) params[0];
            startLatLng = (LatLng) params[1];
            endLatLng = (LatLng) params[2];
            marker = (Marker) params[3];

            try {
                URL url = new URL(myurl);
                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.connect();

                inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuffer sb = new StringBuffer();
                String line = "";
                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }
                data = sb.toString();
                bufferedReader.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                JSONObject jsonObject = new JSONObject(s);
                JSONArray jsonArray = jsonObject.getJSONArray("routes").getJSONObject(0)
                        .getJSONArray("legs").getJSONObject(0).getJSONArray("steps");


                JSONArray jsonRoute = jsonObject.getJSONArray("routes");//.getJSONObject(0).getJSONArray("legs");
                JSONObject jsonObject1 = jsonRoute.getJSONObject(0);
                JSONArray legs = jsonObject1.getJSONArray("legs");
                JSONObject legsObjects = legs.getJSONObject(0);
                JSONObject time = legsObjects.getJSONObject("duration");
                String duration = time.getString("text");


                //        jsonObject = new JSONObject(stringBuilder.toString());
                JSONArray array = jsonObject.getJSONArray("routes");
                JSONObject routes = array.getJSONObject(0);
                JSONArray leg = routes.getJSONArray("legs");
                JSONObject steps = leg.getJSONObject(0);
                JSONObject distance = steps.getJSONObject("distance");

                double dist = Double.parseDouble(distance.getString("text").replaceAll("[^\\.0123456789]", ""));

                if (duration.isEmpty()) {
                    marker.setTitle("N/A");
                    Toast.makeText(getApplicationContext(), "N/A", Toast.LENGTH_SHORT).show();
                } else {
                    marker.setTitle(duration);
                    marker.setSnippet(String.valueOf(dist));
                    Toast.makeText(getApplicationContext(), String.valueOf(dist), Toast.LENGTH_SHORT).show();
                }
                //         String distancetxt = jsonObject1.getJSONObject("duration").getString("text");


                //         Toast.makeText(c, distancetxt + " away.", Toast.LENGTH_SHORT).show();

                /*int count = jsonArray.length();
                String[] polyline_array = new String[count];

                JSONObject jsonobject2;


                for (int i = 0; i < count; i++) {
                    jsonobject2 = jsonArray.getJSONObject(i);

                    String polygone = jsonobject2.getJSONObject("polyline").getString("points");

                    polyline_array[i] = polygone;
                }

                int count2 = polyline_array.length;

                for (int i = 0; i < count2; i++) {

                    PolylineOptions options2 = new PolylineOptions();
                    options2.color(Color.GREEN);
                    options2.width(10);
                    options2.addAll(PolyUtil.decode(polyline_array[i]));

                    mMap.addPolyline(options2);
                }*/
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
