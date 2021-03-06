package com.iwish.taxidriver.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.StateListDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
//import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.iwish.taxidriver.R;
import com.iwish.taxidriver.Request.EndtripDialog;
import com.iwish.taxidriver.Request.RequestDialog;
import com.iwish.taxidriver.Ridehistory.Main2Activity;
import com.iwish.taxidriver.config.Constants;
import com.iwish.taxidriver.connection.ConnectionServer;
import com.iwish.taxidriver.connection.JsonHelper;
import com.iwish.taxidriver.extended.ButtonFonts;
import com.iwish.taxidriver.extended.TexiFonts;
import com.iwish.taxidriver.usersession.UserSession;
import com.iwish.taxidriver.webSocket.BackgroundService;
import com.iwish.taxidriver.webSocket.SocketService;
import com.google.android.gms.location.LocationListener;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.model.Place;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import static com.iwish.taxidriver.R.color.Login_button_white;
import static com.iwish.taxidriver.R.color.low_text_color;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private GoogleMap mMap;
    RadioButton on,off,house;
    RadioGroup radioGroup;
    TextView ontext,offtext,hometext;
    ImageView car;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private GoogleApiClient mGoogleApiClient2;
    public UserSession userSession;
    double destLat,destLong;
    String  address, streetname;
    AutoCompleteTextView destinationET;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation, previousLocation;
    Marker mCurrLocationMarker,source,destination;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final int PERMISSION_REQUEST_GPS_CODE = 1234;
    LocationManager locationManager;
    boolean isGPS;
    private PlaceArrayAdapter mPlaceArrayAdapter;
    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(new LatLng(37.398160, -122.180831), new LatLng(37.430610, -121.972090));
    LatLng origin,dest,previouslatLng;
    PolylineOptions lineOptions;
    boolean startTrack = false;
    ArrayList<LatLng> points;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 5000;
    private FusedLocationProviderClient mFusedLocationClient;
    private SettingsClient mSettingsClient;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Boolean mRequestingLocationUpdates;
    LinearLayout dutypannel, delivery, navigatepannel,trippannel,account;
    BackgroundService backgroundService;
    Intent intent;
    SocketService socketService;
    View topbar,incentivepannel,bottom_menu;
    ButtonFonts navigate,clientLocated,navigatetrip,endtrip;
    TexiFonts clientaddress,time,addresstype,bookings,operatorbill,total ;


    //oncreate method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userSession = new UserSession(MainActivity.this);



         // get id of layout
        radioGroup = findViewById(R.id.duty);
        dutypannel=findViewById(R.id.dutypannel);
        on= findViewById(R.id.on);
        off= findViewById(R.id.off);
        house= findViewById(R.id.home);
        ontext= findViewById(R.id.on_text);
        offtext= findViewById(R.id.off_text);
        delivery= findViewById(R.id.delivery);
        car= findViewById(R.id.car);
        topbar= findViewById(R.id.topbar);
        incentivepannel= findViewById(R.id.incentivepannel);
        navigate= findViewById(R.id.navigate);
        bottom_menu= findViewById(R.id.bottommenu);
        navigatepannel= findViewById(R.id.navigatepannel);
        navigatepannel= findViewById(R.id.navigatepannel);
        clientLocated= findViewById(R.id.clientlocated);
        clientaddress= findViewById(R.id.address);
        time= findViewById(R.id.time);
        navigatetrip= findViewById(R.id.navigatetrip);
        trippannel= findViewById(R.id.trippannel);
        endtrip= findViewById(R.id.endtrip);
        addresstype= findViewById(R.id.addresstype);
        bookings= findViewById(R.id.bookings);
        operatorbill= findViewById(R.id.operatorbill);
        total= findViewById(R.id.total);
        account= findViewById(R.id.account);

        account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(MainActivity.this, Main2Activity.class);
                startActivity(intent);
                Animatoo.animateInAndOut(MainActivity.this);
            }
        });

        setbooking();
        set_initial_duty_status();
        setIncentivepannel();

        // initiate background services
          backgroundService= new BackgroundService(this);
          intent= new Intent(MainActivity.this,BackgroundService.class);
        //end  initiate background services


        //on duty/off duty
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @SuppressLint({"ResourceAsColor", "ResourceType"})
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.on:
                        userSession.setsocketonoff("true");
                        socketService= new SocketService();
                        socketService.setdata(MainActivity.this);
                         socketService.onStartCommand(intent,1,1);
                        userSession.set_current_duty_status("ture");
                         dutypannel.setBackground(getResources().getDrawable(R.drawable.top_bar));
                         Duty_responce(userSession.get_mobile(),"true");
                         on.setButtonDrawable(R.mipmap.onoff_foreground);
                         off.setButtonDrawable(new StateListDrawable());
