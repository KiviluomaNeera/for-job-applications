package fi.tuni.worktimeapp;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

/*
Activity to start a new worktime
 */
public class StartActivity extends AppCompatActivity {
    private FirebaseFirestore mFirestore;
    private Context context;

    private ArrayList<Client> clients = new ArrayList<>();
    private User currentUser;
    private String userId;

    private ArrayList<Task> tasks = new ArrayList<>();
    private Spinner clientSpinner;
    private Spinner taskSpinner;
    private ArrayList<String> clientNames = new ArrayList<>();
    private ArrayList<String> taskNames = new ArrayList<>();
    private ArrayAdapter clientAdapter;
    private ArrayAdapter taskAdapter;
    private EditText infoEditText;

    private int taskId;
    private String taskName;
    private int clientId;
    private String clientName;
    private Work newWork;
    private int nextWorkId = 0;

    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private Location mLocation;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 98;
    private boolean isLocationOK = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // Get intent and userId
        Intent intent = getIntent();
        userId = intent.getStringExtra(EXTRA_MESSAGE);

        infoEditText = findViewById(R.id.editTextTextMultiLine);
        Button startButton = findViewById(R.id.startbutton);
        startButton.setBackgroundColor(Color.rgb(46, 204, 113));
        context = this;

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                mLocation = location;
            }
        };

        // Setting spinner to display clients
        clientSpinner = findViewById(R.id.client_spinner);
        clientAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, clientNames);
        clientAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        clientSpinner.setAdapter(clientAdapter);
        clientSpinner.setOnItemSelectedListener(new ClientSpinnerClass());

        // Setting spinner to display tasks
        taskSpinner = findViewById(R.id.task_spinner);
        taskAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, taskNames);
        taskAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskSpinner.setAdapter(taskAdapter);
        taskSpinner.setOnItemSelectedListener(new TaskSpinnerClass());

        // Getting data from cloud
        mFirestore = FirebaseFirestore.getInstance();
        getClientsFromCloud();
        getUsersFromCloud();
        getTasksFromCloud();
        setNextWorkId();

        // Setting logo to imageview
        ImageView imageView = findViewById(R.id.image);
        Glide.with(this).load(R.drawable.logo).into(imageView);
    }

    // Function for start button
    public void onStartClick(View view) {
        double latitude = 0;
        double longitude = 0;

        // try to get user's location
        try {
            askPermission(context);
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    0, 0, mLocationListener);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    0, 0, mLocationListener);

            if (mLocation != null) {
                isLocationOK = true;
                latitude = mLocation.getLatitude();
                longitude = mLocation.getLongitude();
            } else {
                isLocationOK = false;
            }
        } catch (SecurityException e) {
                isLocationOK = false;
        }

        // Check if location was found succesfully and task is "Auraus" or "Hiekoittaminen"
        if (isLocationOK) {
            if (currentUser.getCurrentWorkId() != 0) {
                newWork.setTaskId(taskId);
                newWork.setTaskName(taskName);
                newWork.setClientId(clientId);
                newWork.setClientName(clientName);

                nextWorkId = currentUser.getCurrentWorkId();
            } else {
                newWork = new Work(nextWorkId, currentUser.getId(), currentUser.getName(),
                        taskId, taskName, clientId, clientName);

                currentUser.setCurrentWorkId(nextWorkId);

                ZonedDateTime startTime = ZonedDateTime.now(ZoneId.of("Europe/Helsinki"));

                newWork.setDay(startTime.getDayOfMonth());
                newWork.setMonth(startTime.getMonthValue());
                newWork.setYear(startTime.getYear());
                newWork.setHour(startTime.getHour());
                newWork.setMinute(startTime.getMinute());

                newWork.setStartLocationLatitude(latitude);
                newWork.setStartLocationLongitude(longitude);
            }

            if (!infoEditText.getText().toString().equals("")) {
                newWork.setInfo(infoEditText.getText().toString());
            }

            // Add/update new work to cloud and update users work id
            addWorkToCloud(newWork);
            addUserToCloud(currentUser);

            // Intent to take user back to MainWindowActivity
            Intent startIntent = new Intent(StartActivity.this, MainWindowActivity.class);
            startIntent.putExtra(EXTRA_MESSAGE, userId);
            startActivity(startIntent);
        }
    }

    // Function for cancel button
    public void onCancelClick(View view) {
        // Intent to take user back to MainWindowActivity
        Intent cancelIntent = new Intent(StartActivity.this, MainWindowActivity.class);
        cancelIntent.putExtra(EXTRA_MESSAGE, userId);
        startActivity(cancelIntent);
    }

    // Function to update user's values to cloud
    public void addUserToCloud(User user) {
        mFirestore.collection("users").
                document(Integer.toString(user.getId())).set(user);
    }

    // Function to get user's from cloud and finding current user
    public void getUsersFromCloud() {
        mFirestore.collection("users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value != null) {
                    for (DocumentSnapshot snapshot : value) {
                        User us = snapshot.toObject(User.class);
                        if (userId.equals(Integer.toString(us.getId()))) {
                            currentUser = us;
                            break;
                        }
                    }
                }
            }
        });
    }

    // Function to get clients from cloud
    public void getClientsFromCloud() {
        mFirestore.collection("clients").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                clients.clear();
                if (value != null) {
                    for (DocumentSnapshot snapshot : value) {
                        Client client = snapshot.toObject(Client.class);
                        clients.add(client);
                        clientNames.add(client.getName());
                    }
                    // Update client adapter and sort clients alphabetically by name
                    clients.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
                    clientNames.sort(String::compareToIgnoreCase);
                    clientAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    // Function to get tasks from cloud
    public void getTasksFromCloud() {
        mFirestore.collection("tasks").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                tasks.clear();
                if (value != null) {
                    for (DocumentSnapshot snapshot : value) {
                        Task task = snapshot.toObject(Task.class);
                        tasks.add(task);
                        taskNames.add(task.getName());
                    }
                    // Update task adapter and sort tasks alphabetically by name
                    tasks.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
                    taskNames.sort(String::compareToIgnoreCase);
                    taskAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    // Function to get next work id from cloud and possibly current work so user can modify it
    private void setNextWorkId() {
        nextWorkId = 0;
        mFirestore.collection("works").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value != null) {
                    for (DocumentSnapshot snapshot : value) {
                        Work work = snapshot.toObject(Work.class);
                        if (nextWorkId < work.getWorkId()) {
                            nextWorkId = work.getWorkId();
                        }

                        if (currentUser.getCurrentWorkId() == work.getWorkId()) {
                            newWork = work;
                            Button startButton = findViewById(R.id.startbutton);
                            startButton.setText("Muokkaa");
                        }
                    }
                    nextWorkId++;
                }
            }
        });
    }

    // Function to ask permission to use location
    private boolean askPermission(final Context context) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission has not been granted yet
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Permission not granted earlier
            } else {
                // Request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            // Permission has already been granted
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
                    // permission was granted
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        //Request location updates
                    }
                } else {
                    // permission denied
                }
                return;
            }
        }
    }

    // Function to add work to cloud
    private void addWorkToCloud(Work work) {
        mFirestore.collection("works").document(Integer.toString(nextWorkId)).set(work);
    }

    // Class for client spinner's functionality
    public class ClientSpinnerClass implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
            Client chosenClient = clients.get(position);
            clientId = chosenClient.getId();
            clientName = chosenClient.getName();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    // Class for task spinner's functionality
    public class TaskSpinnerClass implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Log.d("softa", "Valittu task = "+ tasks.get(position).getName());
            Task chosenTask = tasks.get(position);
            taskId = chosenTask.getId();
            taskName = chosenTask.getName();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }
}