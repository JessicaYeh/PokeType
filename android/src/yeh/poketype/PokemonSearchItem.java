package yeh.poketype;

import android.graphics.drawable.Drawable;

//Data for a row in the Search list. Has an id, name, and icon. Optional types.
public class PokemonSearchItem implements Comparable<PokemonSearchItem> {
	int mId;
	String mName;
	Drawable mIcon;
	String mType1;
	String mType2;

	public PokemonSearchItem(int id, String name, Drawable icon) {
		mId = id;
		mName = name;
		mIcon = icon;
	}
	
	public PokemonSearchItem(int id, String name, Drawable icon, String type1, String type2) {
		mId = id;
		mName = name;
		mIcon = icon;
		mType1 = type1;
		mType2 = type2;
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
