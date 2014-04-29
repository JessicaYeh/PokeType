package yeh.poketype;

//Data for each type/effectiveness row. Has an icon, type, and effectiveness.
public class EffectivenessItem {
	int mIcon;
	String mType;
	int mEffectiveness;

	public EffectivenessItem(int icon, String type, int effectiveness) {
		mIcon = icon;
		mType = type;
		mEffectiveness = effectiveness;
	}

	@Override
	public String toString() {
		return mType;
	}
}