//                         house.setButtonDrawable(new StateListDrawable());
                         car.setVisibility(View.VISIBLE);
                         ontext.setTextColor(ContextCompat.getColor(MainActivity.this, Login_button_white));
                         offtext.setTextColor(ContextCompat.getColor(MainActivity.this, low_text_color));
//                         hometext.setTextColor(ContextCompat.getColor(MainActivity.this, low_text_color));
//                            backgroundService.onStartCommand(intent, 1, 1);
                         break;
                    case R.id.off:
                        userSession.setsocketonoff("false");
                        userSession.set_current_duty_status("false");
                        if(userSession.getSocketConnection())
                        {
                            socketService.onDestroy();
                        }

                        dutypannel.setBackground(getResources().getDrawable(R.drawable.off_duty));
                        Duty_responce(userSession.get_mobile(),"false");
                        on.setButtonDrawable(new StateListDrawable());
                        off.setButtonDrawable(R.mipmap.onoff_foreground);
//                        house.setButtonDrawable(new StateListDrawable());
                        offtext.setTextColor(ContextCompat.getColor(MainActivity.this, Login_button_white));
                        ontext.setTextColor(ContextCompat.getColor(MainActivity.this, Login_button_white));
//                        hometext.setTextColor(ContextCompat.getColor(MainActivity.this, Login_button_white));
                        car.setVisibility(View.INVISIBLE);
                        break;
