package io.rajat.turntotech.nearby;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapsFragment extends Fragment {


    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1 ;
    private static final int PLACE_PICKER_REQUEST = 2;
    MapView mMapView;
    private GoogleMap googleMap;
    EditText locationSearch;
    Button searchSubmit;
    Button pickPlaceButton;
    HashMap<String,Marker> hashMapMarker;

    ArrayList<MyPlace> resultOfQuery;

    public MapsFragment() {
        // Required empty public constructor
    }


    /* Using Geocoder how to fetch locations from a search String */
//    private void fetchLocationsGeoCoder(String searchString){
//        String location = searchString;
//        List<Address> addressList = null;
//
//        if (location != null || !location.equals("")) {
//            Geocoder geocoder = new Geocoder(getContext());
//            try {
//                addressList = geocoder.getFromLocationName(location, 1);
//
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            Address address = addressList.get(0);
//            LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
//            googleMap.addMarker(new MarkerOptions().position(latLng).title(location));
//            googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
//        }
//    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_maps, container, false);

        resultOfQuery = new ArrayList<>();
        hashMapMarker   = new HashMap<>();

        locationSearch = (EditText) view.findViewById(R.id.searchLocationeditText);
        searchSubmit = (Button)view.findViewById(R.id.search_button);
        pickPlaceButton = (Button)view.findViewById(R.id.button2);
        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;

                // For showing a move to my location button
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                googleMap.setMyLocationEnabled(true);
                googleMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());


                // For dropping a marker at a point(turntotech) on the Map
                //LatLng turntotech = new LatLng(40.7084, -74.0149);
                //googleMap.addMarker(new MarkerOptions().position(turntotech).title("TurnToTech").snippet("Education in New York City, New York"));

                // For Dropping Marker to my current Position

                LatLng currentPos = new LatLng(((MainActivity)getActivity()).getLatitude(), ((MainActivity)getActivity()).getLongitude());


                // Add custom Marker to google map
                googleMap.addMarker(
                        new MarkerOptions().
                                position(currentPos).title("My Current Position")
                                .snippet("My current Position in google map")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.custom_pin))
                );


                // For zooming automatically to the location of the marker
                CameraPosition cameraPosition = new CameraPosition.Builder().target(currentPos).zoom(12).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


                //  info window listner for google map pin.
                googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        Bundle bundle = null;

                        // add proper myPlace
                        for (MyPlace myplace :
                                resultOfQuery) {

                            if (myplace.marker_id.equalsIgnoreCase(marker.getId())) {
                                bundle = new Bundle();
                                bundle.putParcelable("placeClicked",myplace);
                                break;
                            }
                        }

                        // Go to Another screen
                        Fragment fragment = new DetailFragment();
                        fragment.setArguments(bundle);
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.mainLayout, fragment);
                        fragmentTransaction.addToBackStack(null);

                        // Commit the transaction
                        fragmentTransaction.commit();

                    }
                });

            }
        });



        searchSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                //Remove Previous Pins/Marker from Previous Search
                for (MyPlace myPlace :
                        resultOfQuery) {
                    Marker marker = hashMapMarker.get(myPlace.placeID);
                    marker.remove();
                    hashMapMarker.remove(myPlace.placeID);
                }
                resultOfQuery.clear();


                String searchText = locationSearch.getText().toString();
                searchText = searchText.replace(" ","+");
                String myCurrentLat = String.valueOf(((MainActivity)getActivity()).getLatitude());
                String myCurrentLon = String.valueOf(((MainActivity)getActivity()).getLongitude());
                String radius = "500";

                String map_serach_string = "https://maps.googleapis.com/maps/api/place/textsearch/json?query="+searchText+"&location="+myCurrentLat+","+myCurrentLon+"&radius="+radius+"&key="+getString(R.string.server_key);

                // Now Using Volley Framework get request with above map_search_string
                // Initialize a new RequestQueue instance
                final RequestQueue requestQueue = Volley.newRequestQueue(getContext());

                // Initialize a new JsonObjectRequest instance
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                        Request.Method.GET,
                        map_serach_string,
                        null,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                // Do something with response

                                // Parse JSON and get Details Address, icon and plcaeid

                                // Process the JSON
                                try{
                                    // Get the JSON array
                                    JSONArray array = response.getJSONArray("results");

                                    if(array.length() > 0) {

                                        // Loop through the array elements
                                        for (int i = 0; i < array.length(); i++) {
                                            // Get current json object
                                            JSONObject result = array.getJSONObject(i);

                                            // Get the current student (json object) data
                                            String icon = result.getString("icon");
                                            String placeID = result.getString("place_id");
                                            String address = result.getString("formatted_address");
                                            String name = result.getString("name");
                                            JSONObject location = (result.getJSONObject("geometry")).getJSONObject("location");
                                            String lat = location.getString("lat");
                                            String lng = location.getString("lng");



                                            // Display those places in mapView
                                            Marker marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng))).title(name).snippet(address));
                                            resultOfQuery.add(new MyPlace(Double.parseDouble(lat), Double.parseDouble(lng), name, icon, placeID, address,marker.getId()));

                                            //googleMap.animateCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                                            hashMapMarker.put(placeID, marker);


                                        }
                                    }
                                    else{
                                        Toast.makeText(getContext(),"No results Found",Toast.LENGTH_LONG).show();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener(){
                            @Override
                            public void onErrorResponse(VolleyError error){
                                // Do something when error occurred

                            }
                        }
                );

                // Add JsonObjectRequest to the RequestQueue
                requestQueue.add(jsonObjectRequest);




            }
        });


        pickPlaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /* Google Auto Complete */
