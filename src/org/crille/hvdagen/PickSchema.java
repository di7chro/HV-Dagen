package org.crille.hvdagen;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class PickSchema extends Activity {
    public static final String PREFS_NAME = "MyPrefsFile";
    private static final String schemaURL = "http://public.crille.org/schema_parse.php?url=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pickschema);
        final EditText schema = (EditText) findViewById(R.id.etPickSchema);

        Button pickSchemaButton = (Button) findViewById(R.id.pickButton);
        pickSchemaButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                String schemaStr = schema.getText().toString();

                // Appends the hash to the URL
                String schemaString = schemaURL + schemaStr;

                // Save the schemaString to preferences
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("SCHEMASTRING", schemaString);
                editor.commit();
                Log.i("SKREV SCH", schemaString);
                // Start the MinDag activity
                Intent goToSchema = new Intent(getApplicationContext(), Schema.class);
                startActivity(goToSchema);
            }
        });
    }
}

