"# openhack-team12" 

# challenge 1
az container create --resource-group openHackChallenge1 --name mc-server-container-sk --image challenge1.azurecr.io/minecraft-server:1.0 --cpu 1 --memory 1 --dns-name-label mcserver12 --ports 25565 --environment-variables EULA=TRUE


# create a secret
kubectl create secret docker-registry supersecret --docker-server=challenge1.azurecr.io --docker-username=challenge1 --docker-password=zJuQ6JKFVN9gFjQVnAejSfLim/KL5L0d --docker-email=andrew.meehan@orderdynamics.com

# setup persistent storage
Find your cluster with  
    az group list --output table  
It looks like MC_* (MC_openHackChallenge1_team12cluster_eastus)  
Make the volume  
    az storage account create --resource-group MC_openHackChallenge1_team12cluster_eastus --name team12storage --location eastus --sku Standard_LRS  
  
Run these two commands  
kubectl create -f storageClass.yml  
kubectl create -f persistentVolumeClaim.yml  
