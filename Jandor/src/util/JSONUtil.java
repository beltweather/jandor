package util;

import java.io.BufferedReader;
import java.io.IOException;

import json.JSONArray;
import json.JSONException;
import json.JSONObject;


public class JSONUtil {

	private JSONUtil() {}
	
	public static JSONObject toJSON(String filename) {
		return toJSON(FileUtil.getResourceReader(filename));
	}
	
	public static JSONObject toJSON(BufferedReader reader) {
		if(reader == null) {
			return null;
		}
		JSONObject json = null;
		StringBuilder sb = new StringBuilder();
		String line;
		
		try {
			
			while((line = reader.readLine()) != null) {
				sb.append(CardUtil.clean(line));
			}

			// (Outdated assumption We expect a single line for our particlular case
			//String str = reader.readLine();
			
			try {
				json = new JSONObject(sb.toString());
				//json = new JSONObject(CardUtil.clean(str));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return json;
	}
	
}
