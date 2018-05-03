package openhack.team12.rest;

import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1PersistentVolumeClaim;
import io.kubernetes.client.util.Config;
import openhack.team12.KubeUtils;

import javax.json.Json;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class PersistentVolumeClaim extends Rest {
    @Override
    public JsonObject getJsonResponse(IHTTPSession session) throws Exception {
        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);

        V1PersistentVolumeClaim pvc = KubeUtils.configureVolumeClaim("testclaim");

        CoreV1Api api = new CoreV1Api();
        api.createNamespacedPersistentVolumeClaim("default", pvc, "true");

        JsonBuilderFactory factory = Json.createBuilderFactory(JSON_CONFIGS);
        JsonObjectBuilder result = factory.createObjectBuilder();


        result.add("result", "done");

        return result.build();
    }
}
