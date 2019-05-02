package com.Ninja.Camera_Dialer;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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

        //// Receive the image file from the main activity
        Uri file;
        Intent myintent = getIntent();
        file = myintent.getParcelableExtra("Image_file");

        System.out.println("sentttt"+file.toString());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        Bitmap bitmap;
        //// Convert the URI image to bitmap then to string
        //Log.d("weidth", getString(bitmap.getWidth()));
        int h ;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),file);

            //Log.d("weidth", getString(bitmap.getWidth()));
            //System.out.print("width before "+getString(bitmap.getWidth()));
            //System.out.print("hieght before "+getString(bitmap.getHeight()));
            //Log.d("weidth", getString(bitmap.getWidth()));
            //System.out.print("width before "+getString(bitmap.getWidth()));
            //System.out.print("hieght before "+getString(bitmap.getHeight()));
            int oldwidth=bitmap.getWidth();
            int oldheight=bitmap.getHeight();
            if(oldwidth*oldheight>307200)
            {
                float  ratio= (float)oldwidth/oldheight;
                int newwidth= (int)(ratio*640);
                System.out.println("newwidth"+newwidth+" old  "+oldwidth + "  " + oldheight);
                 //System.out.print(newwidth);
                bitmap = Bitmap.createScaledBitmap(bitmap,newwidth, 640, true);
            }
            //bitmap = Bitmap.createScaledBitmap(bitmap,500, 400, true);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos); //bm is the bitmap object

            //System.out.print("width after"+getString(bitmap.getWidth()));
            //System.out.print("hieght after"+getString(bitmap.getHeight()));
            byte[] byteArrayImage = baos.toByteArray();
            image_string = Base64.encodeToString(byteArrayImage, Base64.DEFAULT);
            //Log.d("Image String", image_string);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // send the request

        //System.out.println("loaaaaad dialog");
        dialog = ProgressDialog.show(Call_Save.this, "", "Loading. Please wait...", true);
        send();

        //ProgressDialog dialog = ProgressDialog.show(getApplicationContext(), "Loading", "Please wait...", true);
        //pDialog = ProgressDialog.show(this, "Downloading Data..", "Please wait", true,false);

        //Phone select
        displayNames();

        //phone select
        displayPhones();
        //////Call Button
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
                //dialContactPhone("01099984168");
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
//        phone_list.add("01118514663");
//        phone_list.add("01099984168");
//        phone_list.add("500");
//        phone_list.add("300");
//        phone_list.add("100");
        ListView PhonesListView = (ListView)findViewById(R.id.Phone_listview);
        final ArrayAdapter<String> adapter_phone = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,phone_list);
        PhonesListView.setAdapter(adapter_phone);
        PhonesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView txt = (TextView) view;
                System.out.println(txt.getText().toString());
                chosen_phone.setText(txt.getText().toString());

            }
        });
    }

    // Printing the Names on Listview
    private void displayNames() {
//        names_list.add("amira");
//        names_list.add("noura");
//        names_list.add("basma");
        chosen_name= (TextView)findViewById(R.id.Display_name_text);
        ListView NamesListView = (ListView)findViewById(R.id.Names_listview);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,names_list);
        NamesListView.setAdapter(adapter);
        NamesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView txt = (TextView) view;
                System.out.println(txt.getText().toString());
                chosen_name.setText(txt.getText().toString());

            }
        });
    }

    // Add Contact Method
    public void btnAdd_Contact_onClick(View view) {
        Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
        intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
//< get entered-data >
        //String mEmailAddress = "mj@hhh.nom";
        String mPhoneNumber = chosen_phone.getText().toString();
        String mName = chosen_name.getText().toString();

        //ContactsContract.Intents.Insert.EMAIL, mEmailAddress)
        //                .putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, ContactsContract.CommonDataKinds.Email.TYPE_HOME)
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
            // show "Connected" & type of network "WIFI or MOBILE"
            //tvIsConnected.setText("Connected "+networkInfo.getTypeName());
            System.out.println("Connected "+networkInfo.getTypeName());
            // change background color to red
            //tvIsConnected.setBackgroundColor(0xFF7CCC26);


        } else {
            // show "Not Connected"
            //tvIsConnected.setText("Not Connected");
            System.out.println("Not Connected");
            // change background color to green
            //tvIsConnected.setBackgroundColor(0xFFFF0000);
        }

        return isConnected;
    }


    private String httpPost(String myUrl) throws IOException, JSONException {
        String result = "";

        URL url = new URL(myUrl);

        // 1. create HttpURLConnection
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

        // 2. build JSON object
        JSONObject jsonObject = buidJsonObject();
        //jsonObject.put("Eslam", "Medhat" );

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
        Log.d("reply is ", Returned_value);
        if (Returned_value.contains("false") ||Returned_value.contains("Unable"))
        {
            dialog.dismiss();
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setMessage("Look at this dialog!")
//                    .setCancelable(false)
//                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int id) {
//                            //do things
//                        }
//                    });
//            AlertDialog alert = builder.create();
//            alert.show();

            //Toast.makeText(this, "Wrong photo , Try Again", Toast.LENGTH_SHORT).show();
            Intent myIntent = new Intent(getBaseContext(),MainActivity.class);
            startActivity(myIntent);

        }
        JsonToArray(Returned_value);
        return conn.getResponseMessage()+"";

    }

    private void JsonToArray(String returned_value) throws JSONException {
        JSONObject obj = new JSONObject(Returned_value);
        Log.d("My json object", obj.toString());
        JSONArray namesArray = obj.getJSONArray("names");
        if (namesArray != null) {
            for (int i=0;i<namesArray.length();i++){
                names_list.add(namesArray.getString(i));
            }
        }
//        else
//        {
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setMessage("There are no names Scanned , Do u wanna Scan Again ?").setPositiveButton("Yes", dialogClickListener)
//                    .setNegativeButton("No", dialogClickListener).show();
//
//            //Toast.makeText(this, "No names Scanned , Try Again!", Toast.LENGTH_SHORT).show();
//        }

        JSONArray phonesArray = obj.getJSONArray("phones");
        if (phonesArray != null) {
            for (int i=0;i<phonesArray.length();i++){
                phone_list.add(phonesArray.getString(i));
            }
        }
//        else
//        {
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            builder.setMessage("There are no Phones Scanned , Do u wanna Scan Again ?").setPositiveButton("Yes", dialogClickListener)
//                    .setNegativeButton("No", dialogClickListener).show();
//
//            //Toast.makeText(this, "No Phones Scanned , Try Again!", Toast.LENGTH_SHORT).show();
//        }

        System.out.println("dismissssss dialog");

        dialog.dismiss();

    }




    private class HTTPAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            // params comes from the execute() call: params[0] is the url.
            try {
                try {
                    System.out.println("first try");
                    String s =httpPost(urls[0]);
                    System.out.println("after try");
                    Log.d("urll",s);
                    Log.d("amira","hii");
                    return s;

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

            //Phone select
            displayNames();

            //phone select
            displayPhones();
            Log.d("url result",result);
            //tvResult.setText(result);
        }
    }


    public void send() {
        //Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show();
        // perform HTTP POST request
        if(checkNetworkConnection()) {
            //Toast.makeText(this, "Connected!", Toast.LENGTH_SHORT).show();
            // http://431858b7.ngrok.io
            //https://arcane-island-63185.herokuapp.com/result
            new HTTPAsyncTask().execute("https://arcane-island-63185.herokuapp.com");
        }
        else
            Toast.makeText(this, "Not Connected!", Toast.LENGTH_SHORT).show();

    }

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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent myIntent = new Intent(getBaseContext(),MainActivity.class);
        startActivity(myIntent);
        return super.onOptionsItemSelected(item);
    }
}
