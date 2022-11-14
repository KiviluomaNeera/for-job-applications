package fi.tuni.tyoaikalaskuri;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
Class to show user's works
 */
public class OwnInfoActivity extends AppCompatActivity {
    private String userId;
    private ArrayList<Work> works = new ArrayList<>();
    private ListView listView;
    private ArrayAdapter ownInfoAdapter;
    private FirebaseFirestore mFirestore;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_own_info);

        Intent intent = getIntent();
        userId = intent.getStringExtra(EXTRA_MESSAGE);

        mFirestore = FirebaseFirestore.getInstance();
        context = this;

        listView = findViewById(R.id.ownInfoListView);

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
                        if (work.getUserId() == Integer.parseInt(userId) && work.getDuration() != 0) {
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
}