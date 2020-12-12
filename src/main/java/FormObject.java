import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedHashMap;

public class FormObject {
    // Linked HashMap of key, value parring from JSON message
    private LinkedHashMap<String, String> jsonDict;

    public FormObject(String msg){
        try {
            JSONObject obj = new JSONObject(msg);
            this.jsonDict = new LinkedHashMap<>(obj.length());
            this.toMap(obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void toMap(JSONObject obj){
        // Converts Json message to Linked HashMap
        Iterator<String> keysItr = obj.keys();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            String value = obj.getString(key);
            this.jsonDict.put(key, value);
        }
    }

    public String getKeysInsert(){
        // Getter for list of keys in jsonDict
        StringBuilder returnString= new StringBuilder();
        for(String key: this.jsonDict.keySet()){
            returnString.append(key).append(", ");
        }
        return returnString.substring(0, returnString.length() - 2);
    }

    public String getValue(String key){
        // Getter for value of specific key from jsonDict
        return jsonDict.get(key);
    }

    public int getJsonDictLen(){
        // Getter for size of JsonDict
        return this.jsonDict.size();
    }

    public LinkedHashMap<String, String> getJsonDict() {
        // Getter for JsonDict
        return jsonDict;
    }

}
