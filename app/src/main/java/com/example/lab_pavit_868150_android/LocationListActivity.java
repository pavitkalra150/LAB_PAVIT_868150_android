package com.example.lab_pavit_868150_android;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LocationListActivity extends AppCompatActivity {

    private ListView listt;
    FloatingActionButton addLocation;
    FloatingActionButton maps;
    private ArrayList<String> addresses = new ArrayList<>();

    private static final String SHARED_PREF_NAME = "addresses_list";
    private static final String ADDRESSES = "addresses";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_list);

        listt = findViewById(R.id.locationList);
        addLocation = findViewById(R.id.floatingActionButton);
        maps = findViewById(R.id.floatingActionButton2);

        addresses = loadAddressesFromSharedPreferences();
        updateList();

        maps.setOnClickListener(v -> {
            navigate();
        });
        addLocation.setOnClickListener(v -> {
            showInputDialog();
        });
    }

    private ArrayList<String> loadAddressesFromSharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        Set<String> addressesSet = sharedPreferences.getStringSet(ADDRESSES, null);
        if (addressesSet != null) {
            return new ArrayList<>(addressesSet);
        } else {
            return new ArrayList<>();
        }
    }

    private void showInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Location");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String address = input.getText().toString();
                addresses.add(address);
                saveAddressesToSharedPreferences(addresses);
                updateList();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String address = input.getText().toString();
                addresses.add(address);
                updateList();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
    private void saveAddressesToSharedPreferences(ArrayList<String> addresses) {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> addressesSet = new HashSet<>(addresses);
        editor.putStringSet(ADDRESSES, addressesSet);
        editor.apply();
    }
    private void updateList() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(LocationListActivity.this, android.R.layout.simple_list_item_1, addresses);
                listt.setAdapter(adapter);
            }
        });
    }
    public void navigate(){
        Intent nextActivityIntent = new Intent(this, MapsActivity.class);
        startActivity(nextActivityIntent);
    }
}