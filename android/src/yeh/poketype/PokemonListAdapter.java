package yeh.poketype;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

public class PokemonListAdapter extends ArrayAdapter<PokemonSearchItem>
        implements Filterable {
	private Context mContext;
	private ArrayList<PokemonSearchItem> mOriginalPokemon;
	private ArrayList<PokemonSearchItem> mPokemon;
	private Filter mFilter;

	public PokemonListAdapter(Context context,
	        ArrayList<PokemonSearchItem> pokemon) {
		super(context, R.layout.pokemon_list_item, R.id.pokemon_list_name,
		        pokemon);
		mContext = context;
		mOriginalPokemon = pokemon;
		mPokemon = pokemon;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View v = super.getView(position, convertView, parent);

		TextView icon = (TextView) v.findViewById(R.id.pokemon_list_icon);
		TextView id = (TextView) v.findViewById(R.id.pokemon_list_id);
		TextView type1 = (TextView) v.findViewById(R.id.pokemon_list_type1);
		TextView type2 = (TextView) v.findViewById(R.id.pokemon_list_type2);

		String paddedId = String.format("%03d", getItem(position).mId);
		icon.setCompoundDrawablesWithIntrinsicBounds(
		        mContext.getResources().getIdentifier("pokemon" + paddedId,
		                "drawable", mContext.getPackageName()), 0, 0, 0);
		id.setText("#" + paddedId);
		String pokemonType1 = getItem(position).mType1;
		String pokemonType2 = getItem(position).mType2;
		type1.setText(pokemonType1);
		type1.setBackgroundResource(mContext.getResources().getIdentifier(
		        pokemonType1.toLowerCase() + "_background", "drawable",
		        mContext.getPackageName()));
		if (pokemonType2.equals("NULL")) {
			type2.setVisibility(View.GONE);
		} else {
			type2.setVisibility(View.VISIBLE);
			type2.setText(pokemonType2);
			type2.setBackgroundResource(mContext.getResources().getIdentifier(
			        pokemonType2.toLowerCase() + "_background", "drawable",
			        mContext.getPackageName()));
		}

		return v;
	}

	@Override
	public int getCount() {
		return mPokemon.size();
	}

	@Override
	public PokemonSearchItem getItem(int position) {
		return mPokemon.get(position);
	}

	@Override
	public long getItemId(int position) {
		return mPokemon.get(position).mId;
	}

	public Filter getFilter() {
		if (mFilter == null) {
			mFilter = new PokemonTypeFilter();
		}
		return mFilter;
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}

	private class PokemonTypeFilter extends Filter {
		String emptyType1 = mContext.getString(R.string.type_1);
		String emptyType2 = mContext.getString(R.string.type_2);

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			String constraintString = (String) constraint;
			if (constraintString == null || constraintString.length() == 0) {
				results.values = mPokemon;
				results.count = mPokemon.size();
			} else {
				int dividerIndex = constraintString.indexOf("|");
				String constraintType1 = constraintString.substring(0,
				        dividerIndex);
				String constraintType2 = constraintString
				        .substring(dividerIndex + 1);

				ArrayList<PokemonSearchItem> newPokemon;
				// User is not filtering by anything, return all Pokemon
				if (constraintType1.equals(emptyType1)
				        && constraintType2.equals(emptyType2)) {
					newPokemon = mOriginalPokemon;
				} else {
					newPokemon = new ArrayList<PokemonSearchItem>();
					for (int i = 0; i < mOriginalPokemon.size(); i++) {
						String type1 = mOriginalPokemon.get(i).mType1;
						String type2 = mOriginalPokemon.get(i).mType2;
						// Filter by only one type if the two selected filter
						// types are the same
						if (constraintType1.equalsIgnoreCase(constraintType2)) {
							if (type1.equalsIgnoreCase(constraintType1)
							        && type2.equalsIgnoreCase("NULL")) {
								newPokemon.add(mOriginalPokemon.get(i));
							}
						}
						// Otherwise, filter by both types in any order
						else {
							boolean compare11 = type1
							        .equalsIgnoreCase(constraintType1);
							boolean compare12 = type1
							        .equalsIgnoreCase(constraintType2);
							boolean compare21 = type2
							        .equalsIgnoreCase(constraintType1);
							boolean compare22 = type2
							        .equalsIgnoreCase(constraintType2);
							if (compare11 || compare12 || compare21
							        || compare22) {
								newPokemon.add(mOriginalPokemon.get(i));
							}
						}
					}
				}

				results.values = newPokemon;
				results.count = newPokemon.size();
			}

			return results;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint,
		        FilterResults results) {
			mPokemon = (ArrayList<PokemonSearchItem>) results.values;
			notifyDataSetChanged();
		}

	}
}