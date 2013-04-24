package org.crille.hvdagen;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MinDag extends ListActivity {
    private ArrayList<MinDagPost> md_array = new ArrayList<MinDagPost>();
    String mindag_url;

    public class MinDagPost {
        String titel;
        String tagg;
        String description;
        String datum;
        String link;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new MinDagDownloader().execute();
        Intent in = getIntent();
        mindag_url = in.getExtras().getString("STRING");
    }

    private class MinDagDownloader extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;

        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(MinDag.this, "",
                    "Laddar Mindag", true);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {
                HttpClient hc = new DefaultHttpClient();
                Log.i("===========", mindag_url);
                HttpGet get = new HttpGet(mindag_url);
                HttpResponse rp = hc.execute(get);
                if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    String result = EntityUtils.toString(rp.getEntity());
                    JSONObject root = new JSONObject(result);
                    JSONArray sessions = root.getJSONArray("results");
                    for (int i = 0; i < sessions.length(); i++) {
                        JSONObject session = sessions.getJSONObject(i);
                        MinDagPost md_post = new MinDagPost();
                        md_post.titel = session.getString("text");
                        md_post.tagg = session.getString("from_user");
                        md_post.datum = prettyfyDate(session
                                .getString("created_at"));
                        md_post.description = session.getString("profile_image_url");
                        md_post.link = session.getString("profile_image_url");
                        md_array.add(md_post);
                    }
                }
            } catch (Exception e) {
                Log.e("MinDagActivity", "Error loading JSON", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            progressDialog.dismiss();
            setListAdapter(new MinDagListAdaptor(MinDag.this,
                    R.layout.mindag, md_array));
        }
    }

    /**
     * Preserves the important part of the publishing time of an element in the
     * feed. Counting the days since the notice has been published.
     *
     * @param theDate (Format: Thu, 21 Feb 2013 09:27:56 +0000)
     *                Format:  Thu, 07 Mar 2013 10:09:52 +0000 från telefon)
     *                Koden:   EEE, dd MMM yyyy HH:mm:ss z
     * @return Polished date
     */
    private String prettyfyDate(String theDate) {
        SimpleDateFormat format = new SimpleDateFormat(
                "EEE, d MMM yyyy HH:mm:ss Z", Locale.getDefault());

        try {

            Date past = format.parse(theDate);
            Date now = new Date();
            long diff = TimeUnit.MILLISECONDS.toMinutes(now.getTime() - past.getTime());

            if (diff < 60)
                theDate = diff + " minuter sedan";
            else if (diff < 60 * 24)
                theDate = diff / 24 + " timmar, " + diff % 60 + " minuter sedan";
            else if (diff < 60 * 60 * 24) {
                if ((diff / 60 / 24) == 1)
                    theDate = diff / 60 / 24 + " dag, " + diff % 24 + " timmar sedan";
                else
                    theDate = diff / 60 / 24 + " dagar, " + diff % 24 + " timmar sedan";
            }
        } catch (ParseException e) {
            e.printStackTrace();
            /* Kunde inte parse'a datum, sätt det till Thu, 07 Mar */
            theDate = theDate.substring(0, 11);
        }

        return theDate;
    }

    /**
     * Stoppar in alla funna resultat i listan
     *
     * @author imcoh
     */
    private class MinDagListAdaptor extends ArrayAdapter<MinDagPost> {
        private ArrayList<MinDagPost> md_array;

        public MinDagListAdaptor(Context context, int textViewResourceId,
                                 ArrayList<MinDagPost> items) {
            super(context, textViewResourceId, items);
            this.md_array = items;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.mindag, null);
            }
            MinDagPost o = md_array.get(position);
            TextView tvTitel = (TextView) v.findViewById(R.id.mindag_titel);
            TextView tvTagg = (TextView) v.findViewById(R.id.mindag_tagg);
            TextView tvDatum = (TextView) v.findViewById(R.id.mindag_datum);
            TextView tvDesc = (TextView) v.findViewById(R.id.mindag_description);
            TextView tvLink = (TextView) v.findViewById(R.id.mindag_link);

            tvTitel.setText(o.titel);
            tvTagg.setText(o.tagg);
            tvDatum.setText(o.datum);
            tvDesc.setText(o.description);
            tvLink.setText(o.link);

            return v;
        }
    }
}
