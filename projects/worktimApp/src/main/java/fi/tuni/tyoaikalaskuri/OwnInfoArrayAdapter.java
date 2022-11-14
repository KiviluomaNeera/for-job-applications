package fi.tuni.tyoaikalaskuri;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/*
Class for listView to show works
 */
public class OwnInfoArrayAdapter extends ArrayAdapter<Work> {

    public OwnInfoArrayAdapter(Context context, int resource, List<Work> objects) {
        super(context, resource, objects);
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
        TextView locationText = (TextView) convertView.findViewById(R.id.workLocationText);
        TextView infoText = (TextView) convertView.findViewById(R.id.workInfoTextView);

        Work work = getItem(position);

        taskText.setVisibility(View.VISIBLE);
        taskText.setText(work.getTaskName());
        clientText.setVisibility(View.VISIBLE);
        clientText.setText(work.getClientName());

        String date = work.getDay() + "." + work.getMonth() + "." + work.getYear();
        dateText.setVisibility(View.VISIBLE);
        dateText.setText(date);

        durationText.setVisibility(View.VISIBLE);
        int hours = work.getDuration() / 60;
        int minutes = work.getDuration() - hours * 60;
        durationText.setText(hours + "h " + minutes + "min ");

        if (work.getTaskId() == 1 || work.getTaskId() == 2) {
            String location = String.format("Alkusijainti: %.3f, %.3f%nLoppusijainti: %.3f, %.3f",
                    work.getStartLocationLatitude(), work.getStartLocationLongitude(),
                    work.getEndLocationLatitude(), work.getEndLocationLongitude());
            locationText.setVisibility(View.VISIBLE);
            locationText.setText(location);
        } else {
            locationText.setVisibility(View.INVISIBLE);
        }

        if (work.getInfo().equals("")) {
            infoText.setVisibility(View.INVISIBLE);
        } else {
            infoText.setVisibility(View.VISIBLE);
            infoText.setText(work.getInfo());
        }

        return convertView;
    }
}
