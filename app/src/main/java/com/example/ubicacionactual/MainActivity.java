package com.example.ubicacionactual;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    Button crearcuenta;
    Button iniciarsesion;
    EditText nicknameEditText;
    EditText contraseñaeditText;

    String nickname;
    String contraseña;

    boolean crear=false;

    DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        crearcuenta=(Button)findViewById(R.id.iniciarSesion);
        iniciarsesion=(Button)findViewById(R.id.iniciarSesion);
        nicknameEditText=(EditText)findViewById(R.id.nicknameeditText);
        contraseñaeditText=(EditText)findViewById(R.id.contraseñaeditText);

        mDatabase= FirebaseDatabase.getInstance().getReference();
    }


    //consulta en la base de datos si el nickname y la contraseña son iguales
public void IniciarSesion(View view){

        nickname=nicknameEditText.getText().toString();
        contraseña=contraseñaeditText.getText().toString();
        mDatabase.child("usuarios").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //cada snapshot contiene los valores del objeto "mapuser"
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    mapsuser inisiarsesion=snapshot.getValue(mapsuser.class);
                    if (inisiarsesion.getNickname().equals(nickname)&&inisiarsesion.getContraseña().equals(contraseña)){
                        crear=true;
                    }
                    else{}
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
        if(crear){
            Intent intent =new Intent(getApplicationContext(),MapUbications.class);
            intent.putExtra("nickname",nickname);
            intent.putExtra("contraseña",contraseña);
            intent.putExtra("Crear","no");
            startActivity(intent);
            finish();
        }


}


public void CrearCuenta(View view){

    nickname=nicknameEditText.getText().toString();
    contraseña=contraseñaeditText.getText().toString();

    Intent intent =new Intent(getApplicationContext(),MapUbications.class);

    intent.putExtra("nickname",nickname);
    intent.putExtra("contraseña",contraseña);
    intent.putExtra("Crear","si");

    startActivity(intent);
    finish();

}


}
