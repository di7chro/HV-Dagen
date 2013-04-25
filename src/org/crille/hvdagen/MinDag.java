package org.crille.hvdagen;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MinDag extends ListActivity {
    public static final String PREFS_NAME = "MyPrefsFile";

    // Create a local ArrayList
    ArrayList<HashMap<String, String>> mindagItems = new ArrayList<HashMap<String, String>>();

    /**
     * Define the keys in the XML-feed we're interested in
     */
    static final String KEY_ITEM = "item"; // parent node
    static final String KEY_LINK = "link";
    static final String KEY_TITLE = "title";
    static final String KEY_DESC = "shortDescription";
    //static final String KEY_DATE = "pubDate";
    static final String KEY_TAG = "tag";


    /*
         * Fires up the activity_myday and waits for the Login-button to be pressed.
         * Then it starts the ASynkTask to gather the XML-feed in the background.
         * When this is done we can populate the ListAdapter with the stuff from the
         * feed.
         */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Try to get the stored LoginString
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String storedUrl = settings.getString("LOGINSTRING", "");

        // If nothing found: show message and send user to loginscreen
        if (storedUrl == "") {
            Log.i("MINDAG", "url är tom");
            AlertDialog.Builder myBuild = new AlertDialog.Builder(this);
            myBuild.setTitle("Inget sparat");
            myBuild.setMessage("Du har inte loggat in ännu. Nu kommer du dit.");
            myBuild.setNeutralButton("OK", null);
            myBuild.show();

            Intent goToLogin = new Intent(getApplicationContext(), Login.class);
            startActivity(goToLogin);
        } else {

            try {
                // Initiate the ASynkTask
                MinDagDownloader md_poster = new MinDagDownloader();

                // Start the task and give it the URL as input
                md_poster.execute(storedUrl);


                // Fill the ArrayList with the items we got from the ASynkTask
                try {
                    mindagItems = md_poster.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }

                // Add the menuItems to our ListView
                ListAdapter adapter = new SimpleAdapter(this, mindagItems,
                        R.layout.mindag_listan, new String[]{KEY_TITLE,
                        KEY_LINK, KEY_DESC, KEY_TAG}, new int[]{
                        R.id.mydayTitle, R.id.mydayLink,
                        R.id.mydayDescription,
                        R.id.mydayTag});
                setListAdapter(adapter);

                ListView lv = (ListView) findViewById(R.id.myDayList);

            } catch (Exception e) {
                System.out
                        .println("============= MOTHER OF ALL ERRORS IN MYDAY ================");
                e.printStackTrace();
            }

        }

    }

    public class MinDagDownloader extends AsyncTask<String, Void, ArrayList> {
        private ProgressDialog progressDialog;

        protected void onPreExecute() {
            Log.i("pre:", "inne i pre");
            progressDialog = ProgressDialog.show(MinDag.this, "", "Laddar Mindag", true);
        }

        private String deUglify(String description) {
            Spanned temp;
            temp = Html.fromHtml(description);

            return temp.toString();
        }

        private String prettyfyDate(String theDate) {
            SimpleDateFormat format = new SimpleDateFormat(
                    "EEE, dd MMM yyyy HH:mm:ss z");

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
            /* Kunde inte parse'a datum, sätt det till Thu, 07 Mar */
                theDate = theDate.substring(0, 11);
                e.printStackTrace();
            }

            return theDate;
        }

        /**
         * Does all the magic in getting an XML-file from the network, parses it, an
         * filling an ArrayList containing an HashMap of KEY-VALUE-pairs
         *
         * @param theURL of the feed
         * @return The ArrayList called menuItems containing the feed
         */
        protected ArrayList doInBackground(String... theURL) {
            String theFeed = theURL[0];

            XMLParser parser = new XMLParser();
            String xml = parser.getXmlFromUrl(theFeed); // getting XML
            Document doc = parser.getDomElement(xml); // getting DOM element
            NodeList nl = doc.getElementsByTagName(KEY_ITEM);
            // looping through all item nodes <item>
            for (int i = 0; i < nl.getLength(); i++) {
                // creating new HashMap
                HashMap<String, String> map = new HashMap<String, String>();
                Element e = (Element) nl.item(i);
                // adding each child node to HashMap key => value
                map.put(KEY_TITLE, parser.getValue(e, KEY_TITLE));
                map.put(KEY_TAG, "#" + parser.getValue(e, KEY_TAG));
                map.put(KEY_LINK, parser.getValue(e, KEY_LINK));
                map.put(KEY_DESC, deUglify(parser.getValue(e, KEY_DESC)));
                //map.put(KEY_DATE, prettyfyDate(parser.getValue(e, KEY_DATE)));

                // adding HashList to ArrayList
                mindagItems.add(map);
            }
            return mindagItems;
        }

        /**
         * Dummy-method just called after the ASynkTask is done
         * <p/>
         * param result
         */
        @Override
        protected void onPostExecute(ArrayList result) {
            progressDialog.dismiss();
            Log.i("MinDagDownloader", "AsyncTask done. ");
        }
    }

    public class XMLParser {
        /**
         * Just an empty constructor
         */
        public XMLParser() {

        }

        /**
         * Getting XML from URL making HTTP request
         *
         * @param url string
         */
        public String getXmlFromUrl(String url) {
            String xml = null;

            try {
                // defaultHttpClient
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url);
                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                xml = EntityUtils.toString(httpEntity);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // return XML
            return xml;
        }

        /**
         * Getting XML DOM element
         * <p/>
         * param XML string
         * return The DOM-object
         */
        public Document getDomElement(String xml) {
            Document doc; // = null;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            try {

                DocumentBuilder db = dbf.newDocumentBuilder();

                InputSource is = new InputSource();
                is.setCharacterStream(new StringReader(xml));
                doc = db.parse(is);

            } catch (ParserConfigurationException e) {
                Log.e("Error: ", e.getMessage());
                return null;
            } catch (SAXException e) {
                Log.e("Error: ", e.getMessage());
                return null;
            } catch (IOException e) {
                Log.e("Error: ", e.getMessage());
                return null;
            }

            return doc;
        }

        /**
         * Gets the value of the chosen node
         *
         * @param elem element
         * @return The value of the Node
         */
        public final String getElementValue(Node elem) {
            Node child;
            if (elem != null) {
                if (elem.hasChildNodes()) {
                    for (child = elem.getFirstChild(); child != null; child = child
                            .getNextSibling()) {
                        if (child.getNodeType() == Node.TEXT_NODE) {
                            return child.getNodeValue();
                        }
                    }
                }
            }
            return "";
        }

        /**
         * Getting node value
         * <p/>
         * param Element node
         * param key     string
         */
        public String getValue(Element item, String str) {
            NodeList n = item.getElementsByTagName(str);
            return this.getElementValue(n.item(0));
        }
    }
}