package yeh.poketype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

public class PokemonSuggestAdapter extends ArrayAdapter<PokemonSearchItem>
		implements Filterable {
	private Context mContext;
	private ArrayList<PokemonSearchItem> mOriginalPokemon;
	private ArrayList<PokemonSearchItem> mPokemon;
	private Filter mFilter;

	public PokemonSuggestAdapter(Context context,
			ArrayList<PokemonSearchItem> pokemon) {
		super(context, android.R.layout.simple_dropdown_item_1line,
				android.R.id.text1, pokemon);
		mContext = context;
		Collections.sort(pokemon);
		mOriginalPokemon = pokemon;
		mPokemon = pokemon;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = super.getView(position, convertView, parent);

		TextView text = (TextView) v.findViewById(android.R.id.text1);

		String paddedId = String.format("%03d", getItem(position).mId);
		text.setCompoundDrawablesWithIntrinsicBounds(
				mContext.getResources().getIdentifier("pokemon" + paddedId,
						"drawable", mContext.getPackageName()), 0, 0, 0);

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
			mFilter = new PokemonFilter();
		}
		return mFilter;
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
	}

	private class PokemonFilter extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			String constraintString = (String) constraint;
			if (constraintString == null || constraintString.length() == 0) {
				results.values = mPokemon;
				results.count = mPokemon.size();
			} else {
				ArrayList<PokemonSearchItem> newPokemon = new ArrayList<PokemonSearchItem>();
				for (int i = 0; i < mOriginalPokemon.size(); i++) {
					String item = mOriginalPokemon.get(i).mName;
					String lowerPokemon1 = item
							.toLowerCase(Locale.getDefault());
					String lowerPokemon2 = constraintString.toLowerCase(Locale
							.getDefault());
					if (lowerPokemon1.startsWith(lowerPokemon2)) {
						newPokemon.add(mOriginalPokemon.get(i));
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