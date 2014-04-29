package yeh.poketype;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PokemonListAdapter extends ArrayAdapter<PokemonSearchItem> {
	private Context mContext;
	private ArrayList<PokemonSearchItem> mPokemon;

	public PokemonListAdapter(Context context,
	        ArrayList<PokemonSearchItem> pokemon) {
		super(context, R.layout.pokemon_list_item, R.id.pokemon_list_name, pokemon);
		mContext = context;
		mPokemon = pokemon;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View v = super.getView(position, convertView, parent);

		TextView icon = (TextView) v.findViewById(R.id.pokemon_list_icon);
		TextView id = (TextView) v.findViewById(R.id.pokemon_list_id);
		TextView name = (TextView) v.findViewById(R.id.pokemon_list_name);
		TextView type1 = (TextView) v.findViewById(R.id.pokemon_list_type1);
		TextView type2 = (TextView) v.findViewById(R.id.pokemon_list_type2);

		icon.setCompoundDrawablesWithIntrinsicBounds(mPokemon.get(position).mIcon,
		        null, null, null);
		id.setText("#" + String.format("%03d", mPokemon.get(position).mId));
		String pokemonType1 = mPokemon.get(position).mType1;
		String pokemonType2 = mPokemon.get(position).mType2;
		type1.setText(pokemonType1);
		type1.setBackgroundResource(mContext.getResources()
		        .getIdentifier(pokemonType1.toLowerCase() + "_background", "drawable",
		                mContext.getPackageName()));
		if (pokemonType2.equals("NULL")) {
			type2.setVisibility(View.GONE);
		} else {
			type2.setVisibility(View.VISIBLE);
			type2.setText(pokemonType2);
			type2.setBackgroundResource(mContext.getResources()
			        .getIdentifier(pokemonType2.toLowerCase() + "_background",
			                "drawable", mContext.getPackageName()));
		}

		return v;
	}
	
	@Override
	public long getItemId(int position) {
		return mPokemon.get(position).mId;
	}
}