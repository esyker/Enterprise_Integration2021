package ist.meic.ie.events;

import ist.meic.ie.events.exceptions.InvalidEventTypeException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Date;
import java.util.Random;

public class EventItem {
    private Event event;

    public EventItem(String jsonString) throws InvalidEventTypeException, ParseException {
        JSONParser parser = new JSONParser();
        JSONObject event = (JSONObject) parser.parse(jsonString);
        String type = (String) event.get("type");
        switch (type) {
            case "temperature" : this.event = new TemperatureEvent(event); break;
            case "image" : this.event = new ImageEvent(event); break;
            case "video" : this.event = new VideoEvent(event); break;
            case "smoke" : this.event = new SmokeEvent(event); break;
            case "motion" : this.event = new MotionEvent(event); break;
            default: throw new InvalidEventTypeException("Invalid event type");
        }
    }

    public EventItem(String type, int deviceId, int userId) throws InvalidEventTypeException {
        switch (type) {
            case "temperature" : this.event = new TemperatureEvent(-50 + new Random().nextFloat() * (500 - (-50)), deviceId, userId); break;
            case "image" : this.event = new ImageEvent("some image" + deviceId, deviceId, userId); break;
            case "video" : this.event = new VideoEvent("some video" + deviceId, deviceId, userId); break;
            case "smoke" : this.event = new SmokeEvent(-50 + new Random().nextFloat() * (500 - (-50)), deviceId, userId); break;
            case "motion" : this.event = new MotionEvent("some movement" + deviceId, deviceId, userId); break;
            default: throw new InvalidEventTypeException("Invalid event type");
        }
    }

    public EventItem(String type, int deviceId, int userId, Date ts, float measurement, String description) throws InvalidEventTypeException {
        switch (type) {
            case "temperature" : this.event = new TemperatureEvent(measurement, deviceId, userId, ts); break;
            case "image" : this.event = new ImageEvent(description, deviceId, userId, ts); break;
            case "video" : this.event = new VideoEvent(description, deviceId, userId, ts); break;
            case "smoke" : this.event = new SmokeEvent(measurement, deviceId, userId, ts); break;
            case "motion" : this.event = new MotionEvent(description, deviceId, userId, ts); break;
            default: throw new InvalidEventTypeException("Invalid event type");
        }
    }

    public Event getEvent() {
        return event;
    }
}