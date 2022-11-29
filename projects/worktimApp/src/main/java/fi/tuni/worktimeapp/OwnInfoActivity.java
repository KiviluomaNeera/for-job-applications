package fi.tuni.worktimeapp;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;

/*
Class to show user's works
 */
public class OwnInfoActivity extends AppCompatActivity {
    private String userId;
    private ArrayList<Work> works = new ArrayList<>();
    private ListView listView;
    private ArrayAdapter ownInfoAdapter;
    private FirebaseFirestore mFirestore;
    private Context context;
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_own_info);

        Intent intent = getIntent();
        userId = intent.getStringExtra(EXTRA_MESSAGE);

        mFirestore = FirebaseFirestore.getInstance();
        context = this;

        listView = findViewById(R.id.ownInfoListView);

        requestPermissionsIfNecessary(new String[]{
                Manifest.permission.INTERNET
                , Manifest.permission.ACCESS_COARSE_LOCATION
        });

        getWorksFromCloud();
    }

    // Function for ownInfoBack button
    public void onOwnInfoBackButtonClick(View view) {
        // Intent to get user back to MainWindowActivity
        Intent intent = new Intent(OwnInfoActivity.this, MainWindowActivity.class);
        intent.putExtra(EXTRA_MESSAGE, userId);
        startActivity(intent);
    }

    // Function to get works from cloud and adding them to listview
    private void getWorksFromCloud() {
        mFirestore.collection("works").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value != null) {
                    for (DocumentSnapshot snapshot : value) {
                        Work work = snapshot.toObject(Work.class);
                        if (Integer.toString(work.getUserId()).equals(userId) && work.getDuration() != 0) {
                            works.add(work);
                        }
                    }

                    // Sorting users work by date to ascending order
                    works.sort((a, b) -> {
                        if (a.getYear() > b.getYear()) {
                            return -1;
                        } else if (a.getYear() < b.getYear()) {
                            return 1;
                        } else if (a.getMonth() > b.getMonth()) {
                            return -1;
                        } else if (a.getMonth() < b.getMonth()) {
                            return 1;
                        } else if (a.getDay() > b.getDay()) {
                            return -1;
                        } else if (a.getDay() < b.getDay()) {
                            return 1;
                        } else if (a.getHour() > b.getHour()) {
                            return -1;
                        } else if (a.getHour() < b.getHour()) {
                            return 1;
                        } else if (a.getMinute() > b.getMinute()) {
                            return -1;
                        } else if (a.getMinute() < b.getMinute()) {
                            return 1;
                        } else {
                            return 0;
                        }
                    });

                    // Creating adapter and adding it to listview
                    ownInfoAdapter = new OwnInfoArrayAdapter(context, R.layout.info_item, works);
                    ownInfoAdapter.notifyDataSetChanged();
                    listView.setAdapter(ownInfoAdapter);
                }
            }
        });
    }

    public static void onButtonShowPopupWindowClick(Context context, View view, Work work) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupview = inflater.inflate(R.layout.activity_map, null);

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        PopupWindow popupWindow = new PopupWindow(popupview, width, height, true);
        popupWindow.setBackgroundDrawable(null);

        Context ctx = context.getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));


        MapView mMapView = popupview.findViewById(R.id.mapview);
        MapController mMapController;
        mMapView.setTileSource(TileSourceFactory.MAPNIK);
        mMapController = (MapController) mMapView.getController();
        mMapController.setZoom(18);

        GeoPoint gPt = new GeoPoint(work.getStartLocationLatitude(), work.getStartLocationLongitude());
        mMapView.getController().setCenter(gPt);

        // Making date String
        String date = "";
        if (work.getDay() < 10 && work.getMonth() < 10) {
            date = "0" + work.getDay() + ".0" + work.getMonth() + "." + work.getYear();
        } else if (work.getDay() < 10) {
            date = "0" + work.getDay() + "." + work.getMonth() + "." + work.getYear();
        } else if (work.getMonth() < 10) {
            date = work.getDay() + ".0" + work.getMonth() + "." + work.getYear();
        } else {
            date = work.getDay() + "." + work.getMonth() + "." + work.getYear();
        }

        // Making start time String
        String time = "";
        if (work.getHour() < 10 && work.getMinute() < 10) {
            time = "0" + work.getHour() + ":0" + work.getMinute();
        } else if (work.getHour() < 10) {
            time = "0" + work.getHour() + ":" + work.getMinute();
        } else if (work.getMinute() < 10) {
            time = work.getHour() + ":0" + work.getMinute();
        } else {
            time = work.getHour() + ":" + work.getMinute();
        }

        Resources resources = context.getResources();
        final int resourceId = resources.getIdentifier("ic_baseline_location_on_24", "drawable",
                context.getPackageName());
        Drawable d = context.getResources().getDrawable(resourceId);
        Marker marker1 = new Marker(mMapView);
        marker1.setPosition(gPt);
        marker1.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        marker1.setIcon(d);
        marker1.setTitle("Aloitus:");
        marker1.setSnippet("Klo. " + time + " " + date);

        time = "";
        Marker marker2 = new Marker(mMapView);
        marker2.setPosition(new GeoPoint(work.getEndLocationLatitude(), work.getEndLocationLongitude()));
        marker2.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
        marker2.setIcon(d);
        marker2.setTitle("Lopetus:");

        // Calculating end time
        int hours = work.getDuration() / 60;
        int minutes = work.getDuration() - hours * 60;
        int endHour = work.getHour() + hours;
        int endminute = 61;

        while (endminute > 60) {
            if (minutes + work.getMinute() == 60) {
                endHour++;
                endminute = 0;
            } else if (minutes + work.getMinute() > 60) {
                endHour++;
                endminute = minutes + work.getMinute() - 60;
            } else {
                endminute = minutes + work.getMinute();
            }
        }

        while (endHour > 23) {
            endHour -= 24;
        }

        // Making end time String
        if (endHour < 10 && endminute < 10) {
            time = "0" + endHour + ":0" + endminute;
        } else if (endHour < 10) {
            time = "0" + endHour + ":" + endminute;
        } else if (endminute < 10) {
            time = endHour + ":0" + endminute;
        } else {
            time = endHour + ":" + endminute;
        }

        marker2.setSnippet("Klo. " + time + " " + date);

        mMapView.getOverlays().add(marker1);
        mMapView.getOverlays().add(marker2);
        mMapView.invalidate();
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            permissionsToRequest.add(permissions[i]);
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }
}