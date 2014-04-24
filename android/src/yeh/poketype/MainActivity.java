package yeh.poketype;

import java.util.ArrayList;
import java.util.Collections;

import yeh.poketype.ClearableAutoCompleteTextView.OnClearListener;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class MainActivity extends ActionBarActivity {
	// Action bar items
	private ClearableAutoCompleteTextView mSearchBox;
	private ImageView mSearchIcon;

	// Main content area items
	private FrameLayout mContent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Get handle to views in activity_main
		mContent = (FrameLayout) findViewById(R.id.content);

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
