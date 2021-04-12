package ist.meic.ie.events;

import org.json.simple.JSONObject;


public class MotionEvent extends Event {

    private String description;

    public MotionEvent(JSONObject event) {
        super((String) event.get("type"), (int) event.get("deviceId"));
        this.description = (String) event.get("description");
    }
}
