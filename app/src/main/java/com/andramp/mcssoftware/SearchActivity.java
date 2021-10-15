package com.andramp.mcssoftware;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import java.util.ArrayList;
import java.util.HashMap;

//ListActivity is deprecated in API 30 use ListFragment instead?
public class SearchActivity extends ListActivity {

    //Use full media list for searching
    ArrayList<HashMap<String, String>> fullMediaListData = MainActivity.songsLibraryList;
    //Temporary list for search data
    ArrayList<HashMap<String, String>> searchListData = new ArrayList<>();

    ListAdapter adapter;
    ListView lv;

    EditText searchEditText;
    Button btnClear;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Set display brightness, float value from 0.0f to 1.0f
        WindowManager.LayoutParams lp = this.getWindow().getAttributes();
        lp.screenBrightness = 1.0f;
        this.getWindow().setAttributes(lp);

        //Puts back button on action bar
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        //Set screen orientation according to settings
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        if (MainActivity.isLandscape){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        if (MainActivity.isPortrait){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        // Adding menuItems to ListView
        setAdapter(fullMediaListData);

        //Set up the search edit text box and load saved text if any
        searchEditText = findViewById(R.id.txtSearch);
        searchEditText.setText(MainActivity.SearchText);

        //Move cursor to end of text
        searchEditText.setSelection(searchEditText.getText().length());

        //Use text to filter list
        if (MainActivity.SearchText != null) {
            filterSongsList(MainActivity.SearchText);
        }

        btnClear = findViewById(R.id.btnClear);

        //Set an on click listener for the clear button
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                searchEditText.setText("");
                MainActivity.SearchText = ("");
            }
        });

        // selecting single ListView item
        // listening to single list item click
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // Starting new intent
                Intent in = new Intent(getApplicationContext(),
                        MainActivity.class);
                String songPath;
                //Check searchListData not empty
                if (searchListData.size() > 0) {
                    songPath = searchListData.get(position).get("songPath");
                }else{
                    songPath = fullMediaListData.get(position).get("songPath");
                }
                in.putExtra("songPath", songPath);
                setResult(110, in);

                //Store the contents of the search box
                MainActivity.SearchText = searchEditText.getText().toString();

                // Closing SearchListView
                finish();
            }
        });

        // Add TextChangedListener to search box.
        // It listens for user's entered text and then filters the list
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged (CharSequence s, int start, int count, int after) {
            }

            // When text is entered in search box, filter list by search text
           @Override
            public void onTextChanged(CharSequence cs, int start, int before, int count) {
                //Check for space at start
               String str = cs.toString();
               if(str.length() > 0 && str.startsWith(" ")){
                   searchEditText.setText("");
               }else{
                   filterSongsList(cs);
               }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        //Filter song list with saved search text
        if (MainActivity.SearchText != null) {
            filterSongsList(MainActivity.SearchText);
        }
    }

    public void filterSongsList (CharSequence cs) {

        //Will be set true if all keywords in cs are found
        boolean isMatch = false;
        //Clear the search list
        searchListData.clear();
        //Convert to lower case
        String filterString = cs.toString().toLowerCase();
        //Split string when space detected and store in an array
        String [] filterArray  = filterString.split(" ");

        for (HashMap<String, String> song : fullMediaListData) {
            String songTitle = song.get("songTitle");

            for (String s : filterArray) {
                isMatch = false;
                //Check if songTitle not null and contains first element in filterArray
                if ((songTitle != null) && (songTitle.toLowerCase().contains(s))) {
                    isMatch = true;
                } else {
                    break;
                }
            }
            if (isMatch){
                searchListData.add(song);
            }
        }
        setAdapter(searchListData);
    }

    public void setAdapter (ArrayList<HashMap<String, String>> data){
        adapter = new SimpleAdapter(this, data,
                R.layout.activity_search_item, new String[] { "songTitle" }, new int[] {
                R.id.songTitle });
        setListAdapter(adapter);
        lv = getListView();
    }

    //Handle the back button on the menu bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //Store the contents of the search box
        MainActivity.SearchText = searchEditText.getText().toString();

        if (item.getItemId() == android.R.id.home){
            //Call the hardware back key pressed function
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Handle the touch screen "hardware" back button
    @Override
    public void onBackPressed() {

        //Store the contents of the search box
        MainActivity.SearchText = searchEditText.getText().toString();

        // SongPath Data will be null so send data for first song by
        // starting a new intent and setting it to play first song when back button pressed
        Intent in = new Intent(getApplicationContext(),
                MainActivity.class);
        String songPath;
        songPath = fullMediaListData.get(0).get("songPath");
        in.putExtra("songPath", songPath);
        setResult(110, in);
        finish();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

}
