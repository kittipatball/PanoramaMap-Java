package com.kittipat.kittipatex;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapView;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.core.geometry.CoordinateConversion;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.MarkerSymbol;
import com.esri.core.symbol.PictureMarkerSymbol;
import com.panoramagl.PLImage;
import com.panoramagl.PLManager;
import com.panoramagl.PLSphericalPanorama;
import com.panoramagl.utils.PLUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    @BindView(R.id.imvCurrentPin)ImageView imvCurrentPin;
    @BindView(R.id.imvCompass)ImageView imvCompass;

    PLManager plManager;
    private MapView mMapView;
    private GraphicsLayer graphicsLayer = new GraphicsLayer();
    private PictureMarkerSymbol pmsArrow;
    private Graphic graphicArrow;
    private int idGraphicArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        plManager = new PLManager(this);
        plManager.setContentView(R.layout.activity_main);
        plManager.onCreate();
        ButterKnife.bind(this);
        mMapView = (MapView) findViewById(R.id.map);
//        checkWifi();
        setLayoutMap();
        setLayoutPanorama();
        registerSensor();

        pmsArrow = new PictureMarkerSymbol(MainActivity.this,
                getResources().getDrawable(R.drawable.ic_navigation_black_24dp));

        final double lat = 13.904727;
        final double lon = 100.4476343;
        Point point = new Point(lon, lat);
        String strPoint = CoordinateConversion.pointToDecimalDegrees(point,
                SpatialReference.create(SpatialReference.WKID_WGS84), 7);
        point = CoordinateConversion.decimalDegreesToPoint(strPoint,
                SpatialReference.create(SpatialReference.WKID_WGS84_WEB_MERCATOR_AUXILIARY_SPHERE));
        graphicArrow = new Graphic(point, pmsArrow);
        idGraphicArrow = graphicsLayer.addGraphic(graphicArrow);
        mMapView.addLayer(graphicsLayer);
        mMapView.setOnStatusChangedListener(new OnStatusChangedListener() {
            @Override
            public void onStatusChanged(Object o, STATUS status) {
                if (status == STATUS.LAYER_LOADED) {
                    mMapView.centerAndZoom(lat, lon, 15);
                }
            }
        });


        mMapView.setRotationAngle(0);
        mMapView.setAllowRotationByPinch(true);
        mMapView.enableWrapAround(true);
    }


    @OnClick(R.id.imvCurrentPin)
    protected void clickCurrentPin(View view){
        final double lat = 13.904727;
        final double lon = 100.4476343;
        Point point = new Point(lon, lat);
        mMapView.centerAndZoom(lat,lon,15);
    }

//    private void checkWifi(){
//        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//        if(wifi.disconnect()){
//            Toast.makeText(getApplicationContext(),"กรุณาเชื่อมต่อ Internet",Toast.LENGTH_SHORT).show();
//        }
//    }


    private void setLayoutPanorama() {
        PLSphericalPanorama panorama = new PLSphericalPanorama();
        panorama.getCamera().lookAt(0.0f, 90.0f);
        panorama.setImage(new PLImage(PLUtils.getBitmap(this, R.drawable.streetview1), false));
        plManager.setPanorama(panorama);
    }

    private void registerSensor() {
        SensorManager sensorManager;
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void setLayoutMap() {
        RelativeLayout rl = (RelativeLayout) findViewById(R.id.lyMap);
        rl.getLayoutParams().height = getResolution();
        mMapView.enableWrapAround(true);
        mMapView.setAllowRotationByPinch(true);

    }

    private int getResolution(){
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;
        return height/2;
    }

    private void setDegreePin(SensorEvent event){
        float mLastDegree = 0;
        float degree = Math.round(event.values[0]);
        float diffDegree = Math.abs(mLastDegree - degree);
        if ( diffDegree > 1) {
            mLastDegree = degree;
            try {
                pmsArrow.setAngle((float) ((degree) - mMapView.getRotationAngle()));
                graphicsLayer.updateGraphic(idGraphicArrow, pmsArrow);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        plManager.onResume();
    }

    @Override
    protected void onPause() {
        plManager.onPause();
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        plManager.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ORIENTATION){
            plManager.getPanorama().getCamera().lookAt(this,0.0f,event.values[0]+180);
            setDegreePin(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}

