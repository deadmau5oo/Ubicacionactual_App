package com.example.ubicacionactual;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MapUbications extends FragmentActivity implements OnMapReadyCallback {

    private LocationManager ubicacion;

    GoogleMap map;
    SupportMapFragment mapFragment;

    DatabaseReference mDatabase;

    String Nickname;
    String contraseña;
    String Crear;

    //Mi usuario se inicializa para guardar los datos del login y con el que se actualizaran las posiciones
    mapsuser Miusuario=new mapsuser();

    //array que contendra los marcadores en el mapa "Viejos marcadores (desactualizados)"
    private ArrayList<Marker> tmpMarkers= new ArrayList<>();
    //array que contendra los marcadores en el mapa "Nuevos marcadores (actualizados)"
    private ArrayList<Marker> Markers= new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_ubications);

        mapFragment=(SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.google_map);
        mapFragment.getMapAsync(this);
        //paso de parametros del activity main "Login"
        Nickname=getIntent().getStringExtra("nickname");
        contraseña=getIntent().getStringExtra("contraseña");
        Crear=getIntent().getStringExtra("Crear");

        mDatabase= FirebaseDatabase.getInstance().getReference();
    }

    //agrega los parametros de la base de datos a Miusuario para su manipulación directa
    private void CuentaUsuario() {

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1000);
        }
        ubicacion = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        LatLng latLng=new LatLng(ubicacion.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLatitude(),ubicacion.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLongitude());

        Miusuario.setLatitude(latLng.latitude);
        Miusuario.setLongitude(latLng.longitude);
        Miusuario.setNickname(Nickname);
        Miusuario.setContraseña(contraseña);
        //Crea un nuevo registro en la base de datos conforme a los datos ingresados del login y la ubicación actual
        mDatabase.child("usuarios").child(Miusuario.getNickname()).setValue(Miusuario);
    }

    //actualiza la localización del usuarío en tiempo real.
    private void Actualizar(){
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
            }, 1000);
        }
        ubicacion = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LatLng latLng=new LatLng(ubicacion.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLatitude(),ubicacion.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLongitude());
        ubicacion.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,0,locationListener);

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));
    }

    LocationListener locationListener= new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());

            Miusuario.setContraseña(contraseña);
            Miusuario.setLatitude(latLng.latitude);
            Miusuario.setLongitude(latLng.longitude);
            Miusuario.setNickname(Nickname);

            //actualiza los datos conforme al nickname obtenido del login
            mDatabase.child("usuarios").child(Miusuario.getNickname()).setValue(Miusuario);
            Toast.makeText(MapUbications.this,"Coordenadas GPS actualizadas: " + "lat--> " + location.getLatitude() + " long--> " +  location.getLongitude(),Toast.LENGTH_LONG).show();
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
    };


    @Override
    public void onMapReady(GoogleMap googleMap) {
        map=googleMap;
        if(Crear.equals("si")){
            CuentaUsuario();
            Toast.makeText(MapUbications.this,"SE CREO UNA CUENTA",Toast.LENGTH_LONG).show();

        }
        if(Crear.equals("no")) {
            //inisiarSesion();
            CuentaUsuario();
            Toast.makeText(MapUbications.this,"SE INICIO SESION",Toast.LENGTH_LONG).show();

        }
        Actualizar();

        //actualizar los marcadores del mapa conforme los datos de la base de datos
        mDatabase.child("usuarios").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(Marker marker:Markers){
                    marker.remove();
                }
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    mapsuser mu = snapshot.getValue(mapsuser.class);
                    MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(mu.getLatitude(),mu.getLongitude())).title(mu.getNickname());
                    tmpMarkers.add(map.addMarker(markerOptions));
                }
                Markers.clear();
                Markers.addAll(tmpMarkers);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MapUbications.this,"Error en la base de datos",Toast.LENGTH_LONG).show();

            }
        });
    }
    }

