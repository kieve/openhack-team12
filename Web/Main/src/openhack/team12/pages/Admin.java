package openhack.team12.pages;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1ServiceList;
import io.kubernetes.client.util.Config;
import openhack.team12.KubeUtils;
import openhack.team12.KubeUtils.ServerIps;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Admin extends Page {
	Admin() {}

	@Override
	String getTitle() {
		return "Welcome to Team 12 Minecraft";
	}

	@Override
	void process(Map<String, Object> templateInput) throws Exception {
		ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);
        CoreV1Api api = new CoreV1Api();

        V1ServiceList mcServers = api.listNamespacedService("default", "yes", null, null, false,
                    null, 64, null, 30, false);
        List<ServerIps> ips = KubeUtils.getServerIps(mcServers);

        Map<String, String> serverStrings = new HashMap<>();
        for (ServerIps ip : ips) {
            serverStrings.put(ip.m_name,
                    "Server Name: " + ip.m_name
                            + " (" + ip.m_users + "/" + ip.m_capacity + ")" + "<br>"
                    + " Server = " + ip.m_serverAddress + "<br>"
                    + " RCON = " + ip.m_rconAddress + "<br>"
                    + " <a href='javascript:deleteServer(\"" + ip.m_name + "\");'>"
                            + "Delete</a>"
                    + "<br><br>");
        }

        templateInput.put("serverStrings", serverStrings);
	}
}
