package com.google.android.gms.locationTest;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.android.PolyUtil;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;


/**
 * Created by blue rabbit on 2018-01-17.
 */

public class MainActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {
    //implementujemy interfejsy sluzace do obslugi mapy

    private FusedLocationProviderClient mFusedLocationClient;
    TextView Result;
    private LocationRequest request;
    private Marker myMarker;
    List<Marker> markerList;
    private List<Marker> equatorList;
    Spinner spinner;
    private List<Polyline> polyList = new ArrayList<>();
    GoogleMap mGoogleMap;
    double Latitude;
    List<Address> addresses;
    LocationRequest locationRequest;
    FusedLocationProviderApi fusedLocationProviderApi;
    double Longtitude;
    String adress;
    Button MyLocationButton;
    Button AddMarkerButton;
    DirectionsResult result;
    Button ClearButton;
    Button eq;
    Button route;
    GoogleApiClient mGoogleApiClient;
    private GoogleApiClient googleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_activity);
        spinner = (Spinner) findViewById(R.id.spinner);
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.driving, R.layout.support_simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        markerList = new ArrayList<>(); //Lista markerów
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        route = (Button) findViewById(R.id.route);
        MyLocationButton = (Button) findViewById(R.id.addmaker);
        AddMarkerButton = (Button) findViewById(R.id.AddMakerButton); //Laczenie markerow
        ClearButton = (Button) findViewById(R.id.Clear); //Przycisk do czyszczenia mapy
        Result = (TextView) findViewById(R.id.distance); //Wyswietlacz odleglosci
        eq = (Button) findViewById(R.id.eq); //Przycisk do dlusgosci rownika
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map); //ustalamy mape
        mapFragment.getMapAsync(this);

        String[] PERMISSIONS = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}; //pozwolenie dostepu do lokalizacji
        int PERMISSION_ALL = 1;
        request = new LocationRequest();

        if (!hasPermissions(this, PERMISSIONS)) //sprawdzenie pozwolenia dostepu
        {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);  //pobieramy nasza lekalizacje
        MyLocationButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { //ustawiamy nasza lokalizace
                SetMyAddress();

                MyLocationButton.setClickable(false); //wylaczamy przycisk
            }

        });

        AddMarkerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.color(Color.RED); //opcje rysowania lini
                polylineOptions.width(5); // jak wyzej
                for (Marker marker : markerList)
                    polylineOptions.add(marker.getPosition());
                polyList.add(mGoogleMap.addPolyline(polylineOptions)); //rysujemy linie

                double result = 0;
                for (int i = 0; i < markerList.size() - 1; i++)
                    result += DistanceCalculator.calculationByDistance(markerList.get(i).getPosition(), markerList.get(i + 1).getPosition()); //obliczamy odleglosc

                Result.setText((int) result + " km"); //wyswietlamy
            }
        });
        ClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClearMap(mGoogleMap); //czyscimy mape
            }
        });
        eq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng f = new LatLng(0, 0);
                LatLng d = new LatLng(0, 180);
                Result.setText((DistanceCalculator.calculationByDistance(f, d) * 2 + " km")); //obliczamy dlugosc rownika - nie jestem pewna czy o to chodzilo
            }
        });
        route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (markerList.size() < 2) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Add markers!", Toast.LENGTH_SHORT); //krzyk bledu :)
                    toast.show();
                } else {
                    result = RoutePainting();
                    addPolyline(result, mGoogleMap);

                }
                //        addMarkersToMap(result, mGoogleMap);

            }
        });
        getLocation();


    }


    private void getLocation() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(30000);
        FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    private void SetMyAddress() //funkcja ustawiania naszego adresu
    {
        //znow sprawdzamy pozwolenia
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation()                                //pobieramy nasza lokalizacje
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
//jesli sie uda
                        if (location != null) {
                            Latitude = location.getLatitude(); //tworzymy punkt
                            Longtitude = location.getLongitude();
                            Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                            try {
                                addresses = geocoder.getFromLocation(Latitude, Longtitude, 1);
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.wtf("Error", e + "");
                            }

                            setupMarker(mGoogleMap); //i ustawiamy go na mapie


                        } else {
                            Log.wtf("Error", " Null location");

                        }
                    }
                });


    }

