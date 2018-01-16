package io.rajat.turntotech.nearby;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment {


    TextView name;
    TextView address;
    WebView webView;

    public DetailFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        webView = (WebView)view.findViewById(R.id.webView);
        //Android WebView, how to handle redirects in app instead of opening a browser

        webView.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request){

                return false; // then it is not handled by default action
            }
        });

        Bundle bundle = getArguments();
        if(bundle!=null){
            MyPlace selectedPlace = (MyPlace) bundle.getParcelable("placeClicked");

            name = (TextView) view.findViewById(R.id.textView3);
            address = (TextView)view.findViewById(R.id.textView4);
            getWebSiteFromPlaceID(selectedPlace.placeID);
            name.setText(selectedPlace.placeName);
            address.setText(selectedPlace.address);
        }else{
            webView.loadUrl("https://www.google.com/");
        }


        return view;
    }

    private void getWebSiteFromPlaceID(String place_id) {

        String place_serach_string = "https://maps.googleapis.com/maps/api/place/details/json?placeid="+place_id+"&key="+getString(R.string.server_key);

        // Now Using Volley Framework get request with above map_search_string
        // Initialize a new RequestQueue instance
        final RequestQueue requestQueue = Volley.newRequestQueue(getContext());

        // Initialize a new JsonObjectRequest instance
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                place_serach_string,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Print Response
                        System.out.println(response);

                        // Parse JSON and get Details Address, icon and plcaeid

                        // Process the JSON
                        try{
                            // Get the JSON array
                            JSONObject results = response.getJSONObject("result");
                            final String full_url = results.getString("website");
                            Log.e("TTT",full_url);
                            if (full_url.isEmpty() || full_url.equalsIgnoreCase("")){
                                webView.loadUrl("https://www.google.com/");
                            }else{
                                webView.loadUrl(full_url);
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

}
