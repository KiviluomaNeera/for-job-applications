package fi.tuni.worktimeapp;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import android.widget.EditText;
import android.widget.ImageView;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Objects;

/*
ACtivity for logging in
 */
public class MainActivity extends AppCompatActivity {
    private EditText idText;
    private ArrayList<Integer> users = new ArrayList<>();
    private Context context;
    private FirebaseFirestore mFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        mFirestore = FirebaseFirestore.getInstance();

        // Get user ids from cloud to users list
        getValuesFromCloud();

        // Set logo to UI
        idText = findViewById(R.id.editTextId);
        ImageView imageView = findViewById(R.id.image);
        Glide.with(context).load(R.drawable.logo).into(imageView);
    }

    // Functionality for log in button
    public void onLogInClick(View view) {
        String text = idText.getText().toString();

        // Check if EditText is empty
        if (TextUtils.isEmpty(text)) {
            idText.setError("Anna käyttäjätunnus!");
        } else {
            // Check if given id is known
            Integer id = Integer.parseInt(text);
            boolean isFound = false;
            for (Integer user : users) {
                if (Objects.equals(user, id)) {
                    // Intent to take user to MainWindowActivity
                    Intent intent = new Intent(this, MainWindowActivity.class);
                    // Intent takes user id to MainWindowActivity
                    intent.putExtra(EXTRA_MESSAGE, text);
                    startActivity(intent);
                    isFound = true;
                    break;
                }
            }

            // Show error to user if id can't be found
            if (!isFound) {
                idText.setError("Käyttäjätunnusta ei löydy!");
            }
        }
    }

    // Function to get user ids from cloud
    public void getValuesFromCloud() {
        mFirestore.collection("users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                users.clear();
                if (value != null) {
                    for (DocumentSnapshot snapshot : value) {
                        User user = snapshot.toObject(User.class);
                        users.add(user.getId());
                    }
                }
            }
        });
    }
}