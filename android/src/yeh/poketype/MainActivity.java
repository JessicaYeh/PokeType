package yeh.poketype;

import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONObject;

import yeh.poketype.ClearableAutoCompleteTextView.OnClearListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
	// Action bar items
	private ClearableAutoCompleteTextView mSearchBox;
	private ImageView mSearchIcon;

	// Main content area items
	private FrameLayout mContent;
	private LinearLayout mHelp;
	private LinearLayout mData;
	private ProgressBar mLoading;
	private TextView mInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Get handle to views in activity_main
		mContent = (FrameLayout) findViewById(R.id.content);
		mHelp = (LinearLayout) findViewById(R.id.help);
		mData = (LinearLayout) findViewById(R.id.pokemon_data);
		mLoading = (ProgressBar) findViewById(R.id.loading);
		mInfo = (TextView) findViewById(R.id.pokemon_info);

		// Inflate custom view that has a search field for action bar
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
		        | ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_HOME);
		LayoutInflater inflater = (LayoutInflater) this
		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.action_search, null);
		actionBar.setCustomView(v);

		// Get handles to action bar items
		mSearchIcon = (ImageView) v.findViewById(R.id.search_icon);
		mSearchBox = (ClearableAutoCompleteTextView) v
		        .findViewById(R.id.search);

		// Hide the search field
		showSearch(false);

		// Populate the AutoCompleteTextView with suggestions
		ArrayList<PokemonSearchItem> pokemon = new ArrayList<PokemonSearchItem>();
		String[] pokemon_names = getResources().getStringArray(
		        R.array.pokemon_names);
		TypedArray icons = getResources().obtainTypedArray(
		        R.array.pokemon_icons);
		for (int i = 0; i < icons.length(); i++) {
			pokemon.add(new PokemonSearchItem(i + 1, pokemon_names[i], icons
			        .getDrawable(i)));
		}
		icons.recycle();
		Collections.sort(pokemon);
		PokemonSuggestAdapter adapter = new PokemonSuggestAdapter(this, pokemon);
		mSearchBox.setAdapter(adapter);

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

	// AsyncTask for searching for Pokemon
	private class SearchTask extends AsyncTask<String, Void, PokemonData> {
		@Override
		protected void onPreExecute() {
			// Show loading spinner
			mLoading.setVisibility(View.VISIBLE);
			// Hide Pokemon info
			mData.setVisibility(View.GONE);
			// Minimize the search field
			showSearch(false);
		}

		@Override
		protected PokemonData doInBackground(String... params) {
			PokemonData pokemon = null;

			String url = "";
			// Check whether to search by id or name
			try {
				Integer.parseInt(params[0]);
				url = "http://poketype.floccul.us/api/pokemon/id/"
				        + params[0];
			} catch (NumberFormatException e) {
				url = "http://poketype.floccul.us/api/pokemon/" + params[0];
			}
			
			// Download data
			AjaxRequest request = new AjaxRequest("GET", url);
			JSONObject data;
			try {
				data = new JSONObject(request.send());
				if (!data.isNull("id")) {
					pokemon = new PokemonData(data, null);
				}
			} catch (Exception e) {
			}
			
			return pokemon;
		}

		@Override
		protected void onPostExecute(PokemonData result) {
			if (result != null) {
				mHelp.setVisibility(View.GONE);
				mInfo.setText(result.mInfo.toString());
			}

			// Hide loading spinner
			mLoading.setVisibility(View.GONE);
			// Show Pokemon info
			if (mHelp.getVisibility() == View.GONE) {
				mData.setVisibility(View.VISIBLE);
			}
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
			// hide the keyboard
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(mSearchBox.getWindowToken(), 0);
		} else {
			// hide search icon and show search box
			mSearchIcon.setVisibility(View.GONE);
			mSearchBox.setVisibility(View.VISIBLE);
			mSearchBox.requestFocus();
			// show the keyboard
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(mSearchBox, InputMethodManager.SHOW_IMPLICIT);
		}
	}
}
