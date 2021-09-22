#!/bin/bash
#set -x

# Use this script as a starting point to create your own deployment.yml

# Make sure the cluster is running and get the ip_address

# Initialize script variables
NAME="idaas-connect-fhir"
IMAGE="<image_url>"
NAME_SPACE="<namespace>"
PORT="<port>"
MEMORY="memory(Gi)"
CORES="500m"

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
        - containerPort: $PORT
        resources:
          limits:
            memory: "$MEMORY"
            cpu: "$CORES"
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
    - name: $PORT-tcp
      protocol: TCP
      port: $PORT
      targetPort: $PORT
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
echo "oc COMMAND:"
echo "oc apply -f $DEPLOYMENT_FILE"
oc apply -f $DEPLOYMENT_FILE --namespace=$NAME_SPACE
echo ""

echo ""
echo "DEPLOYED SERVICE:"
oc describe services $NAME
echo ""
echo "DEPLOYED PODS:"
oc describe pods --selector app=$NAME

# Show the IP address and the PORT of the running app
port=$(oc get services | grep "$NAME " | sed 's/.*:\([0-9]*\).*/\1/g')
echo "RUNNING APPLICATION:"
echo "URL=http://$ip_addr"
echo "PORT=$port"
echo ""
echo "$NAME running at: http://$ip_addr:$port"