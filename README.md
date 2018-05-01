"# openhack-team12" 

#challenge 1
az container create --resource-group openHackChallenge1 --name mc-server-container-sk --image challenge1.azurecr.io/minecraft-server:1.0 --cpu 1 --memory 1 --dns-name-label mcserver12 --ports 25565 --environment-variables EULA=TRUE


#create a secret
kubectl create secret docker-registry supersecret --docker-server=challenge1.azurecr.io --docker-username=challenge1 --docker-password=zJuQ6JKFVN9gFjQVnAejSfLim/KL5L0d --docker-email=andrew.meehan@orderdynamics.com
