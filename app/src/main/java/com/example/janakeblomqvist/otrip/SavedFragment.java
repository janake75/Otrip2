package com.example.janakeblomqvist.otrip;

import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;

/**
 * Created by jan-akeblomqvist on 2016-10-12.
 */

public class SavedFragment extends Fragment {

    // data object we want to retain
    private Location data;

    // this method is only called once for this fragment
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // retain this fragment
        setRetainInstance(true);
    }

    public void setData(Location data) {
        this.data = data;
    }

    public Location getData() {
        return data;
    }

}
