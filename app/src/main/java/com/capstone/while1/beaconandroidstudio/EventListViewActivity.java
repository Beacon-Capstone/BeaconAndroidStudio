package com.capstone.while1.beaconandroidstudio;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ListView;
import android.widget.SimpleExpandableListAdapter;

import com.capstone.while1.beaconandroidstudio.beacondata.BeaconConsumer;
import com.capstone.while1.beaconandroidstudio.beacondata.BeaconData;
import com.capstone.while1.beaconandroidstudio.beacondata.Event;

import java.util.ArrayList;

/**
 * Created by Kean on 4/23/2017.
 */

public class EventListViewActivity extends AppCompatActivity {
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.event_list_view);

        listView = (ListView) findViewById(android.R.id.list);

        ArrayList<Event> events = BeaconData.getEvents();
        if (events != null) {
            ArrayAdapter<Event> arrayAdapter = new ArrayAdapter<>(
                    this,
                    R.layout.event_list_view_item_layout,
                    events
            );

            listView.setAdapter(arrayAdapter);
        }
    }
}
