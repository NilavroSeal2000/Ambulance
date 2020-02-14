package com.example.ambulance;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tomtom.online.sdk.common.location.LatLngAcc;
import com.tomtom.online.sdk.search.OnlineSearchApi;
import com.tomtom.online.sdk.search.SearchApi;
import com.tomtom.online.sdk.search.data.fuzzy.FuzzySearchQueryBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
{
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 101;
    private Location currentLocation;
    private Marker currentMarker, destinationMarker;
    private LocationCallback locationCallback;
    private Button pickupbtn, destinationbtn;
    private SupportMapFragment supportMapFragment;
    private LatLng startLatLng, endLatLng;

      FirebaseAuth auth;
      FirebaseUser user;
      DatabaseReference reference,reference2;
      String user_id,name,phone,Latitude_P,Longitude_P,Latitude_D,Longitude_D;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        pickupbtn = (Button)findViewById(R.id.pickup_btn);
        destinationbtn = (Button)findViewById(R.id.dest_btn);
        //
        auth = FirebaseAuth.getInstance();
         user=auth.getCurrentUser();
         user_id=user.getUid();
         reference = FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
         reference.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 name = dataSnapshot.child("name").getValue(String.class);
                 phone = dataSnapshot.child("phone").getValue(String.class);
             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {

             }
         });
         reference2= FirebaseDatabase.getInstance().getReference().child("Location");
        Locate location_object=new Locate(user_id,"0.0","0.0","0.0","0.0");
        FirebaseUser firebaseUser = auth.getCurrentUser();
        reference2.child(firebaseUser.getUid()).setValue(location_object).addOnCompleteListener(new OnCompleteListener<Void>()
        {
            @Override
            public void onComplete(@NonNull Task<Void> task)
            {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Location saved", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Location could not be saved", Toast.LENGTH_LONG).show();
                }

            }
        });

        //

        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult)
            {
                currentLocation = locationResult.getLastLocation();
                Log.d("Location : ", currentLocation.getLatitude() + "  " + currentLocation.getLongitude());
                reference2= FirebaseDatabase.getInstance().getReference().child("Location");
                Locate location_object=new Locate(user_id,Double.toString(currentLocation.getLatitude()),Double.toString(currentLocation.getLongitude()),"0.0","0.0");
                FirebaseUser firebaseUser = auth.getCurrentUser();
                reference2.child(firebaseUser.getUid()).setValue(location_object).addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "Location saved", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Location could not be saved", Toast.LENGTH_LONG).show();
                        }

                    }
                });
                supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
                supportMapFragment.getMapAsync(MapsActivity.this);
            }
        };
        startLocationUpdates();

        // If this initialization of Places is not done then, on clicking on the button, the app will crash.

        //if(Places.isInitialized() == false)
            //Places.initialize(getApplicationContext(), getString(R.string.map_key));

        //final List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME);

        pickupbtn.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(getApplicationContext(), com.example.ambulance.AutocompleteActivity.class);
                startActivityForResult(intent, 200);
            }
        });

        destinationbtn.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(getApplicationContext(), com.example.ambulance.AutocompleteActivity.class);
                startActivityForResult(intent,400);

            }
        });





    }

    private void startLocationUpdates()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

        /*task.addOnSuccessListener(new OnSuccessListener<Location>()
        {
            @Override
            public void onSuccess(Location location)
            {
                if(location != null)
                {
                    currentLocation = location;
                    Toast.makeText(getApplicationContext(), currentLocation.getLatitude() + " " + currentLocation.getLongitude(), Toast.LENGTH_LONG).show();
                    SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
                    supportMapFragment.getMapAsync(MapsActivity.this);
                }
            }
        });*/
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        this.googleMap = googleMap;
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

        if(currentMarker == null)
        {
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            markerOptions.title("I'm Here");
            this.googleMap.setMyLocationEnabled(true);
            this.googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            this.googleMap.getUiSettings().setCompassEnabled(true);
            this.googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19));
            currentMarker = this.googleMap.addMarker(markerOptions);

            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            try
            {
                List<Address> list = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
                String address = list.get(0).getAddressLine(0);
                pickupbtn.setText(address);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

        }
        else
            currentMarker.setPosition(latLng);

        View myLocationButton =  ((View)supportMapFragment.getView().findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) myLocationButton.getLayoutParams();

        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        rlp.setMargins(400, 1100, 0, 0);

        if(Build.VERSION.SDK_INT >= 17)
            rlp.addRule(RelativeLayout.ALIGN_PARENT_END);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode)
        {
            case REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    startLocationUpdates();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200)
        {
            if (resultCode == RESULT_OK)
            {
                Bundle bundle = data.getExtras();
                String name = bundle.get("selected_text").toString();
                double lat = Double.parseDouble(bundle.get("latitude").toString());
                double lng = Double.parseDouble(bundle.get("longitude").toString());
                startLatLng = new LatLng(lat, lng);
//                Latitude_P = Double.toString(lat);
//                Longitude_P= Double.toString(lng);
//                Locate location_object1=new Locate(user_id,Latitude_P,Longitude_P,"3.5","0.0");
//                FirebaseUser firebaseUser = auth.getCurrentUser();
//                reference2.child(firebaseUser.getUid()).setValue(location_object1).addOnCompleteListener(new OnCompleteListener<Void>()
//                {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task)
//                    {
//                        if (task.isSuccessful()) {
//                            Toast.makeText(getApplicationContext(), "Location saved", Toast.LENGTH_LONG).show();
//                        } else {
//                            Toast.makeText(getApplicationContext(), "Location could not be saved", Toast.LENGTH_LONG).show();
//                        }
//
//                    }
//                });


                pickupbtn.setText(name);

                if (currentMarker == null)
                {
                    MarkerOptions options = new MarkerOptions();
                    options.title("Pickup");
                    options.position(startLatLng);
                    currentMarker = googleMap.addMarker(options);
                }
                else
                    currentMarker.setPosition(startLatLng);

                if(destinationMarker != null)
                {
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(currentMarker.getPosition());
                    builder.include(destinationMarker.getPosition());

                    LatLngBounds bounds = builder.build();
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 100);
                }


            }
            else if(resultCode == AutocompleteActivity.RESULT_ERROR)
            {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.d("Error: ", status.getStatusMessage());
            }
            else if(resultCode == RESULT_CANCELED)
            {

            }
        }
        else if (requestCode == 400)
        {
            if (resultCode == RESULT_OK)
            {
                Bundle bundle = data.getBundleExtra("place");
                String name = bundle.getString("name");
                double lat = Double.parseDouble(bundle.getString("latitude"));
                double lng = Double.parseDouble(bundle.getString("longitude"));
                endLatLng = new LatLng(lat, lng);
                destinationbtn.setText(name);

                if (destinationMarker == null)
                {
                    MarkerOptions options = new MarkerOptions();
                    options.title("Destination");
                    options.position(endLatLng);
                    destinationMarker = googleMap.addMarker(options);
                }
                else
                    destinationMarker.setPosition(endLatLng);

                if(currentMarker != null)
                {
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    builder.include(currentMarker.getPosition());
                    builder.include(destinationMarker.getPosition());

                    LatLngBounds bounds = builder.build();
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 100);
                    googleMap.animateCamera(cameraUpdate);
                }
            }
            else if(resultCode == AutocompleteActivity.RESULT_ERROR)
            {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.d("Error: ", status.getStatusMessage());
            }
            else if(resultCode == RESULT_CANCELED)
            {

            }
        }

    }

    public void goToPay(View view)
    {
        Intent intent = new Intent(MapsActivity.this, PaymentGateway.class);
        Bundle bundle = new Bundle();
        bundle.putString("pickup_latitude", currentMarker.getPosition().latitude + "");
        bundle.putString("pickup_longitude", currentMarker.getPosition().longitude + "");
        bundle.putString("destination_latitude", destinationMarker.getPosition().latitude + "");
        bundle.putString("destination_longitude", destinationMarker.getPosition().longitude + "");
        intent.putExtra("ride", bundle);
        startActivity(intent);

    }

    public void signOut(View view)
    {
        Intent intent = new Intent(MapsActivity.this, SignInActivity.class);
        startActivity(intent);
    }


}

//https://maps.googleapis.com/maps/api/place/findplacefromtext/json?&inputtype=textquery&input=apollo&key=AIzaSyBt6dqua_Hr_AhCk0gJm9Kxh5X6DJBLYz8&fields=formatted_address,geometry,icon,name,permanently_closed,photos,place_id,plus_code,types&sessiontoken=12345