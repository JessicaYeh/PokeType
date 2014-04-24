package yeh.poketype;

import android.graphics.drawable.Drawable;

//Data for a row in the Search list. Has an id, name, and icon.
public class PokemonSearchItem implements Comparable<PokemonSearchItem> {
	int mId;
	String mName;
	Drawable mIcon;
	
	public PokemonSearchItem(int id, String name, Drawable icon) {
		mId = id;
		mName = name;
		mIcon = icon;
	}
	
	@Override
	public String toString() {
		return mName;
	}
	
	@Override
	public int compareTo(PokemonSearchItem rhs) {
		return this.mName.compareTo(rhs.mName);
	}
}
