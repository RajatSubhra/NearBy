package io.rajat.turntotech.nearby;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Rajat on 5/22/17.
 */

public class MyPlace implements Parcelable {

    public double latitude;
    public double longitude;
    public  String placeName;
    public String  placeID;
    public String imageURL;
    public String address;

    public String marker_id;


    MyPlace()
    {

    }

    MyPlace(double lat,double lng,String name, String icon, String id,String detail_adress, String mID)
    {
        this.latitude = lat;
        this.longitude = lng;
        this.placeName = name;
        this.imageURL = icon;
        this.placeID = id;
        this.address = detail_adress;
        this.marker_id = mID;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
