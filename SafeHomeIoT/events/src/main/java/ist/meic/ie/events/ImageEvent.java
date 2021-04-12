package ist.meic.ie.events;

import org.json.simple.JSONObject;

public class ImageEvent extends Event{

    private String description;

    public ImageEvent(JSONObject event) {
        super((String) event.get("type"), (int) event.get("deviceId"));
        this.description = (String) event.get("description");
    }


    public String getDescription() {
        return description;
    }
}
