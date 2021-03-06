package com.bydavy.boussole;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;
import android.widget.TextView;
import android.os.Vibrator;

import com.bydavy.boussole.view.CompassView;

public class Boussole extends Activity {

    Location createNewLocation(double longitude, double latitude) {
        Location loc = new Location("");
        loc.setLatitude(latitude);
        loc.setLongitude(longitude);
        return loc;
    }
    final Location targetLocation = createNewLocation(5.7708357d,45.1946000d);

    // Mise en place du vibrator
    Vibrator v;

	//La vue de notre boussole
	private CompassView compassView;
    private TextView distanceTextView;
	
	//Le gestionnaire des capteurs
	private SensorManager sensorManager;
	//Notre capteur de la boussole numerique
	private Sensor sensor;

    private Vibrator vibrator;
	
	//Notre listener sur le capteur de la boussole numerique
	private final SensorEventListener sensorListener = new SensorEventListener() {
		@Override
		public void onSensorChanged(SensorEvent event) {
			updateOrientation(event.values[SensorManager.DATA_X]);
		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};

    // Define a listener that responds to location updates
    private final LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            float dist = location.distanceTo(new Location(targetLocation));
            // on rajoute les vibrations en fonction de la distance
            updateLocation(location.bearingTo(targetLocation));
            updateDistance(dist);
            if (dist >2){
                vibrator.vibrate((long)dist*100);
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public void onProviderEnabled(String provider) {}

        public void onProviderDisabled(String provider) {}
    };

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        vibrator=(Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        //Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(true);
        criteria.setCostAllowed(false);
        criteria.setSpeedRequired(true);

        String provider = locationManager.getBestProvider(criteria, true);
        //provider = LocationManager.NETWORK_PROVIDER;

        // Register the listener with the Location Manager to receive location updates
        try {locationManager.requestLocationUpdates(provider, 10, 0, locationListener);}
        catch(SecurityException e){
            Log.e("ERR", "SecurityException"+e);
        };

        compassView = (CompassView)findViewById(R.id.compassView);
        distanceTextView = (TextView)findViewById(R.id.distancetextView);
        distanceTextView.setTextColor(getResources().getColor(R.color.textColor));
        distanceTextView.setText("distance...");

        //R�cup�ration du gestionnaire de capteurs
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        //Demander au gestionnaire de capteur de nous retourner les capteurs de type boussole
        List<Sensor> sensors =sensorManager.getSensorList(Sensor.TYPE_ORIENTATION);
        //s�il y a plusieurs capteurs de ce type on garde uniquement le premier
        if (sensors.size() > 0) {
        	sensor = sensors.get(0);
        }


    }
    
	//Mettre à jour l'orientation
    protected void updateOrientation(float rotation) {
		compassView.setNorthOrientation(rotation);
	}
    //Mettre à jour la localisation
    protected void updateLocation(float location) { compassView.setMyLocation(location); }
    // Mettre à jour la distance
    protected void updateDistance(float distance) { distanceTextView.setText(""+distance); }

	@Override
    protected void onResume(){
    	super.onResume();
    	//Lier les evenements de la boussole numerique au listener
    	sensorManager.registerListener(sensorListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	@Override
	protected void onStop(){
		super.onStop();
		//Retirer le lien entre le listener et les evenements de la boussole numerique
		sensorManager.unregisterListener(sensorListener);
	}
}