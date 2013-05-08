package org.crille.hvdagen;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class SchemaDetail extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.schema_detail);
        Bundle in = getIntent().getExtras();
        TextView tvDate = (TextView) findViewById(R.id.schema_detail_date);
        TextView tvStartTime = (TextView) findViewById(R.id.schema_detail_starttime);
        TextView tvEndTime = (TextView) findViewById(R.id.schema_detail_endtime);
        TextView tvLocation = (TextView) findViewById(R.id.schema_detail_location);
        TextView tvProgram = (TextView) findViewById(R.id.schema_detail_program);
        TextView tvCourse = (TextView) findViewById(R.id.schema_detail_course);
        TextView tvSign = (TextView) findViewById(R.id.schema_detail_sign);
        TextView tvMoment = (TextView) findViewById(R.id.schema_detail_moment);

        tvDate.setText("Datum: " + in.getString("date"));
        tvStartTime.setText("Starttid: " + in.getString("starttime"));
        tvEndTime.setText("Sluttid: " + in.getString("endtime"));
        tvLocation.setText("Sal: " + in.getString("location"));
        tvProgram.setText("Program: " + in.getString("program"));
        tvCourse.setText("Kurs: " + in.getString("course"));
        tvSign.setText("Lärare: " + in.getString("sign"));
        tvMoment.setText("Moment: " + in.getString("moment"));
    }
}
