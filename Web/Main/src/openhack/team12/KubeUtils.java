package openhack.team12;

import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.models.AppsV1beta1Deployment;
import io.kubernetes.client.models.AppsV1beta1DeploymentSpec;
import io.kubernetes.client.models.V1Container;
import io.kubernetes.client.models.V1ContainerPort;
import io.kubernetes.client.models.V1EnvVar;
import io.kubernetes.client.models.V1LocalObjectReference;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.models.V1PersistentVolumeClaim;
import io.kubernetes.client.models.V1PersistentVolumeClaimSpec;
import io.kubernetes.client.models.V1PersistentVolumeClaimVolumeSource;
import io.kubernetes.client.models.V1PodSpec;
import io.kubernetes.client.models.V1PodTemplateSpec;
import io.kubernetes.client.models.V1ResourceRequirements;
import io.kubernetes.client.models.V1Service;
import io.kubernetes.client.models.V1ServiceList;
import io.kubernetes.client.models.V1ServicePort;
import io.kubernetes.client.models.V1ServiceSpec;
import io.kubernetes.client.models.V1Volume;
import io.kubernetes.client.models.V1VolumeMount;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KubeUtils {
    public static class ServerIps {
        public String m_name;
        public String m_serverAddress;
        public String m_rconAddress;
        public String m_users;
        public String m_capacity;
    }

    private static V1ObjectMeta createMeta(String name) {
        V1ObjectMeta meta = new V1ObjectMeta();
        meta.name(name);
        meta.namespace("default");
        meta.uid(UUID.randomUUID().toString());
        return meta;
    }

    public static V1PersistentVolumeClaim configureVolumeClaim(String name) {
        V1PersistentVolumeClaim result = new V1PersistentVolumeClaim();
        result.apiVersion("v1");
        result.kind("PersistentVolumeClaim");

        result.setMetadata(createMeta("mc-pvc-" + name));

        V1PersistentVolumeClaimSpec spec = new V1PersistentVolumeClaimSpec();
        spec.addAccessModesItem("ReadWriteOnce");
        spec.storageClassName("team12storageclass");

        V1ResourceRequirements specReqs = new V1ResourceRequirements();
        specReqs.putRequestsItem("storage", new Quantity("5Gi"));
        spec.setResources(specReqs);
        result.setSpec(spec);
        return result;
    }

    public static AppsV1beta1Deployment configureDeployment(String name) {
        AppsV1beta1Deployment result = new AppsV1beta1Deployment();
        result.apiVersion("apps/v1beta1");
        result.kind("Deployment");

        result.setMetadata(createMeta("mc-dep-" + name));

        AppsV1beta1DeploymentSpec spec = new AppsV1beta1DeploymentSpec();
        spec.replicas(1);

        V1PodTemplateSpec templateSpec = new V1PodTemplateSpec();

        V1ObjectMeta meta = new V1ObjectMeta();
        meta.putLabelsItem("app", "mc-dep-" + name);
        templateSpec.setMetadata(meta);

        V1PodSpec podSpec = new V1PodSpec();
        V1Container container = new V1Container();
        container.name("mc-dep-" + name);
        container.image("challenge1.azurecr.io/minecraft-server:2.0");
        V1VolumeMount volumeMount = new V1VolumeMount();
        volumeMount.mountPath("/data");
        volumeMount.name("minecraftvolume");
        container.addVolumeMountsItem(volumeMount);
        V1EnvVar env = new V1EnvVar();
        env.setName("EULA");
        env.setValue("TRUE");
        container.addEnvItem(env);

        V1ContainerPort port25565 = new V1ContainerPort();
        port25565.setName("server");
        port25565.setContainerPort(25565);
        container.addPortsItem(port25565);

        V1ContainerPort port25575 = new V1ContainerPort();
        port25575.setName("rcon");
        port25575.setContainerPort(25575);
        container.addPortsItem(port25575);

        podSpec.addContainersItem(container);

        V1Volume volume = new V1Volume();
        volume.name("minecraftvolume");
        V1PersistentVolumeClaimVolumeSource pvcSource = new V1PersistentVolumeClaimVolumeSource();
        pvcSource.setClaimName("mc-pvc-" + name);
        volume.persistentVolumeClaim(pvcSource);
        podSpec.addVolumesItem(volume);
        V1LocalObjectReference imageSecret = new V1LocalObjectReference();
        imageSecret.setName("supersecret");
        podSpec.addImagePullSecretsItem(imageSecret);
        templateSpec.spec(podSpec);
        spec.template(templateSpec);

        result.setSpec(spec);
        return result;
    }

    public static V1Service configureService(String name) {
        V1Service result = new V1Service();
        result.apiVersion("v1");
        result.kind("Service");

        result.setMetadata(createMeta("mc-svc-" + name));

        V1ServiceSpec spec = new V1ServiceSpec();
        spec.setType("LoadBalancer");

        V1ServicePort port25565 = new V1ServicePort();
        port25565.setName("server");
        port25565.setPort(25565);
        spec.addPortsItem(port25565);

        V1ServicePort port25575 = new V1ServicePort();
        port25575.setName("rcon");
        port25575.setPort(25575);
        spec.addPortsItem(port25575);

        spec.putSelectorItem("app", "mc-dep-" + name);

        result.spec(spec);
        return result;
    }

    public static List<ServerIps> getServerIps(V1ServiceList mcServers) {
        List<ServerIps> ips = new ArrayList<>();
        for (V1Service serviceItem : mcServers.getItems()) {
            String serviceName = serviceItem.getMetadata().getName();
            if (!serviceName.startsWith("mc-svc-")) {
                continue;
            }
            ServerIps ip = new ServerIps();
            ip.m_name = serviceName.substring("mc-svc-".length(), serviceName.length());

            String externalIp = "none yet";
            try {
                externalIp = serviceItem.getStatus().getLoadBalancer().getIngress().get(0)
                    .getIp();
            } catch (NullPointerException e) {
                // Do nothing, no ip yet
            }
            List<V1ServicePort> ports = serviceItem.getSpec().getPorts();
            ip.m_serverAddress = externalIp + ":" +  ports.get(0).getPort().toString();
            ip.m_rconAddress = externalIp + ":" + ports.get(1).getPort().toString();

            if (externalIp == null || externalIp.equals("none yet")) {
                ip.m_users = "Offline";
                ip.m_capacity = "0";
            } else {
                try {
                    MineStat mineStat = new MineStat(externalIp, ports.get(0).getPort());
                    ip.m_users = "" + mineStat.getCurrentPlayers();
                    ip.m_capacity = "" + mineStat.getMaximumPlayers();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            ips.add(ip);
        }
        return ips;
    }
}
