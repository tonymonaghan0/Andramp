/*TODO
   Minimum SDK is now 21 Android 5.0 L Lollipop
   VLC v3 shows blank screen use v2.9.1
   In VLC Preferences disable the playback history checkbox to ensure it starts from beginning
   In Preferences -> History -> uncheck Playback History checkbox
   Also try
   In Preferences -> Advanced -> Clear playback history
   *******************************************
   *******************************************
   *Fix warnings
   *Handle missing files in m3u playlist
   *
   *On tablet song list starts at M?
   *
   *Sort VLC not playing CDG file unless swiping left to start from beginning
   *
   *Ensure that llama.mp3 can always be available to play even if the HOMe path is wrong
   *
   *Sort path not found error. embed llama.mp3? Emulated storage on android 10?
   *
   *On random (shuffle),Keeps repeating songs and sometimes play stops after about an hour?
   *
   *Option to queue or play now when searching, long press?
   *
   *Lock screen Play/Pause, Next and Previous
   *
   *Volume control long press for Next and Previous?
   *
   *Search tags?
   *
   *Check playlist button working ok, seems to select next track?
   *
   *Crashing??????
   *Crash logger
   *Playlist search showing double, full search is OK??
   *
   *
   *Extract the full path of each item in the songs list
   *and store in a file.m3u
   *
   *andramp.m3u is written on close and fix reading with full path
   *
   On install load andramp.m3u with llama.mp3 in it
   Make an M3U playlist write routine
   Store last playlist in a file called andramp.m3u and load on startup.
   *
   *
   On title add touch to play? Add pinch and zoom?
   Add support for Youtube/Google search using VLC to play video?
   Increase text size on PopUpMenu options in xlarge screen mode
   Turn off screen after 10 minutes if screen not touched
   Build and search database?
   Build signed release APK
   Move items up and down play list and move next in queue with long press
   Save last playlist played automatically and load on startup, save play list to file
   Scan device for mp3s and save in database?
   Move next, Enque next perhaps with long click
   Swipe song title for next/previous
   Load songs off internet?
*/


