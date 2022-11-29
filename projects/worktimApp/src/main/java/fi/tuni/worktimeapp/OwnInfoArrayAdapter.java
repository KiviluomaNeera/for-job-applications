package fi.tuni.worktimeapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.provider.AlarmClock.EXTRA_MESSAGE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;

import java.util.ArrayList;
import java.util.List;

/*
Class for listView to show works
 */
public class OwnInfoArrayAdapter extends ArrayAdapter<Work> {
    private Context context;
    public OwnInfoArrayAdapter(Context context, int resource, List<Work> objects) {
        super(context, resource, objects);
        this.context = context;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = ((Activity)getContext()).getLayoutInflater()
                    .inflate(R.layout.info_item, parent, false);
        }

        TextView taskText = (TextView) convertView.findViewById(R.id.workTaskText);
        TextView clientText = (TextView) convertView.findViewById(R.id.workClientText);
        TextView dateText = (TextView) convertView.findViewById(R.id.workDateText);
        TextView durationText = (TextView) convertView.findViewById(R.id.workDurationText);
        TextView startTimeText = (TextView) convertView.findViewById(R.id.workStartTime);
        TextView endTimeText = (TextView) convertView.findViewById(R.id.workEndTime);
        TextView infoText = (TextView) convertView.findViewById(R.id.workInfoTextView);
        Button mapButton = (Button) convertView.findViewById(R.id.mapButton);

        Work work = getItem(position);

        taskText.setVisibility(View.VISIBLE);
        taskText.setText(work.getTaskName());
        clientText.setVisibility(View.VISIBLE);
        clientText.setText(work.getClientName());

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

        dateText.setVisibility(View.VISIBLE);
        dateText.setText(date);

        durationText.setVisibility(View.VISIBLE);
        int hours = work.getDuration() / 60;
        int minutes = work.getDuration() - hours * 60;
        durationText.setText(hours + "h " + minutes + "min ");

        startTimeText.setVisibility(View.VISIBLE);
        if (work.getHour() < 10 && work.getMinute() < 10) {
            startTimeText.setText("0" + work.getHour() + ":0" + work.getMinute());
        } else if (work.getHour() < 10) {
            startTimeText.setText("0" + work.getHour() + ":" + work.getMinute());
        } else if (work.getMinute() < 10) {
            startTimeText.setText(work.getHour() + ":0" + work.getMinute());
        } else {
            startTimeText.setText(work.getHour() + ":" + work.getMinute());
        }

        endTimeText.setVisibility(View.VISIBLE);
        int endHour = work.getHour() + hours;
        int endminute = 61;
        int breaks = work.getBreakAmount();

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

        if (endHour < 10 && endminute < 10) {
            endTimeText.setText("0" + endHour + ":0" + endminute);
        } else if (endHour < 10) {
            endTimeText.setText("0" + endHour + ":" + endminute);
        } else if (endminute < 10) {
            endTimeText.setText(endHour + ":0" + endminute);
        } else {
            endTimeText.setText(endHour + ":" + endminute);
        }

        if (work.getInfo().equals("")) {
            infoText.setVisibility(View.INVISIBLE);
        } else {
            infoText.setVisibility(View.VISIBLE);
            infoText.setText(work.getInfo());
        }

        if (work.getTaskId() == 1 || work.getTaskId() == 2) {
            mapButton.setVisibility(View.VISIBLE);
        } else {
            mapButton.setVisibility(View.INVISIBLE);
        }

        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OwnInfoActivity.onButtonShowPopupWindowClick(context, view, work);
            }
        });

        return convertView;
    }
}
