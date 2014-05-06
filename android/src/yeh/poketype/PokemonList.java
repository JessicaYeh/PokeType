package yeh.poketype;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class PokemonList extends ActionBarActivity {
	// Shared preferences
	public final static String POKEMON_ID = "yeh.poketype.POKEMON_ID";
	private SharedPreferences sharedPref;
	private SharedPreferences.Editor editor;

	private PokemonListAdapter mAdapter;

	// Action bar views
	private TextView mTitle;
	private ClearableAutoCompleteTextView mSearchBox;
	private ImageView mSearchIcon;

	// Views in UI
	private Spinner mType1;
	private Spinner mType2;
	private ListView mListView;
	private TextView mNoneFound;

	// Spinner counts to prevent undesired onItemSelected calls
	private int mSpinnerCount = 0;
	private int mInitSpinnerCount = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pokemon_list);

		// Get views
		mType1 = (Spinner) findViewById(R.id.spinner_type1);
		mType2 = (Spinner) findViewById(R.id.spinner_type2);
		mListView = (ListView) findViewById(R.id.list);
		mNoneFound = (TextView) findViewById(R.id.no_types_found);

		// Open preferences file
		sharedPref = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		editor = sharedPref.edit();

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

		ArrayList<PokemonSearchItem> pokemon = new ArrayList<PokemonSearchItem>();

		Resources res = getResources();
		// Get the array of Pokemon names.
		String[] pokemonNames = res.getStringArray(R.array.pokemon_names);
		// Get the arrays of Pokemon types.
		String[] pokemonTypes1 = res.getStringArray(R.array.pokemon_types1);
		String[] pokemonTypes2 = res.getStringArray(R.array.pokemon_types2);
		// Create the list of Pokemon.
		for (int i = 0; i < pokemonNames.length; i++) {
			pokemon.add(new PokemonSearchItem(i + 1, pokemonNames[i],
					pokemonTypes1[i], pokemonTypes2[i]));
		}

		// Restore state information from shared preferences
		int type1 = sharedPref.getInt("pokemon_type1", 0);
		if (type1 != 0) {
			mType1.setSelection(type1);
		}
		int type2 = sharedPref.getInt("pokemon_type2", 0);
		if (type2 != 0) {
			mType2.setSelection(type2);
		}

		// Insert the Pokemon into the ListView.
		mAdapter = new PokemonListAdapter(this, pokemon);
		mListView.setAdapter(mAdapter);
		filterByTypes(
				mType1.getItemAtPosition(mType1.getSelectedItemPosition())
						.toString(),
				mType2.getItemAtPosition(mType2.getSelectedItemPosition())
						.toString());

		// Set click listener on the list view
		mListView.setOnItemClickListener(new PokemonClickListener());

		// Set selected listeners on the spinners
		mType1.setOnItemSelectedListener(typeSelectedListener);
		mType2.setOnItemSelectedListener(typeSelectedListener);

		// Set listview position from the shared preferences
		int position = sharedPref.getInt("pokemon_list_position", 0);
		if (position != 0) {
			mListView.setSelectionFromTop(position, 0);
		}

		// How many spinners are on the page
		mSpinnerCount = 2;

		// Hide the search field
		showSearch(false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.pokemon_list, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}

	// Click listener for the Pokemon in the ListView. Returns to the
	// main activity and searches for that Pokemon.
	private class PokemonClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// Pass the selected Pokemon id to the main screen
			Intent mIntent = new Intent();
			mIntent.putExtra(POKEMON_ID, "" + id);
			setResult(Activity.RESULT_OK, mIntent);
			saveState();
			finish();
		}
	}

	OnItemSelectedListener typeSelectedListener = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> parentView,
				View selectedItemView, int position, long id) {
			// Spinner counts to prevent undesired calls to onItemSelected
			if (mInitSpinnerCount < mSpinnerCount) {
				mInitSpinnerCount++;
			} else {
				String type1 = mType1.getItemAtPosition(
						mType1.getSelectedItemPosition()).toString();
				String type2 = mType2.getItemAtPosition(
						mType2.getSelectedItemPosition()).toString();
				filterByTypes(type1, type2);
				mListView.setSelectionAfterHeaderView();
				saveState();
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> parentView) {
		}
	};

	private void filterByTypes(String type1, String type2) {
		// Filter by the two types selected in the Spinners
		mAdapter.getFilter().filter(type1 + "|" + type2,
				new Filter.FilterListener() {
					public void onFilterComplete(int count) {
						// Display a message if there are no Pokemon with the
						// selected combination of types.
						if (count == 0) {
							mNoneFound.setVisibility(View.VISIBLE);
						} else {
							mNoneFound.setVisibility(View.GONE);
						}
					}
				});
	}

	@Override
	public void onBackPressed() {
		saveState();
		super.onBackPressed();
	}

	@Override
	public void onPause() {
		saveState();
		super.onPause();
	}

	// Store information in the shared preferences so that when the list
	// activity is opened again, things are in the same place
	private void saveState() {
		editor.putInt("pokemon_list_position",
				mListView.getFirstVisiblePosition());
		editor.putInt("pokemon_type1", mType1.getSelectedItemPosition());
		editor.putInt("pokemon_type2", mType2.getSelectedItemPosition());
		editor.commit();
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