package com.andramp.mcssoftware;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.DocumentsContract;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements OnCompletionListener, SeekBar.OnSeekBarChangeListener {

	//Spinner for getPlaylist function
	//ProgressBar spinner;

	//Timer for screen dimming
	myTimerTask myTask;
	Timer myTimer;

	private final Handler myTaskHandler = new Handler(Looper.getMainLooper());
	public static final long SCREEN_DIM_TIME = 20000; //Set to 20s
	public static final float MIN_BRIGHTNESS = 0.2f;
	public static final float MAX_BRIGHTNESS = 1.0f;

	//Define the name of the string used for holding the search text
	public static String SearchText;

	ImageButton btnPlay;
	ImageButton btnStop;
	ImageButton btnForward;
	ImageButton btnBackward;
	ImageButton btnNext;
	ImageButton btnPrevious;
	ImageButton btnRepeat;
	ImageButton btnShuffle;
	ImageButton btnPlayList;
	ImageButton btnSearch;
	ImageButton btnMenu;

	//Used to enable checking if back button pressed twice
	private long back_pressed = 0;

	private MediaPlayer mp;
	// Handler to update UI timer, progress bar etc,.
	private final Handler mHandler = new Handler(Looper.getMainLooper());
	private Utilities utils;
	private final int seekForwardTime = 5000; // 5000 milliseconds
	private final int seekBackwardTime = 5000; // 5000 milliseconds
	public static int currentSongIndex = 0;
	private boolean isShuffle = false;
	private boolean isRepeat = false;
	private boolean isSettings = false; //Set to true when settings activity finished

	//Playlist?
	public static ArrayList<HashMap<String, String>> songsList = new ArrayList<>();

	//Used in SearchActivity (Media library?)
	public static ArrayList<HashMap<String, String>> songsLibraryList = new ArrayList<>();

	//Used when m3u is clicked to start Andramp
	public ArrayList<HashMap<String, String>> m3uList = new ArrayList<>();

	//Used in getPlayList function
	public ArrayList<HashMap<String, String>> playList = new ArrayList<>();

	//Setup the volume slider
	private SeekBar volumeProgressBar = null;
	//Setup the fade in and out volumes and times
	private final int fadeInTime = 200; // Set fade in ms
	private final int fadeOutTime = 200; // Set fade out ms
	private int iVolume;
	private final static int INT_VOLUME_MAX = 100;
	private final static int INT_VOLUME_MIN = 0;
	//Need to use float variables when setting media player volume
	private final static float FLOAT_VOLUME_MAX = 1; //max volume
	private final static float FLOAT_VOLUME_MIN = 0; //min volume

	//Constants for call to fade out
	private final static int PLAY = 0;
	private final static int STOP = 1;
	private final static int NEXT = 2;
	private final static int PREVIOUS = 3;
	private final static int FORWARD = 4;
	private final static int BACKWARD = 5;
	private final static int PLAYLIST = 6;
	private final static int VLC = 7;
	private final static int SEARCH = 8;

	//Used to determine if PlaySong starts playing if manual is on
	private int intButtonPressed = PLAY;

	//Setup song progress bar
	private SeekBar songProgressBar;
	private TextView songTitleLabel;
	private TextView songCurrentDurationLabel;
	private TextView songTotalDurationLabel;
	private int currentPosition;
	private int newPosition;

	//Setup the Manual, Screen, Landscape and Portrait booleans
	public static boolean isManual;
	public static boolean isScreen;
	public static boolean isLandscape;
	public static boolean isPortrait;


	//For use with volume control
	private AudioManager mAudioManager;

	public static String HOME_DIR;
	public static String strSongsDir;
	public static DatabaseAdapter adapter;

	//The app files folder eg storage/emulated/0/Android/data/com.andramp.mcssoftware/files/
	public static String APP_FILES_PATH;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		try {

			//Set Theme back to default after using the launcher theme
			// Make sure this is before calling super.onCreate
			setTheme(R.style.AppTheme);

			super.onCreate(savedInstanceState);

			//This will stop file exposure error when calling VLC
			//The VM ignores the file URI exposure.
			StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
			StrictMode.setVmPolicy(builder.build());

			// The request code used in ActivityCompat.requestPermissions()
			// and returned in the Activity's onRequestPermissionsResult()
			int PERMISSION_ALL = 1;
			String[] PERMISSIONS = {
					android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
					android.Manifest.permission.READ_EXTERNAL_STORAGE,
					Manifest.permission.READ_PHONE_STATE
			};

			// Call for runtime permissions on API>22
			if (Build.VERSION.SDK_INT > 22) {
				//hasPermissions is a helper to check all permissions are granted
				if (!hasPermissions(this, PERMISSIONS)) {
					ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
				}
			}

			// remove title bar on main screen
			requestWindowFeature(Window.FEATURE_NO_TITLE);

			setContentView(R.layout.activity_main);

			//Returns the app files folder eg storage/emulated/0/Android/data/com.andramp.mcssoftware/files/
			//Used by DatabaseHelper to set initial HOME_DIR
			APP_FILES_PATH = getAppFilesFolder(this);

			//SQLite database holds settings and track names and paths
			adapter = new DatabaseAdapter(this);
			//TODO sort databaseHelper.open warning
			adapter.open();
			//Read the saved settings
			ReadSettings();


			//String APP_FILES_PATH = Objects.requireNonNull(this.getExternalFilesDir(null)).getAbsolutePath();
			//String APP_FILES_PATH = "storage/emulated/0/Android/data/com.andramp.mcssoftware/files/";
			//String Internal_SDCard_Root = Objects.requireNonNull(Objects.requireNonNull(this.getExternalFilesDir(null)).getParent()).split("/Android")[0];

			//Check llama.mp3 file exists if not copy it to storage/emulated/0/Android/data/com.andramp.mcssoftware/files/llama.mp3
			//APP_FILES_PATH = getAppFilesFolder(this);
			File llama = new File(APP_FILES_PATH + "/llama.mp3");
			if (!llama.exists()) {
				InputStream in = getResources().openRawResource(R.raw.llama);
				copyFile(in, APP_FILES_PATH + "/llama.mp3");
			}

			//Check llama.mp3 file exists if not copy it to storage/emulated/0/Android/data/com.andramp.mcssoftware/files/andramp.m3u
			File andramp = new File(APP_FILES_PATH + "/andramp.m3u");
			if (!andramp.exists()) {
				InputStream in = getResources().openRawResource(R.raw.andramp);
				copyFile(in, APP_FILES_PATH + "/andramp.m3u");
			}

			// Create objects
			utils = new Utilities();
			mp = new MediaPlayer();

			//Set volume to max
			mp.setVolume(100,100);

			//Load songsList and songsLibraryList
			songsList = getPlayList(HOME_DIR);
			//Clear list or else list is duplicated?
			playList.clear();
			songsLibraryList = getPlayList(HOME_DIR);

			setVolumeControlStream(AudioManager.STREAM_MUSIC);

			// All player buttons and seek bars
			volumeProgressBar = findViewById(R.id.volumeProgressBar);
			// Set height
			volumeProgressBar.setScaleY(4F);
			btnPlay = findViewById(R.id.btnPlay);
			btnPlay.setSoundEffectsEnabled(false);
			btnStop = findViewById(R.id.btnStop);
			btnStop.setSoundEffectsEnabled(false);
			btnForward = findViewById(R.id.btnForward);
			btnForward.setSoundEffectsEnabled(false);
			btnBackward = findViewById(R.id.btnBackward);
			btnBackward.setSoundEffectsEnabled(false);
			btnNext = findViewById(R.id.btnNext);
			btnNext.setSoundEffectsEnabled(false);
			btnPrevious = findViewById(R.id.btnPrevious);
			btnPrevious.setSoundEffectsEnabled(false);
			btnRepeat = findViewById(R.id.btnRepeat);
			btnRepeat.setSoundEffectsEnabled(false);
			btnShuffle = findViewById(R.id.btnShuffle);
			btnShuffle.setSoundEffectsEnabled(false);
			btnPlayList = findViewById(R.id.btnPlayList);
			btnSearch = findViewById(R.id.btnSearch);
			btnMenu = findViewById(R.id.btnMenu);
			songProgressBar = findViewById(R.id.songProgressBar);
			// Set height
			songProgressBar.setScaleY(4F);
			songTitleLabel = findViewById(R.id.songTitle);
			//Set scrollable text box
			songTitleLabel.setMovementMethod(new ScrollingMovementMethod());
			songCurrentDurationLabel = findViewById(R.id.songCurrentDurationLabel);
			songTotalDurationLabel = findViewById(R.id.songTotalDurationLabel);
			//To handle volume control
			mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);


			// Listeners
			songProgressBar.setOnSeekBarChangeListener(this); // Important
			volumeProgressBar.setOnSeekBarChangeListener(this); // Important
			mp.setOnCompletionListener(this); // Important detects end of play

			// Are we called from main or by our MP3/M3U intent?
			Intent intent = getIntent();

			if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_MAIN)) {
				//Called from Main
				//TODO load andramp.m3u and save on exit
				//Getting all songs list from HOME_DIR
				// By default play first song then pause it to setup media player etc.
				playSong(currentSongIndex);
				mp.pause();
				mp.seekTo(0);
				// Changing button image to play button
				btnPlay.setImageResource(R.drawable.ic_media_play);

			} else if (intent.getData() != null) {
				//Called from either selecting an MP3 or M3U
				//Get URI from intent data
				Uri MyUri = intent.getData();
				//Create a file to get the path and name
				File f = new File(Objects.requireNonNull(MyUri.getPath()));
				String extension = FileExtension(f);

				//Check if MP3 or M3U
				if (extension.equalsIgnoreCase("mp3")) {
					//Called from mp3
					AddMp3ToList(f);
				}

				if (extension.equalsIgnoreCase("m3u")) {
					//Called from m3u
					m3uRead(f);
				}
			}

			/*
			 * Play button click event
			 * plays a song and changes button to pause image
			 * pauses a song and changes button to play image
			 * */

			btnPlay.setOnClickListener(arg0 -> {

				//Check if CDG file present and isManual is true if so play with VLC at currentSongIndex
				//Also checks if file is not an MP3
				String currentSongPath = songsList.get(currentSongIndex).get("songPath");
				assert currentSongPath != null;
				if (((checkCDGFileExists(currentSongPath) && isManual)) || !checkFileExtension(currentSongPath, ".mp3")) {
					startVLC(currentSongPath);
				}else{

					//Set the button clicked parameter
					intButtonPressed = PLAY;

					// check for already playing
					if (mp.isPlaying()) {
						if (mp != null) {
							// Fades out over fadeOutTime ms and pauses media player
							fadeOut(fadeOutTime, PLAY);
							// Changing button image to play button
							btnPlay.setImageResource(R.drawable.ic_media_play);
						}
					} else {
						// Resume song
						if (mp != null) {
							// Starts media player and fades in over fadeInTime ms
							fadeIn(fadeInTime);
							// Changing button image to pause button
							btnPlay.setImageResource(R.drawable.ic_media_pause);
						}
					}
				}
			});

			/*
			 * Stop button click event
			 * Stops a song and changes play button to play image
			 * Returns song to start time
			 * */

			btnStop.setOnClickListener(arg0 -> {

				//Set the button clicked parameter
				intButtonPressed = STOP;

				fadeOut(fadeOutTime, STOP);
				// Changing button image to play button
				btnPlay.setImageResource(R.drawable.ic_media_play);

			});


			/*
			 * Forward button click event
			 * Forwards song specified seconds
			 * */

			btnForward.setOnClickListener(arg0 -> {
				// get current song position
				currentPosition = mp.getCurrentPosition();
				// check if seekForward time is less than song duration
				newPosition = Math.min(currentPosition + seekForwardTime, mp.getDuration());

				fadeOut(fadeOutTime, FORWARD);
			});

			/*
			 * Backward button click event
			 * Backward song to specified seconds
			 * */

			btnBackward.setOnClickListener(arg0 -> {
				// get current song position
				currentPosition = mp.getCurrentPosition();
				// check if seekBackward time is greater than 0 sec
				newPosition = Math.max(currentPosition - seekBackwardTime, 0);
				fadeOut(fadeOutTime, BACKWARD);
			});

			/*
			 * Next button click event
			 * Plays next song by taking currentSongIndex + 1
			 * */

			btnNext.setOnClickListener(arg0 -> {

				// check if next song is there or not
				if (currentSongIndex < (songsList.size() - 1)) {
					currentSongIndex = currentSongIndex + 1;
				} else {
					// play first song
					currentSongIndex = 0;
				}
				//Can't fade out as fade out timer can't run in UI thread without causing lockups
				//fadeOut(fadeOutTime, NEXT);
				//Set volume to min
				//mp.setVolume(FLOAT_VOLUME_MIN, FLOAT_VOLUME_MIN);


				//Set the button clicked parameter. Don't start playing if manual is on
				intButtonPressed = NEXT;
				playSong(currentSongIndex);

				if (isManual) {
					// Changing button image to play button
					btnPlay.setImageResource(R.drawable.ic_media_play);
				}

			});

			/*
			 * Previous button click event
			 * Plays previous song by currentSongIndex - 1
			 * */

			btnPrevious.setOnClickListener(arg0 -> {

				if (currentSongIndex > 0) {
					currentSongIndex = currentSongIndex - 1;
				} else {
					// play last song
					currentSongIndex = songsList.size() - 1;
				}
				//Can't fade out as fade out timer can't run in UI thread without causing lockups
				//fadeOut(fadeOutTime, PREVIOUS);
				//Set volume to min
				//mp.setVolume(FLOAT_VOLUME_MIN, FLOAT_VOLUME_MIN);

				//Set the button clicked parameter
				intButtonPressed = PREVIOUS;
				playSong(currentSongIndex);

				if (isManual) {
					// Changing button image to play button
					btnPlay.setImageResource(R.drawable.ic_media_play);
				}

			});

			/*
			 * Repeat button Click event
			 * Enables repeat flag to true
			 * */

			btnRepeat.setOnClickListener(arg0 -> {
				if (isRepeat) {
					isRepeat = false;
					Toast.makeText(getApplicationContext(), "Repeat is OFF", Toast.LENGTH_SHORT).show();
					btnRepeat.setImageResource(R.drawable.ic_media_repeat);
				} else {
					// make repeat to true
					isRepeat = true;
					Toast.makeText(getApplicationContext(), "Repeat is ON", Toast.LENGTH_SHORT).show();
					// make shuffle to false
					isShuffle = false;
					btnRepeat.setImageResource(R.drawable.ic_media_repeat_on);
					btnShuffle.setImageResource(R.drawable.ic_media_shuffle);
				}
			});

			/*
			 * Button Click event for Shuffle button
			 * Enables shuffle flag to true
			 * */

			btnShuffle.setOnClickListener(arg0 -> {
				if (isShuffle) {
					isShuffle = false;
					Toast.makeText(getApplicationContext(), "Shuffle is OFF", Toast.LENGTH_SHORT).show();
					btnShuffle.setImageResource(R.drawable.ic_media_shuffle);
				} else {
					// make repeat to true
					isShuffle = true;
					Toast.makeText(getApplicationContext(), "Shuffle is ON", Toast.LENGTH_SHORT).show();
					// make shuffle to false
					isRepeat = false;
					btnShuffle.setImageResource(R.drawable.ic_media_shuffle_on);
					btnRepeat.setImageResource(R.drawable.ic_media_repeat);
				}
			});

			/*
			 * Button Click event for PlayList button
			 * Shows Playlist Activity
			 * */

			btnPlayList.setOnClickListener(arg0 -> {

				//Open playlist activity
				Intent i = new Intent(getApplicationContext(), PlayListActivity.class);
				startActivityForResult(i, 100);
				//OnActivityResult is triggered on return from PlayListActivity
			});

			/*
			 * Button Click event for Search button
			 * Shows Search options
			 * */

			btnSearch.setOnClickListener(arg0 -> {

				//Open search activity
				Intent i = new Intent(getApplicationContext(), SearchActivity.class);
				startActivityForResult(i, 110);
				//OnActivityResult is triggered on return from SearchActivity


			});


			/*
			 * Button Click event for Menu button
			 * Shows Menu
			 * */

			btnMenu.setOnClickListener(arg0 -> {

				PopupMenu popup = new PopupMenu(MainActivity.this, btnMenu);
				MenuInflater inflater = popup.getMenuInflater();
				inflater.inflate(R.menu.main, popup.getMenu());

				//registering popup with OnMenuItemClickListener
				//popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
				popup.setOnMenuItemClickListener(item -> {

					int itemId = item.getItemId();
					if (itemId == R.id.action_exit) {
						android.os.Process.killProcess(android.os.Process.myPid());
						System.exit(1);
					} else if (itemId == R.id.action_settings) {

						//Launches the settings activity
						Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
						startActivityForResult(i, 50);

					} else if (itemId == R.id.action_add_file) {

						//Select file dialog
						Intent intent1 = new Intent()
								.setType("audio/*")
								.setAction(Intent.ACTION_GET_CONTENT);
						startActivityForResult(Intent.createChooser(intent1, "Select a file"), 80);

					} else if (itemId == R.id.action_add_folder) {

						//Select folder dialog
						Intent intent1 = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
						startActivityForResult(intent1, 75);

					} else if (itemId == R.id.action_open_m3u) {
						Intent i = new Intent(getApplicationContext(), PlayListActivity.class);
						startActivityForResult(i, 100);
					} else if (itemId == R.id.action_save_m3u) {
						m3uWrite(songsList, "andramp.m3u");
					} else if (itemId == R.id.action_new_m3u) {
						//Clears all songs in play list
						//HashMap<String, String> song = new HashMap<>();
						HashMap<String, String> song;
						song = songsList.get(currentSongIndex);
						songsList.clear();
						songsList.add(song);
					} else if (itemId == R.id.action_search) {

						//Open search activity
						Intent i = new Intent(getApplicationContext(), SearchActivity.class);
						startActivityForResult(i, 110);
						//OnActivityResult is triggered on return from SearchActivity

					}

					return true;

				});

				popup.show();
			});


