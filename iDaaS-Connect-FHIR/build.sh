#!/bin/bash
echo -e "Build environment variables:"
REGISTRY_URL=${REGISTRY_URL}
REGISTRY_NAMESPACE=${REGISTRY_NAMESPACE}
IMAGE_NAME=${IMAGE_NAME}
BUILD_NUMBER=${BUILD_NUMBER}

echo -e "Checking for Dockerfile at the repository root"
if [ -f Dockerfile ]; then 
   echo "Dockerfile found"
else
    echo "Dockerfile not found"
    exit 1
fi

FULL_IMAGE_NAME=$REGISTRY_URL/$REGISTRY_NAMESPACE/$IMAGE_NAME

#Classic pipeline Container Registry job requires PIPELINE_IMAGE_URL to be defined, so the next deploy job could access this value:
export PIPELINE_IMAGE_URL="${FULL_IMAGE_NAME}:$BUILD_NUMBER"

echo -e "Building container image"
set -x
  "Specific build script for docker image"
set +x
