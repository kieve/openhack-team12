package openhack.team12.rest;

import com.google.gson.JsonSyntaxException;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import fi.iki.elonen.NanoHTTPD.Method;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1DeleteOptions;
import io.kubernetes.client.models.V1PersistentVolumeClaim;
import io.kubernetes.client.models.V1PersistentVolumeClaimList;
import io.kubernetes.client.util.Config;
import openhack.team12.KubeUtils;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.ArrayList;
import java.util.List;

public class PersistentVolumeClaim extends Rest {
    @Override
    public JsonObject getJsonResponse(IHTTPSession session) throws Exception {
        String uri = session.getUri();
        String name = uri.substring(Endpoint.PERSISTENT_VOLUME_CLAIM.getPath().length(),
                uri.length());
        if (name.length() > 0) {
            name = name.replace("/", "");
        }

        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);
        CoreV1Api api = new CoreV1Api();

        JsonBuilderFactory factory = Json.createBuilderFactory(JSON_CONFIGS);
        JsonObjectBuilder result = factory.createObjectBuilder();

        String output = "ok";
        switch (session.getMethod()) {
        case PUT:
            V1PersistentVolumeClaim pvc = KubeUtils.configureVolumeClaim(name);
            api.createNamespacedPersistentVolumeClaim("default", pvc, "true");
            break;
        case DELETE:
            V1DeleteOptions deleteOptions = new V1DeleteOptions();
            deleteOptions.setApiVersion("v1");
            try {
                api.deleteNamespacedPersistentVolumeClaim(name, "default", deleteOptions, "true", 0,
                        true, null);
            } catch (JsonSyntaxException e) {
                // ignore
            }
            break;
        case GET:
            V1PersistentVolumeClaimList pvcResult = api.listNamespacedPersistentVolumeClaim(
                    "default", "yes", null, null, false, null, 64, null, 30, false);
            System.out.println(result);
            List<String> pvcs = new ArrayList<>();
            for (V1PersistentVolumeClaim claim : pvcResult.getItems()) {
                pvcs.add(claim.getMetadata().getName());
            }
            JsonArrayBuilder arrayBuilder = factory.createArrayBuilder(pvcs);
            result.add("pvcs", arrayBuilder);
            break;
        default:
            output = "invalid method";
        }

        result.add("result", output);
        return result.build();
    }
}