//                    case R.id.home:
//                        userSession.set_current_duty_status("home");
//                        dutypannel.setBackground(getResources().getDrawable(R.drawable.home_duty));
//                        Duty_responce(userSession.get_mobile(),"home");
//                        on.setButtonDrawable(new StateListDrawable());
//                        off.setButtonDrawable(new StateListDrawable());
//                        house.setButtonDrawable(R.mipmap.onoff_foreground);
//                        hometext.setTextColor(ContextCompat.getColor(MainActivity.this, Login_button_white));
//                        offtext.setTextColor(ContextCompat.getColor(MainActivity.this, low_text_color));
//                        ontext.setTextColor(ContextCompat.getColor(MainActivity.this, low_text_color));
//                        car.setVisibility(View.VISIBLE);
//                        break;
                }
            }
        });
        //end of on duty/off duty




        //build google client object
        mGoogleApiClient2 = new GoogleApiClient.Builder(MainActivity.this)
                .addApi(com.google.android.gms.location.places.Places.GEO_DATA_API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .addConnectionCallbacks(this)
                .build();
        //end of build google client object




        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();

            } else {
                //Request Location Permission
                checkLocationPermission();
//                latitude = mLastLocation.getLatitude();
//                longitude = mLastLocation.getLongitude();
            }
        } else {
            buildGoogleApiClient();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        init();
        destinationET = findViewById(R.id.destination_ET);
        destinationET.setThreshold(3);
        mPlaceArrayAdapter = new PlaceArrayAdapter(this, android.R.layout.simple_list_item_1, BOUNDS_MOUNTAIN_VIEW, null);
        destinationET.setAdapter(mPlaceArrayAdapter);
        locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
        isGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);



        //get gps permission by user
        if (!isGPS) {

            new AlertDialog.Builder(this)
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs the Location permission, please accept to use location functionality")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Prompt the user once explanation has been shown
//                            ActivityCompat.requestPermissions(MapsActivity.this,
//                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                                    MY_PERMISSIONS_REQUEST_LOCATION);

                            showGPSSettingsAlert();
                        }
                    })
                    .create()
                    .show();

        }
        //get gps permission by user

    }
    //oncreate method


    //place adapter for fatching places
    private AdapterView.OnItemClickListener mAutocompleteClickListener
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final PlaceArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i(TAG, "Selected: " + item.description);
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient2, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            Log.i(TAG, "Fetching details for ID: " + item.placeId);
        }
    };
    //place adapter for fatching places


    @Override
    protected void onStart() {
        super.onStart();
        buildGoogleApiClient();
    }

    //update place call back method
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e(TAG, "Place query did not complete. Error: " +
                        places.getStatus().toString());
                return;
            }
            // Selecting the first object buffer.
            final Place place = (Place) places.get(0);
            CharSequence attributions = places.getAttributions();
            Log.e("place", place.getName() + place.getId());
            destLat = place.getLatLng().latitude;
            destLong = place.getLatLng().longitude;
            mMap.clear();
            Toast.makeText(getApplication(), "place" + place.getName() + place.getId(), Toast.LENGTH_LONG).show();

        }
    };
    //update place call back method



    //get current location metthod
    @SuppressLint("RestrictedApi")
    private void init() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // location is received
                mLastLocation = locationResult.getLastLocation();

            }
        };

        mRequestingLocationUpdates = false;
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }
    //end of get current location metthod


    @Override
    protected void onPause() {
        super.onPause();
    }

    //set google map
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            return;
        }
        mMap.setMyLocationEnabled(false);



    }
    //end of set google map

    //connected google map api
    @SuppressLint("RestrictedApi")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        checkLocationPermission();
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            Log.i("google api client", String.valueOf(mGoogleApiClient.isConnected()));
            if(mGoogleApiClient.isConnected())
            {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);
                mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);
                Log.i(TAG, "Google Places API connected.");
              }
            else {
            Log.i(TAG, "Google Places API not  connected.");


            }




    }
    //end of connected google map api


    //method to check api is suspended or not
    @Override
    public void onConnectionSuspended(int i) {
        mPlaceArrayAdapter.setGoogleApiClient(null);
        Log.e(TAG, "Google Places API connection suspended.");
    }
    //end of method to check api is suspended or not


    //method for getting error if api is not connected
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Google Places API connection failed with error code: " + connectionResult.getErrorCode());
        Toast.makeText(this, "Google Places API connection failed with error code:" + connectionResult.getErrorCode(), Toast.LENGTH_LONG).show();
    }
    //end of method for getting error if api is not connected


    //method to get updated location
    @SuppressLint("WrongConstant")
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        View view = null;
        if (mCurrLocationMarker == null) {
            LatLng myLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

            if(source!=null){
                source.remove();
            }
            if(destination!=null){
                drawRoute(view);
            }
             mMap.setMyLocationEnabled(true);

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 15));

            Log.d("location", "Latitude:" + mLastLocation.getLatitude() + "\n" + "Longitude:" + mLastLocation.getLongitude());



            Geocoder geocoder= new Geocoder(this);
            try {
                List<Address>address=  geocoder.getFromLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude(),1);
                streetname=address.get(0).getLocality();

            } catch (IOException e) {
                e.printStackTrace();
            }
            userSession.setLocation(String.valueOf(mLastLocation.getLatitude()),String.valueOf(mLastLocation.getLongitude()),streetname);
//            Toast.makeText(this, "ernrrwiornoinrnrknk44i", Toast.LENGTH_SHORT).show();



        }
        if (startTrack) {
            previouslatLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            if  (destination != null)
//                destination.remove();
            if (origin.latitude != mLastLocation.getLatitude()  && origin.longitude != mLastLocation.getLongitude() ) {
            }
            double rota = 0.0;
            double startrota = 0.0;
            if (previousLocation != null) {
                rota = bearingBetweenLocations(previouslatLng, new LatLng(destLat,destLong));
            }
            previousLocation = location;
//            Log.e(TAG, "Firing onLocationChanged..........................");
//            Log.e(TAG, "lat :" + location.getLatitude() + "long :" + location.getLongitude());
//            Log.e(TAG, "bearing :" + location.getBearing());

        }

    }
    //end of method to get updated location


    //create object google api client
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }
    //end of create object google api client


    //method for checking Location permission
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to Request the permission.
                new AlertDialog.Builder(this).setTitle("Location Permission Needed").setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can Request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            }
        }
    }
    //method for checking Location permission

    //method for enabling gps by user
    public void showGPSSettingsAlert() {
        isGPS = false;
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(R.string.word_GPS);
        alertDialog.setCancelable(false);
        alertDialog.setMessage(R.string.word_GPS_not_enabled);
        alertDialog.setPositiveButton(R.string.word_GPS_enable, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), PERMISSION_REQUEST_GPS_CODE);
            }
        });

        alertDialog.show();
    }
    //method for enabling gps by user

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If Request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();

                        }
                        mMap.setMyLocationEnabled(true);
                        //  addMarker(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSION_REQUEST_GPS_CODE) {
            isGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (!isGPS)
                showGPSSettingsAlert();
        }
    }


    //method for mark point in map
    public void addMarker(LatLng latLng) {
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
            mMap.clear();
        }
