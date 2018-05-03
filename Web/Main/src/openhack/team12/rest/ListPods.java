package openhack.team12.rest;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodList;
import io.kubernetes.client.util.Config;
import openhack.team12.WebServer;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.ArrayList;
import java.util.List;

public class ListPods extends Rest {
    @Override
    public JsonObject getJsonResponse(IHTTPSession session) throws Exception {
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();
        V1PodList list = api.listPodForAllNamespaces(null, null, null, null, null, null, null, null,
                null);

        List<String> names = new ArrayList<>();
        for (V1Pod item : list.getItems()) {
            names.add(item.getMetadata().getName());
        }

        JsonBuilderFactory factory = Json.createBuilderFactory(JSON_CONFIGS);
        JsonObjectBuilder result = factory.createObjectBuilder();

        JsonArrayBuilder jsonNames = factory.createArrayBuilder(names);

        result.add("Pods", jsonNames);

        return result.build();
    }
}
