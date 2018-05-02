package openhack.team12.rest;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;

import java.io.*;

public abstract class Rest {
	private enum Endpoint {
		UPTIME("/rest/uptime", new Uptime());

		private String m_path;
		private Rest m_processor;

		Endpoint(String path, Rest processor) {
			m_path = path;
			m_processor = processor;
		}

		public String getPath() {
			return m_path;
		}

		public Rest getProcessor() {
			return m_processor;
		}
	}

	public abstract String getJsonResponse(IHTTPSession session);

	public static Response process(IHTTPSession session) {
		String uri = session.getUri();
		Endpoint endpoint = null;

		for (Endpoint endpointQuery : Endpoint.values()) {
			if (endpointQuery.getPath().equals(uri)) {
				endpoint = endpointQuery;
				break;
			}
		}
		if (endpoint == null) {
			return null;
		}

		Rest processor = endpoint.getProcessor();
		String jsonResult = processor.getJsonResponse(session);

		return NanoHTTPD.newFixedLengthResponse(jsonResult);
	}
}
