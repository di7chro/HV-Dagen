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
        TextView tvMoment = (TextView) findViewById(R.id.schema_detail_moment);
        TextView tvCourse = (TextView) findViewById(R.id.schema_detail_course);
        tvMoment.setText(in.getString("moment"));
        tvCourse.setText(in.getString("course"));
    }
}
