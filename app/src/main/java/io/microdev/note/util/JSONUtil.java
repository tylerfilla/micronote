package io.microdev.note.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

public class JSONUtil {
    
    public static JSONObject convertMapToJSONObject(Map map) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        
        for (Object mapKey : map.keySet()) {
            if (!(mapKey instanceof String)) {
                throw new JSONException("Map key not an instance of String");
            }
            
            Object mapValue = map.get(mapKey);
            
            if (mapValue instanceof Map) {
                jsonObject.put((String) mapKey, convertMapToJSONObject((Map) mapValue));
            } else if (mapValue instanceof List) {
                jsonObject.put((String) mapKey, convertListToJSONArray((List) mapValue));
            } else {
                jsonObject.put((String) mapKey, mapValue);
            }
        }
        
        return jsonObject;
    }
    
    public static JSONArray convertListToJSONArray(List<Object> list) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        
        for (Object object : list) {
            if (object instanceof Map) {
                jsonArray.put(convertMapToJSONObject((Map) object));
            } else if (object instanceof List) {
                jsonArray.put(convertListToJSONArray((List) object));
            } else {
                jsonArray.put(object);
            }
        }
        
        return jsonArray;
    }
    
}
