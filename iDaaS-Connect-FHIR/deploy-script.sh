#!/bin/bash
#set -x

# Use this script as a starting point to create your own deployment.yml

# Make sure the cluster is running and get the ip_address
ip_addr=$(ibmcloud ks workers --cluster $PIPELINE_KUBERNETES_CLUSTER_NAME | grep normal | awk '{ print $2 }')
if [ -z $ip_addr ]; then
  echo "$PIPELINE_KUBERNETES_CLUSTER_NAME not created or workers not ready"
  exit 1
fi

# Initialize script variables
NAME="idaas-connect-fhir"
IMAGE="us.icr.io/clinical_data_platform/idaas-connect-fhir:$PIPELINE_STAGE_INPUT_REVS"
if [ -z IMAGE ]; then
  echo "$IMAGE not set. If using $PIPELINE_IMAGE_URL this variable is only configured when a "Container Registry" build job is used as the stage input."
  exit 1
fi
PORT=$(ibmcloud cr image-inspect $IMAGE --format '{{ range $key,$value := .ContainerConfig.ExposedPorts }} {{ $key }} {{ "" }} {{end}}' | sed -E 's/^[^0-9]*([0-9]+).*$/\1/')
if [ -z "$PORT" ]; then
    PORT=5000
    echo "Port not found in Dockerfile, using $PORT"
fi

echo ""
echo "Deploy environment variables:"
echo "NAME=$NAME"
echo "IMAGE=$IMAGE"
echo "PORT=$PORT"
echo ""

DEPLOYMENT_FILE="deployment.yml"
echo "Creating deployment file $DEPLOYMENT_FILE"

# Build the deployment file
DEPLOYMENT=$(cat <<EOF''
apiVersion: apps/v1
kind: Deployment
metadata:
  name: $NAME
spec:
  replicas: 1
  selector:
    matchLabels:
      app: $NAME
  template:
    metadata:
      labels:
        app: $NAME
    spec:
      containers:
      - name: $NAME
        image: $IMAGE
        imagePullPolicy: Always
        ports:
        - containerPort: 5000
---
apiVersion: v1
kind: Service
metadata:
  name: $NAME
  labels:
    app: $NAME
spec:
  type: NodePort
  ports:
    - port: 5000
  selector:
    app: $NAME
EOF
)

# Substitute the variables
echo "$DEPLOYMENT" > $DEPLOYMENT_FILE
sed -i 's/$NAME/'"$NAME"'/g' $DEPLOYMENT_FILE
sed -i 's=$IMAGE='"$IMAGE"'=g' $DEPLOYMENT_FILE
sed -i 's/$PORT/'"$PORT"'/g' $DEPLOYMENT_FILE

# Show the file that is about to be executed
echo ""
echo "DEPLOYING USING MANIFEST:"
echo "cat $DEPLOYMENT_FILE"
cat $DEPLOYMENT_FILE
echo ""

# Execute the file
echo "KUBERNETES COMMAND:"
echo "kubectl apply -f $DEPLOYMENT_FILE"
kubectl apply -f $DEPLOYMENT_FILE --namespace=anthem-cdp-poc
echo ""

echo ""
echo "DEPLOYED SERVICE:"
kubectl describe services $NAME
echo ""
echo "DEPLOYED PODS:"
kubectl describe pods --selector app=$NAME
echo ""

# Show the IP address and the PORT of the running app
port=$(kubectl get services | grep "$NAME " | sed 's/.*:\([0-9]*\).*/\1/g')
echo "RUNNING APPLICATION:"
echo "URL=http://$ip_addr"
echo "PORT=$port"
echo ""
echo "$NAME running at: http://$ip_addr:$port"