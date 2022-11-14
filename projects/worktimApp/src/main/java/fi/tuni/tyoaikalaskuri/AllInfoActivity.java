package fi.tuni.tyoaikalaskuri;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

/*
Class to show every user's works
 */
public class AllInfoActivity extends AppCompatActivity {
    private String userId;
    private ArrayList<User> users = new ArrayList<>();
    private ArrayList<Work> works = new ArrayList<>();
    private ListView userListView;
    private ArrayAdapter userAdapter;

    private FirebaseFirestore mFirestore;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_info);

        // Get intent from MainWindowActivity
        Intent intent = getIntent();
        userId = intent.getStringExtra(EXTRA_MESSAGE);

        context = this;
        mFirestore = FirebaseFirestore.getInstance();

        userListView = findViewById(R.id.allInfoListView);

        // Touchlistener so two listviews where one is inside the other can be scrolled
        userListView.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        getUsersFromCloud();
    }

    // Function for allInfoBack button
    public void onAllInfoBackClick(View view) {
        // Intent to get user back to MainWindowActivity
        Intent intent = new Intent(AllInfoActivity.this, MainWindowActivity.class);
        intent.putExtra(EXTRA_MESSAGE, userId);
        startActivity(intent);
    }

    // Function to get users from cloud
    private void getUsersFromCloud() {
        mFirestore.collection("users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value != null) {
                    for (DocumentSnapshot snapshot : value) {
                        User newUser = snapshot.toObject(User.class);
                        users.add(newUser);
                    }
                    getWorksFromCloud();
                }
            }
        });
    }

    // Function to get finished works from cloud
    private void getWorksFromCloud() {
        mFirestore.collection("works").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value != null) {
                    for (DocumentSnapshot snapshot : value) {
                        Work newWork = snapshot.toObject(Work.class);
                        if (newWork.getDuration() > 0) {
                            works.add(newWork);
                        }
                    }
                    // Creating arrayadapter for users
                    userAdapter = new AllInfoArrayAdapter(context, R.layout.allinfo_item, users, works);
                    userAdapter.notifyDataSetChanged();
                    userListView.setAdapter(userAdapter);
                }
            }
        });
    }
}