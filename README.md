# 天池中间件大赛 2018

## 运行

**consumer agent**

```
-Detcd.url=http://localhost:2379 -Dtype=consumer -Dserver.port=20000
```

**provider agent**

```
-Xms512M -Xmx512M -Detcd.url=http://localhost:2379 -Dtype=provider -Dserver.port=30000 -Ddubbo.protocol.port=20889 -Dlogs.dir=logs/provider-small

-Xms1536M -Xmx1536M -Detcd.url=http://localhost:2379 -Dtype=provider -Dserver.port=30001 -Ddubbo.protocol.port=20890 -Dlogs.dir=logs/provider-medium

-Xms2560M -Xmx2560M -Detcd.url=http://localhost:2379 -Dtype=provider -Dserver.port=30002 -Ddubbo.protocol.port=20891 -Dlogs.dir=logs/provider-large
```

## build docker for development

```
./build.sh
```

## build docker for production

```
docker build . -t mesh-agent-yangbajing
```
