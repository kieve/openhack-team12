package openhack.team12.rest;

import com.google.gson.JsonSyntaxException;
import fi.iki.elonen.NanoHTTPD.IHTTPSession;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.AppsV1beta1Api;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.AppsV1beta1Deployment;
import io.kubernetes.client.models.V1DeleteOptions;
import io.kubernetes.client.models.V1PersistentVolumeClaim;
import io.kubernetes.client.models.V1Service;
import io.kubernetes.client.models.V1ServiceList;
import io.kubernetes.client.util.Config;
import openhack.team12.KubeUtils;
import openhack.team12.KubeUtils.ServerIps;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.List;

public class MinecraftServer extends Rest {
    private final boolean m_isAdmin;

    public MinecraftServer(boolean isAdmin) {
        m_isAdmin = isAdmin;
    }

    @Override
    public JsonObject getJsonResponse(IHTTPSession session) throws Exception {
        String uri = session.getUri();
        Endpoint endpoint = m_isAdmin ? Endpoint.MINECRAFT_SERVER_ADMIN : Endpoint.MINECRAFT_SERVER;
        String name = uri.substring(endpoint.getPath().length(), uri.length());
        if (name.length() > 0) {
            name = name.replace("/", "");
        }

        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);
        CoreV1Api api = new CoreV1Api();
        AppsV1beta1Api beta1Api = new AppsV1beta1Api();


        JsonBuilderFactory factory = Json.createBuilderFactory(JSON_CONFIGS);
        JsonObjectBuilder result = factory.createObjectBuilder();

        String output = "ok";
        switch (session.getMethod()) {
        case PUT:
            if (!m_isAdmin) {
                output = "not authorized";
                break;
            }

            // Create the PVC
            V1PersistentVolumeClaim pvc = KubeUtils.configureVolumeClaim(name);
            api.createNamespacedPersistentVolumeClaim("default", pvc, "true");

            // Create the deployment
            AppsV1beta1Deployment deployment = KubeUtils.configureDeployment(name);
            beta1Api.createNamespacedDeployment("default", deployment, "true");

            // Create the service
            V1Service service = KubeUtils.configureService(name);
            api.createNamespacedService("default", service, "true");
            break;
        case DELETE:
            if (!m_isAdmin) {
                output = "not authorized";
                break;
            }

            V1DeleteOptions deleteOptionsV1 = new V1DeleteOptions();
            deleteOptionsV1.setApiVersion("v1");
            try {
                api.deleteNamespacedService("mc-svc-" + name, "default", "true");
            } catch (JsonSyntaxException e) {
                // ignore
            }
            try {
                beta1Api.deleteNamespacedDeployment("mc-dep-" + name, "default", deleteOptionsV1,
                        "true", 0, true, null);
            } catch (JsonSyntaxException e) {
                // ignore
            }
            try {
                api.deleteNamespacedPersistentVolumeClaim("mc-pvc-" + name, "default",
                        deleteOptionsV1, "true", 0, true, null);
            } catch (JsonSyntaxException e) {
                // ignore
            }

            break;
        case GET:

            V1ServiceList mcServers = api.listNamespacedService("default", "yes", null, null, false,
                    null, 64, null, 30, false);

            List<ServerIps> ips = KubeUtils.getServerIps(mcServers);

            JsonArrayBuilder mcServerBuilder = factory.createArrayBuilder();

            for (ServerIps serverIp : ips) {
                JsonObjectBuilder objectBuilder = factory.createObjectBuilder()
                        .add("name", serverIp.m_name);

                JsonObject endpoints = factory.createObjectBuilder()
                        .add("minecraft", serverIp.m_serverAddress)
                        .add("rcon", serverIp.m_rconAddress)
                        .build();

                objectBuilder.add("endpoints", endpoints);
                mcServerBuilder.add(objectBuilder);
            }

            result.add("servers", mcServerBuilder);

            return result.build();
        default:
            output = "invalid method";
        }

        result.add("result", output);
        return result.build();
    }
}