//			/*
//			 * songTitleLabel click event
//			 *	When clicked will zoom out to zoom factor
//			 *	Click again to return to normal size
//			 * */
//			songTitleLabel.setOnClickListener(new View.OnClickListener() {
//
//				float zoomFactor = 1.5f;
//				boolean zoomedOut = false;
//
//				@Override
//				public void onClick(View v) {
//
//					if (zoomedOut) {
//						v.setScaleX(1);
//						v.setScaleY(1);
//						zoomedOut = false;
//					} else {
//						v.setScaleX(zoomFactor);
//						v.setScaleY(zoomFactor);
//						zoomedOut = true;
//					}
//
//				}
//			});


			//Setup the options read from the SQLite database
			SetupOptions();
		} catch (Exception e) {
			// this is the line of code that sends a real error message to the log
			Log.e("ERROR", "ERROR IN CODE: " + e.toString());

			Toast.makeText(getApplicationContext(), "ERROR IN CODE: " + e.toString(), Toast.LENGTH_LONG).show();

			// this is the line that prints out the location in
			// the code where the error occurred.
			e.printStackTrace();
		}

		initVolumeControl();


	}

	//hasPermissions is a helper to check all permissions are granted
	public static boolean hasPermissions(Context context, String... permissions) {
		if (context != null && permissions != null) {
			for (String permission : permissions) {
				if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
					return false;
				}
			}
		}
		return true;
	}


	/**	onActivityResult
	 *
	 *
	 */
	@Override
	protected void onActivityResult(int requestCode,
									int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

//		SongsManager songManager = new SongsManager();

		//folder select activity finished
		//Activity.RESULT_OK should be -1
		if ((requestCode == 75) && (resultCode == Activity.RESULT_OK)){
			if ((data.getData()) != null) {
				//Set the song directory from the returned data
				Uri uri = data.getData();
				strSongsDir = uri.getLastPathSegment();

				//convert to real path
				strSongsDir = "/storage/" + strSongsDir + "/";

				//replace the colon with /
				strSongsDir = strSongsDir.replace(":", "/");

				//replace primary with emulated/0
				strSongsDir = strSongsDir.replace("primary", "emulated/0");

				//Load songList from folder
				songsList = getPlayList(strSongsDir);
				//playSong knows it's being called after activity finished
				isSettings = true;
				//Play first song in list
				playSong(0);
			}
		}

		//File select activity finished
		//Activity.RESULT_OK should be -1
		if ((requestCode == 80) && (resultCode == Activity.RESULT_OK)){

			if ((data.getData()) != null) {

				//Get the song path from the returned data
				Uri uri = data.getData();

				//convert to real path
				//Will also check for different formats of how storage is represented
				String docId = DocumentsContract.getDocumentId(uri);
				String[] split = docId.split(":");
				String type = split[0];
				String strFilePath;
				if ("primary".equalsIgnoreCase(type)) {
					//strFilePath = Environment.getExternalStorageDirectory() + "/" + split[1];
					String Internal_SDCard_Root = Objects.requireNonNull(Objects.requireNonNull(this.getExternalFilesDir(null)).getParent()).split("/Android")[0];
					strFilePath = Internal_SDCard_Root + "/" + split[1];
				}else{
					strFilePath = "storage/" + split[0] + "/" + split[1];
				}

				File f = new File(strFilePath);
				String extension = FileExtension(f);

				//Check if MP3 or M3U
				if (extension.equalsIgnoreCase("mp3")) {
					//File is an mp3
					AddMp3ToList(f);
				}

				if (extension.equalsIgnoreCase("m3u")) {
					//File is an m3u
					m3uRead(f);
				}
			}
		}

		//Settings activity finished
		if (requestCode == 50) {
			//In order to ensure all changes are recognised
			restartActivity();
		}

		//VLC activity finished
		if (requestCode == 42) {
			//Toast.makeText(this, "VLC resultCode " + resultCode, Toast.LENGTH_LONG).show();
			if (resultCode == 0) {
				intButtonPressed = VLC;
				if (currentSongIndex <  (songsList.size()) - 1){
					currentSongIndex = currentSongIndex + 1;
				}else{
					currentSongIndex = 0;
				}
				playSong(currentSongIndex);
			}


		}

		//Playlist activity finished
		if (requestCode == 100) {

			//Playlist activity returns resultCode == 100
			if (resultCode == 100) {

				//Use this to prevent a null pointer exception warning
				Bundle extras = data.getExtras();
				if (extras != null) {
					currentSongIndex = data.getExtras().getInt("songIndex");
				} else {
					currentSongIndex = 0;
				}

				//Can't fade out as fade out timer can't run in UI thread without causing lockups
				//fadeOut(fadeOutTime, NEXT);

				intButtonPressed = PLAYLIST;
				playSong(currentSongIndex);

				if (isManual) {
					// Changing button image to play button
					btnPlay.setImageResource(R.drawable.ic_media_play);
				}
			}
		}

		//Search activity finished and passes songPath string
		if (requestCode == 110) {
			//Use this to prevent a null pointer exception warning
			Bundle extras = data.getExtras();
			if (extras != null){
                //Bundle extras = data.getExtras();
                //if (extras != null) {
                String songPath = data.getExtras().getString("songPath");
				assert songPath != null;
				File f = new File(songPath);
                String filename = f.getName();
                //Create a song array
                HashMap<String, String> song = new HashMap<>();
                //Add one song at end and remove the .mp3 ie last 4 characters
                song.put("songTitle", filename.substring(0, filename.length() - 4));
                song.put("songPath", songPath);
                // Add song to SongList array list
                songsList.add(song);
                intButtonPressed = SEARCH;
                //Set currentSongIndex to last song
                currentSongIndex = songsList.size() - 1;
                } else {
                //data is null set index to start
				currentSongIndex = 0;
                }

			playSong(currentSongIndex);
			//Pause at start
			//mp.pause();
			//mp.seekTo(0);
			if (isManual) {
				// Changing button image to play button
				btnPlay.setImageResource(R.drawable.ic_media_play);
			}

		}
	}


	/**
	 * Function to play a song
	 *
	 * @param songIndex - index of song
	 */
	public void playSong(int songIndex) {

		try {
			//Check file exists if not do a toast
//TODO Ensure songsList is not null or get an error
			File f = new File((Objects.requireNonNull(songsList.get(songIndex).get("songPath"))));
			if (f.exists()) {

				mp.reset();
				mp.setDataSource(songsList.get(songIndex).get("songPath"));
				mp.prepare();

				// Displaying Song title
				String songTitle = songsList.get(songIndex).get("songTitle");
				songTitleLabel.setText(songTitle);

				//Don't start playing if manual on and called from NEXT, PREVIOUS, PLAYLIST, SEARCH or VLC
				if (!((isManual) && ((intButtonPressed == VLC) || (intButtonPressed == NEXT) || (intButtonPressed == PREVIOUS) || (intButtonPressed == PLAYLIST) || (intButtonPressed == SEARCH)))) {

					//Don't start playing if coming from Settings Activity finished
					if (!isSettings) {
						fadeIn(fadeInTime);
						// Changing Button Image to pause image
						btnPlay.setImageResource(R.drawable.ic_media_pause);
					} else {
						isSettings = false;
					}
				}

				// set Progress bar values
				songProgressBar.setProgress(0);
				songProgressBar.setMax(100);

				// Updating progress bar
				updateProgressBar();

			}else{
				Toast.makeText(this, "File not found", Toast.LENGTH_LONG).show();
			}



		} catch (IllegalArgumentException | IllegalStateException | IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Check first character is #
	 */
	public boolean isFirstCharacterHash(final String value) {
		final char c = value.charAt(0);
		return (c == '#');
	}

	/**
	 * Check second character is :
	 */
	public boolean isSecondCharacterColon(final String value1) {
		final char c1 = value1.charAt(1);
		return (c1 == ':');
	}


	/**
	 * Update timer on seekbar
	 */
	public void updateProgressBar() {
		mHandler.postDelayed(mUpdateTimeTask, 100);
	}

	/**
	 * Background Runnable thread
	 */
	private final Runnable mUpdateTimeTask = new Runnable() {
		public void run() {

			if (mp != null) {

				long totalDuration = mp.getDuration();
				long currentDuration = mp.getCurrentPosition();

				// Displaying Total Duration time
				songTotalDurationLabel.setText(utils.milliSecondsToTimer(totalDuration));
				// Displaying time completed playing
				songCurrentDurationLabel.setText(utils.milliSecondsToTimer(currentDuration));

				// Updating progress bar
				int progress = utils.getProgressPercentage(currentDuration, totalDuration);
				//Log.d("Progress", ""+progress);
				songProgressBar.setProgress(progress);

				// Running this thread after 100 milliseconds
				mHandler.postDelayed(this, 100);
			}
		}
	};

	/**
	 *
	 * */
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

	}

	/**
	 * When user starts moving the progress handler
	 */
	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// remove message Handler from updating progress bar
		mHandler.removeCallbacks(mUpdateTimeTask);
	}

	/**
	 * When user stops moving the progress handler
	 */
	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		mHandler.removeCallbacks(mUpdateTimeTask);
		int totalDuration = mp.getDuration();
		int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

		// forward or backward to certain seconds
		mp.seekTo(currentPosition);

		// update timer progress again
		updateProgressBar();
	}

	/**
	 * On Song Playing completed
	 * if repeat is ON play same song again
	 * Check if last in list and stop unless repeat on or shuffle on
	 * if shuffle is ON play random song
	 * Check if manual is selected, if it is load song index and pause
	 **/
	@Override
	public void onCompletion(MediaPlayer arg0) {

		//Check if only 1 song in list as causes error if not dealt with
		if (songsList.size() == 1){
			playSong(currentSongIndex);
			//Check if repeat is off and if so pause
			if (!isRepeat) {
				mp.pause();
				mp.seekTo(0);
				btnPlay.setImageResource(R.drawable.ic_media_play);
			}
		}else{

			//Set the button clicked parameter. Don't start playing if manual is on
			intButtonPressed = NEXT;

			// check for repeat is ON or OFF
			if (isRepeat) {
				// repeat is on play same song again
				playSong(currentSongIndex);
			} else if (isShuffle) {
				// shuffle is on - play a random song
				Random rand = new Random();
				//currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
				currentSongIndex = rand.nextInt((songsList.size() - 1) + 1);
				playSong(currentSongIndex);
			} else {
				// no repeat or shuffle ON - play next song
				if (currentSongIndex < (songsList.size() - 1)) {
					currentSongIndex = currentSongIndex + 1;
					playSong(currentSongIndex);
					//Check if manual is selected
					if (isManual) {
						//mp.pause();
						//mp.seekTo(0);
						// Changing button image to play button
						btnPlay.setImageResource(R.drawable.ic_media_play);
					}

				} else {
					// Reset index to start of list and stop
					currentSongIndex = 0;
					// Play first song then pause it to setup media player etc. Optional Pause?
					playSong(currentSongIndex);
					mp.pause();
					mp.seekTo(0);
					// Changing button image to play button
					btnPlay.setImageResource(R.drawable.ic_media_play);

				}
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.main, menu);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.action_exit) {
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(1);

		} else if (itemId == R.id.action_settings) {
			//Launches the settings activity
			Intent i = new Intent(getApplicationContext(), SettingsActivity.class);
			ReadSettings();
			startActivity(i);

		} else if (itemId == R.id.action_add_file) {
			//Select file dialog
			Intent intent = new Intent()
					.setType("audio/*")
					.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(Intent.createChooser(intent, "Select a file"), 80);

		} else if (itemId == R.id.action_add_folder) {
			//Select folder dialog
			Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
			startActivityForResult(intent, 75);

		} else if (itemId == R.id.action_open_m3u) {
			Intent i = new Intent(this, PlayListActivity.class);
			startActivityForResult(i, 100);

		} else if (itemId == R.id.action_save_m3u) {
			m3uWrite(songsList, "andramp.m3u");
		} else if (itemId == R.id.action_new_m3u) {
			//Clears all songs in play list
			//HashMap<String, String> song = new HashMap<>();
			HashMap<String, String> song;
			song = songsList.get(currentSongIndex);
			songsList.clear();
			songsList.add(song);

		} else if (itemId == R.id.action_search) {
			//Open search activity
			Intent i = new Intent(getApplicationContext(), SearchActivity.class);
			startActivityForResult(i, 110);
			//OnActivityResult is triggered on return from SearchActivity
		}

		return true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (mp != null) {
			mp.release();
			mp = null;
		}
		//Close database adapter
		if (adapter != null) {
			adapter.close();
		}
	}

	/**
	 * Starts media player and fades in.
	 */
	public void fadeIn(final int fadeDuration) {

		//Check if at start and skip fade in
		//Use mp.getCurrentPosition() gives current position in milliseconds
		int intPosition = mp.getCurrentPosition();

		if (intPosition == 0) {
			//Set volume to max if at start of song
			mp.setVolume(FLOAT_VOLUME_MAX, FLOAT_VOLUME_MAX);
			//Play music
			if (!mp.isPlaying()) mp.start();

		} else if (intPosition > 0) {

			//Set volume to 0
			iVolume = INT_VOLUME_MIN;
			updateVolume(0);

			//Play music
			if (!mp.isPlaying()) mp.start();
			final Timer timer = new Timer(true);
			TimerTask timerTask = new TimerTask() {
				@Override
				public void run() {
					updateVolume(1);
					if (iVolume == INT_VOLUME_MAX) {
						timer.cancel();
						timer.purge();
					}
				}
			};

			// calculate delay, cannot be zero, set to 1 if zero
			int delay = fadeDuration / INT_VOLUME_MAX;
			if (delay == 0) delay = 1;
			timer.schedule(timerTask, delay, delay);
		}
	}


	/**
	 * Fades out and pauses media player
	 */
	public void fadeOut(final int fadeDuration, final int buttonPressed) {

		// Sets iVolume to max
		iVolume = INT_VOLUME_MAX;
		updateVolume(0);

		//Start decreasing volume in increments
		final Timer timer = new Timer(true);
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				updateVolume(-1);
				if (iVolume == INT_VOLUME_MIN) {
					switch (buttonPressed) {

						case PLAY:
							mp.pause();
							break;

						case STOP:
							mp.pause();
							mp.seekTo(0);
							break;

						case NEXT:

							//Can't fade out on NEXT as fade out timer can't run in UI thread without causing lockups
							//If timer not in UI thread then you can't change the UI here without crash

							/*playSong(currentSongIndex);
							//Don't start playing if manual is on
							if (isManual) {
								mp.pause();
								mp.seekTo(0);
								// Changing button image to play button
								btnPlay.setImageResource(R.drawable.ic_media_play);
							}*/
							break;

						case PREVIOUS:

							//Can't fade out on PREVIOUS as fade out timer can't run in UI thread without causing lockups
							//If timer not in UI thread then you can't change the UI here without crash

							/*playSong(currentSongIndex);
							//Don't start playing if manual is on
							if (isManual) {
								mp.pause();
								mp.seekTo(0);
								// Changing button image to play button
								btnPlay.setImageResource(R.drawable.ic_media_play);
							}*/
							break;

						case FORWARD:

						case BACKWARD:
							mp.seekTo(newPosition);
							if (mp.isPlaying()) {
								fadeIn(fadeInTime);
							}
							break;

						default:
							break;

					}
					timer.cancel();
					timer.purge();
				}
			}

		};

		// calculate delay, cannot be zero, set to 1 if zero
		int delay = fadeDuration / INT_VOLUME_MAX;
		if (delay <= 0) delay = 1;
		timer.schedule(timerTask, delay, delay);
		//fadeOutButtonPressed ( buttonPressed);
	}

	//Update the volume, change can be + or - 1 to 100 or 0
	private void updateVolume(int change) {

		//increment or decrement depending on type of fade
		iVolume = iVolume + change;

		//ensure iVolume within boundaries
		if (iVolume < INT_VOLUME_MIN)
			iVolume = INT_VOLUME_MIN;
		else if (iVolume > INT_VOLUME_MAX)
			iVolume = INT_VOLUME_MAX;

		//convert to float value
		float fVolume = 1 - ((float) Math.log(INT_VOLUME_MAX - iVolume) / (float) Math.log(INT_VOLUME_MAX));

		//ensure fVolume within boundaries
		if (fVolume < FLOAT_VOLUME_MIN)
			fVolume = FLOAT_VOLUME_MIN;
		else if (fVolume > FLOAT_VOLUME_MAX)
			fVolume = FLOAT_VOLUME_MAX;

		mp.setVolume(fVolume, fVolume);
	}


	private void initVolumeControl() {
		try {
			// Set volume seekbar to have as many divisions as volume control allows about 15?
			// Then sets it to current volume
			volumeProgressBar.setMax(mAudioManager
					.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
			volumeProgressBar.setProgress(mAudioManager
					.getStreamVolume(AudioManager.STREAM_MUSIC));


			volumeProgressBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
				@Override
				public void onStopTrackingTouch(SeekBar arg0) {
				}

				@Override
				public void onStartTrackingTouch(SeekBar arg0) {
				}

				@Override
				public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
					mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
							progress, 0);
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setBrightness(float brightness) {
		WindowManager.LayoutParams lp = this.getWindow().getAttributes();
		lp.screenBrightness = brightness;// dim the display, float value from 0.0f to 1.0f
		this.getWindow().setAttributes(lp);
	}


	@Override
	public void onResume() {
		super.onResume();
		//Read the saved settings and setup options
		ReadSettings();
	}


	//Copy file from path1 to path2
	public void copyFile(InputStream in ,String  path) {
		try {
//			InputStream in = getResources().openRawResource(R.raw.llama);
//			OutputStream out = new FileOutputStream(HOME_DIR + "/llama.mp3");

			OutputStream out = new FileOutputStream(path);

			// Transfer bytes from in to out
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	//Detects back button pressed twice to exit within 1000ms
	@Override
	public void onBackPressed() {
		if (back_pressed + 1000 > System.currentTimeMillis()) {
			super.onBackPressed();
		} else {
			Toast.makeText(getBaseContext(),
					"Press once again to exit!", Toast.LENGTH_SHORT)
					.show();
		}
		back_pressed = System.currentTimeMillis();
	}

	//Detects the menu key pressed, which causes a crash in API > 22
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
		return super.onKeyDown(keyCode, event);
	}


	//Read the settings from the SQLite database andramp.db
	public void ReadSettings() {

		//SQLite database holds settings and tracks
		isManual = ConvertStringToBoolean(adapter.getSetting("manual"));
		isScreen = ConvertStringToBoolean(adapter.getSetting("screen"));
		isLandscape = ConvertStringToBoolean(adapter.getSetting("landscape"));
		isPortrait = ConvertStringToBoolean(adapter.getSetting("portrait"));
		HOME_DIR = adapter.getSetting("home");

	}


	//Setup the options read from the SQLite database
	@SuppressLint("SourceLockedOrientationActivity")
	public void SetupOptions() {

		if (isManual) {
			//Hide stop, forward, backward, shuffle and repeat buttons and volume label
			//Show PlayList button
			isRepeat = false;
			//btnRepeat.setImageResource(R.drawable.ic_media_repeat);
			isShuffle = false;
			//btnShuffle.setImageResource(R.drawable.ic_media_shuffle);
			btnRepeat.setVisibility(View.GONE);
			btnShuffle.setVisibility(View.GONE);
			btnForward.setVisibility(View.GONE);
			btnBackward.setVisibility(View.GONE);
			btnStop.setVisibility(View.GONE);

		} else {
			//Show stop, forward, backward, shuffle and repeat buttons and volume label
			//Hide PlayList button
			btnRepeat.setVisibility(View.VISIBLE);
			btnShuffle.setVisibility(View.VISIBLE);
			btnForward.setVisibility(View.VISIBLE);
			btnBackward.setVisibility(View.VISIBLE);
			btnStop.setVisibility(View.VISIBLE);
//			//btnPlayList.setVisibility(View.GONE);
//			//Make play button small, set to 66dp height and width
//			LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) btnPlay.getLayoutParams();
//			//Is it a percentage???
//			params.width = 100;
//			params.height = 100;
//			btnPlay.setLayoutParams(params);
//			//textViewVolume.setVisibility(View.VISIBLE);
		}

		if (isScreen) {
			//Keep screen on
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
			//Dim screen, float value from 0.0 to 1.0
			setBrightness(MIN_BRIGHTNESS);
		} else {
			getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		if (isLandscape) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		}

		if (isPortrait) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}

	}

	/**
	 * Detect user activity
	 * Use to set brightness to max for 20s
	 */
	@Override
	public void onUserInteraction() {

		//Cancel Timer and Task if already running
		if (myTimer != null) {
			myTimer.cancel();
			myTimer = null;
		}
		if (myTask != null) {
			myTask.cancel();
			myTask = null;
		}

		setBrightness(MAX_BRIGHTNESS);
		//Create new instance and re-schedule timer here
		//otherwise, IllegalStateException of
		//"TimerTask is scheduled already will be thrown
		myTimer = new Timer();
		myTask = new myTimerTask();
		myTimer.schedule(myTask, SCREEN_DIM_TIME);

	}

	class myTimerTask extends TimerTask {
		public void run() {
			//Perform background work here
			//myTaskHandler.post(new Runnable() {
			myTaskHandler.post(() -> {
				//Perform GUI updating work here
				//Toast work also
				setBrightness(MIN_BRIGHTNESS);
			});

			if (myTimer != null) {
				myTimer.cancel();
				myTimer = null;
			}
			if (myTask != null) {
				myTask.cancel();
				myTask = null;
			}

		}
	}


	/**
	 * Convert string to boolean
	 * NB don't use ==, use .equals()
	 */
	public boolean ConvertStringToBoolean(String s) {
		boolean result = false;
		if (s.equals("true")) {
			result = true;
		}
		return result;
	}

	/**
	 * Play file with VLC
	 */
	public void  startVLC(String filePath) {

		//Check VLC is installed
		if (isPackageInstalled("org.videolan.vlc")) {

			String filePrefix = "file://";
			int vlcRequestCode = 42;
			Uri uri = Uri.parse(filePrefix + filePath);
			Intent vlcIntent = new Intent(Intent.ACTION_VIEW);
			vlcIntent.setPackage("org.videolan.vlc");
			vlcIntent.setDataAndTypeAndNormalize(uri, "*/*");
			//vlcIntent.setDataAndTypeAndNormalize(uri, "video/*");
			//vlcIntent.setDataAndTypeAndNormalize(uri, "audio/*");
			//vlcIntent.putExtra("title", "Test");
			vlcIntent.putExtra("from_start", true);
			vlcIntent.putExtra("position", 0);
			startActivityForResult(vlcIntent, vlcRequestCode);


		} else {
			//VLC v3 shows blank screen use v2.9.1
			//In Preferences disable the history checkbox
			Toast.makeText(this, "Please Install VLC Media Player v2.9.1", Toast.LENGTH_LONG).show();
		}


	}

	@SuppressLint("QueryPermissionsNeeded")
	public boolean isPackageInstalled(String targetPackage){
		List<ApplicationInfo> packages;
		PackageManager pm;

		pm = getPackageManager();
		packages = pm.getInstalledApplications(0);
		for (ApplicationInfo packageInfo : packages) {
			if(packageInfo.packageName.equals(targetPackage))
				return true;
		}
		return false;
	}

	/**
	 * Check if cdg file exists
	 */
	public boolean checkCDGFileExists (String filePath){
		String [] filenameArray = filePath.split("\\.");
		String extension = filenameArray[filenameArray.length-1];
		String CDGfileNoPath = filePath.substring(0,filePath.length()- extension.length());
		String CDGfilePath = CDGfileNoPath + "cdg";
		File file = new File(CDGfilePath);
		return ( file.exists());
	}

	/**
	 * Check file extension
	 */
	public boolean checkFileExtension (String filePath, String extension){
		String ext = filePath.substring(filePath.lastIndexOf(".")).toLowerCase();
		//return (extension.toLowerCase().equals(ext));
		return (extension.equalsIgnoreCase(ext));
	}


	public String FileExtension (File f){
		//Get the file name as a String, split it into an array with "." as the delimiter,
		//and then get the last index of the array, which would be the file extension.
		String filename = f.getName();
		String[] filenameArray = filename.split("\\.");
		return filenameArray[filenameArray.length - 1];
	}

	public void AddMp3ToList(File f){
		//Create a song array
		HashMap<String, String> song = new HashMap<>();
		//Add just one entry to end of songsList and remove the .mp3 ie last 4 characters
		String filename = f.getName();
		song.put("songTitle", filename.substring(0, filename.length() - 4));
		song.put("songPath", f.getAbsolutePath());
		song.put("songIndex", String.valueOf(songsList.size()));
		// Add song to SongList array list
		songsList.add(song);
		//Set currentSongIndex to last song
		currentSongIndex = songsList.size() - 1;
		//Automatically play MP3 to load song
		playSong(currentSongIndex);
		//Pause at start
		mp.pause();
		mp.seekTo(0);
		// Changing button image to play button
		btnPlay.setImageResource(R.drawable.ic_media_play);
	}

	//Called when permissions are allowed or denied
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
	{
		if (requestCode == 1) {// If request is cancelled, the result arrays are empty.
			if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				//Permissions granted so restart
				restartActivity();
			} else {
				//If user didn't provide you permission.
				Toast.makeText(this, "Please grant permissions.", Toast.LENGTH_LONG).show();
				finish();
			}
		}
	}

	public void restartActivity() {
		//Restart after 0.5s
		AlarmManager mgr = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		assert mgr != null;
		mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 500, PendingIntent.getActivity(this.getBaseContext(), 0, new    Intent(getIntent()), getIntent().getFlags()));
		android.os.Process.killProcess(android.os.Process.myPid());
	}

	/**
	 * Function to read all mp3 files from "folderPath" on sdcard
	 * and store the details in an ArrayList
	 * */
	ArrayList<HashMap<String, String>> getPlayList(String folderPath){

		try {

			//Song position index
			int x = 1;
			File home = new File(folderPath);
			for (File file : Objects.requireNonNull(home.listFiles(new FileExtensionFilter()))) {
				HashMap<String, String> song = new HashMap<>();
				song.put("songTitle", file.getName().substring(0, (file.getName().length() - 4)));
				song.put("songPath", file.getPath());
				song.put("songIndex", String.valueOf(x));
				// Adding each song to SongList
				playList.add(song);
//TODO Display x and folderPath to act as a progress indicator or just a spinner?
				x++;
			}
			//Add subdirectories
			for (File file : Objects.requireNonNull(home.listFiles())) {
				if (file.isDirectory ()) {
					getPlayList(file.getAbsolutePath());
				}
			}

			//Log.i("INFO", "playList.size " + playList.size());
			return playList;

		} catch (Exception e) {
			return null;
		}

	}


	/**
	 * Class to filter files which are having .mp3 extension
	 * */
	static class FileExtensionFilter implements FilenameFilter {
		public boolean accept(File dir, String name) {
			return (name.endsWith(".mp3") || name.endsWith(".MP3")|| name.endsWith(".Mp3")|| name.endsWith(".mP3"));
		}
	}

	public void m3uRead(File f){

		//Clear m3uList
		m3uList.clear();

		//songIndex counter
		int x = 0;

		String[] filepathArray = f.getAbsolutePath().split("/");
		String baseDir = "/" + filepathArray[1] + "/" + filepathArray[2];

		//Tablet uses /emulated/0 for internal SD card so need to add filepathArray[3]
		if (filepathArray[2].equalsIgnoreCase("emulated"))
		{
			baseDir = baseDir + "/" + filepathArray[3];
		}

		//Path of f without the file name
		String fDir = f.getParent() + "/";

		try {
			//Read in a line of m3u, ignore if starts with #
			//and put title and path into song list
			FileInputStream is;
			BufferedReader reader;
			is = new FileInputStream(f);
			reader = new BufferedReader(new InputStreamReader(is));
			String line = reader.readLine();

			while (line != null) {
				//If line starts with a # don't store title and path
				if (!isFirstCharacterHash(line)) {
					//Check second character not :
					if (isSecondCharacterColon(line)) {
						//Remove first 2 characters as it is a drive letter from windows
						line = line.substring(2);
					}

					//Get the mp3 path, change \ to / if needed
					line = line.replace('\\', '/');

//TODO Sort reading andramp.m3u
					//If no directory use M3U file directory as MP3 path
					String path;
					if (line.matches("(?i).*/.*")) {
						path = baseDir + line;
					} else {
						path = fDir + line;
					}
					//Get the mp3 title
					String title = path.substring(path.lastIndexOf("/") + 1);
					//Create a new song hashmap for each line
					HashMap<String, String> m3uSong = new HashMap<>();
					m3uSong.put("songTitle", title.substring(0, title.length() - 4));
					m3uSong.put("songPath", path);
					m3uSong.put("songIndex", String.valueOf(x));
					// Add song to m3uList array list
					m3uList.add(m3uSong);
					x++;
				}
				line = reader.readLine();
			}
			reader.close();

			songsList = m3uList;

			//Load first song then pause at start
			playSong(0);
			mp.pause();
			mp.seekTo(0);
			// Changing button image to play button
			btnPlay.setImageResource(R.drawable.ic_media_play);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



	/**
	 * Function to write an m3u file
	 * Will take a songsList and a filename as parameters
	 * */
	public void m3uWrite(ArrayList<HashMap<String, String>> list, String filename){

//TODO Add file storage dialogue
		int x = 0;
		//Create a new song hashmap for each line
		HashMap<String, String> m3uSong ;
		String path;
		FileOutputStream fos ;

		try {
			//String APP_FILES_PATH = Objects.requireNonNull(this.getExternalFilesDir(null)).getAbsolutePath();
			//String fullPath = Environment.getExternalStorageDirectory().getPath() +"/Music/" + filename;
			String fullPath = APP_FILES_PATH +"/" + filename;
			File myFile = new File(fullPath);
//TODO Add a replace dialogue
			if(myFile.exists()){
				boolean delete = myFile.delete();
				if (delete){
					Toast.makeText(this, "File deleted", Toast.LENGTH_SHORT).show();
				}
			}

			fos = new FileOutputStream(  fullPath, true);
			FileWriter fWriter;

			try {
				fWriter = new FileWriter(fos.getFD());

				while (x < list.size())
				{
					m3uSong = list.get(x);
					path = m3uSong.get("songPath");
//TODO Store relative path?
					path = path + "\n";
					fWriter.write(path);
					x++;
				}

				fWriter.close();

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				fos.getFD().sync();
				fos.close();
				Toast.makeText(this, "New M3U file saved", Toast.LENGTH_SHORT).show();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		//Logcat and StackTrace writer test
		try
		{
			throw new RuntimeException("This is an intentional crash");
		}
		catch(Exception e)
		{
			saveStackTraceToSDCard(e);
			saveLogcatToSDCard();
		}

	}

	public void saveStackTraceToSDCard(Throwable throwable) {

		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		throwable.printStackTrace(pw);
		String msg = sw.getBuffer().toString();
		//String APP_FILES_PATH = Objects.requireNonNull(this.getExternalFilesDir(null)).getAbsolutePath();
		//String filename = Environment.getExternalStorageDirectory() + File.separator + "andramp_stacktrace.txt";
		String filename = APP_FILES_PATH + File.separator + "andramp_stacktrace.txt";

		PrintWriter printWriter = null;
		File file = new File(filename);
		try {
			if (!file.exists()) {
				boolean newFile = file.createNewFile();
				if (!newFile){
					Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
				}
			}
			//Append
			//printWriter = new PrintWriter(new FileOutputStream(filename, true));
			//Overwrite
			printWriter = new PrintWriter(new FileOutputStream(filename, false));
			printWriter.write(msg);
		} catch (IOException ioex) {
			ioex.printStackTrace();
		} finally {
			if (printWriter != null) {
				printWriter.flush();
				printWriter.close();
			}
		}
	}

	public void saveLogcatToSDCard(){
		//String APP_FILES_PATH = Objects.requireNonNull(this.getExternalFilesDir(null)).getAbsolutePath();
		//String filename = Environment.getExternalStorageDirectory() + File.separator + "andramp_logcat.txt";
		String filename = APP_FILES_PATH + File.separator + "andramp_logcat.txt";
		String command = "logcat -d *:V";

		try{
			Process process = Runtime.getRuntime().exec(command);

			BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line;
			try{
				File file = new File(filename);

				//Delete old file
				if(file.exists()){
					boolean delete = file.delete();
					if (delete){
						Toast.makeText(this, "File deleted", Toast.LENGTH_SHORT).show();
					}
				}

				//Create new file
				if (file.createNewFile()) {
					FileWriter writer = new FileWriter(file);
					while ((line = in.readLine()) != null) {
						writer.write(line + "\n");
					}
					writer.flush();
					writer.close();
				}else{
					Toast.makeText(this, "Cannot create file", Toast.LENGTH_SHORT).show();
				}
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}

	//Returns the app files folder eg storage/emulated/0/Android/data/com.andramp.mcssoftware/files/
	public static String getAppFilesFolder(Context context) {
		String folder;
		File externalFolder = context.getExternalFilesDir(null);
		assert externalFolder != null;
		folder = externalFolder.getAbsolutePath();
		return folder;
	}

}