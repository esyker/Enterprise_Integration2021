package ist.meic.ie.events;

import org.json.simple.JSONObject;

public class TemperatureEvent extends Event {
    private int measurement;

    public TemperatureEvent (JSONObject event) {
        super((String) event.get("type"), (int) event.get("deviceId"));
        this.measurement = (int) event.get("measurement");
    }

    public int getMeasurement() {
        return measurement;
    }


}
