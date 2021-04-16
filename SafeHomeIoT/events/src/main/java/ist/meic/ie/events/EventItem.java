package ist.meic.ie.events;

import ist.meic.ie.events.exceptions.InvalidEventTypeException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Random;

public class EventItem {
    private Event event;

    public EventItem(String type, int deviceId, int userId, String description, float measurement) throws InvalidEventTypeException {
        switch (type) {
            case "temperature" : this.event = new TemperatureEvent(measurement, deviceId); break;
            case "image" : this.event = new ImageEvent(description, deviceId); break;
            case "video" : this.event = new VideoEvent(description, deviceId); break;
            case "smoke" : this.event = new SmokeEvent(measurement, deviceId); break;
            case "motion" : this.event = new MotionEvent(description, deviceId); break;
            default: throw new InvalidEventTypeException("Invalid event type");
        }
        this.event.setUserId(userId);
    }

    public EventItem(String jsonString, String topicName) throws InvalidEventTypeException, ParseException {
        JSONParser parser = new JSONParser();
        JSONObject event = (JSONObject) parser.parse(jsonString);
        String type = (String) event.get("type");
        switch (topicName) {
            case "temperature-events" : this.event = new TemperatureEvent(event); break;
            case "image-events" : this.event = new ImageEvent(event); break;
            case "video-events" : this.event = new VideoEvent(event); break;
            case "smoke-events" : this.event = new SmokeEvent(event); break;
            case "motion-events" : this.event = new MotionEvent(event); break;
            default: throw new InvalidEventTypeException("Invalid event type");
        }
        this.event.setUserId((int)event.get("user"));
    }

    //Random event
    public EventItem(String type, int deviceId, int userId) throws InvalidEventTypeException {
        switch (type) {
            case "temperature" : this.event = new TemperatureEvent(-50 + new Random().nextFloat() * (500 - (-50)), deviceId); break;
            case "image" : this.event = new ImageEvent("some image" + deviceId, deviceId); break;
            case "video" : this.event = new VideoEvent("some video" + deviceId, deviceId); break;
            case "smoke" : this.event = new SmokeEvent(-50 + new Random().nextFloat() * (500 - (-50)), deviceId); break;
            case "motion" : this.event = new MotionEvent("some movement" + deviceId, deviceId); break;
            default: throw new InvalidEventTypeException("Invalid event type");
        }
        this.event.setUserId(userId);
    }

    public Event getEvent() {
        return event;
    }
}
