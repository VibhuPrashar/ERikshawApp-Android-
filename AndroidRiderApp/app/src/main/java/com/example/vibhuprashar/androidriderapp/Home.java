package com.example.vibhuprashar.androidriderapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.vibhuprashar.androidriderapp.Common.Common;
import com.example.vibhuprashar.androidriderapp.Helper.CustomerInfoWindow;
import com.example.vibhuprashar.androidriderapp.Model.FCMResponse;
import com.example.vibhuprashar.androidriderapp.Model.Notification;
import com.example.vibhuprashar.androidriderapp.Model.Rider;
import com.example.vibhuprashar.androidriderapp.Model.Sender;
import com.example.vibhuprashar.androidriderapp.Model.Token;
import com.example.vibhuprashar.androidriderapp.Remote.IFCMService;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.location.LocationListener;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.maps.android.SphericalUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener {


    SupportMapFragment mapFragment;

    //Location update variables

    private GoogleMap mMap;
    private static final int MY_PERMISSION_REQUEST_CODE = 99;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 1;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    DatabaseReference ref;
    GeoFire geoFire;
    Marker mCurrent;

    //Bottom Sheet
    ImageView imgExpandable;
    BottomSheetRiderFragment mBottomSheet;
    Button btnPickupRequest;

    boolean isDriverFound=false;
    String driverId ="";
    int radius =1;
    int distance =1;
    private static final int LIMIT =3;

    IFCMService mService;


    DatabaseReference driversAvailable;

    PlaceAutocompleteFragment place_location,place_destination;

    String mPlaceLocation,mPlaceDestination;


    AutocompleteFilter typeFilter;


  @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mService = Common.getFCMService();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        //Maps ARea
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



       // Geofire Code


       imgExpandable = (ImageView)findViewById(R.id.imgExpandable);
       /* mBottomSheet = BottomSheetRiderFragment.newInstance("Rider Bottom Sheet");
        imgExpandable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheet.show(getSupportFragmentManager(),mBottomSheet.getTag());
            }
        });*/

        btnPickupRequest = (Button)findViewById(R.id.btnPickupRequest);
        btnPickupRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!isDriverFound)
                    requestPickupHere(FirebaseAuth.getInstance().getCurrentUser().getUid());
                else
                    sendRequestToDriver(driverId);



            }
        });


        place_destination = (PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.place_destination);
        place_location = (PlaceAutocompleteFragment)getFragmentManager().findFragmentById(R.id.place_location);
        typeFilter = new AutocompleteFilter.Builder().setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS).setTypeFilter(3).build();



        place_location.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mPlaceLocation = place.getAddress().toString();
                mMap.clear();

                mCurrent = mMap.addMarker(new MarkerOptions().position(place.getLatLng()).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)).title("PickUp Here"));

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(),15.0f));
            }

            @Override
            public void onError(Status status) {

            }
        });

        place_destination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                mPlaceDestination = place.getAddress().toString();
                mMap.addMarker(new MarkerOptions().position(place.getLatLng()).icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker)).title("Destination"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(),15.0f));

                BottomSheetRiderFragment mBottomSheet = BottomSheetRiderFragment.newInstance(mPlaceLocation,mPlaceDestination);
               // mBottomSheet.show(getSupportFragmentManager(),mBottomSheet.getTag());

            }

            @Override
            public void onError(Status status) {

            }
        });



        setUpLocation();

        updateFirebaseToken();








    }

    private void updateFirebaseToken() {

            FirebaseDatabase db = FirebaseDatabase.getInstance();
            DatabaseReference tokens = db.getReference(Common.token_tb1);
            Token token = new Token(FirebaseInstanceId.getInstance().getToken());
            tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(token);

        }




    private void sendRequestToDriver(String driverId) {

      DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tb1);
      tokens.orderByKey().equalTo(driverId).addListenerForSingleValueEvent(new ValueEventListener() {
          @Override
          public void onDataChange(DataSnapshot dataSnapshot) {
              for(DataSnapshot postSnapshot: dataSnapshot.getChildren())
              {
                  Token token = postSnapshot.getValue(Token.class);
                  String json_lat_lng = new Gson().toJson(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()));
                  String riderToken = FirebaseInstanceId.getInstance().getToken();
                  Notification data = new Notification(riderToken,json_lat_lng);
                  Sender content = new Sender(token.getToken(),data);

                  mService.sendMessage(content).enqueue(new Callback<FCMResponse>() {
                      @Override
                      public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                          if(response.body().success==1)
                              Toast.makeText(Home.this, "RequestSent", Toast.LENGTH_SHORT).show();
                          else
                              Toast.makeText(Home.this, "Failed!", Toast.LENGTH_SHORT).show();
                      }

                      @Override
                      public void onFailure(Call<FCMResponse> call, Throwable t) {

                          Log.e("Error",t.getMessage());

                      }
                  });
              }
          }

          @Override
          public void onCancelled(DatabaseError databaseError) {

          }
      });

    }

    private void requestPickupHere(String uid) {


      DatabaseReference dbRequest = FirebaseDatabase.getInstance().getReference(Common.pickup_request_tb1);
      GeoFire mGeoFire = new GeoFire(dbRequest);
      mGeoFire.setLocation(uid,new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()));

      if(mCurrent.isVisible())
          mCurrent.remove();

      mCurrent= mMap.addMarker(new MarkerOptions().title("Pickup Here:").snippet("").position(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));
      mCurrent.showInfoWindow();

      btnPickupRequest.setText("Getting Your Driver....");


      findDriver();






    }

    private void findDriver() {


      final DatabaseReference drivers = FirebaseDatabase.getInstance().getReference(Common.driver_tb1);
      GeoFire gfDrivers = new GeoFire(drivers);

      GeoQuery geoQuery = gfDrivers.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()),radius);
      geoQuery.removeAllListeners();
      geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
          @Override
          public void onKeyEntered(String key, GeoLocation location) {



              if(!isDriverFound)
              {
                  //Toast.makeText(Home.this, "121", Toast.LENGTH_SHORT).show();
                  isDriverFound = true;
                  driverId = key;
                  btnPickupRequest.setText("Call Driver");
                 // Toast.makeText(Home.this, ""+key, Toast.LENGTH_SHORT).show();

              }


          }

          @Override
          public void onKeyExited(String key) {

          }

          @Override
          public void onKeyMoved(String key, GeoLocation location) {

          }

          @Override
          public void onGeoQueryReady() {


              if(!isDriverFound && radius<LIMIT)
              {
                  radius++;
                  findDriver();

              }
              else
              {
                  //Toast.makeText(Home.this, "No Available Drivers Around You", Toast.LENGTH_SHORT).show();
                  btnPickupRequest.setText("RequestPickup");
              }

          }

          @Override
          public void onGeoQueryError(DatabaseError error) {

          }
      });






    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {

            case MY_PERMISSION_REQUEST_CODE:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    if(checkPlayServices())
                    {
                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();

                    }

                }
                break;


        }






    }

    private void setUpLocation() {



        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
        {

            ActivityCompat.requestPermissions(this, new String[]{

                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, MY_PERMISSION_REQUEST_CODE);
        }
        else
        {
            if(checkPlayServices())
            {
                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();

            }

        }






    }

    private boolean checkPlayServices() {

      int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
      if(resultCode!= ConnectionResult.SUCCESS)
      {

          if(GooglePlayServicesUtil.isUserRecoverableError(resultCode))
              GooglePlayServicesUtil.getErrorDialog(resultCode,this,PLAY_SERVICES_RESOLUTION_REQUEST).show();
          else
          {
              Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show();
              finish();
          }
          return false;

      }

      return true;

    }

    private void displayLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;

        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null)
        {
            LatLng center = new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
            LatLng northSide = SphericalUtil.computeOffset(center,100000,0);
            LatLng southSide = SphericalUtil.computeOffset(center,100000,180);

            LatLngBounds bounds = LatLngBounds.builder().include(northSide).include(southSide).build();
            place_location.setBoundsBias(bounds);
            place_location.setFilter(typeFilter);
            place_destination.setBoundsBias(bounds);
            place_destination.setFilter(typeFilter);

            driversAvailable = FirebaseDatabase.getInstance().getReference(Common.driver_tb1);
            driversAvailable.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    loadAllAvailableDriver(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            final double latitude = mLastLocation.getLatitude();
            final double longtiude = mLastLocation.getLongitude();




                    loadAllAvailableDriver(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()));


            Log.d("RikshawApp",String.format("Location Has Been Changed: %f/ %f",latitude,longtiude));

        }
        else
        {

            Log.d("Rikshaw App","Can not get the location ");
        }











    }

    private void loadAllAvailableDriver(final LatLng location) {


        mMap.clear();
        mCurrent = mMap.addMarker(new MarkerOptions().position(location).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)).title(String.format("You")));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,15.0f));

        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference(Common.driver_tb1);
      GeoFire gf = new GeoFire(driverLocation);

      GeoQuery geoQuery = gf.queryAtLocation(new GeoLocation(location.latitude,location.longitude),distance);
      geoQuery.removeAllListeners();
      geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
          @Override
          public void onKeyEntered(String key, final GeoLocation location) {

              FirebaseDatabase.getInstance().getReference(Common.user_driver_tb1).child(key).addListenerForSingleValueEvent(new ValueEventListener() {
                  @Override
                  public void onDataChange(DataSnapshot dataSnapshot) {


                      Rider rider = dataSnapshot.getValue(Rider.class);
                      mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude,location.longitude)).flat(true).title(rider.getName()).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker)));





                  }

                  @Override
                  public void onCancelled(DatabaseError databaseError) {

                  }
              });


          }

          @Override
          public void onKeyExited(String key) {

          }

          @Override
          public void onKeyMoved(String key, GeoLocation location) {

          }

          @Override
          public void onGeoQueryReady() {


              if (distance<=LIMIT)
              {
                  distance++;
                  loadAllAvailableDriver(location);
              }

          }

          @Override
          public void onGeoQueryError(DatabaseError error) {

          }
      });









    }

    private void buildGoogleApiClient() {


      mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
      mGoogleApiClient.connect();;









    }

    private void createLocationRequest() {

      mLocationRequest = new LocationRequest();
      mLocationRequest.setInterval(UPDATE_INTERVAL);
      mLocationRequest.setFastestInterval(FATEST_INTERVAL);
      mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
      mLocationRequest.setSmallestDisplacement(DISPLACEMENT);




    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {


      displayLocation();
      startLocationUpdates();


    }

    private void startLocationUpdates() {

      if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
      {
          return;

      }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {


      mLastLocation = location;
      displayLocation();



    }

    @Override
    public void onMapReady(GoogleMap googleMap) {


      try
      {
          boolean isSuccess = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,R.raw.uber_style_map));

          if(!isSuccess)
              Log.e("Error","Map Style Load Failed");

      }

      catch(Resources.NotFoundException ex)
        {
            ex.printStackTrace();
        }


      mMap = googleMap;

      mMap.getUiSettings().setZoomControlsEnabled(true);
      mMap.getUiSettings().setZoomGesturesEnabled(true);
      mMap.setInfoWindowAdapter(new CustomerInfoWindow(this));























    }
}
