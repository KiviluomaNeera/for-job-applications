package fi.tuni.worktimeapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import static android.provider.AlarmClock.EXTRA_MESSAGE;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

/*
Class for app's MainWindow
 */
public class MainWindowActivity extends AppCompatActivity {
    private Integer NEW_TIME_ACTIVITY_REQUEST_CODE = 1;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 98;
    private TextView helloUserText;
    private User currentUser;
    private String userId = "";
    private Button addToCloudButton;
    private Button stopButton;
    private Button breakButton;
    private Button seeAllInfoButton;
    private Button changeInfoButton;
    private FloatingActionButton fab;
    private TextView infoText;
    private Work currentWork = null;
    private boolean isWorkEnding = false;

    private FirebaseFirestore mFirestore;
    private Context context;

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Location mLocation;
    private boolean isLocationOK = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_window);

        // Find intent and take userId
        Intent intentLogIn = getIntent();
        userId = intentLogIn.getStringExtra(EXTRA_MESSAGE);
        context = this;

        mFirestore = FirebaseFirestore.getInstance();
        getUsersFromCloud();

        // Set logo to UI
        ImageView imageView = findViewById(R.id.image);
        Glide.with(this).load(R.drawable.logo).into(imageView);

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mLocation = location;
            }
        };

        helloUserText = findViewById(R.id.helloText);
        stopButton = findViewById(R.id.stopButton);
        addToCloudButton = findViewById(R.id.addToCloudbutton);
        seeAllInfoButton = findViewById(R.id.seeAllInfoButton);
        breakButton = findViewById(R.id.breakButton);
        infoText = findViewById(R.id.workInfoText);
        changeInfoButton = findViewById(R.id.changeButton);

        stopButton.setBackgroundColor(Color.rgb(203, 67, 53));

        // Floating button to move to StartActivity
        fab = findViewById(R.id.fab);
        fab.setOnClickListener( view -> {
            Intent intent = new Intent(MainWindowActivity.this, StartActivity.class);
            intent.putExtra(EXTRA_MESSAGE, userId);
            startActivityForResult(intent, NEW_TIME_ACTIVITY_REQUEST_CODE);
        });
    }

    // Function for addToCloud Button
    public void onAddToCloudClick(View view) {
        // Intent to take user to AddToCloudActivity
        Intent addIntent = new Intent(MainWindowActivity.this, addToCloudActivity.class);
        addIntent.putExtra(EXTRA_MESSAGE, userId);
        startActivity(addIntent);
    }

    // Function for changeInfo Button
    public void onChangeInfoClick(View view) {
        Intent changeIntent = new Intent(MainWindowActivity.this, StartActivity.class);
        changeIntent.putExtra(EXTRA_MESSAGE, userId);
        startActivity(changeIntent);
    }

    // Function for stop Button
    public void onStopButtonClick(View view) {
        double latitude = 0;
        double longitude = 0;

        // Check if task is "Auraus" or "Hiekoittaminen"
        if (currentWork.getTaskId() == 1 || currentWork.getTaskId() == 2) {
            // try to get location
            try {
                askPermission(context);
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mLocationListener);
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mLocationListener);

                if (mLocation != null) {
                    isLocationOK = true;
                    latitude = mLocation.getLatitude();
                    longitude = mLocation.getLongitude();
                    currentWork.setEndLocationLatitude(latitude);
                    currentWork.setEndLocationLongitude(longitude);
                } else {
                    isLocationOK = false;
                }
            } catch (SecurityException e) {
                isLocationOK = false;
            }
        }
        // Set users work id to 0 to show that the user doesn't have work timer going anymore
        currentUser.setCurrentWorkId(0);

        // Get duration of work
        isWorkEnding = true;
        int duration = calculateDuration();

        // Check if user had breaks during work
        if (currentWork.getBreakAmount() != 0) {
            duration -= currentWork.getBreakAmount() * 30;
        }
        currentWork.setDuration(duration);

        // Update work and user values to cloud
        addWorkToCloud(currentWork);
        addUserToCloud(currentUser);

        // Change visibilities to buttons
        stopButton.setVisibility(View.INVISIBLE);
        fab.setVisibility(View.VISIBLE);

        // Adding to Sheet
        addToSheet(currentWork);

        currentWork = null;

        // Change infoText to tell the user that there's no work going on at the moment
        infoText.setText("Ei meneill????n olevaa ty??t??");
    }

    private void addToSheet(Work work) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, "https://script.google.com/macros/s/AKfycbwOjBDqa73mnxYq9bkR26Yeb_q6GcWioG5JZgBkZS6-TMJFl4_paDHCXlcpUuRfiC6g/exec",
                response -> {
                    Log.d("softa", "Respo tuli");
                },
                error -> {

                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> parmas = new HashMap<>();

                Log.d("softa", "Passing params");

                //here we pass params
                parmas.put("action","addItem");
                parmas.put("nimi",work.getUserName());
                parmas.put("tyo",work.getTaskName());
                parmas.put("asiakas", work.getClientName());
                parmas.put("tuntimaara", String.valueOf(work.getDuration()));
                parmas.put("info", work.getInfo());
                parmas.put("aloitustunti", String.valueOf(work.getHour()));
                parmas.put("aloitusminuutti", String.valueOf(work.getMinute()));
                parmas.put("paiva", String.valueOf(work.getDay()));
                parmas.put("kuukausi", String.valueOf(work.getMonth()));
                parmas.put("vuosi", String.valueOf(work.getYear()));

                return parmas;
            }
        };

        int socketTimeOut = 30000;// u can change this .. here it is 50 seconds

        RetryPolicy retryPolicy = new DefaultRetryPolicy(socketTimeOut, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(retryPolicy);

        RequestQueue queue = Volley.newRequestQueue(context);

        queue.add(stringRequest);
    }

    // Function for SeeOwnInfo button
    public void onSeeOwnInfoClick(View view) {
        // Intent to get the user to OwnInfoActivity
        Intent ownInfoIntent = new Intent(MainWindowActivity.this, OwnInfoActivity.class);
        ownInfoIntent.putExtra(EXTRA_MESSAGE, userId);
        startActivity(ownInfoIntent);
    }

    // Function for SeeAllInfo button
    public void onSeeAllInfoClick(View view) {
        // Intent to take user to AllInfoActivity
        Intent allInfoIntent = new Intent(MainWindowActivity.this, AllInfoActivity.class);
        allInfoIntent.putExtra(EXTRA_MESSAGE, userId);
        startActivity(allInfoIntent);
    }

    // Function for break button
    public void onBreakButtonClick(View view) {
        // Get amount of breaks, add one to it and set the new value to currentWork
        int breaks = currentWork.getBreakAmount();
        currentWork.setBreakAmount(breaks + 1);
        // Update value to the cloud and to the info text
        addWorkToCloud(currentWork);
        setInfoText();
    }

    // Function to add/update work to cloud
    public void addWorkToCloud(Work work) {
        mFirestore.collection("works")
                .document(Integer.toString(work.getWorkId())).set(work);
    }

    // Function to add/update user to cloud
    public void addUserToCloud(User user) {
        mFirestore.collection("users").
                document(Integer.toString(user.getId())).set(user);
    }

    // Function for getting users from cloud and finding current user
    public void getUsersFromCloud() {
        mFirestore.collection("users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value != null) {
                    for (DocumentSnapshot snapshot : value) {
                        User us = snapshot.toObject(User.class);
                        if (userId.equals(Integer.toString(us.getId()))) {
                            setUserInfo(us);
                            break;
                        }
                    }
                }
            }
        });
    }

    // Function for getting works from cloud and finding current work
    public void getWorkFromCloud() {
        mFirestore.collection("works").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value != null) {
                    for (DocumentSnapshot snapshot : value) {
                        Work work = snapshot.toObject(Work.class);
                        if (currentUser.getCurrentWorkId() == work.getWorkId()) {
                            currentWork = work;
                            // Change info text
                            setInfoText();
                            break;
                        }
                    }
                }
            }
        });
    }

    // Function to set user info to the UI
    private void setUserInfo(User us) {
        currentUser = us;
        helloUserText.setText("Hei " + currentUser.getName() + "!");

        // if user's id is not 66, hide addToCloud and seeAllInfo buttons
        if (currentUser.getId() != 66) {
            addToCloudButton.setVisibility(View.INVISIBLE);
            seeAllInfoButton.setVisibility(View.INVISIBLE);
        }

        // If user doesn't have work going, hide stop and break buttons and change info text
        if (currentUser.getCurrentWorkId() == 0) {
            stopButton.setVisibility(View.INVISIBLE);
            breakButton.setVisibility(View.INVISIBLE);
            infoText.setText("Ei meneill????n olevaa ty??t??");
            changeInfoButton.setVisibility(View.INVISIBLE);
        } else {
            // Otherwise hide fab and get works from cloud to find currentWork
            fab.setVisibility(View.INVISIBLE);
            getWorkFromCloud();
        }
    }

    // Function to calculate duration of work
    private int calculateDuration() {
        int duration = 0;

        // Generate ZonedDateTime from current work
        ZonedDateTime startTime = ZonedDateTime.of(currentWork.getYear(), currentWork.getMonth(),
                currentWork.getDay(), currentWork.getHour(), currentWork.getMinute()
                , 0, 0, ZoneId.of("Europe/Helsinki"));

        // Get time now
        ZonedDateTime endTime = ZonedDateTime.now(ZoneId.of("Europe/Helsinki"));
        duration = (int)
                (endTime.toEpochSecond() - startTime.toEpochSecond()) / 60;

        int hours = duration / 60;
        int minutes = duration - hours * 60;

        if (minutes != 0 && minutes != 30 && hours != 0 && isWorkEnding) {
            if (minutes < 15) {
                minutes = 0;
            } else if (minutes < 45) {
                minutes = 30;
            } else {
                minutes = 0;
                hours++;
            }
            duration = hours * 60 + minutes;
        }

        isWorkEnding = false;
        return duration;
    }

    // Function to change infoText
    private void setInfoText() {
        int duration = calculateDuration();
        int amountOfHours = duration / 60;
        int minutes = duration - 60 * amountOfHours;

        String info = String.format("%s    %s%nTaukoja: %d  Kesto: %dh %dmin",
                currentWork.getTaskName(), currentWork.getClientName(), currentWork.getBreakAmount(),
                amountOfHours, minutes);
        infoText.setText(info);
    }

    // Function to ask permission to use location from user
    public boolean askPermission(final Context context) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Asked for permission earlier but permission not given
            } else {
                // Request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            // Permission already granted
            return true;
        }
    }

    // Function for request permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission was granted
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        //Request location updates:
                    }
                } else {
                    // permission denied
                }
                return;
            }
        }
    }
}