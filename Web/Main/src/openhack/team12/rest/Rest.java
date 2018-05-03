package openhack.team12.rest;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Response;
import io.kubernetes.client.ApiException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import javax.json.stream.JsonGenerator;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public abstract class Rest {
	protected enum Endpoint {
		UPTIME("/rest/uptime", new Uptime()),
		LIST_PODS("/rest/admin/list-pods", new ListPods()),
		PERSISTENT_VOLUME_CLAIM("/rest/admin/pvc", new PersistentVolumeClaim());

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

	public static Map<String, Object> JSON_CONFIGS = new HashMap<>();
	static {
		JSON_CONFIGS.put(JsonGenerator.PRETTY_PRINTING, true);
	}

	public abstract JsonObject getJsonResponse(IHTTPSession session) throws Exception;

	public static Response process(IHTTPSession session) {
		String uri = session.getUri();
		Endpoint endpoint = null;

		for (Endpoint endpointQuery : Endpoint.values()) {
			if (uri.startsWith(endpointQuery.getPath())) {
				endpoint = endpointQuery;
				break;
			}
		}
		if (endpoint == null) {
			return null;
		}

		Rest processor = endpoint.getProcessor();
		JsonObject result;
		try {
			result = processor.getJsonResponse(session);
		} catch (Exception e) {
			e.printStackTrace();
			if (e instanceof ApiException) {
				ApiException apiException = (ApiException) e;
				System.out.println(apiException.getResponseBody());
			}
			return NanoHTTPD.newFixedLengthResponse("{ \"error\" : \"unknown\" }");
		}

		StringWriter stringWriter = new StringWriter();
		JsonWriter jsonWriter = Json.createWriterFactory(JSON_CONFIGS).createWriter(stringWriter);
		jsonWriter.write(result);

		return NanoHTTPD.newFixedLengthResponse(stringWriter.toString());
	}
}
