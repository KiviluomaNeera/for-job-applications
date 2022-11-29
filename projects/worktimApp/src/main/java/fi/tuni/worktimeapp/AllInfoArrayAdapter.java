package fi.tuni.worktimeapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/*
Class for AllInfo listView's ArrayAdapter
 */
public class AllInfoArrayAdapter extends ArrayAdapter<User> {
    private ArrayList<Work> works = new ArrayList<>();
    private ArrayAdapter workAdapter;
    private Context context;

    public AllInfoArrayAdapter(Context context, int resource, List<User> objects, ArrayList<Work> works) {
        super(context, resource, objects);
        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();
        this.works = works;
        this.context = context;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = ((Activity) getContext()).getLayoutInflater()
                    .inflate(R.layout.allinfo_item, parent, false);
        }

        TextView userName = (TextView) convertView.findViewById(R.id.UserNameText);
        TextView durationText = (TextView) convertView.findViewById(R.id.durationsText);
        TextView durationLastMonthText = (TextView) convertView.findViewById(R.id.durationsLastMontText);
        ListView workView = (ListView) convertView.findViewById(R.id.allWorkListView);

        workView.setBackgroundColor(Color.rgb(246, 243, 243));

        // Touchlistener so two listviews where one is inside the other can be scrolled
        workView.setOnTouchListener(new View.OnTouchListener() {
            // Setting on Touch Listener for handling the touch inside ScrollView
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // Disallow the touch request for parent scroll on touch of child view
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        User user = getItem(position);
        workView.setVisibility(View.VISIBLE);

        ZonedDateTime time = ZonedDateTime.now(ZoneId.of("Europe/Helsinki"));
        int duration = 0;
        int durationLastMonth = 0;

        // Get user's work and duration of works in this and last month
        ArrayList<Work> usersWork = new ArrayList<>();
        for (Work work : works) {
            if (work.getUserId() == user.getId()) {
                usersWork.add(work);
                if (work.getMonth() == time.getMonthValue()) {
                    duration += work.getDuration();
                } else if (work.getMonth() == time.getMonthValue() - 1) {
                    durationLastMonth += work.getDuration();
                }
            }
        }

        // Sorting users work by date to ascending order
        usersWork.sort((a, b) -> {
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

        workAdapter = new OwnInfoArrayAdapter(context, R.layout.info_item, usersWork);
        workAdapter.notifyDataSetChanged();
        AllInfoArrayAdapter.this.notifyDataSetChanged();
        workView.setAdapter(workAdapter);

        userName.setVisibility(View.VISIBLE);
        userName.setText(user.getName());

        // Calculate work hours from ongoing month
        durationText.setVisibility(View.VISIBLE);
        int hours = duration / 60;
        int minutes = duration - hours * 60;
        durationText.setText("Työtunteja tässä kuussa: " + hours + "h " + minutes + "min ");

        // Calculate work hours from last month
        durationLastMonthText.setVisibility(View.VISIBLE);
        int hoursLast = durationLastMonth / 60;
        int minutesLast = durationLastMonth - hoursLast * 60;
        durationLastMonthText.setText
                ("Työtunteja viime kuussa: " + hoursLast + "h " + minutesLast + "min ");

        return convertView;
    }
}
