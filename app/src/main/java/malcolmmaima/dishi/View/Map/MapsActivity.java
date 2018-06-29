package malcolmmaima.dishi.View.Map;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import malcolmmaima.dishi.R;
import malcolmmaima.dishi.View.Map.Remote.IGoogleApi;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    private List<LatLng> polyLineList;
    private Marker marker;
    private float v;
    private double lat, lng;
    private Handler handler;
    private LatLng startPosition, endPosition;
    private int index, next;
    private Button btnGo;
    private EditText edtPlace;
    private String destination;
    private PolylineOptions polylineOptions, blackPolyLineOptions;
    private Polyline blackPolyLine, greyPolyLine;
    private LatLng myLocation;


    IGoogleApi mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        polyLineList = new ArrayList<>();
        btnGo = findViewById(R.id.btnSearch);
        edtPlace = findViewById(R.id.edtPlace);

        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                destination = edtPlace.getText().toString();
                destination = destination.replace(" ", "+"); //Replace space to + to make url
                mapFragment.getMapAsync(MapsActivity.this);
            }
        });

        mService = Common.getGoogleApi();

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // Add a marker to my location and move the camera
        final LatLng myLocation = new LatLng(-1.281647, 36.822638);
        mMap.addMarker(new MarkerOptions().position(myLocation).title("My location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                    .target(googleMap.getCameraPosition().target)
                    .zoom(17)
                    .bearing(30)
                    .tilt(45)
                    .build()));

        String requestUrl = null;
        try {

            requestUrl = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+myLocation.latitude+","+myLocation.longitude+"&"+
                    "destination="+destination+"&"+
                    "keys"+getResources().getString(R.string.google_directions_key);

            //Toast.makeText(this, "URL "+ requestUrl, Toast.LENGTH_SHORT).show();
            Log.d("URL", requestUrl);
            mService.getDataFomGoogleApi(requestUrl)
            .enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {

                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        JSONArray jsonArray = jsonObject.getJSONArray("routes");
                        for(int i = 0; i<jsonArray.length(); i++){

                            JSONObject route = jsonArray.getJSONObject(i);
                            JSONObject poly = route.getJSONObject("overview_polyline");
                            String polyline = poly.getString("points");
                            polyLineList = decodePoly(polyline);



                        }

                        //Adjusting bounds
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for(LatLng latLng:polyLineList){
                            builder.include(latLng);
                            LatLngBounds bounds = builder.build();

                            CameraUpdate mCameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 2);
                            mMap.animateCamera(mCameraUpdate);

                            polylineOptions = new PolylineOptions();
                            polylineOptions.color(Color.GRAY);
                            polylineOptions.startCap(new SquareCap());
                            polylineOptions.endCap(new SquareCap());
                            polylineOptions.jointType(JointType.ROUND);
                            polylineOptions.addAll(polyLineList);
                            greyPolyLine = mMap.addPolyline(polylineOptions);

                            blackPolyLineOptions = new PolylineOptions();
                            blackPolyLineOptions.color(Color.GRAY);
                            blackPolyLineOptions.startCap(new SquareCap());
                            blackPolyLineOptions.endCap(new SquareCap());
                            blackPolyLineOptions.jointType(JointType.ROUND);
                            blackPolyLineOptions.addAll(polyLineList);
                            blackPolyLine = mMap.addPolyline(blackPolyLineOptions);

                            mMap.addMarker(new MarkerOptions().position(polyLineList.get(polyLineList.size() - 1)));

                            //Animator
                            final ValueAnimator polyLineAnimator = ValueAnimator.ofInt(0, 100);
                            polyLineAnimator.setDuration(2000); //2 secs

                            polyLineAnimator.setInterpolator(new LinearInterpolator());
                            polyLineAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                    List<LatLng> points = greyPolyLine.getPoints();
                                    int percentValue = (int)valueAnimator.getAnimatedValue();
                                    int size = points.size();
                                    int newPoints = (int) (size * (percentValue / 100.0f));

                                    List<LatLng> p = points.subList(0, newPoints);
                                    blackPolyLine.setPoints(p);

                                }
                            });

                            polyLineAnimator.start();
                            //Add car marker
                            marker = mMap.addMarker(new MarkerOptions().position(myLocation)
                                .flat(true)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));

                            //Car moving
                            handler = new Handler();
                            index = -1;
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if(index < polyLineList.size() - 1){
                                        index++;
                                        next = index + 1;
                                    }

                                    if(index < polyLineList.size() - 1){
                                        startPosition = polyLineList.get(index);
                                        endPosition = polyLineList.get(next);
                                    }

                                    ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
                                    valueAnimator.setDuration(3000); //2 secs

                                    valueAnimator.setInterpolator(new LinearInterpolator());
                                    valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                        @Override
                                        public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                            v = valueAnimator.getAnimatedFraction();

                                            lng = v * endPosition.longitude + (1-v)
                                                    * startPosition.longitude;

                                            lat = v * endPosition.longitude + (1 - v)
                                                    * startPosition.latitude;

                                            LatLng newPos = new LatLng(lat,lng);
                                            marker.setPosition(newPos);
                                            marker.setAnchor(0.5f, 0.5f);
                                            marker.setRotation(getBearing(startPosition, newPos));
                                            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                                                .target(newPos)
                                                .zoom(15.5f)
                                                .build()));
                                        }

                                        private float getBearing(LatLng startPosition, LatLng newPos) {
                                          double lat = Math.abs(startPosition.latitude - newPos.latitude);
                                          double lng = Math.abs(startPosition.longitude - newPos.longitude);

                                            if(startPosition.latitude < newPos.latitude && startPosition.longitude < newPos.longitude){
                                                return (float) (Math.toDegrees(Math.atan(lng/lat)));
                                            }
                                            else if(startPosition.latitude >= newPos.latitude && startPosition.longitude < newPos.longitude){
                                                return (float) ((90-Math.toDegrees(Math.atan(lng/lat))) + 90);
                                            }

                                            else if(startPosition.latitude >= newPos.latitude && startPosition.longitude >= newPos.longitude){
                                                return (float) (Math.toDegrees(Math.atan(lng/lat)) + 180);
                                            }

                                            else if(startPosition.latitude < newPos.latitude && startPosition.longitude >= newPos.longitude){
                                                return (float) ((90-Math.toDegrees(Math.atan(lng/lat))) + 270);
                                            }

                                            return  -1;
                                        }
                                    });

                                    valueAnimator.start();
                                    handler.postDelayed(this, 3000);
                                }
                            }, 3000);

                        }


                    } catch (Exception e){}
                }

                /**
                 * Method to decode polyline points
                 * Courtesy : jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
                 * */
                private List decodePoly(String encoded) {

                    List poly = new ArrayList();
                    int index = 0, len = encoded.length();
                    int lat = 0, lng = 0;

                    while (index < len) {
                        int b, shift = 0, result = 0;
                        do {
                            b = encoded.charAt(index++) - 63;
                            result |= (b & 0x1f) << shift;
                            shift += 5;
                        } while (b >= 0x20);
                        int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                        lat += dlat;

                        shift = 0;
                        result = 0;
                        do {
                            b = encoded.charAt(index++) - 63;
                            result |= (b & 0x1f) << shift;
                            shift += 5;
                        } while (b >= 0x20);
                        int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                        lng += dlng;

                        LatLng p = new LatLng((((double) lat / 1E5)),
                                (((double) lng / 1E5)));
                        poly.add(p);
                    }

                    return poly;
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(MapsActivity.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
