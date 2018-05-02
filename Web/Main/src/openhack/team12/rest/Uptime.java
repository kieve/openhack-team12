package openhack.team12.rest;

import fi.iki.elonen.NanoHTTPD;
import openhack.team12.WebServer;

public class Uptime extends Rest {
    @Override
    public String getJsonResponse(NanoHTTPD.IHTTPSession session) {
        return "{ \"uptime:\"" + (System.currentTimeMillis() - WebServer.START_TIME) + "ms\" }";
    }
}
