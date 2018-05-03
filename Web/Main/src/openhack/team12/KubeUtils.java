package openhack.team12;

import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.models.V1PersistentVolumeClaim;
import io.kubernetes.client.models.V1PersistentVolumeClaimSpec;
import io.kubernetes.client.models.V1ResourceRequirements;

import java.util.UUID;

public class KubeUtils {
    public static V1PersistentVolumeClaim configureVolumeClaim(String name) {
        V1PersistentVolumeClaim result = new V1PersistentVolumeClaim();
        result.apiVersion("v1");
        result.kind("PersistentVolumeClaim");

        V1ObjectMeta meta = new V1ObjectMeta();
        meta.name(name);
        meta.namespace("default");
        meta.uid(UUID.randomUUID().toString());
        result.setMetadata(meta);

        V1PersistentVolumeClaimSpec spec = new V1PersistentVolumeClaimSpec();
        spec.addAccessModesItem("ReadWriteOnce");
        spec.storageClassName("team12storageclass");

        V1ResourceRequirements specReqs = new V1ResourceRequirements();
        specReqs.putRequestsItem("storage", new Quantity("5Gi"));
        spec.setResources(specReqs);
        result.setSpec(spec);
        return result;
    }
}
