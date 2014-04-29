package yeh.poketype;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class PokemonList extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pokemon_list);

		ArrayList<PokemonSearchItem> pokemon = new ArrayList<PokemonSearchItem>();

		Resources res = getResources();
		// Get the array of Pokemon names.
		String[] pokemonNames = res.getStringArray(R.array.pokemon_names);
		// Get the arrays of Pokemon types.
		String[] pokemonTypes1 = res.getStringArray(R.array.pokemon_types1);
		String[] pokemonTypes2 = res.getStringArray(R.array.pokemon_types2);
		// Get the array of Pokemon icons.
		TypedArray icons = res.obtainTypedArray(R.array.pokemon_icons);
		for (int i = 0; i < icons.length(); i++) {
			pokemon.add(new PokemonSearchItem(i + 1, pokemonNames[i], icons
			        .getDrawable(i), pokemonTypes1[i], pokemonTypes2[i]));
		}
		icons.recycle();

		// Insert the Pokemon into the ListView.
		PokemonListAdapter adapter = new PokemonListAdapter(this, pokemon);
		setListAdapter(adapter);
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
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Log.d("JY", ""+id);
	}
}
