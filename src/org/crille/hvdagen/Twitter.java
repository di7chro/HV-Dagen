package org.crille.hvdagen;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class Twitter extends ListActivity {
    String searchString;
    private ArrayList<Tweet> tweets = new ArrayList<Tweet>();

    public class Tweet {
        String from;
        String text;
        String date;
        String pic;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new TwitterFeedDownloader().execute();
        Intent in = getIntent();
        searchString = in.getExtras().getString("STRING");
    }

    private class TwitterFeedDownloader extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;

        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(Twitter.this, "",
                    "Laddar Tweets", true);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            /* "http://search.twitter.com/search.json?q=from:University_West"); */
            try {
                HttpClient hc = new DefaultHttpClient();
                Log.i("===========", searchString);
                HttpGet get = new HttpGet(searchString);
                HttpResponse rp = hc.execute(get);
                if (rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    String result = EntityUtils.toString(rp.getEntity());
                    JSONObject root = new JSONObject(result);
                    JSONArray sessions = root.getJSONArray("results");
                    for (int i = 0; i < sessions.length(); i++) {
                        JSONObject session = sessions.getJSONObject(i);
                        Tweet tweet = new Tweet();
                        tweet.text = session.getString("text");
                        tweet.from = session.getString("from_user");
                        tweet.date = prettyfyDate(session
                                .getString("created_at"));
                        tweet.pic = session.getString("profile_image_url");
                        //tweet.url = session.getString("url");
                        tweets.add(tweet);
                    }
                }
            } catch (Exception e) {
                Log.e("TwitterFeedActivity", "Error loading JSON", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            progressDialog.dismiss();
            setListAdapter(new TweetListAdaptor(Twitter.this,
                    R.layout.twitter, tweets));
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
                "EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

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
     * Laddar hem Twitter-bilden
     *
     * @author imcoh
     */
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage = (ImageView) findViewById(R.id.twitterPic);

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    /**
     * Stoppar in alla funna resultat i listan
     *
     * @author imcoh
     */
    private class TweetListAdaptor extends ArrayAdapter<Tweet> {
        private ArrayList<Tweet> tweets;

        public TweetListAdaptor(Context context, int textViewResourceId,
                                ArrayList<Tweet> items) {
            super(context, textViewResourceId, items);
            this.tweets = items;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.twitter, null);
            }
            Tweet o = tweets.get(position);
            TextView tvText = (TextView) v.findViewById(R.id.tweet_text);
            TextView tvDate = (TextView) v.findViewById(R.id.tweet_date);
            TextView tvFrom = (TextView) v.findViewById(R.id.tweet_from);
            Log.i("Twitterbild", o.pic);
            // new DownloadImageTask((ImageView)
            // findViewById(R.id.twitterPic)).execute(o.pic);
            tvFrom.setText("@" + o.from);
            tvDate.setText(o.date);
            tvText.setText(o.text);

            return v;
        }
    }
}