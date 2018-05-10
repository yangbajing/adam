#!/bin/sh

./sbt "project agent" assembly
docker build . -t mesh-agent-yangbajing:latest -f Dockerfile-dev.docker

