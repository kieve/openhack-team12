package openhack.team12.rest;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import openhack.team12.WebServer;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;

public class Uptime extends Rest {
    @Override
    public JsonObject getJsonResponse(IHTTPSession session) {
        JsonBuilderFactory factory = Json.createBuilderFactory(JSON_CONFIGS);

        return factory.createObjectBuilder()
                .add("uptime", (System.currentTimeMillis() - WebServer.START_TIME) + "ms")
                .build();
    }
}
