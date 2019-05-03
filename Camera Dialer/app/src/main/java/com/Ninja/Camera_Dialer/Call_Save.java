package com.Ninja.Camera_Dialer;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;

import static android.Manifest.permission.CALL_PHONE;

import android.provider.ContactsContract;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class Call_Save extends AppCompatActivity {

    ProgressDialog dialog;
    Button call_btn;
    TextView chosen_name;
    TextView chosen_phone;
    static String image_string;
    String Returned_value;
    ArrayList<String> names_list = new ArrayList<String>();
    ArrayList<String> phone_list = new ArrayList<String>();

    @SuppressLint("WrongThread")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call__save);

        // Receive the image file from the main activity

        Uri file;
        Intent myintent = getIntent();
        file = myintent.getParcelableExtra("Image_file");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Bitmap bitmap;

        // Convert the URI image to bitmap then to string
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),file);

            //Compressing Image Before Sending
            int oldwidth=bitmap.getWidth();
            int oldheight=bitmap.getHeight();
            if(oldwidth*oldheight>307200)
            {
                float  ratio= (float)oldwidth/oldheight;
                int newwidth= (int)(ratio*640);
                bitmap = Bitmap.createScaledBitmap(bitmap,newwidth, 640, true);
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos);
            byte[] byteArrayImage = baos.toByteArray();
            image_string = Base64.encodeToString(byteArrayImage, Base64.DEFAULT); //image in String format
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Please Wait Dialog
        dialog = ProgressDialog.show(Call_Save.this, "", "Loading. Please wait...", true);

        // send the request
        send();

        //Phone select
        displayNames();

        //phone select
        displayPhones();

        //Call Button
        call();
    }


    // Call Method
    private void call() {
        call_btn=(Button)findViewById(R.id.Call_btn);
        call_btn.setOnClickListener(new OnClickListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v)
            {
                String number = chosen_phone.getText().toString();
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" +number));
                if (ContextCompat.checkSelfPermission(getApplicationContext(), CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(intent);
                } else {
                    requestPermissions(new String[]{CALL_PHONE}, 1);
                }
            }
        });
    }


    // Printing the phone Numbers on Listview
    private void displayPhones() {
        chosen_phone= (TextView)findViewById(R.id.Display_phone_text);
        ListView PhonesListView = (ListView)findViewById(R.id.Phone_listview);
        final ArrayAdapter<String> adapter_phone = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,phone_list);
        PhonesListView.setAdapter(adapter_phone);
        PhonesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView txt = (TextView) view;
                chosen_phone.setText(txt.getText().toString());

            }
        });
    }

    // Printing the Names on Listview
    private void displayNames() {
        chosen_name= (TextView)findViewById(R.id.Display_name_text);
        ListView NamesListView = (ListView)findViewById(R.id.Names_listview);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,names_list);
        NamesListView.setAdapter(adapter);
        NamesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView txt = (TextView) view;
                chosen_name.setText(txt.getText().toString());

            }
        });
    }

    // Add Contact Method
    public void btnAdd_Contact_onClick(View view) {
        Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
        intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);

        String mPhoneNumber = chosen_phone.getText().toString();
        String mName = chosen_name.getText().toString();

        intent.putExtra(ContactsContract.Intents.Insert.PHONE, mPhoneNumber)
                .putExtra(ContactsContract.Intents.Insert.NAME, mName)
                .putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_HOME);

        startActivity(intent);

    }

    // check network connection
    public boolean checkNetworkConnection() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        boolean isConnected = false;
        if (networkInfo != null && (isConnected = networkInfo.isConnected())) {
            System.out.println("Connected "+networkInfo.getTypeName());

        } else {
            System.out.println("Not Connected");
        }
        return isConnected;
    }


    //Post and Get Request
    private String httpPost(String myUrl) throws IOException, JSONException {
        URL url = new URL(myUrl);

        // 1. create HttpURLConnection
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

        // 2. build JSON object
        JSONObject jsonObject = buidJsonObject();

        // 3. add JSON content to POST request body
        setPostRequestContent(conn, jsonObject);

        // 4. make POST request to the given URL
        conn.connect();

        // 5. return response message

        InputStream in = conn.getInputStream();
        StringBuffer sb = new StringBuffer();
        try {
            int chr;
            while ((chr = in.read()) != -1) {
                sb.append((char) chr);
            }
            Returned_value = sb.toString();
        } finally {
            in.close();
        }

        //Wrong Returned Values or Empty
        if (Returned_value.contains("false") ||Returned_value.contains("Unable"))
        {
            dialog.dismiss();
            Intent myIntent = new Intent(getBaseContext(),MainActivity.class);
            startActivity(myIntent);

        }
        JsonToArray(Returned_value);
        return conn.getResponseMessage()+"";

    }

    //Converting Json to Array
    private void JsonToArray(String returned_value) throws JSONException {
        JSONObject obj = new JSONObject(Returned_value);

        //Names List
        JSONArray namesArray = obj.getJSONArray("names");
        if (namesArray != null) {
            for (int i=0;i<namesArray.length();i++){
                names_list.add(namesArray.getString(i));
            }
        }

        //Phones List
        JSONArray phonesArray = obj.getJSONArray("phones");
        if (phonesArray != null) {
            for (int i=0;i<phonesArray.length();i++){
                phone_list.add(phonesArray.getString(i));
            }
        }

        dialog.dismiss();

    }



    private class HTTPAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                try {
                    return httpPost(urls[0]);

                } catch (JSONException e) {
                    e.printStackTrace();
                    return "Error!";
                }
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {

            //Name select
            displayNames();

            //phone select
            displayPhones();
        }
    }


    //Sending to URL
    public void send() {
        if(checkNetworkConnection()) {
            new HTTPAsyncTask().execute("https://arcane-island-63185.herokuapp.com");
        }
        else
            Toast.makeText(this, "Not Connected!", Toast.LENGTH_SHORT).show();

    }

    //Build json from string
    private JSONObject buidJsonObject() throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("image", image_string);

        return jsonObject;
    }

    private void setPostRequestContent(HttpURLConnection conn, JSONObject jsonObject) throws IOException {

        OutputStream os = conn.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(jsonObject.toString());
        Log.i(MainActivity.class.toString(), jsonObject.toString());
        writer.flush();
        writer.close();
        os.close();
    }



    //Menu Bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_menu, menu);
        return true;
    }

    //Try Again Button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent myIntent = new Intent(getBaseContext(),MainActivity.class);
        startActivity(myIntent);
        return super.onOptionsItemSelected(item);
    }
}
