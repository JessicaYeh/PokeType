package yeh.poketype;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONObject;

import yeh.poketype.ClearableAutoCompleteTextView.OnClearListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
	private Context mContext = this;
	private Bitmap mImageDownload = null;
	
	private SharedPreferences sharedPref;
	private SharedPreferences.Editor editor;

	// Action bar views
	private TextView mTitle;
	private ClearableAutoCompleteTextView mSearchBox;
	private ImageView mSearchIcon;

	// Main content area views
	private FrameLayout mContent;
	private LinearLayout mHelp;
	private LinearLayout mData;
	private ProgressBar mLoading;
	private ImageButton mListButton;

	// Pokemon info views
	private ImageView mImage;
	private TextView mPokemonName;
	private TextView mPokemonType1;
	private TextView mPokemonType2;

	// Type effectiveness views and variables
	private ExpandableListView mTypeEffectiveness;
	private EffectivenessAdapter mEffectivenessAdapter;
	private ArrayList<EffectivenessGroup> mGroups = new ArrayList<EffectivenessGroup>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Open preferences file
		sharedPref = PreferenceManager
		        .getDefaultSharedPreferences(getApplicationContext());
		editor = sharedPref.edit();
		// The list activity hasn't been opened yet
		editor.putBoolean("pokemon_list_opened", false);
		editor.commit();

		// Get handle to views in activity_main
		mContent = (FrameLayout) findViewById(R.id.content);
		mHelp = (LinearLayout) findViewById(R.id.help);
		mData = (LinearLayout) findViewById(R.id.pokemon_data);
		mLoading = (ProgressBar) findViewById(R.id.loading);
		mListButton = (ImageButton) findViewById(R.id.list_button);
		mImage = (ImageView) findViewById(R.id.pokemon_image);
		mPokemonName = (TextView) findViewById(R.id.pokemon_name);
		mPokemonType1 = (TextView) findViewById(R.id.pokemon_type1);
		mPokemonType2 = (TextView) findViewById(R.id.pokemon_type2);

		// Inflate custom view that has a search field for action bar
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
		        | ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_HOME);
		LayoutInflater inflater = (LayoutInflater) this
		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.action_search, null);
		actionBar.setCustomView(v);

		// Get handles to action bar items
		mTitle = (TextView) v.findViewById(R.id.app_name);
		mSearchIcon = (ImageView) v.findViewById(R.id.search_icon);
		mSearchBox = (ClearableAutoCompleteTextView) v
		        .findViewById(R.id.search);

		// Populate the AutoCompleteTextView with suggestions
		ArrayList<PokemonSearchItem> pokemon = new ArrayList<PokemonSearchItem>();
		String[] pokemon_names = getResources().getStringArray(
		        R.array.pokemon_names);
		for (int i = 0; i < pokemon_names.length; i++) {
			pokemon.add(new PokemonSearchItem(i + 1, pokemon_names[i]));
		}
		Collections.sort(pokemon);
		PokemonSuggestAdapter adapter = new PokemonSuggestAdapter(this, pokemon);
		mSearchBox.setAdapter(adapter);

		// Set the adapter on the type effectiveness ExpandableListView
		mTypeEffectiveness = (ExpandableListView) findViewById(R.id.type_effectiveness);
		mEffectivenessAdapter = new EffectivenessAdapter(this, mGroups);
		mTypeEffectiveness.setAdapter(mEffectivenessAdapter);
		
		// Hide the search field
		showSearch(false);

		// When the search icon is clicked, hide search icon, show the search
		// field
		mSearchIcon.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showSearch(true);
			}
		});

		// When the search field is cleared, hide it, and show the search icon
		mSearchBox.setOnClearListener(new OnClearListener() {
			@Override
			public void onClear() {
				showSearch(false);
			}
		});

		// When focus on the search field is lost, hide search
		mContent.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showSearch(false);
			}
		});

		// When 'enter' is pressed in the search field, execute the search
		mSearchBox.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View view, int keyCode, KeyEvent event) {
				// Determine if the enter key was pressed
				if (event.getAction() == KeyEvent.ACTION_DOWN
				        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
					// Handle for the search field
					ClearableAutoCompleteTextView search = (ClearableAutoCompleteTextView) view;
					// Initiate a Pokemon search using the search contents
					new SearchTask().execute(search.getText().toString());
					return true;
				}
				return false;
			}
		});

		// When a suggestion is clicked, execute the search
		mSearchBox
		        .setOnItemClickListener(new AdapterView.OnItemClickListener() {
			        @Override
			        public void onItemClick(AdapterView<?> parent, View view,
			                int position, long id) {
				        // Initiate a Pokemon search using the suggestion
				        // clicked
				        new SearchTask().execute("" + id);
			        }
		        });

		// When the list button (in center) is clicked, open the list activity
		mListButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Record that the list activity has been opened
				boolean listOpened = sharedPref.getBoolean("pokemon_list_opened", false);
				if (!listOpened) {
					editor.putBoolean("pokemon_list_opened", true);
					editor.commit();
				}
				// Start the list activity
				Intent intent = new Intent(mContext, PokemonList.class);
				startActivityForResult(intent, 1);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onBackPressed() {
		// If the list activity has already been opened during the lifecycle of
		// the app, pressing back will take you to that activity. Otherwise,
		// the back button has default behavior.
		boolean listOpened = sharedPref.getBoolean("pokemon_list_opened", false);
		if (listOpened) {
			Intent intent = new Intent(mContext, PokemonList.class);
			startActivityForResult(intent, 1);
		}
		else {
		    super.onBackPressed();
		}
	}
	
	// Get the Pokemon id from the Pokemon list activity and search for it
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
			String id = data.getStringExtra(PokemonList.POKEMON_ID);
			if (!id.equals("")) {
				new SearchTask().execute(id);
			}
		}
	}

	// Fill the ExpandableListView groups with weaknesses, normal, and
	// resistances
	public void populateTypeEffectiveness(JSONArray w, JSONArray n, JSONArray r) {
		mGroups.clear();

		ArrayList<EffectivenessItem> weaknesses = new ArrayList<EffectivenessItem>();
		addToTypeEffectivenessArray(weaknesses, w);
		mGroups.add(new EffectivenessGroup(getString(R.string.weaknesses),
		        weaknesses));

		ArrayList<EffectivenessItem> normal = new ArrayList<EffectivenessItem>();
		addToTypeEffectivenessArray(normal, n);
		mGroups.add(new EffectivenessGroup(getString(R.string.normal_damage),
		        normal));

		ArrayList<EffectivenessItem> resistances = new ArrayList<EffectivenessItem>();
		addToTypeEffectivenessArray(resistances, r);
		mGroups.add(new EffectivenessGroup(getString(R.string.resistances),
		        resistances));
	}

	// Parses a JSONArray of type/effectiveness pairs and adds it to the
	// ArrayList
	private void addToTypeEffectivenessArray(
	        ArrayList<EffectivenessItem> array, JSONArray json) {
		for (int i = 0; i < json.length(); i++) {
			JSONObject item;
			try {
				item = json.getJSONObject(i);
				String type = item.getString("attack");
				int effectiveness = Integer.parseInt(item
				        .getString("effectiveness"));
				array.add(new EffectivenessItem(getResources().getIdentifier(
				        type, "drawable", getPackageName()),
				        capitalizeFirst(type), effectiveness));
			} catch (Exception e) {
			}
		}
	}

	// Capitalize the first letter of a string
	private String capitalizeFirst(String str) {
		if (str.length() == 0)
			return str;
		return str.substring(0, 1).toUpperCase(Locale.getDefault())
		        + str.substring(1);
	}

	// AsyncTask for searching for Pokemon
	private class SearchTask extends AsyncTask<String, Void, PokemonData> {
		private boolean mNetworkConnected = false;
		private boolean mHttpSuccess = false;
		private boolean mPokemonFound = false;
		private String mQuery = "";

		@Override
		protected void onPreExecute() {
			// Hide Pokemon info
			mData.setVisibility(View.GONE);
			// Hide list button
			mListButton.setVisibility(View.GONE);
			// Show loading spinner
			mLoading.setVisibility(View.VISIBLE);
			// Minimize the search field
			showSearch(false);
		}

		@Override
		protected PokemonData doInBackground(String... params) {
			PokemonData pokemon = null;

			mNetworkConnected = hasInternet();

			// Search for the Pokemon if there is an Internet connection
			if (mNetworkConnected) {
				mQuery = params[0];

				// Download data
				AjaxRequest request = new AjaxRequest("GET",
				        getPokemonUrl(mQuery));
				JSONObject data;
				try {
					data = new JSONObject(request.send());
					if (!data.isNull("id")) {
						Bitmap image = downloadImage(data.getString("image"));
						pokemon = new PokemonData(data, image);
						mPokemonFound = true;
					} else {
						mPokemonFound = false;
					}
					mHttpSuccess = true;
				} catch (Exception e) {
					mHttpSuccess = false;
				}
			}

			return pokemon;
		}

		@Override
		protected void onPostExecute(PokemonData result) {
			if (result != null) {
				// Update the image
				mImage.setImageBitmap(result.mImage);

				// Update the Pokemon info
				try {
					// Update the type effectiveness ExpandableListView
					populateTypeEffectiveness(
					        result.mInfo.getJSONArray("weaknesses"),
					        result.mInfo.getJSONArray("normal"),
					        result.mInfo.getJSONArray("resistances"));

					// Update the Pokemon name
					mPokemonName.setText(result.mInfo.getString("name"));

					// Update the Pokemon types
					String type1 = result.mInfo.getString("type1");
					String type2 = result.mInfo.getString("type2");
					mPokemonType1.setText(capitalizeFirst(type1));
					mPokemonType1.setBackgroundResource(getResources()
					        .getIdentifier(type1 + "_background", "drawable",
					                getPackageName()));
					if (type2.equals("null")) {
						mPokemonType2.setVisibility(View.GONE);
					} else {
						mPokemonType2.setVisibility(View.VISIBLE);
						mPokemonType2.setText(capitalizeFirst(type2));
						mPokemonType2.setBackgroundResource(getResources()
						        .getIdentifier(type2 + "_background",
						                "drawable", getPackageName()));
					}

					// Hide help
					mHelp.setVisibility(View.GONE);

				} catch (Exception e) {
				}
				
				// Update the type effectiveness ExpandableListView
				mEffectivenessAdapter.notifyDataSetChanged();

				// Expand the Weaknesses group, collapse the other two groups
				mTypeEffectiveness.expandGroup(0);
				mTypeEffectiveness.collapseGroup(1);
				mTypeEffectiveness.collapseGroup(2);
				mTypeEffectiveness.setSelectionAfterHeaderView();
			} else {
				// Display an error
				String errorTitle = "";
				String errorMessage = "";

				if (!mNetworkConnected) {
					errorTitle = getString(R.string.error_title_no_internet);
					errorMessage = getString(R.string.error_message_no_internet);
				} else if (!mHttpSuccess) {
					errorTitle = getString(R.string.error_title_http);
					errorMessage = getString(R.string.error_message_http);
				} else if (!mPokemonFound) {
					errorTitle = getString(R.string.error_title_no_pokemon);
					errorMessage = getString(R.string.error_message_no_pokemon)
					        + " '" + mQuery + "'";
				} else {
					errorTitle = getString(R.string.error_title_unknown);
					errorMessage = getString(R.string.error_message_unknown);
				}

				showError(errorTitle, errorMessage);
			}

			// Hide loading spinner
			mLoading.setVisibility(View.GONE);
			// Show list button
			mListButton.setVisibility(View.VISIBLE);
			// Show Pokemon info
			if (mHelp.getVisibility() == View.GONE) {
				mData.setVisibility(View.VISIBLE);
			}
		}

		// Download an image from the imageUrl and return it.
		private Bitmap downloadImage(String imageUrl) {
			// If the image exists, recycle it, to prevent memory leaks.
			if (mImageDownload != null) {
				mImageDownload.recycle();
				mImageDownload = null;
			}

			URL url;
			try {
				url = new URL(imageUrl);
				mImageDownload = BitmapFactory.decodeStream(url.openStream());
			} catch (Exception e) {
				Log.e("Error", e.getMessage());
				e.printStackTrace();
			}

			return mImageDownload;
		}

		// Returns the URL to get the Pokemon data using the given query
		private String getPokemonUrl(String query) {
			String url;

			// Check whether to search by id or name
			try {
				Integer.parseInt(query);
				url = "http://poketype.floccul.us/api/pokemon/id/" + query;
			} catch (NumberFormatException e) {
				url = "http://poketype.floccul.us/api/pokemon/" + query;
			}

			return url;
		}

		// Check whether there is an Internet connection.
		private boolean hasInternet() {
			ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connectivityManager
			        .getActiveNetworkInfo();
			if (networkInfo != null) {
				return true;
			}
			return false;
		}

		// Display an error popup with the given title and message.
		private void showError(String title, String message) {
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
			        mContext);
			alertDialogBuilder.setTitle(title);
			alertDialogBuilder
			        .setMessage(message)
			        .setCancelable(false)
			        .setPositiveButton(getString(R.string.error_exit),
			                new DialogInterface.OnClickListener() {
				                public void onClick(DialogInterface dialog,
				                        int id) {
					                dialog.cancel();
				                }
			                });
			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();
		}
	}

	// this toggles between the visibility of the search icon and the search box
	// to show search icon - reset = true
	// to show search box - reset = false
	protected void showSearch(boolean show) {
		if (!show) {
			// hide search box and show search icon
			mSearchBox.setText("");
			mSearchBox.setVisibility(View.GONE);
			mSearchIcon.setVisibility(View.VISIBLE);
			mTitle.setVisibility(View.VISIBLE);
			// hide the keyboard
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(mSearchBox.getWindowToken(), 0);
		} else {
			// hide search icon and show search box
			mSearchIcon.setVisibility(View.GONE);
			mSearchBox.setVisibility(View.VISIBLE);
			mTitle.setVisibility(View.GONE);
			mSearchBox.requestFocus();
			// show the keyboard
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(mSearchBox, InputMethodManager.SHOW_IMPLICIT);
		}
	}
}