//        mCurrLocationMarker = mMap.addMarker(new MarkerOptions().draggable(true).title("I am here ").position(latLng));
//        icon(BitmapDescriptorFactory.fromResource(R.mipmap.marker_)));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        //   String x = getAddress(latLng.latitude, latLng.longitude);
//        latitude = latLng.latitude;
//        longitude = latLng.longitude;
//
        if (address != null)

            Log.e("address", "nothing happen");
    }
    //end of method for mark point in map


    //mark source location on map
    public void Source(Double lat, Double log , String msg){
        LatLng current;
        current = new LatLng(lat,log);
        destination = mMap.addMarker(new MarkerOptions().draggable(true).title(msg).
                position(current).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                current, 16));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 15));
    }
    //end of mark source location on map


    @Override
    protected void onResume() {
        super.onResume();

        if (mMap != null) {
        }

    }


    //method for draw route on map
    public void drawRoute(View view) {

        double Lati;
        double Logi;


        if(userSession.getLocationStatus()){
             Lati= Double.valueOf(userSession.getdroplat());
            Logi= Double.valueOf(userSession.getdroplong());
            origin = new LatLng(userSession.getLatitute(),userSession.getLogitute());
            dest = new LatLng(Lati,Logi);
            Source(Lati,Logi,"drop");
        }
        else
        {
            switch (userSession.getBookingtype())
            {
                case "daily":
                    Lati= Double.valueOf(userSession.getpiclat());
                     Logi= Double.valueOf(userSession.getpiclong());
                    origin = new LatLng(userSession.getLatitute(),userSession.getLogitute());
                    dest = new LatLng(Lati,Logi);
                    Source(Lati,Logi,"drop");
                    break;

                case "rental":
                     Lati= Double.valueOf(userSession.getRentaldetail().get("rentalpiclat"));
                    Logi= Double.valueOf(userSession.getRentaldetail().get("rentalpiclong"));
                    origin = new LatLng(userSession.getLatitute(),userSession.getLogitute());
                    dest = new LatLng(Lati,Logi);
                    Source(Lati,Logi,"drop");
                    break;

                case "OutStation":
                    Lati= Double.valueOf(userSession.getpiclat());
                    Logi= Double.valueOf(userSession.getpiclong());
                    origin = new LatLng(userSession.getLatitute(),userSession.getLogitute());
                    dest = new LatLng(Lati,Logi);
                    Source(Lati,Logi,"drop");
                    break;


            }

        }


                    // Getting URL to the Google Directions API
                    String url = getUrl(origin, dest);
                    Log.d("onMapClick", url.toString());
                    FetchUrl FetchUrl = new FetchUrl();

                    // Start downloading json data from Google Directions API
                    FetchUrl.execute(url);
                    //move map camera
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                    startTrack = true;



//        DistanceTimeCalculate distanceTimeCalculate =new DistanceTimeCalculate(Lati,Logi,mLastLocation.getLatitude(),mLastLocation.getLongitude());


//        origin = new LatLng(26.236285,78.179939);
//        dest = new LatLng(destLat, destLong);

    }


    //end of method for draw route on map


    //method for generating url b/w two location
    private String getUrl(LatLng origin, LatLng dest) {


        //  String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters + "&key=" + MY_API_KEY
        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;


        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web SocketService
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

        // Building the url to the web SocketService
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters
                + "&key=" + "AIzaSyApe8T3JMiqj9OgEbqd--zTBfl3fibPeEs";


        return url;
    }
    //end of method for generating url b/w two location


    //method for find route b/w two lacation
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web SocketService
            String data = "";

            try {
                // Fetching the data from web SocketService
                data = downloadUrl(url[0]);
                Log.e("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);


        }
    }
    //end of method for find route b/w two lacation


    //method for get route b/w two places
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
    //method for get route b/w two places


    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.d("ParserTask", jsonData[0].toString());
                DirectionsJSONParser parser = new DirectionsJSONParser();
                Log.d("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.d("ParserTask", "Executing routes");
                Log.d("ParserTask", routes.toString());

            } catch (Exception e) {
                Log.d("ParserTask", e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            //  ArrayList<LatLng> points;
            lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(5);
                lineOptions.color(Color.BLUE);
                Log.d("onPostExecute", "onPostExecute lineoptions decoded");

            }
            Log.d("lineoption", String.valueOf(lineOptions));
            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                mMap.addPolyline(lineOptions);
            } else {
                Log.d("onPostExecute", "without Polylines drawn");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGoogleApiClient.disconnect();
        mGoogleApiClient2.disconnect();
        userSession.setSocketConnection(false);
    }





    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to Request the missing permissions, and then overriding
