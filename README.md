# 天池中间件大赛 2018

## 运行并测试

**启动Etcd**

```
ETCDCTL_API=3 ./etcd --listen-client-urls="http://0.0.0.0:2379,http://0.0.0.0:4001" --advertise-client-urls="http://0.0.0.0:2379,http://0.0.0.0:4001"
```

**构建并运行测试**

```
./scripts/build.sh
./scripts/docker-run.sh
sleep 5
./scripts/bench.sh
```

**重新构建**

```
./scripts/docker-stop.sh
./scripts/docker-rm.sh
```

删除Docker容器后再执行 **构建并运行测试**
