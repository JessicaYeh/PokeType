package yeh.poketype;

import java.util.ArrayList;

//A group of type/effectiveness items (Super Effective, Normal, Not Effective)
public class EffectivenessGroup {
	String mName;
	ArrayList<EffectivenessItem> mTypes;
	
	public EffectivenessGroup(String name, ArrayList<EffectivenessItem> types) {
		mName = name;
		mTypes = types;
	}
}
