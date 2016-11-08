package com.example.janakeblomqvist.otrip;

import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.GpsStatus.NmeaListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextClock;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
//import android.hardware.SensorEventListener;
//import android.hardware.SensorManager;

public class MainActivity extends AppCompatActivity implements LocationListener, OnClickListener, Listener, NmeaListener {

    File tracking;
    private Compass compass;
    LocationManager mLocationManager;
    TextView margin;
    TextView sats;
    TextView nmea;
    TextView tvHeading;
    Button counter1;
    Button counter2;
    Button rallytrip;

    CheckBox fixed;
    Chronometer mChronometer;
    TextClock digital;
    TableRow row;
    TableRow row2;
    TableRow rallyview;
    TableRow row3;

    String mLocationProvider;
    int counter = 0;
    int upd;
    float mTripDistance = 0.0f;
    Location mCurrentLocation;
    Location mStartPoint1 = new Location("");
    Location mStartPoint2 = new Location("");
    Location mTripPosition = new Location("");
    FileOutputStream outputStream;
    SavedFragment where;
    int nmeaLines = 0;
    boolean showNmea = false;
    boolean stopped= true;

    // define the display assembly compass picture
    private ImageView image;

    // record the compass picture angle turned
    private float currentDegree = 0f;

    float[] mGravity = null;
    float[] mGeomagnetic = null;
    float[] mRotationMatrixA = new float[16];
    float[] mRotationMatrixB = new float[16];
    HeadingFilter headings = new HeadingFilter();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        margin = (TextView) findViewById(R.id.textView1);
        nmea = (TextView) findViewById(R.id.textView3);
        //       compass = (ImageView) findViewById(R.id.imageViewCompass);
        tvHeading = (TextView) findViewById(R.id.tvHeading);
        sats = (TextView) findViewById(R.id.textView2);
        counter1 = (Button) findViewById(R.id.button1);
        counter2 = (Button) findViewById(R.id.Button01);
        rallytrip = (Button) findViewById(R.id.rallytrip);
        row = (TableRow) findViewById(R.id.row);
        row2 = (TableRow) findViewById(R.id.row2);
        row3 = (TableRow) findViewById(R.id.row3);
        rallyview = (TableRow) findViewById(R.id.rallytripvew);
        fixed = (CheckBox) findViewById(R.id.Fixed);
        digital = (TextClock) findViewById(R.id.textClock);
        digital.setFormat24Hour ("HH:mm:ss");
        mTripDistance = 0.0f;
        mTripPosition = null;

