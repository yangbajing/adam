#!/bin/bash

ETCD_HOST=$(ip addr show docker0 | grep 'inet\b' | awk '{print $2}' | cut -d '/' -f 1)
ETCD_PORT=2379
ETCD_URL=http://$ETCD_HOST:$ETCD_PORT

echo ETCD_URL = $ETCD_URL

if [[ "$1" == "consumer" ]]; then
  echo "Starting consumer agent..."
  java -jar \
       -Xms1536M \
       -Xmx1536M \
       -Dtype=consumer \
       -Dserver.port=20000\
       -Dendpoint.port=20010\
       -Detcd.url=$ETCD_URL \
       -Dlogs.dir=/root/logs \
       /root/dists/mesh-agent.jar
elif [[ "$1" == "provider-small" ]]; then
  echo "Starting small provider agent..."
  java -jar \
       -Xms512M \
       -Xmx512M \
       -Dtype=provider \
       -Dserver.port=30000\
       -Dendpoint.port=30010\
       -Ddubbo.protocol.port=20889 \
       -Detcd.url=$ETCD_URL \
       -Dlogs.dir=/root/logs \
       /root/dists/mesh-agent.jar
elif [[ "$1" == "provider-medium" ]]; then
  echo "Starting medium provider agent..."
  java -jar \
       -Xms1536M \
       -Xmx1536M \
       -Dtype=provider \
       -Dserver.port=30001\
       -Dendpoint.port=30011\
       -Ddubbo.protocol.port=20890 \
       -Detcd.url=$ETCD_URL \
       -Dlogs.dir=/root/logs \
       /root/dists/mesh-agent.jar
elif [[ "$1" == "provider-large" ]]; then
  echo "Starting large provider agent..."
  java -jar \
       -Xms2560M \
       -Xmx2560M \
       -Dtype=provider \
       -Dserver.port=30002\
       -Dendpoint.port=30012\
       -Ddubbo.protocol.port=20891 \
       -Detcd.url=$ETCD_URL \
       -Dlogs.dir=/root/logs \
       /root/dists/mesh-agent.jar
else
  echo "Unrecognized arguments, exit."
  exit 1
fi
