package com.andramp.mcssoftware;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;


public class SettingsActivity extends Activity {

    private CheckBox chkManual;
    private CheckBox chkScreenOn;
    private CheckBox chkLandscape;
    private CheckBox chkPortrait;

    public String strHomeDir;


    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Puts back button on action bar
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }


        chkManual = findViewById(R.id.chkManual);
        chkManual.setSoundEffectsEnabled(false);
        chkScreenOn = findViewById(R.id.chkScreenOn);
        chkScreenOn.setSoundEffectsEnabled(false);
        chkLandscape = findViewById(R.id.chkLandscape);
        chkLandscape.setSoundEffectsEnabled(false);
        chkPortrait = (findViewById(R.id.chkPortrait));
        chkPortrait.setSoundEffectsEnabled(false);

        //Set checkbox to values from ReadSettings in MainActivity
        chkManual.setChecked(MainActivity.isManual);
        chkScreenOn.setChecked(MainActivity.isScreen);
        chkLandscape.setChecked(MainActivity.isLandscape);
        chkPortrait.setChecked(MainActivity.isPortrait);

        strHomeDir = MainActivity.HOME_DIR;
        TextView textViewHomeDir = (findViewById(R.id.textViewHomeDir));
        textViewHomeDir.setText(strHomeDir);

        //textViewHomeDir.setText(MainActivity.HOME_DIR);
        //textViewHomeDir.setText(MainActivity.HOME_DIR);


        //Set screen orientation according to settings
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        if (MainActivity.isLandscape){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        if (MainActivity.isPortrait){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }


        /*
         * Manual CheckBox click event
         *
         * */

        chkManual.setOnClickListener(arg0 -> {
            //Check if ticked and set isManual
            MainActivity.isManual = chkManual.isChecked();
        });

        /*
         * Screen On CheckBox click event
         *
         * */

        chkScreenOn.setOnClickListener(arg0 -> {
            //Check if ticked and set isScreen
            MainActivity.isScreen = chkScreenOn.isChecked();
        });

        /*
         * Landscape on CheckBox click event
         *
         * */

        chkLandscape.setOnClickListener(arg0 -> {

            //Check if ticked and set isLandscape
            if (chkLandscape.isChecked()) {
                MainActivity.isLandscape = true;
                chkPortrait.setChecked(false);
                MainActivity.isPortrait = false;
                //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }else{
                MainActivity.isLandscape = false;
            }

        });

        /*
         * Portrait on CheckBox click event
         *
         * */
        chkPortrait.setOnClickListener(arg0 -> {

            //Check if ticked and set isLandscape
            if (chkPortrait.isChecked()) {
                MainActivity.isPortrait = true;
                chkLandscape.setChecked(false);
                MainActivity.isLandscape = false;
                //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }else{
                MainActivity.isPortrait = false;
            }
        });

        /*
         * Hone Directory click event
         *
         * */
        textViewHomeDir.setOnClickListener(arg0 -> {

            //Select folder dialog
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            startActivityForResult(intent, 75);


       });

    }

    /**
     * Receiving directory from folder dialog
     *
     */
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //folder activity finished
        //Activity.RESULT_OK should be -1
        if ((requestCode == 75) && (resultCode == Activity.RESULT_OK)){

                //Set the home directory from the returned data

                if ((data.getData()) != null) {
                    Uri uri = data.getData();
                    strHomeDir = uri.getLastPathSegment();

                    //convert to real path
                    strHomeDir = "/storage/" + strHomeDir +"/";

                    //replace the colon with /
                    strHomeDir = strHomeDir.replace(":","/");

                    //replace primary with emulated/0
                    strHomeDir = strHomeDir.replace("primary", "emulated/0");

                    MainActivity.HOME_DIR = strHomeDir;
                    restartActivity();
                }

                //Toast.makeText(this, "Folder Activity finished", Toast.LENGTH_LONG).show();

        }


    }


    public void restartActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }



    //Menu for back button on action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent in = new Intent(getApplicationContext(),
                    MainActivity.class);
            setResult(50, in);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Detects the menu and back key pressed, which causes a crash in API > 22
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {

            if (Build.VERSION.SDK_INT > 22) {
                Toast.makeText(getBaseContext(),
                        "Physical menu key pressed, please use the 3 dots icon on top right.", Toast.LENGTH_LONG)
                        .show();
                return true;
            } else {
                return super.onKeyDown(keyCode, event);
            }

        }

        if (keyCode == KeyEvent.KEYCODE_BACK) {

//            Toast.makeText(getBaseContext(),
//                    "Physical back key pressed, please use the back icon on top left.", Toast.LENGTH_LONG)
//                    .show();
            Intent in = new Intent(getApplicationContext(),
                    MainActivity.class);
            setResult(50, in);
            finish();

        }

        return super.onKeyDown(keyCode, event);
    }



    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        WriteSettings();
    }

    @Override
    public void onPause(){
        super.onPause();
        WriteSettings();
    }



    public String ConvertBooleanToString (Boolean b){
        String result = "false";
        if(b) {
            result = "true";
        }
        return result;
    }

    public void WriteSettings(){

        MainActivity.adapter.updateSetting("manual", ConvertBooleanToString(chkManual.isChecked()));
        MainActivity.adapter.updateSetting("screen", ConvertBooleanToString(chkScreenOn.isChecked()));
        MainActivity.adapter.updateSetting("landscape", ConvertBooleanToString(chkLandscape.isChecked()));
        MainActivity.adapter.updateSetting("portrait", ConvertBooleanToString(chkPortrait.isChecked()));
        MainActivity.adapter.updateSetting("home", strHomeDir);
    }

}
