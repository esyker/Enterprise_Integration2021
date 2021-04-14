package ist.meic.ie.events;

import ist.meic.ie.events.exceptions.InvalidEventTypeException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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

    public Event getEvent() {
        return event;
    }
}
