package fi.tuni.worktimeapp;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashSet;

/*
Class for adding new values to cloud
 */
public class addToCloudActivity extends AppCompatActivity {
    private FirebaseFirestore mFirestore;
    private EditText addClientText;
    private EditText addUserText;
    private EditText addTaskText;
    private String userId;
    private int nextCustomerId;
    private int nextUserId;
    private int nextTaskId;
    private HashSet<String> clients = new HashSet<>();
    private HashSet<String> tasks = new HashSet<>();
    private HashSet<String> users = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_to_cloud);

        // Find intent and get userId
        // Otherwise this class doesn't need userId but it needs to
        // send it back to MainWindowClass
        Intent intentLogIn = getIntent();
        userId = intentLogIn.getStringExtra(EXTRA_MESSAGE);

        mFirestore = FirebaseFirestore.getInstance();

        addClientText = findViewById(R.id.editTextClient);
        addUserText = findViewById(R.id.editTextUser);
        addTaskText = findViewById(R.id.editTextTask);

        // Get amount of clients, users and tasks for calculating new
        // id to new value
        amountOfClients();
        amountOfUsers();
        amountOfTasks();
    }

    // Function for back button
    public void onBackButtonClick(View view) {
        // Intent to get user back to MainWindowActivity
        Intent backIntent = new Intent(addToCloudActivity.this, MainWindowActivity.class);
        backIntent.putExtra(EXTRA_MESSAGE, userId);
        startActivity(backIntent);
    }

    // Function for addClient button
    public void onAddClientClick(View view) {
        String text = addClientText.getText().toString();
        // Check if string is empty
        if (!text.equals("")) {
            // Check if name is already saved to cloud
            if (!clients.contains(text)) {
                // Add new client to cloud
                Client newClient = new Client(nextCustomerId, text);
                addClientToCloud(newClient);

                // Intent to take user back to MainWindowClass
                Intent backIntent = new Intent(addToCloudActivity.this, MainWindowActivity.class);
                backIntent.putExtra(EXTRA_MESSAGE, userId);
                startActivity(backIntent);
            } else {
                addClientText.setError("T??m?? asiakas l??ytyy jo!");
            }
        } else {
            addClientText.setError("Kirjoita asiakkaan nimi!");
        }
    }

    // Function for addUser button
    public void onAddUserClick(View view) {
        String text = addUserText.getText().toString();
        // Check if string is empty and nothing happens if it is
        if (!text.equals("")) {
            // Check if name is already saved to cloud
            if (!users.contains(text)) {
                // Add new user to cloud
                User newUser = new User(nextUserId, text);
                addUserToCloud(newUser);

                // Intent to take user back to MainWindowClass
                Intent backIntent = new Intent(addToCloudActivity.this,
                        MainWindowActivity.class);
                backIntent.putExtra(EXTRA_MESSAGE, userId);
                startActivity(backIntent);
            } else {
                addUserText.setError("T??m?? k??ytt??j?? l??ytyy jo!");
            }
        } else {
            addUserText.setError("Kirjoita k??ytt??j??n nimi!");
        }
    }

    // Function for addTask button
    public void onAddTaskClick(View view) {
        String text = addTaskText.getText().toString();
        // Check if string is empty and nothing happens if it is
        if (!text.equals("")) {
            // Check if name is already saved to cloud
            if (!tasks.contains(text)) {
                // Add new task to cloud
                Task newTask = new Task(nextTaskId, text);
                addTaskToCloud(newTask);

                // Intent to take user back to MainWindowClass
                Intent backIntent = new Intent(addToCloudActivity.this,
                        MainWindowActivity.class);
                backIntent.putExtra(EXTRA_MESSAGE, userId);
                startActivity(backIntent);
            } else {
                addTaskText.setError("T??m?? teht??v?? l??ytyy jo!");
            }
        } else {
            addTaskText.setError("Kirjoita teht??v??n nimi!");
        }
    }

    // Function to get amount of clients and clients names from cloud
    private void amountOfClients() {
        nextCustomerId = 0;
        mFirestore.collection("clients").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value != null) {
                    for (DocumentSnapshot snapshot : value) {
                        Client client = snapshot.toObject(Client.class);
                        clients.add(client.getName());
                        if (nextCustomerId < client.getId()) {
                            nextCustomerId = client.getId();
                        }
                    }
                    nextCustomerId++;
                }
            }
        });
    }

    // Function to get amount of users and users names from cloud
    private void amountOfUsers() {
        nextUserId = 0;
        mFirestore.collection("users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value != null) {
                    for (DocumentSnapshot snapshot : value) {
                        User user = snapshot.toObject(User.class);
                        users.add(user.getName());
                        if (nextUserId < user.getId()) {
                            nextUserId = user.getId();
                        }
                    }
                    nextUserId++;
                }
            }
        });
    }

    // Function to get amount of tasks and tasks names from cloud
    private void amountOfTasks() {
        nextTaskId = 0;
        mFirestore.collection("tasks").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value != null) {
                    for (DocumentSnapshot snapshot : value) {
                        Task task = snapshot.toObject(Task.class);
                        tasks.add(task.getName());
                        if (nextTaskId < task.getId()) {
                            nextTaskId = task.getId();
                        }
                    }
                    nextTaskId++;
                }
            }
        });
    }

    // Function to add user to cloud
    public void addUserToCloud(User user) {
        mFirestore.collection("users").
                document(Integer.toString(user.getId())).set(user);
    }

    // Function to add client to cloud
    public void addClientToCloud(Client client) {
        mFirestore.collection("clients").
                document(Integer.toString(client.getId())).set(client);
    }

    // Function to add task to cloud
    public void addTaskToCloud(Task task) {
        mFirestore.collection("tasks").
                document(Integer.toString(task.getId())).set(task);
    }
}