//                try {
//
//                    // You can also try full screen mode - PlaceAutocomplete.MODE_FULLSCREEN
//                    Intent intent =
//                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
//                                    .build(getActivity());
//
//                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
//                } catch (GooglePlayServicesRepairableException e) {
//                    // TODO: Handle the error.
//                } catch (GooglePlayServicesNotAvailableException e) {
//                    // TODO: Handle the error.
//                }


                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try {
                    startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });


        return  view;
    }


    // A place has been received; use requestCode to track the request.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
//            if (resultCode == RESULT_OK) {
//                Place place = PlaceAutocomplete.getPlace(getContext(), data);
//                googleMap.addMarker(new MarkerOptions().position(place.getLatLng()).title((String) place.getName()));
//                googleMap.animateCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
//
//            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
//                Status status = PlaceAutocomplete.getStatus(getContext(), data);
//
//
//
//            } else if (resultCode == RESULT_CANCELED) {
//                // The user canceled the operation.
//            }
//        }
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                for (MyPlace myPlace :
                        resultOfQuery) {
                    Marker marker = hashMapMarker.get(myPlace.placeID);
                    marker.remove();
                    hashMapMarker.remove(myPlace.placeID);
                }
                resultOfQuery.clear();

                Place place = PlacePicker.getPlace(getContext(),data);


                Marker marker = googleMap.addMarker(new MarkerOptions().position(place.getLatLng()).title((String) place.getName()));
                resultOfQuery.add(new MyPlace(place.getLatLng().latitude, place.getLatLng().longitude, (String) place.getName(), "", place.getId(), place.getAddress().toString(),marker.getId()));
                hashMapMarker.put(marker.getId(),marker);
                googleMap.animateCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));
                String toastMsg = String.format("Place: %s", place.getName());
                Toast.makeText(getContext(), toastMsg, Toast.LENGTH_LONG).show();
            }
        }
    }



    /* Custom Ifo Window Adapter */

    public class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {


        private View view;
        MyPlace placeyouClicked;


        public CustomInfoWindowAdapter() {
            view = getActivity().getLayoutInflater().inflate(R.layout.custom_info_window,null);
            //view.removeOnAttachStateChangeListener();
        }

        @Override
        public View getInfoWindow(Marker marker) {

            view.bringToFront();

            String imageURL;
            final ImageView image = ((ImageView) view.findViewById(R.id.imageView));
            final TextView title_textView = (TextView)view.findViewById(R.id.textView);
            final TextView snippet_textView = (TextView)view.findViewById(R.id.textView2);


            for (MyPlace myplace :
                    resultOfQuery) {
                if (myplace.marker_id.equalsIgnoreCase(marker.getId())) {
                    placeyouClicked = myplace;
                    if(!myplace.imageURL.isEmpty() && !myplace.imageURL.equalsIgnoreCase("")) {
                        imageURL = myplace.imageURL;
                        // using picasso set the image with url
                        Picasso.with(getContext()).load(imageURL)
                                .placeholder(R.drawable.dummy_map_icon)
                                .into(image);
                    }
                    if(!myplace.placeName.isEmpty() && !myplace.placeName.equalsIgnoreCase("")){
                        title_textView.setText(myplace.placeName);
                    }
                    if(!myplace.address.isEmpty() && !myplace.address.equalsIgnoreCase("")){


                        String temp_adress = myplace.address.substring(0,myplace.address.indexOf(","));
                        snippet_textView.setText(temp_adress);
                    }


                    break;
                }
            }



            return view;
        }

        @Override
        public View getInfoContents(Marker marker) {


            return null;
        }
    }


}
