package com.haroonfazal.haroonapps.bustracker.Activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import java.util.List;

public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    TextView textName, textEmail;
    LottieAnimationView loader;
    ImageView ivArrowUp;
    NavigationView navigationView;
    FirebaseAuth auth;

    GoogleMap mMap;
    Polyline polyline;
    FusedLocationProviderClient fusedLocationClient;
    LocationCallback locationCallback;
    LocationRequest locationRequest;
    Marker currentLocMarker, nearestMarker;
    LatLng currentLatlng, nearestLatlng;
    String nearestDriverID = "";

    Double distance = 0.0;

    boolean isDriverProfile = false;
    boolean isNotificationSent = false;
    CustomBottomSheetDialogFragment dialogFragment;
    ArrayList<UserLocation> stopList24 = new ArrayList<UserLocation>();
    ArrayList<UserLocation> stopListNNazimabad = new ArrayList<UserLocation>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initViews();
        init();
        getUserInfo();
        initLocationCallback();
    }

    private void getAllDrivers() {
        DatabaseReference referenceDrivers = FirebaseDatabase.getInstance().getReference().child("Drivers");
        referenceDrivers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    int count = 0;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        count++;
                        String lat = snapshot.child("lat").getValue(String.class);
                        String lng = snapshot.child("lng").getValue(String.class);
                        LatLng latlng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

                        double d1 = calculationByDistance(currentLatlng, latlng);
                        if (distance == 0.0) {
                            distance = d1;
                            nearestDriverID = snapshot.getKey();
                        } else if (d1 < distance) {
                            distance = d1;
                            nearestLatlng = latlng;
                            nearestDriverID = snapshot.getKey();
                        }

                        if (count == dataSnapshot.getChildrenCount()) {
                            getNearestDriver();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getNearestDriver() {
        DatabaseReference referenceDrivers = FirebaseDatabase.getInstance().getReference().child("Drivers").child(nearestDriverID);
        referenceDrivers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    String name = snapshot.child("name").getValue(String.class);
                    String lat = snapshot.child("lat").getValue(String.class);
                    String lng = snapshot.child("lng").getValue(String.class);
                    String vehicle_number = snapshot.child("vehiclenumber").getValue(String.class);
                    LatLng latlng = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));

                    nearestLatlng = latlng;
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.title(name);
                    markerOptions.snippet("Van number: " + vehicle_number);
                    markerOptions.position(latlng);
                    markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.mynewbusicon));

                    if (nearestMarker != null) {
                        nearestMarker.remove();
                    }
                    nearestMarker = mMap.addMarker(markerOptions);
                    showTimeBottomSheet(nearestMarker);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getUserInfo() {
        DatabaseReference referenceDrivers = FirebaseDatabase.getInstance().getReference().child("Drivers");
        referenceDrivers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                FirebaseUser user = auth.getCurrentUser();
                if (dataSnapshot.child(user.getUid()).child("lat").exists()) {
                    isDriverProfile = true;
                    String driver_name = dataSnapshot.child(user.getUid()).child("name").getValue(String.class);
                    String driver_email = dataSnapshot.child(user.getUid()).child("email").getValue(String.class);
                    textName.setText(driver_name);
                    textEmail.setText(driver_email);
                    loader.setVisibility(View.GONE);

                    navigationView.getMenu().clear();
                    navigationView.inflateMenu(R.menu.driver_menu);
                } else {
                    DatabaseReference referenceUsers = FirebaseDatabase.getInstance().getReference().child("Users");
                    referenceUsers.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            isDriverProfile = false;
                            FirebaseUser user1 = auth.getCurrentUser();
                            String user_name = dataSnapshot.child(user1.getUid()).child("name").getValue(String.class);
                            String user_email = dataSnapshot.child(user1.getUid()).child("email").getValue(String.class);
                            textName.setText(user_name);
                            textEmail.setText(user_email);
                            FirebaseMessaging.getInstance().subscribeToTopic("news");

                            navigationView.getMenu().clear();
                            navigationView.inflateMenu(R.menu.user_menu);

                            getAllDrivers();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
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

    }

    private void showTimeBottomSheet(Marker marker) {
        double distance = calculationByDistance(currentLatlng, nearestLatlng);
        DecimalFormat df = new DecimalFormat("#.##");
        String dist = df.format(distance);

        StringBuilder sb;
        Object[] dataTransfer = new Object[5];

        sb = new StringBuilder();
        sb.append("https://maps.googleapis.com/maps/api/directions/json?");
        sb.append("origin=").append(nearestLatlng.latitude).append(",").append(nearestLatlng.longitude);
        sb.append("&destination=").append(currentLatlng.latitude).append(",").append(currentLatlng.longitude);
        sb.append("&key=" + "AIzaSyAvCJARyieO_JjDsyIqA2dNbxbHxf8XV8g");

        DirectionAsync getDirectionsData = new DirectionAsync(getApplicationContext());
        dataTransfer[0] = sb.toString();
        dataTransfer[1] = new LatLng(nearestLatlng.latitude, nearestLatlng.longitude);
        dataTransfer[2] = new LatLng(currentLatlng.latitude, currentLatlng.longitude);
        dataTransfer[3] = marker;

        getDirectionsData.execute(dataTransfer);
    }

    private void initLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (currentLocMarker != null) {
                        currentLocMarker.remove();
                    }

                    currentLatlng = new LatLng(location.getLatitude(), location.getLongitude());
                    currentLocMarker = mMap.addMarker(new MarkerOptions().position(currentLatlng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                    if (currentLocMarker != null) {
                        currentLocMarker.setVisible(true);
                    }

                    if (nearestLatlng != null) {
                        if (calculationByDistance(currentLatlng, nearestLatlng) < 1 && !isNotificationSent) {
                            isNotificationSent = true;
                            Toast.makeText(DashboardActivity.this, "You ride is arriving", Toast.LENGTH_SHORT).show();
                            sendRideArrivingNotification();
                        }
                    }
                }
            }
        };
    }


    private void sendRideArrivingNotification() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("channelid1", "001", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("This is description");
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);

            Notification.Builder builder = new Notification.Builder(getApplicationContext(), notificationChannel.getId());
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setContentTitle("IOBM Bus Tracker");
            builder.setContentText("Your ride is arriving soon.!");
            builder.setSmallIcon(R.drawable.share_location)
                    .setPriority(Notification.PRIORITY_HIGH);

            NotificationManagerCompat nmc = NotificationManagerCompat.from(getApplicationContext());
            nmc.notify(1252120, builder.build());

            Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setContentIntent(pendingIntent);

            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.notify(1252120, builder.build());
        } else {
            Notification.Builder builder = new Notification.Builder(getApplicationContext());
            builder.setSmallIcon(R.mipmap.ic_launcher);

            builder.setContentTitle("IOBM Bus Tracker");

            builder.setContentText("Your ride is arriving soon!").
                    setPriority(Notification.PRIORITY_DEFAULT);

            NotificationManagerCompat nmc = NotificationManagerCompat.from(getApplicationContext());
            nmc.notify(1252120, builder.build());

            Intent intent = new Intent(getApplicationContext(), DashboardActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);

            builder.setContentIntent(pendingIntent);

            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.notify(1252120, builder.build());
        }

        MediaPlayer mp = MediaPlayer.create(this, R.raw.notification);
        mp.start();
    }

    private void init() {
        auth = FirebaseAuth.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        ivArrowUp = findViewById(R.id.ivInfo);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);

        textName = header.findViewById(R.id.title_text);
        loader = header.findViewById(R.id.loader);
        textEmail = header.findViewById(R.id.email_text);

        ivArrowUp.setOnClickListener(v -> {
            showTimeBottomSheet(nearestMarker);
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (isDriverProfile) {
            if (id == R.id.nav_signout) {
                if (auth != null) {
                    auth.signOut();
                    finish();
                    Intent myIntent = new Intent(DashboardActivity.this, MainActivity.class);
                    startActivity(myIntent);
                }

            } else if (id == R.id.nav_share_Location) {
                if (isServiceRunning(getApplicationContext(), LocationShareService.class)) {
                    Toast.makeText(getApplicationContext(), "You are already sharing yourlocation.", Toast.LENGTH_SHORT).show();
                } else if (isDriverProfile) {
                    Intent intent = new Intent(DashboardActivity.this, LocationShareService.class);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        startForegroundService(intent);
                    } else {
                        startService(intent);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Only driver can share location", Toast.LENGTH_SHORT).show();
                }
            } else if (id == R.id.nav_stop_Location) {
                Intent intent = new Intent(DashboardActivity.this, LocationShareService.class);
                stopService(intent);
            }
        } else {
            if (id == R.id.nav_signout_user) {
                if (auth != null) {
                    auth.signOut();
                    finish();
                    Intent myIntent = new Intent(DashboardActivity.this, MainActivity.class);
                    myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(myIntent);
                }
            }
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @SuppressLint("PotentialBehaviorOverride")
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle));
        mMap.setOnMarkerClickListener(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {

                    if (location != null) {
                        currentLatlng = new LatLng(location.getLatitude(), location.getLongitude());

                        currentLocMarker = mMap.addMarker(new MarkerOptions().position(currentLatlng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                        if (currentLocMarker != null) {
                            currentLocMarker.setVisible(true);
                        }
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatlng, 15));
                    }
                });
        includeStops();
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

    private BitmapDescriptor bitmapFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    protected void createLocationRequest() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
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

    private double calculationByDistance(LatLng start, LatLng end) {
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
    public boolean onMarkerClick(@NonNull Marker marker) {
        return false;
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    protected void onStart() {
        super.onStart();
        createLocationRequest();
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

                JSONArray array = jsonObject.getJSONArray("routes");
                JSONObject routes = array.getJSONObject(0);
                JSONArray leg = routes.getJSONArray("legs");
                JSONObject steps = leg.getJSONObject(0);
                JSONObject distance = steps.getJSONObject("distance");

                double dist = Double.parseDouble(distance.getString("text").replaceAll("[^\\.0123456789]", ""));

                if (duration.isEmpty()) {
                    marker.setTitle("N/A");
                } else {
                    marker.setTitle(duration);
                    marker.setSnippet(String.valueOf(dist));
                }
                nearestMarker = marker;
                ivArrowUp.setVisibility(View.VISIBLE);

                if (nearestMarker != null) {
                    if (dialogFragment == null)
                        dialogFragment = new CustomBottomSheetDialogFragment(nearestMarker);

                    if (!dialogFragment.isAdded()) {
                        dialogFragment.show(getSupportFragmentManager(), "Dialog");
                    } else {
                        dialogFragment.updateDetails(nearestMarker);
                    }
                }

                int count = jsonArray.length();
                String[] polyline_array = new String[count];

                JSONObject jsonobject2;


                for (int i = 0; i < count; i++) {
                    jsonobject2 = jsonArray.getJSONObject(i);

                    String polygone = jsonobject2.getJSONObject("polyline").getString("points");

                    polyline_array[i] = polygone;
                }

                int count2 = polyline_array.length;

                if (polyline != null) {
                    polyline.remove();
                }
                for (String value : polyline_array) {
                    PolylineOptions options2 = new PolylineOptions();
                    options2.color(ContextCompat.getColor(DashboardActivity.this, R.color.colorPrimary));
                    options2.width(10);
                    options2.addAll(PolyUtil.decode(value));

                    polyline = mMap.addPolyline(options2);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}