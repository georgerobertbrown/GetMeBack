package com.gncbrown.GetMeBack.Utilities;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class NamedLocation implements Parcelable {

    private String name;
    private String location;

    // Constructor
    public NamedLocation(String name, String location) {
        this.name = name;
        this.location = location;
    }

    public NamedLocation(String name, LatLng location) {
        this.name = name;
        this.location = String.format("%f,%f", location.latitude, location.longitude);
    }

    // Constructor to read from Parcel
    protected NamedLocation(Parcel in) {
        name = in.readString();
        location = in.readString();
    }

    // Creator
    public static final Creator<NamedLocation> CREATOR = new Creator<NamedLocation>() {
        @Override
        public NamedLocation createFromParcel(Parcel in) {
            return new NamedLocation(in);
        }

        @Override
        public NamedLocation[] newArray(int size) {
            return new NamedLocation[size];
        }
    };

    // Getters
    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    // Describe contents (usually 0)
    @Override
    public int describeContents() {
        return 0;
    }

    // Write to Parcel
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(location);
    }

    @Override
    public String toString() {
        return "NamedLocation{" +
                "name='" + name + '\'' +
                ", location='" + location + '\'' +
                '}';
    }

    public LatLng getLatLng() {
        String[] latLngString = location.split(",");
        double latitude = 0.0;
        double longitude = 0.0;
        try {
            latitude = Utils.stringToDouble(latLngString[0]);
            longitude = Utils.stringToDouble(latLngString[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new LatLng(latitude, longitude);
    }
}