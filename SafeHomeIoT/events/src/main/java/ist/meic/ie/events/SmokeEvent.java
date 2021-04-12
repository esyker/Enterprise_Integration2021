package ist.meic.ie.events;

import org.json.simple.JSONObject;

public class SmokeEvent extends Event{
    private float measurement;

    public SmokeEvent(JSONObject event) {
        super((String) event.get("type"), (int) event.get("deviceId"));
        this.measurement = (float) event.get("measurement");
    }


    public float getMeasurement() {
        return measurement;
    }

}
