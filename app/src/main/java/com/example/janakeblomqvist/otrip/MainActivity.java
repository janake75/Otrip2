package com.example.janakeblomqvist.otrip;

import java.util.ArrayList;
import java.io.FileOutputStream;
import java.io.File;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.GpsStatus.Listener;
import android.location.GpsStatus.NmeaListener;
import android.location.LocationListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.location.LocationManager;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;
import android.graphics.Color;
import android.location.Criteria;
import android.content.Context;
import android.util.Log;
import android.widget.TextClock;  // Required to create an digital clock
import android.widget.Chronometer;
import android.os.SystemClock;
import android.hardware.Sensor;
import android.app.FragmentManager;
import android.hardware.SensorEvent;
//import android.hardware.SensorEventListener;
//import android.hardware.SensorManager;

public class MainActivity extends AppCompatActivity implements LocationListener, OnClickListener, Listener, NmeaListener {

    File tracking;

    LocationManager mLocationManager;
//    TextView location;
    TextView margin;
    TextView sats;
    TextView nmea;
    TextView kurs1;
    TextView kurs2;
    Button counter1;
    Button counter2;
    Location mCurrentLocation;
    String mLocationProvider;
    int counter = 0;
    int upd;
    Location mStartPoint1 = new Location("");
    Location mStartPoint2 = new Location("");
    FileOutputStream outputStream;
    Chronometer mChronometer;
    TextClock digital;
    TableRow row;
    TableRow row2;
    TableRow row3;
    SavedFragment where;
    int nmeaLines = 0;
    boolean showNmea = false;
    boolean stopped= true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        margin = (TextView) findViewById(R.id.textView1);
        nmea = (TextView) findViewById(R.id.textView3);
        kurs1 = (TextView) findViewById(R.id.textView6);
        kurs2 = (TextView) findViewById(R.id.textView7);
        sats = (TextView) findViewById(R.id.textView2);
        counter1 = (Button) findViewById(R.id.button1);
        counter2 = (Button) findViewById(R.id.Button01);
        row = (TableRow) findViewById(R.id.row);
        row2 = (TableRow) findViewById(R.id.row2);
        row3 = (TableRow) findViewById(R.id.row3);
        digital = (TextClock) findViewById(R.id.textClock);
        digital.setFormat24Hour ("HH:mm:ss");


        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);

        mLocationProvider = mLocationManager.getBestProvider(criteria, false);
        mChronometer = (Chronometer) findViewById(R.id.chronometer);

        counter1.setOnClickListener(this);
        counter2.setOnClickListener(this);
        counter1.setBackgroundColor(Color.BLACK);
        kurs1.setBackgroundColor(Color.BLACK);
        kurs2.setBackgroundColor(Color.BLACK);
        counter2.setBackgroundColor(Color.BLACK);
        counter1.setTextColor(Color.GRAY);
        kurs1.setTextColor(Color.GRAY);
        kurs2.setTextColor(Color.GRAY);
        counter2.setTextColor(Color.GRAY);
        mChronometer.setBackgroundColor(Color.BLACK);
        mChronometer.setTextColor(Color.GRAY);
        mChronometer.setOnClickListener(this);
        nmea.setOnClickListener(this);
        row.setBackgroundColor(Color.BLACK);
        row2.setBackgroundColor(Color.BLACK);
        row3.setBackgroundColor(Color.BLACK);
        digital.setBackgroundColor(Color.BLACK);
        digital.setTextColor(Color.GREEN);
        mChronometer.setTextColor(Color.GRAY);
        digital.setTextColor(Color.GRAY);
        margin.setBackgroundColor(Color.BLACK);
        margin.setTextColor(Color.GRAY);
        sats.setBackgroundColor(Color.BLACK);
        sats.setTextColor(Color.GRAY);

        // TextView that will tell the user what degree is he heading
 //       tvHeading = (TextView) findViewById(R.id.textView2);

        // initialize your android device sensor capabilities
 //       mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        if ( mLocationProvider != null){

            mCurrentLocation = mLocationManager.getLastKnownLocation(mLocationProvider);
             mLocationManager.requestLocationUpdates(mLocationProvider, 0, 0.0f, this);
        }

        mLocationManager.addGpsStatusListener(this);
        mLocationManager.addNmeaListener(this);
        //NmeaListener

        // find the retained fragment on activity restarts
        FragmentManager fm = getFragmentManager();
        where = (SavedFragment) fm.findFragmentByTag("plats");

        // create the fragment and data the first time
        if (where == null) {
            Log.d("my", "onCreate() No saved state available");
            // add the fragment
            where = new SavedFragment();
            fm.beginTransaction().add(where, "plats").commit();
            // load the data from the web
            where.setData(mStartPoint1);
        }else
            Log.d("my", "onCreate() Saved state available");

        mStartPoint1 = where.getData();

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        // store the data in the fragment
        where.setData(mStartPoint1);
        Log.d("my", "onDestroy() Saved Start1");

    }

    /*
    View.OnClickListener mStartListener = new OnClickListener() {

        public void onClick(View v) {
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
    };
*/
    @Override
    public void onGpsStatusChanged(int event) {
        upd++;
        GpsStatus status = mLocationManager.getGpsStatus(null);
        switch( event ){
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                sats.setText(" GPS Status: First fix after" + status.getTimeToFirstFix()	);
                break;
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                Iterable<GpsSatellite> list = new ArrayList<GpsSatellite>();
                int count = 0;
                list = status.getSatellites();
                String gpses = "";
                for ( GpsSatellite sat : list){
                    if (sat.usedInFix())
                        gpses += sat.getPrn() + ",";
                    else
                        gpses += "(" + sat.getPrn() + "),";
                    count++;
                }
                sats.setText(  "" + count + " Sat's " + " upd: " + upd + " Pos N:" + mCurrentLocation.getLatitude() + " E:" + mCurrentLocation.getLongitude() );
                break;
            case GpsStatus.GPS_EVENT_STARTED:
                sats.setText(" GPS Status: Started"	);
                break;
            case GpsStatus.GPS_EVENT_STOPPED:
                sats.setText(" GPS Status: Stopped"	);
                break;

        }

    }
    @Override
    protected void onResume() {
        super.onResume();
     }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
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
            if (nmeaLines > 10) {   // 200 is roughly the number of lines that fits the textfield
                nmea.setText(str);
                nmeaLines = 1;
            } else {
                nmea.setText(nmea.getText() + str);
                nmeaLines++;
            }
        }

    }

    @Override
    public void onClick(View v) {
//        Log.d("my", "onClick()");
        if(v ==  counter1){
            mStartPoint1 = mCurrentLocation;
            counter1.setText("Waiting..");
        }else if(v ==  counter2){
            mStartPoint2 = mCurrentLocation;
            counter2.setText("Waiting..");
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
        counter1.setBackgroundColor(Color.BLACK);
        counter2.setBackgroundColor(Color.BLACK);
        counter1.setTextColor( Color.GRAY );
        counter2.setTextColor( Color.GRAY );
        if (mCurrentLocation != null){
            if (mStartPoint1 != null && mCurrentLocation != null){
                float dist = mStartPoint1.distanceTo(mCurrentLocation);
                if (dist <= 6768000.0f) {
                    counter1.setText("" + String.format("%6.0f", dist));
                    kurs1.setText(" "+mStartPoint1.bearingTo(mCurrentLocation));
                }else {
                    counter1.setText("--");
                    kurs1.setText(" - ");
                }
            }
            if (mStartPoint2 != null && mCurrentLocation != null){
                float dist = mStartPoint2.distanceTo(mCurrentLocation);
                if (dist <= 6768000.0f) {
                    counter2.setText("" + String.format("%6.0f", dist));
                    kurs2.setText(" "+mStartPoint1.bearingTo(mCurrentLocation));
                }else {
                    counter2.setText("--");
                    kurs2.setText(" - ");
                }
            }
            float acc = mCurrentLocation.getAccuracy();
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
        // TODO Auto-generated method stub
        super.onRestoreInstanceState(savedInstanceState);
        //mStartPoint1 = new Location("");
        mStartPoint2 = new Location("");
        //mStartPoint1.setLatitude(savedInstanceState.getDouble("StartLat1"));
        //mStartPoint1.setLongitude(savedInstanceState.getDouble("StartLong1"));
        mStartPoint2.setLatitude(savedInstanceState.getDouble("StartLat2"));
        mStartPoint2.setLongitude(savedInstanceState.getDouble("StartLong2"));
 //       nmeat.setText(savedInstanceState.getString("message"));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
        if( outState != null){
            if ( mStartPoint1 != null ){
                outState.putDouble("StartLat1",mStartPoint1.getLatitude());
                outState.putDouble("StartLong1",mStartPoint1.getLongitude());
            }
            if( mStartPoint2 != null ){
                outState.putDouble("StartLat2",mStartPoint2.getLatitude());
                outState.putDouble("StartLong2",mStartPoint2.getLatitude());
            }
            outState.putString("message","Reloaded");
        }else {
/*            try {
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
*/
        }
    }


}
