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

public class Schema extends ListActivity {
    String PREFS_NAME = "MyPrefsFile";
    String storedUrl = "";
    ArrayList<SchemaItem> schemaItems = new ArrayList<SchemaItem>();

    public class SchemaItem {
        String date;
        String starttime;
        String endtime;
        String location;
        String program;
        String course;
        String sign;
        String moment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Try to get the stored LoginString
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        storedUrl = settings.getString("SCHEMASTRING", "");

        // If nothing found: show message and send user to pickschema screen
        if (storedUrl == "") {
            Log.i("SCHEMA", "SCHEMASTR är tom");
            AlertDialog.Builder myBuild = new AlertDialog.Builder(this);
            myBuild.setTitle("Inget schema valt");
            myBuild.setMessage("Du har inte valt schema ännu. Nu kommer du dit.");
            myBuild.setNeutralButton("OK", null);
            myBuild.show();

            Intent goToPickschema = new Intent(getApplicationContext(), PickSchema.class);
            startActivity(goToPickschema);
        } else {
            new SchemaDownloader().execute();
        }
    }

    public class SchemaDownloader extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progressDialog;

        protected void onPreExecute() {
            Log.i("pre:", "inne i pre");
            progressDialog = ProgressDialog.show(Schema.this, "", "Laddar Schema", true);
        }

        @Override
        protected void onPostExecute(Void result) {
            Log.i("SchemaDownloader", "AsyncTask done. ");
            progressDialog.dismiss();
            setListAdapter(new SchemaListAdaptor(Schema.this,
                    R.layout.schema, schemaItems));
        }

        protected Void doInBackground(Void... arg0) {

            try {
                XMLParser parser = new XMLParser();
                String xml = parser.getXmlFromUrl(storedUrl); // getting XML
                Document doc = parser.getDomElement(xml); // getting DOM element
                NodeList nl = doc.getElementsByTagName("booking");
                // looping through all item nodes <item>
                for (int i = 0; i < nl.getLength(); i++) {
                    SchemaItem si = new SchemaItem();
                    Element e = (Element) nl.item(i);

                    si.date = parser.getValue(e, "date");
                    si.starttime = parser.getValue(e, "starttime");
                    si.endtime = parser.getValue(e, "endtime");
                    si.location = parser.getValue(e, "location");
                    si.program = parser.getValue(e, "program");
                    si.course = parser.getValue(e, "course");
                    si.sign = parser.getValue(e, "sign");
                    si.moment = parser.getValue(e, "moment");

                    // adding HashList to ArrayList
                    schemaItems.add(si);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("SCHEMA", "Error loading XML");
            }
            return null;
        }
    }

    private class SchemaListAdaptor extends ArrayAdapter<SchemaItem> {
        private ArrayList<SchemaItem> sch_items;

        public SchemaListAdaptor(Context context, int textViewResourceId,
                                 ArrayList<SchemaItem> items) {
            super(context, textViewResourceId, items);
            this.sch_items = items;

        }

        private String prettyDate(String oldDate) {
            String month = "", day;
            int m;

            m = Integer.parseInt(oldDate.substring(4, 6));
            day = oldDate.substring(6, 8);
            switch (m) {
                case 1:
                    month = "Jan";
                    break;
                case 2:
                    month = "Feb";
                    break;
                case 3:
                    month = "Mar";
                    break;
                case 4:
                    month = "Apr";
                    break;
                case 5:
                    month = "Maj";
                    break;
                case 6:
                    month = "Jun";
                    break;
                case 7:
                    month = "Jul";
                    break;
                case 8:
                    month = "Aug";
                    break;
                case 9:
                    month = "Sep";
                    break;
                case 10:
                    month = "Okt";
                    break;
                case 11:
                    month = "Nov";
                    break;
                case 12:
                    month = "Dec";
                    break;
            }
            return day + " " + month;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.schema, null);
            }
            SchemaItem si = schemaItems.get(position);
            TextView tvDate = (TextView) v.findViewById(R.id.schemaDate);
            TextView tvTime = (TextView) v.findViewById(R.id.schemaTime);
            TextView tvLocation = (TextView) v.findViewById(R.id.schemaLocation);
            TextView tvProgram = (TextView) v.findViewById(R.id.schemaProgram);
            TextView tvCourse = (TextView) v.findViewById(R.id.schemaCourse);
            TextView tvSign = (TextView) v.findViewById(R.id.schemaSign);
            TextView tvMoment = (TextView) v.findViewById(R.id.schemaMoment);

            tvDate.setText(prettyDate(si.date));
            tvTime.setText(si.starttime + "-" + si.endtime);
            tvLocation.setText(si.location);
            tvProgram.setText("Program: " + si.program);
            tvCourse.setText("Kurs: " + si.course);
            tvSign.setText("Lärare: " + si.sign);
            tvMoment.setText(si.moment);

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
