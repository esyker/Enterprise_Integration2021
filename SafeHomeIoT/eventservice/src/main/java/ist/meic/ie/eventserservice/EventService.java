package ist.meic.ie.eventserservice;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.mysql.cj.xdevapi.JsonParser;
import ist.meic.ie.events.Event;
import ist.meic.ie.events.EventItem;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import scala.util.parsing.json.JSON;

import java.io.*;
import java.util.List;
import java.util.MissingFormatArgumentException;
import java.util.stream.Collectors;

public class EventService implements RequestStreamHandler {
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) {
        LambdaLogger logger = context.getLogger();
        try {
            JSONParser parser = new JSONParser();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            JSONObject obj = (JSONObject) parser.parse(reader);

            if (obj.get("eventType") == null) throw new MissingFormatArgumentException("No event type provided!");
            if (obj.get("userId") == null) throw new MissingFormatArgumentException("No userId provided!");
            if (obj.get("lastReceivedId") == null) throw new MissingFormatArgumentException("No last received provided!");

            String eventType = (String) obj.get("eventType");
            int userId = ((Long) obj.get("userId")).intValue();
            int lastReceived = ((Long) obj.get("lastReceivedId")).intValue();

            List<Event> events = EventReopsitory.getEvents(eventType, userId, lastReceived);
            List<String> stringEvents = events.stream()
                    .map(Event::toString)
                    .collect(Collectors.toList());

            OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
            writer.write(stringEvents.toString());
            writer.close();

        } catch (Exception e) {
            logger.log("Error : " + e.toString());
        }

    }
}
