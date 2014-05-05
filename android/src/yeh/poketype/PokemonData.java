package yeh.poketype;

import org.json.JSONObject;

import android.graphics.Bitmap;

public class PokemonData {
	JSONObject mInfo;
	Bitmap mImage;
	
	public PokemonData(JSONObject info, Bitmap image) {
		mInfo = info;
		mImage = image;
	}
}