//funkcje wymagane przez nasze interfejsy:

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startLocationUpdates();
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if(mLastLocation == null){
            startLocationUpdates();
        }

        if (mLastLocation != null)
        {


            // set your tag
            Log.d("asa", mLastLocation+ "");


        } else {
            Toast.makeText(this, "Location not Detected, Did you turn off your location?", Toast.LENGTH_SHORT).show();
        }

    }
    protected void startLocationUpdates() {
        // Create the location request
        LocationRequest mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(30 * 1000)
                .setFastestInterval(5 * 1000);

        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.getFusedLocationProviderClient( this);
    }
    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(final GoogleMap googleMap)
    {
        this.mGoogleMap = googleMap;
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener()
        {

            @Override
            public void onMapClick(LatLng latLng)
            {

                // tworzymy marker
                if (markerList.size() < 2)
                {
                    Marker OnTouchMarker = mGoogleMap.addMarker(new MarkerOptions().position(latLng).title(latLng.latitude + " : " + latLng.longitude));
                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                    markerList.add(OnTouchMarker);
                    // ustawiamy go w miejscu dotkniecia

                }
                else
                {
                    Toast toast = Toast.makeText(getApplicationContext(), "Use CLEAR map to set new markers", Toast.LENGTH_SHORT); //krzyk bledu :)
                    toast.show();
                }

            }
        });
    }


public List<Address> returnAddress(List<Marker> markerList, int index)
{
    List<Address> adressw = null;
    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
    try
    {
        adressw = geocoder.getFromLocation(markerList.get(index).getPosition().latitude, markerList.get(1).getPosition().longitude, 1);
    }
    catch (IOException e)
    {
        e.printStackTrace();
    }
    return adressw;
}

    private void setupMarker(final GoogleMap map) //ustawiamy marker z tytulem my place
    {
        if(markerList.size() < 2) {
            LatLng lat = new LatLng(Latitude, Longtitude);
            adress = addresses.get(0).getAddressLine(0);
            myMarker = map.addMarker(new MarkerOptions().position(lat).title(adress));
            markerList.add(myMarker); //dodajemy fo do listy Markerow (sluzy do odleglosci i rysowania linii)
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(lat));
        }
        else
        {
            Toast toast = Toast.makeText(getApplicationContext(), "Use CLEAR map to set new markers", Toast.LENGTH_SHORT); //krzyk bledu :)
            toast.show();
        }
    }



    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    private void ClearMap(final GoogleMap map) //czyszczenie mapy
    {
        map.clear();
        polyList.clear();
        Result.setText("Distance");
        markerList.clear();
        MyLocationButton.setClickable(true);

    }
    private GeoApiContext getGeoContext()
    {
        GeoApiContext geoApiContext = new GeoApiContext();
        return geoApiContext.setQueryRateLimit(3).
                setApiKey(getString(R.string.directionsApiKey)).
                setConnectTimeout(1, TimeUnit.SECONDS).
                setReadTimeout(1, TimeUnit.SECONDS).
                setWriteTimeout(1, TimeUnit.SECONDS);
    }

   private DirectionsResult RoutePainting() {
        DateTime now = new DateTime();
       String travelmode = spinner.getSelectedItem().toString();


       try {
           result = DirectionsApi.newRequest(getGeoContext())
           .mode(TravelMode.valueOf(travelmode)).origin(new com.google.maps.model.LatLng(markerList.get(0).getPosition().latitude , markerList.get(0).getPosition().longitude))
           .destination(new com.google.maps.model.LatLng(markerList.get(1).getPosition().latitude , markerList.get(1).getPosition().longitude)).departureTime(now)
           .await();
       } catch (ApiException e) {
           e.printStackTrace();
       } catch (InterruptedException e) {
           e.printStackTrace();
       } catch (IOException e) {
           e.printStackTrace();
       }
       return result;

   }
    private String getEndLocationTitle(DirectionsResult results)
    {
        return  "Time :"+ results.routes[0].legs[0].duration.humanReadable + " Distance :" + results.routes[0].legs[0].distance.humanReadable;
    }
    private void addPolyline(DirectionsResult results, GoogleMap mMap)
    {
        Random generator = new Random();
        List<LatLng> decodedPath = PolyUtil.decode(results.routes[0].overviewPolyline.getEncodedPath());
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.rgb(generator.nextInt(255), generator.nextInt(255), generator.nextInt(255))); //opcje rysowania lini
        polylineOptions.width(10); // jak wyzej
        mMap.addPolyline(polylineOptions.addAll(decodedPath));
    }
    private void addMarkersToMap(DirectionsResult results, GoogleMap mMap) {
        mMap.addMarker(new MarkerOptions().position(new LatLng(results.routes[0].legs[0].startLocation.lat,results.routes[0].legs[0].startLocation.lng)));
        mMap.addMarker(new MarkerOptions().position(new LatLng(results.routes[0].legs[0].endLocation.lat,results.routes[0].legs[0].endLocation.lng)));
    }
}
