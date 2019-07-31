package com.angadi.myapplication.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.angadi.myapplication.Custum.GPSTracker;
import com.angadi.myapplication.Custum.WeatherHttpClient;
import com.angadi.myapplication.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

   // ProgressBar loader;
   // Button ButtonCurrentLocation;
    Typeface weatherFont;
    LocationManager locationManager;

    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    boolean GpsStatus;
    GPSTracker gps;
    double latitude;
    ProgressDialog progressDialog;
    double longitude;

    @BindView(R.id.loader) ProgressBar loader;
    @BindView(R.id.ButtonCurrentLocation) Button ButtonCurrentLocation;
    @BindView(R.id.city_field) TextView cityField;
    @BindView(R.id.updated_field) TextView updatedField;
    @BindView(R.id.details_field) TextView detailsField;
    @BindView(R.id.current_temperature_field) TextView currentTemperatureField;
    @BindView(R.id.humidity_field) TextView humidity_field;
    @BindView(R.id.pressure_field) TextView pressure_field;
    @BindView(R.id.weather_icon) TextView weatherIcon;
    @BindView(R.id.TextviewMinTemp) TextView TextviewMinTemp;
    @BindView(R.id.TextviewMaxTemp) TextView TextviewMaxTemp;
    @BindView(R.id.RootLayout)
    ScrollView RootLayout;


    String city = "";
    /* Please Put your API KEY here */
    String OPEN_WEATHER_MAP_API = "336197969fad306250735ffe1c2cac9e";
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorstatusbar));
        }

        progressDialog = new ProgressDialog( this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);




        DateFormat df = new SimpleDateFormat("HH:MM:SS");
        String date = df.format(Calendar.getInstance().getTime());

        try {
            String string1 = "04:00:00";
            Date time1 = new SimpleDateFormat("HH:mm:ss").parse(string1);
            Calendar calendar1 = Calendar.getInstance();
            calendar1.setTime(time1);

            String string2 = "16:00:00";
            Date time2 = new SimpleDateFormat("HH:mm:ss").parse(string2);
            Calendar calendar2 = Calendar.getInstance();
            calendar2.setTime(time2);
            calendar2.add(Calendar.DATE, 1);

            String someRandomTime = "01:00:00";
            Date d = new SimpleDateFormat("HH:mm:ss").parse(date);
            Calendar calendar3 = Calendar.getInstance();
            calendar3.setTime(d);
            calendar3.add(Calendar.DATE, 1);

            Date x = calendar3.getTime();
            if (x.after(calendar1.getTime()) && x.before(calendar2.getTime()))
            {
                //checkes whether the current time is between 14:49:00 and 20:11:13.

            }
            else
            {
                Log.e("false--->","false");
                RootLayout.setBackgroundResource(R.drawable.backgroung_night);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }



        weatherFont = Typeface.createFromAsset(getAssets(), "fonts/weathericons-regular-webfont.ttf");
        weatherIcon.setTypeface(weatherFont);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        }
        else {

            GPSStatus();
            gps = new GPSTracker(this);


            if (GpsStatus == true)
            {

                latitude = gps.getLatitude();
                longitude = gps.getLongitude();

                Log.e("Lat-->", String.valueOf(latitude));
                Log.e("Lng-->", String.valueOf(longitude));

                city = "lat="+latitude+"&"+"lon="+longitude;
                taskLoadUp(city);



            } else {


                displayLocationSettingsRequest(this);
            }
        }
    }


    public void taskLoadUp(String query) {
        if (WeatherHttpClient.isNetworkAvailable(getApplicationContext())) {
            DownloadWeather task = new DownloadWeather();
            task.execute(query);
        } else {
            Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
        }
    }



    class DownloadWeather extends AsyncTask< String, Void, String > {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
          //  loader.setVisibility(View.VISIBLE);

            progressDialog.show();


        }
        public String doInBackground(String...args)
        {
            String s = args[0];
            String xml = WeatherHttpClient.excuteGet("http://api.openweathermap.org/data/2.5/weather?" + s +
                    "&units=metric&appid=" + OPEN_WEATHER_MAP_API);

            return xml;
        }
        @Override
        public void onPostExecute(String xml)
        {

            try {
                if (xml != null && xml.length()>0)
                {
                    JSONObject json = new JSONObject(xml);
                    if (json != null) {
                        JSONObject details = json.getJSONArray("weather").getJSONObject(0);
                        JSONObject main = json.getJSONObject("main");
                        DateFormat df = DateFormat.getDateTimeInstance();

                        cityField.setText(json.getString("name").toUpperCase(Locale.US) + ", " + json.getJSONObject("sys").getString("country"));
                        detailsField.setText(details.getString("description").toUpperCase(Locale.US));
                        currentTemperatureField.setText(String.format("%.2f", main.getDouble("temp")) + "°");
                        TextviewMinTemp.setText("Min: " + String.format("%.2f", main.getDouble("temp_min")) + "°");
                        TextviewMaxTemp.setText("Max: " + String.format("%.2f", main.getDouble("temp_max")) + "°");
                        humidity_field.setText("Humidity: " + main.getString("humidity") + "%");

                        pressure_field.setText("Pressure: " + main.getString("pressure") + " hPa");
                        updatedField.setText(df.format(new Date(json.getLong("dt") * 1000)));


                        weatherIcon.setText(Html.fromHtml(WeatherHttpClient.setWeatherIcon(details.getInt("id"),
                                json.getJSONObject("sys").getLong("sunrise") * 1000,
                                json.getJSONObject("sys").getLong("sunset") * 1000)));

                        // loader.setVisibility(View.GONE);

                        progressDialog.hide();

                        ButtonCurrentLocation.setVisibility(View.VISIBLE);

                    }
                }

            } catch (JSONException e) {
                Toast.makeText(getApplicationContext(), "Error, Check City", Toast.LENGTH_SHORT).show();
               // loader.setVisibility(View.GONE);
                progressDialog.hide();
            }


        }



    }


    public void GPSStatus() {
        locationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);
        GpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000 / 2);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // Log.i(TAG, "All location settings are satisfied.");
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Log.i(TAG, "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            // Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the

                    // contacts-related task you need to do.

                    gps = new GPSTracker(this);

                    // Check if GPS enabled
                    if (gps.canGetLocation())
                    {

                        double latitude = gps.getLatitude();
                        double longitude = gps.getLongitude();

                        city = "lat="+latitude+"&"+"lon="+longitude;
                        ButtonCurrentLocation.setVisibility(View.VISIBLE);

                        taskLoadUp(city);

                        // \n is for new line
                      //  Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
                    } else {
                        // Can't get location.
                        // GPS or network is not enabled.
                        // Ask user to enable GPS/network in settings.
                        gps.showSettingsAlert();
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                   // Toast.makeText(this, "You need to grant permission", Toast.LENGTH_SHORT).show();
                  //  loader.setVisibility(View.GONE);
                    progressDialog.hide();

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                    alertDialog.setMessage("Do you want to quit?");
                    alertDialog.setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    dialog.cancel();
                                    finish();
                                }
                            });
                    alertDialog.setNegativeButton("No",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    dialog.cancel();
                                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

                                    }
                                }
                            });
                    alertDialog.show();
                }
                return;
            }
        }
    }

    @OnClick(R.id.ButtonCurrentLocation)
    public void setButtonCurrentLocation()
    {


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        }
        else {

            GPSStatus();
            gps = new GPSTracker(this);


            if (GpsStatus == true) {


                latitude = gps.getLatitude();
                longitude = gps.getLongitude();

                Log.e("Lat-->", String.valueOf(latitude));
                Log.e("Lng-->", String.valueOf(longitude));

                city = "lat="+latitude+"&"+"lon="+longitude;
                taskLoadUp(city);

            } else {

                displayLocationSettingsRequest(this);
            }
        }


    }





}