//               public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                                                      int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback, Looper.myLooper());
        Log.d(TAG, "Location update started ..............: ");
    }


    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, (com.google.android.gms.location.LocationListener) this);
        Log.d(TAG, "Location update stopped .......................");
    }

    public void rotateMarker(final Marker marker, final float toRotation, final float st) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float startRotation = st;
        final long duration = 1555;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);

                float rot = t * toRotation + (1 - t) * startRotation;

                marker.setRotation(-rot > 180 ? rot / 2 : rot);
                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
    }


    public void animateMarker(final LatLng toPosition, final boolean hideMarke) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mMap.getProjection();
        Point startPoint = proj.toScreenLocation(source.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 5000;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                source.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarke) {
                        source.setVisible(false);
                    } else {
                        source.setVisible(true);
                    }
                }
            }
        });
    }

    private double bearingBetweenLocations(LatLng latLng1, LatLng latLng2) {

        double PI = 3.14159;
        double lat1 = latLng1.latitude * PI / 180;
        double long1 = latLng1.longitude * PI / 180;
        double lat2 = latLng2.latitude * PI / 180;
        double long2 = latLng2.longitude * PI / 180;
        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;

        return brng;
    }



    public void Duty_responce(final String mobile ,String aBoolean){
        ConnectionServer connectionServer = new ConnectionServer();
        connectionServer.requestedMethod("POST");
        connectionServer.set_url(Constants.DUTY_RESPONCE);
        connectionServer.buildParameter("mobile",mobile);
        connectionServer.buildParameter("duty",aBoolean);

        connectionServer.execute(new ConnectionServer.AsyncResponse() {
            @Override
            public void processFinish(String output) {
                Log.e("output", output);
                JsonHelper jsonHelper = new JsonHelper(output);
                if (jsonHelper.isValidJson()) {
                    String response = jsonHelper.GetResult("response");
                    if (response.equals("TRUE")) {


                    }

                }

            }
        });

    }


     @SuppressLint("WrongConstant")
     public void set_initial_duty_status(){

         switch(userSession.get_duty_status()){
             case "ture":

                 dutypannel.setBackground(getResources().getDrawable(R.drawable.top_bar));
                 on.setButtonDrawable(R.mipmap.onoff_foreground);
                 off.setButtonDrawable(new StateListDrawable());
//                 house.setButtonDrawable(new StateListDrawable());
                 car.setVisibility(View.VISIBLE);
                 ontext.setTextColor(ContextCompat.getColor(MainActivity.this, Login_button_white));
                 offtext.setTextColor(ContextCompat.getColor(MainActivity.this, low_text_color));
//                 hometext.setTextColor(ContextCompat.getColor(MainActivity.this, low_text_color));
                 break;

//             case "home":
//                 dutypannel.setBackground(getResources().getDrawable(R.drawable.home_duty));
//                 on.setButtonDrawable(new StateListDrawable());
//                 off.setButtonDrawable(new StateListDrawable());
//                 house.setButtonDrawable(R.mipmap.onoff_foreground);
//                 hometext.setTextColor(ContextCompat.getColor(MainActivity.this, Login_button_white));
//                 offtext.setTextColor(ContextCompat.getColor(MainActivity.this, low_text_color));
//                 ontext.setTextColor(ContextCompat.getColor(MainActivity.this, low_text_color));
//                 car.setVisibility(View.VISIBLE);
//                 break;

             default:
                 dutypannel.setBackground(this.getResources().getDrawable(R.drawable.off_duty));
                 on.setButtonDrawable(new StateListDrawable());
                 off.setButtonDrawable(R.mipmap.onoff_foreground);
//                 house.setButtonDrawable(new StateListDrawable());
                 offtext.setTextColor(ContextCompat.getColor(MainActivity.this, Login_button_white));
                 ontext.setTextColor(ContextCompat.getColor(MainActivity.this, Login_button_white));
//                 hometext.setTextColor(ContextCompat.getColor(MainActivity.this, Login_button_white));
                 car.setVisibility(View.INVISIBLE);
                 break;
         }
     }


     public void  setbooking()
     {
         if(userSession.getbookingstatus())
         {
             delivery.setVisibility(View.VISIBLE);
             navigatepannel.setVisibility(View.VISIBLE);
             topbar.setVisibility(View.GONE);
             bottom_menu.setVisibility(View.GONE);
             incentivepannel.setVisibility( View.GONE);
             clientaddress.setText(userSession.getClientaddress());
             Calendar  calander = Calendar.getInstance();
             SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
             String currentTime = simpleDateFormat.format(calander.getTime());
             time.setText(currentTime);
             clientLocated.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     OTPDialog();
                 }
             });
             if(userSession.getLocationStatus())
             {
                 Geocoder geocoder=new Geocoder(this);
                 List<Address> address = null;
                 String clientaddress1;
                 try {
                     switch (userSession.getBookingtype())
                     {
                         case "daily":
                             address = geocoder.getFromLocation(Double.parseDouble(userSession.getdroplat()),Double.parseDouble(userSession.getdroplong()), 1);
                              clientaddress1 = address.get(0).getAddressLine(0);
                             addresstype.setText("Drop Address");
                             clientaddress.setText(clientaddress1);
                             trippannel.setVisibility(View.VISIBLE);
                             navigatepannel.setVisibility(View.GONE);
                             break;
                         case "rental":
//                             clientaddress1  = address.get(0).getAddressLine(0);
//                             addresstype.setText("Drop Address");
//                             clientaddress.setText(clientaddress1);
                             trippannel.setVisibility(View.VISIBLE);
                             navigatepannel.setVisibility(View.GONE);
                              break;
                         case "OutStation":
                             address = geocoder.getFromLocation(Double.parseDouble(userSession.getdroplat()),Double.parseDouble(userSession.getdroplong()), 1);
                              clientaddress1 = address.get(0).getAddressLine(0);
                             addresstype.setText("Drop Address");
                             clientaddress.setText(clientaddress1);
                             trippannel.setVisibility(View.VISIBLE);
                             navigatepannel.setVisibility(View.GONE);
                             break;
                     }
                        } catch (IOException e) {
                     e.printStackTrace();
                 }
//                  clientaddress1 = address.get(0).getAddressLine(0);
//                 addresstype.setText("Drop Address");
//                 clientaddress.setText(clientaddress1);
//                 trippannel.setVisibility(View.VISIBLE);
//                 navigatepannel.setVisibility(View.GONE);
                 endtrip.setOnClickListener(new View.OnClickListener() {
                     @Override
                     public void onClick(View view) {
                         endtrip();
                     }
                 });
             }
         }
     }


     public void OTPDialog()
     {
         RequestDialog requestDialog= new RequestDialog(MainActivity.this);
         requestDialog.show();
     }

    public void endtrip()
    {
        EndtripDialog endtripDialog= new EndtripDialog(MainActivity.this);
        endtripDialog.show();
    }

    public void setIncentivepannel()
    {
    bookings.setText(userSession.gettotalbooking().get("booking"));
    total.setText("₹"+userSession.gettotalbooking().get("total"));
    operatorbill.setText("₹"+userSession.gettotalbooking().get("operater"));
    }
}
