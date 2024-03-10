#!/bin/bash

echo 'Building container...'
DOCKER_BUILDKIT=1 docker build -t dohmap-local .
echo 'Deploying...'
docker-compose up -d
echo 'Done.'
