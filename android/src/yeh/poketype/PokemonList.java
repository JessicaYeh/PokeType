package yeh.poketype;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Spinner;

public class PokemonList extends ActionBarActivity {
	public final static String POKEMON_ID = "yeh.poketype.POKEMON_ID";
	private SharedPreferences sharedPref;
	private SharedPreferences.Editor editor;
	
	private Spinner mType1;
	private Spinner mType2;
	private ListView mListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pokemon_list);
		
		// Get views
		mType1 = (Spinner) findViewById(R.id.spinner_type1);
		mType2 = (Spinner) findViewById(R.id.spinner_type2);

		// Open preferences file
		sharedPref = PreferenceManager
		        .getDefaultSharedPreferences(getApplicationContext());
		editor = sharedPref.edit();

		ArrayList<PokemonSearchItem> pokemon = new ArrayList<PokemonSearchItem>();

		Resources res = getResources();
		// Get the array of Pokemon names.
		String[] pokemonNames = res.getStringArray(R.array.pokemon_names);
		// Get the arrays of Pokemon types.
		String[] pokemonTypes1 = res.getStringArray(R.array.pokemon_types1);
		String[] pokemonTypes2 = res.getStringArray(R.array.pokemon_types2);
		// Create the list of Pokemon.
		for (int i = 0; i < pokemonNames.length; i++) {
			pokemon.add(new PokemonSearchItem(i + 1, pokemonNames[i], null, pokemonTypes1[i], pokemonTypes2[i]));
		}

		// Insert the Pokemon into the ListView.
		mListView = (ListView) findViewById(R.id.list);
		PokemonListAdapter adapter = new PokemonListAdapter(this, pokemon);
		mListView.setAdapter(adapter);
		
		// Set click listener on the list view
		mListView.setOnItemClickListener(new PokemonClickListener());
		
		// Restore state information from shared preferences
		int type1 = sharedPref.getInt("pokemon_type1", 0);
		if (type1 != 0) {
			mType1.setSelection(type1);
		}
		int type2 = sharedPref.getInt("pokemon_type2", 0);
		if (type2 != 0) {
			mType2.setSelection(type2);
		}
		int position = sharedPref.getInt("pokemon_list_position", 0);
		if (position != 0) {
			mListView.setSelectionFromTop(position, 0);
		}
		adapter.getFilter().filter("fire|fire");
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
	private class PokemonClickListener implements
	        ListView.OnItemClickListener {
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
		editor.putInt("pokemon_list_position", mListView.getFirstVisiblePosition());
		editor.putInt("pokemon_type1", mType1.getSelectedItemPosition());
		editor.putInt("pokemon_type2", mType2.getSelectedItemPosition());
		editor.commit();
	}
}
