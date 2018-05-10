#!/usr/bin/env bash

docker run --net=host --name=agent-provider-small -d mesh-agent-yangbajing:latest provider-small
docker run --net=host --name=agent-provider-medium -d mesh-agent-yangbajing:latest provider-medium
docker run --net=host --name=agent-provider-large -d mesh-agent-yangbajing:latest provider-large
docker run --net=host --name=agent-consumer -d mesh-agent-yangbajing:latest consumer
