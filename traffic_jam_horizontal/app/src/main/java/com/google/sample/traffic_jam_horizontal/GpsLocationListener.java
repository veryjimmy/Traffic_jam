package com.google.sample.traffic_jam_horizontal;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2017/3/27.
 */

public class GpsLocationListener implements LocationListener
{
    private final String TAG = "GpsLocationListener";
    private static final long minTime = 2000;           //gps刷新的最小时间（mini sec）
    private static final float minDistance = 10;        //gps刷新的最短距离
    private String pid;
    private List<String> list;                          //储存gps定位数据的list
    private List<String> listName;                      //储存gps定位数据对应名称的list
    private LocationManager locationManager;            //gps定位相关的管理器
    private String provider;                            //最优的gps定位器名称
    private Context context;                            //service

    public GpsLocationListener(Context context)
    {
        this.context = context;

        list = new ArrayList<>();
        listName = new ArrayList<>();
//        listName.add("_id");
        listName.add("Time");
        listName.add("Latitude");
        listName.add("Longitude");
//        listName.add("Speed");

    }
    public String getId()
    {
        return pid;
    }
    public List<String> getGpsData()
    {
        return list;
    }
    public List<String> getGpsDataName()
    {
        return listName;
    }

    //定位刷新器，在service里面使用
    public void getLocation()
    {
        locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context , Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }

        Location location = null;
        List<String> locationList = locationManager.getProviders(true);
        //使用gps定位
        if (locationList.contains(LocationManager.GPS_PROVIDER))
        {
            provider = LocationManager.GPS_PROVIDER;
            location = locationManager.getLastKnownLocation(provider);
            //如果gps無法定位，則使用網絡定位
            if (locationList.contains(LocationManager.NETWORK_PROVIDER) && location == null)
            {
                provider = LocationManager.NETWORK_PROVIDER;
                location = locationManager.getLastKnownLocation(provider);
            }
        }//不存在gps定位，則使用網絡定位
        else if (locationList.contains(LocationManager.NETWORK_PROVIDER))
        {
            provider = LocationManager.NETWORK_PROVIDER;
            location = locationManager.getLastKnownLocation(provider);
        }
        list.clear();
//        list.add(pid);
        String currentTime;
        //HH表示24小时制,如果换成hh表示12小时制
        currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date());
        list.add(currentTime);
        if (location != null)
        {
            list.add(String.valueOf(location.getLatitude()));//緯度
            list.add(String.valueOf(location.getLongitude()));//經度
            //list.add(String.valueOf(location.getSpeed()));
            Log.i(TAG,"latitude:" + location.getLatitude());
            Log.i(TAG,"Longitude:" + location.getLongitude());
        }
        else
        {
            list.add("0");
            list.add("0");
        }
        locationManager.requestLocationUpdates(provider, minTime, minDistance,this );
    }

    @Override
    public void onLocationChanged(Location location)
    {

    }

    @Override
    public void onProviderDisabled(String provider)
    {

    }

    @Override
    public void onProviderEnabled(String provider)
    {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {

    }


}
