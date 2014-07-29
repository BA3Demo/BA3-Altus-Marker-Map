package com.example.markers;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.microedition.khronos.opengles.GL10;

import us.ba3.me.HaloPulse;
import us.ba3.me.markers.DynamicMarker;
import us.ba3.me.markers.DynamicMarkerMapDelegate;
import us.ba3.me.markers.DynamicMarkerMapInfo;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.opengl.EGLConfig;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends PreferenceActivity implements LocationListener, DynamicMarkerMapDelegate{
	
	static int markerNum = 0;

	boolean move = false;
	boolean firstLoc = true;
	boolean track = false;
	
	String currentMap = "Basic Map";
	
	String maps[][] = new String[3][2];
	
	private Spinner spinner1;
	private Spinner GPS;
	
	static tap mapView;
	
    static FileOutputStream fos = null;
    FileInputStream fis = null;
	
	protected LocationManager locationManager;
	
	protected LocationListener locationListener;
	
	GLSurfaceView mGLView;
	
	static AssetManager assetManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

		RelativeLayout layout = (RelativeLayout) findViewById(R.id.Layout);

		assetManager = getAssets();
		
		maps[0][0] = "Basic Map";
		maps[0][1] = "http://a.tile.openstreetmap.org";
		maps[1][0] = "Satellite";
		maps[1][1] = "http://otile1.mqcdn.com/tiles/1.0.0/sat";
		maps[2][0] = "Road Map";
		maps[2][1] = "http://b.tile.opencyclemap.org/cycle";

		String FILENAME = "markerLocation";
		String marker = "";
		
		boolean fileBad = false;
		try {
			fis = openFileInput(FILENAME);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fileBad = true;
		}
		
		if(!fileBad){
			for(int bite = 0;bite != -1;){
				marker = marker + (char) bite;
				try {
					bite = fis.read();
				} catch (IOException e1) {
					e1.printStackTrace();
					break;
				}
			}
		}
		
		try {
			fos = openFileOutput(FILENAME, MODE_APPEND);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fileBad = true;
		}
		
		mapView = new tap(getApplication());
		mapView.addInternetMap(maps[0][0], maps[0][1], "png");
		
		layout.addView(mapView);
		
		//Add dynamic marker map layer
		
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		addListenerOnSpinnerItemSelection();
		
		Button delAll  = (Button) findViewById(R.id.butdel);
		delAll.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				deleteAll();
			} 
		});
		delAll.bringToFront();
		Button info  = (Button) findViewById(R.id.butinfo);
		info.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				showInfo();
			} 
		});
		info.bringToFront();
		
		DynamicMarkerMapInfo mapInfo = new DynamicMarkerMapInfo();
		mapInfo.name = "Markers";
		mapInfo.zOrder = 2;
		mapInfo.delegate = this;
		mapView.addMapUsingMapInfo(mapInfo);
		
		DynamicMarkerMapInfo location = new DynamicMarkerMapInfo();
		location.name = "Location";
		location.zOrder = 5;
		location.delegate = this;
		mapView.addMapUsingMapInfo(location);
		
		String[] data = marker.split("test");
		for(int i = 1;i < data.length;i++){
			String[] cord = data[i].split(";");
			markerNum++;
			
			DynamicMarker test = new DynamicMarker();
			test.name = "test" + markerNum;
			test.setImage(loadBitmap("bluedot.png"), false);
			test.anchorPoint = new PointF(16,16);
			test.location.longitude = Double.parseDouble(cord[2]);
			test.location.latitude = Double.parseDouble(cord[1]);
			mapView.addDynamicMarkerToMap("Markers", test);
		}

        mGLView = new GLSurfaceView(this);
        mGLView.setRenderer(new ClearRenderer());
		
	}
	
	public static void markPoint(us.ba3.me.Location loc){
        markerNum++;
		
		DynamicMarker test = new DynamicMarker();
		test.name = "test" + markerNum;
		test.setImage(loadBitmap("bluedot.png"), false);
		test.anchorPoint = new PointF(16,16);
		test.location = loc;
		mapView.addDynamicMarkerToMap("Markers", test);
		
		String string = "test" + markerNum + ";" + loc.latitude + ";" + loc.longitude;

		try {
			fos.write(string.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//Helper function to load images
	public static Bitmap loadBitmap(String assetName) {
        InputStream istr = null;
        try {
            istr = assetManager.open(assetName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(istr);
        return bitmap;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
		 
	public void addListenerOnSpinnerItemSelection() {
		spinner1 = (Spinner) findViewById(R.id.map);
		spinner1.setOnItemSelectedListener(new CustomOnItemSelectedListener());
		spinner1.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				// get name of selected item
				TextView view = (TextView)selectedItemView;
				changeMap(view.getText().toString());
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
				
				
			}
		});
		spinner1.bringToFront();
		
		GPS = (Spinner) findViewById(R.id.gps);
		GPS.setSelection(1);
		GPS.setOnItemSelectedListener(new CustomOnItemSelectedListener());
		GPS.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				TextView textView = (TextView)view;
				String gps = textView.getText().toString();
				if(gps.equalsIgnoreCase("GPS Off")){

					mapView.setTrackUp(false);
					gpsOff();
					mapView.removeHaloPulse("beacon");
					mapView.removeDynamicMarkerFromMap("Location", "loc");
					firstLoc = true;
					track = false;
					
				} else if(gps.equalsIgnoreCase("GPS On")){
					
					mapView.setTrackUp(true);
					gpsOn();
					track = false;
					
				} else if(gps.equalsIgnoreCase("GPS and Track On")){
					
					mapView.setTrackUp(true);
					gpsOn();
					track = true;
					
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
				
				
			}
		});
		GPS.bringToFront();
	}
	
	public void changeMap(String view){
		
		String link = null;
		
		boolean png = true;
		
		Log.i("menu", view);
		
		mapView.removeMap(currentMap, false);
		
		for(int i = 0;i < 3;i++){
			if(maps[i][0].equals(view)){
				link = maps[i][1];
				if(i == 1){
					png = false;
				}else{
					png = true;
				}
			}
		}
		
		currentMap = view;
		
		if(png){
			mapView.addInternetMap(view, link, "png");
		}else{
			mapView.addInternetMap(view, link, "jpg");
		}
		
	}
	
	public void gpsOn() {
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
		Log.i("gps", "on");
	}
	
	public void gpsOff() {
		locationManager.removeUpdates(this);
		Log.i("gps", "off");
	}
	
	public void deleteAll() {
		
		mapView.removeMap("Markers", true);
		DynamicMarkerMapInfo mapInfo = new DynamicMarkerMapInfo();
		mapInfo.name = "Markers";
		mapInfo.zOrder = 2;
		mapView.addMapUsingMapInfo(mapInfo);
		
		deleteFile("markerLocation");
	}
	
	public void deleteLast(View view) {
		
	    mapView.removeDynamicMarkerFromMap("Markers", "test" + markerNum);
	    markerNum--;
	    
	}
	
	public void showInfo(){
		
		Info info = new Info();
		info.show(getFragmentManager(), "");
		
	}

	@Override
	public void onLocationChanged(Location l) {
		if(firstLoc){
			DynamicMarker marker = new DynamicMarker();
			marker.name = "loc";
			marker.setImage(loadBitmap("greendot.png"), false);
			marker.anchorPoint = new PointF(16,16);
			marker.location = new us.ba3.me.Location(l.getLatitude(),l.getLongitude());
			mapView.addDynamicMarkerToMap("Location", marker);
			
			HaloPulse beacon = new HaloPulse();
		    beacon.name = "beacon";
		    beacon.location = new us.ba3.me.Location(l.getLatitude(),l.getLongitude());
		    beacon.minRadius = 5;
		    beacon.maxRadius = 75;
		    beacon.animationDuration = 2.5f;
		    beacon.repeatDelay = 0;
		    beacon.fade = true;
		    beacon.fadeDelay = 1;
		    beacon.zOrder = 4;
		    beacon.lineStyle.strokeColor = Color.WHITE;
		    beacon.lineStyle.outlineColor = Color.rgb(0, 0, 255);
		    beacon.lineStyle.outlineWidth = 4;
		    mapView.addHaloPulse(beacon);
		    firstLoc = false;
		    
		    
		}else{
			mapView.setHaloPulseLocation("beacon", new us.ba3.me.Location(l.getLatitude(),l.getLongitude()));
			mapView.setDynamicMarkerLocation("Location", "loc", new us.ba3.me.Location(l.getLatitude(),l.getLongitude()), 0.0);
			if(track){
				us.ba3.me.Location loc = new us.ba3.me.Location(l.getLatitude(),l.getLongitude());
				mapView.setLocation(loc);
				mapView.setCameraOrientation(l.getBearing(), 0, 0, 3);
			}
			mapView.setTrackUp(track);
			mapView.setPanEnabled(!track);
			mapView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
		}
	}
	
	@Override
	public void tapOnMarker(String mapName, String markerName, PointF screenPoint, PointF markerPoint){
		Log.i("marker", "tap");
	}

	@Override
	public void onProviderDisabled(String arg0) {
	}

	@Override
	public void onProviderEnabled(String arg0) {
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
	}

}

class ClearRenderer implements GLSurfaceView.Renderer {
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        // Do nothing special.
    }

    public void onSurfaceChanged(GL10 gl, int w, int h) {
    }

    public void onDrawFrame(GL10 gl) {
    }

	@Override
	public void onSurfaceCreated(GL10 gl,
			javax.microedition.khronos.egl.EGLConfig config) {
		
	}
}
