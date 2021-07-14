#!/bin/bash
echo -e "Build environment variables:"
echo "REGISTRY_URL=${REGISTRY_URL}"
echo "REGISTRY_NAMESPACE=${REGISTRY_NAMESPACE}"
echo "IMAGE_NAME=${IMAGE_NAME}"
echo "BUILD_NUMBER=${BUILD_NUMBER}"

# Learn more about the available environment variables at:
# https://cloud.ibm.com/docs/services/ContinuousDelivery?topic=ContinuousDelivery-deliverypipeline_environment#deliverypipeline_environment

# To review or change build options use:
# ibmcloud cr build --help

echo -e "Checking for Dockerfile at the repository root"
if [ -f Dockerfile ]; then 
   echo "Dockerfile found"
else
    echo "Dockerfile not found"
    exit 1
fi

#ensure docker and buildkit are present if not already in current pipeline-base-image
which buildctl > /dev/null || (curl -fsSL https://github.com/moby/buildkit/releases/download/v0.8.0/buildkit-v0.8.0.linux-amd64.tar.gz | tar zxf - --strip-components=1 -C /usr/bin bin/buildctl)
which docker > /dev/null || (curl -fsSL https://download.docker.com/linux/static/stable/x86_64/docker-19.03.9.tgz | tar zxf - --strip-components=1 -C /usr/bin docker/docker)

FULL_IMAGE_NAME=$REGISTRY_URL/$REGISTRY_NAMESPACE/$IMAGE_NAME

echo -e "Building container image"
set -x
ibmcloud cr login
buildctl build --frontend dockerfile.v0 --local context=. --local dockerfile=. \
  --output type=image,name=${FULL_IMAGE_NAME}:$BUILD_NUMBER,push=true \
  --export-cache type=registry,ref=${FULL_IMAGE_NAME}:buildcache \
  --import-cache type=registry,ref=${FULL_IMAGE_NAME}:buildcache
set +x
