package org.crille.hvdagen;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
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
import java.util.ArrayList;

public class MinDag extends ListActivity {
    String PREFS_NAME = "MyPrefsFile";
    String storedUrl = "";
    ArrayList<MindagItem> mindagItems = new ArrayList<MindagItem>();

    public class MindagItem {
        String course;
        String time;
        String location;
        String description;
        String link;
        String tag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Try to get the stored LoginString
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        storedUrl = settings.getString("LOGINSTRING", "");

        // If nothing found: show message and send user to loginscreen
        if (storedUrl == "") {
            Log.i("MINDAG", "url är tom");
            /*
            Om man vill kan man ha en ruta här som säger att man inte loggat in,
            men jag tycker att det är snabbare om man helt enkelt kommer till målet direkt.

            AlertDialog.Builder myBuild = new AlertDialog.Builder(this);
            myBuild.setTitle("Inget sparat");
            myBuild.setMessage("Du har inte loggat in ännu. Nu kommer du dit.");
            myBuild.setNeutralButton("OK", null);
            myBuild.show();
            */
            Intent goToLogin = new Intent(getApplicationContext(), Login.class);
            startActivity(goToLogin);
        } else {
            new MinDagDownloader().execute();
        }
    }

    public class MinDagDownloader extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;

        protected void onPreExecute() {
            Log.i("pre:", "inne i pre");
            progressDialog = ProgressDialog.show(MinDag.this, "", "Laddar Mindag", true);
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.i("SchemaDownloader", "AsyncTask done. ");
            progressDialog.dismiss();
            setListAdapter(new MindagListAdaptor(MinDag.this,
                    R.layout.mindag, mindagItems));
        }

        protected Void doInBackground(Void... arg0) {

            try {
                XMLParser parser = new XMLParser();
                String xml = parser.getXmlFromUrl(storedUrl); // getting XML
                Document doc = parser.getDomElement(xml); // getting DOM element
                NodeList nl = doc.getElementsByTagName("item");
                // looping through all item nodes <item>
                for (int i = 0; i < nl.getLength(); i++) {
                    MindagItem mdi = new MindagItem();
                    Element e = (Element) nl.item(i);

                    mdi.course = parser.getValue(e, "course");
                    mdi.time = parser.getValue(e, "time");
                    mdi.location = parser.getValue(e, "location");
                    mdi.description = parser.getValue(e, "description");
                    mdi.link = parser.getValue(e, "link");
                    mdi.tag = "#" + parser.getValue(e, "tag");

                    // adding HashList to ArrayList
                    mindagItems.add(mdi);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("MINDAG", "Error loading XML");
            }
            return null;
        }
    }

    private class MindagListAdaptor extends ArrayAdapter<MindagItem> {
        private ArrayList<MindagItem> tweets;

        public MindagListAdaptor(Context context, int textViewResourceId,
                                 ArrayList<MindagItem> items) {
            super(context, textViewResourceId, items);
            this.tweets = items;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.mindag, null);
            }
            MindagItem mdi = mindagItems.get(position);
            TextView tvCourse = (TextView) v.findViewById(R.id.mindagCourse);
            TextView tvTime = (TextView) v.findViewById(R.id.mindagTime);
            TextView tvLocation = (TextView) v.findViewById(R.id.mindagLocation);
            TextView tvDescription = (TextView) v.findViewById(R.id.mindagDescription);
            TextView tvLink = (TextView) v.findViewById(R.id.mindagLink);
            TextView tvTag = (TextView) v.findViewById(R.id.mindagTag);

            tvCourse.setText(mdi.course);
            tvTime.setText(mdi.time);
            tvLocation.setText(mdi.location);
            tvDescription.setText(mdi.description);
            tvLink.setText(mdi.link);
            tvTag.setText(mdi.tag);

            return v;
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