        compass = new Compass(this);
        compass.arrowView = (ImageView) findViewById(R.id.imageViewCompass);

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);

        mLocationProvider = mLocationManager.getBestProvider(criteria, false);
        mChronometer = (Chronometer) findViewById(R.id.chronometer);

        counter1.setOnClickListener(this);
        counter2.setOnClickListener(this);
        rallytrip.setOnClickListener(this);

        rallytrip.setTextColor(Color.BLUE);
        /*
        {
        counter1.setBackgroundColor(Color.BLACK);
        counter2.setBackgroundColor(Color.BLACK);
        rallytrip.setBackgroundColor(Color.BLACK);
        tvHeading.setBackgroundColor(Color.BLACK);
        compass.setBackgroundColor(Color.BLACK);
        counter1.setTextColor(Color.GRAY);
        rallytrip.setTextColor(Color.BLUE);
        tvHeading.setTextColor(Color.GRAY);
        counter2.setTextColor(Color.GRAY);
        mChronometer.setBackgroundColor(Color.BLACK);
        mChronometer.setTextColor(Color.GRAY);
        mChronometer.setOnClickListener(this);
        nmea.setOnClickListener(this);
        row.setBackgroundColor(Color.BLACK);
        row2.setBackgroundColor(Color.BLACK);
        row3.setBackgroundColor(Color.BLACK);
        rallyview.setBackgroundColor(Color.BLACK);
        digital.setBackgroundColor(Color.BLACK);
        digital.setTextColor(Color.GREEN);
        mChronometer.setTextColor(Color.GRAY);
        digital.setTextColor(Color.GRAY);
        margin.setBackgroundColor(Color.BLACK);
        margin.setTextColor(Color.GRAY);
        sats.setBackgroundColor(Color.BLACK);
        sats.setTextColor(Color.GRAY);
        }
     */


        if ( mLocationProvider != null){

            mCurrentLocation = mLocationManager.getLastKnownLocation(mLocationProvider);
             mLocationManager.requestLocationUpdates(mLocationProvider, 0, 0.0f, this);
        }

        mLocationManager.addGpsStatusListener(this);
        mLocationManager.addNmeaListener(this);

        fixed.setChecked(false);
        //NmeaListener

        // find the retained fragment on activity restarts
        FragmentManager fm = getFragmentManager();

        where = (SavedFragment) fm.findFragmentByTag("Loc1");
        // create the fragment and data the first time
        if (where == null) {
            Log.d("my", "onCreate() No saved state available");
            // add the fragment
            where = new SavedFragment();
            fm.beginTransaction().add(where, "Loc1").commit();
            // load the data from the web
            where.setData(mStartPoint1);
            fm.executePendingTransactions();
        } else {
            Log.d("my", "onCreate() Saved state available");
            mStartPoint1 = where.getData();
        }

        where = (SavedFragment) fm.findFragmentByTag("Loc2");
        // create the fragment and data the first time
        if (where == null) {
            Log.d("my", "onCreate() No saved state available");
            // add the fragment
            where = new SavedFragment();
            fm.beginTransaction().add(where, "Loc2").commit();
            // load the data from the web
            where.setData(mStartPoint2);
            fm.executePendingTransactions();
        } else {
            Log.d("my", "onCreate() Saved state available");
            mStartPoint2 = where.getData();
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        compass.stop();

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("my", "onResume");

        compass.start();

    }


    @Override
    protected void onPause() {
        super.onPause();

        compass.stop();
/*
        FragmentManager fm = getFragmentManager();
        where = (SavedFragment) fm.findFragmentByTag("Loc1");
        if (where == null) {
            Log.d("my", "onDestroy() No saved state available");
            where = new SavedFragment();
            fm.beginTransaction().add(where, "Loc1").commit();
            where.setData(mStartPoint1);
            fm.executePendingTransactions();
        }else {
            fm.beginTransaction().add(where, "Loc1").commit();
            where.setData(mStartPoint1);
            fm.executePendingTransactions();
        }
        where = (SavedFragment) fm.findFragmentByTag("Loc2");
        if (where == null) {
            Log.d("my", "onDestroy() No saved state available");
            where = new SavedFragment();
            fm.beginTransaction().add(where, "Loc2").commit();
            where.setData(mStartPoint2);
            fm.executePendingTransactions();
        }else {
            fm.beginTransaction().add(where, "Loc2").commit();
            where.setData(mStartPoint2);
            fm.executePendingTransactions();
        }
        Log.d("my", "onDestroy() Saved Positions");
*/
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        counter++;
        showupdate();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onNmeaReceived(long timestamp, String str) {
        if (showNmea) {
            if (nmeaLines > 10) {   // 10 is roughly the number of lines that fits the textfield
                nmea.setText(str);
                nmeaLines = 1;
            } else {
                nmea.setText(nmea.getText() + str);
                nmeaLines++;
            }
        }

    }

    @Override
    public void onGpsStatusChanged(int event) {
        upd++;
        GpsStatus status = mLocationManager.getGpsStatus(null);

        switch (event) {
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                sats.setText(" GPS Status: First fix after" + status.getTimeToFirstFix());
                fixed.setChecked(true);
                fixed.setTextColor(Color.GREEN);
                break;
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                Iterable<GpsSatellite> list = new ArrayList<GpsSatellite>();
                int count = 0;
                list = status.getSatellites();
                String gpses = "";
                for (GpsSatellite sat : list) {
                    if (sat.usedInFix())
                        gpses += sat.getPrn() + ",";
                    else
                        gpses += "(" + sat.getPrn() + "),";
                    count++;
                }
                if (mCurrentLocation != null)
                    sats.setText("" + count + " Sat's " + " upd: " + upd + " Pos N:" + mCurrentLocation.getLatitude() + " E:" + mCurrentLocation.getLongitude());
                else
                    sats.setText("" + count + " Sat's " + " upd: " + upd);
                break;
            case GpsStatus.GPS_EVENT_STARTED:
                sats.setText(" GPS Status: Started");
                break;
            case GpsStatus.GPS_EVENT_STOPPED:
                sats.setText(" GPS Status: Stopped");
                break;

        }

    }

    @Override
    public void onClick(View v) {
        Log.d("my", "onClick()");
        if(v ==  counter1){
            mStartPoint1 = mCurrentLocation;
            counter1.setText("Wait..");
        }else if(v ==  counter2){
            mStartPoint2 = mCurrentLocation;
            counter2.setText("Wait..");
        } else if (v == rallytrip) {
            mTripPosition = mCurrentLocation;
            mTripDistance = 0.0f;
            rallytrip.setText("Wait..");
            Log.d("my", "onClick() rallytrip was clicked");
        }else if(v ==  nmea){
//            Log.d("my", "onClick() showNmea was clicked");
            showNmea = !showNmea;
            if (!showNmea) {
                nmea.setText(nmea.getText() + "Logging is off");
//                Log.d("my", "onClick() showNmea is off");
            }else{
                nmea.setText("");
//                Log.d("my", "onClick() showNmea is on");
            }
        }else if(v ==  mChronometer){
            if ( stopped ) {
                mChronometer.setBase(SystemClock.elapsedRealtime());
                mChronometer.start();
                mChronometer.setTextColor(Color.GREEN);
                stopped = false;
            }else{
                mChronometer.stop();
                stopped = true;
                mChronometer.setTextColor(Color.YELLOW);
            }

        }
        showupdate();

    }
    private void showupdate() {
    /*
        counter1.setBackgroundColor(Color.BLACK);

        counter2.setBackgroundColor(Color.BLACK);
        counter1.setTextColor( Color.GRAY );
        counter2.setTextColor( Color.GRAY );
     */
        if (mCurrentLocation != null){
            float acc = mCurrentLocation.getAccuracy();
            if (mStartPoint1 != null && mCurrentLocation != null){
                float dist = mStartPoint1.distanceTo(mCurrentLocation);
                if (dist <= 6768000.0f) {
                    counter1.setText("" + String.format("%6.0f", dist));
//                    kurs1.setText(" "+mStartPoint1.bearingTo(mCurrentLocation));
                }else {
                    counter1.setText("--");
//                    kurs1.setText(" - ");
                }
            }
            if (mStartPoint2 != null && mCurrentLocation != null){
                float dist = mStartPoint2.distanceTo(mCurrentLocation);
                if (dist <= 6768000.0f) {
                    counter2.setText("" + String.format("%6.0f", dist));
//                    kurs2.setText(" "+mStartPoint1.bearingTo(mCurrentLocation));
                }else {
                    counter2.setText("--");
//                    kurs2.setText(" - ");
                }
            }
            if (mTripPosition != null && mCurrentLocation != null) {
                float dist = mTripPosition.distanceTo(mCurrentLocation);
                if (dist > 5.0) {
                    mTripDistance += dist;
                    mTripPosition = mCurrentLocation;
                }
                rallytrip.setText("" + String.format("%6.0f", mTripDistance));

            }
            margin.setText("Nogrannhet: " + acc + "  Kurs: " + mCurrentLocation.getBearing() + "  Hast: " + mCurrentLocation.getSpeed() + " m/s");
            if ( acc < 10.0f){
                counter1.setTextColor( Color.GREEN );
                counter2.setTextColor( Color.GREEN );
            }else if ( acc < 50.0f){
                counter1.setTextColor(Color.YELLOW );
                counter2.setTextColor( Color.YELLOW );
            }else{
                counter1.setTextColor( Color.RED );
                counter2.setTextColor( Color.RED );
            }
        }
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
    }

}
