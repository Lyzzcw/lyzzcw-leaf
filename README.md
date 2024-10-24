# Leaf

> There are no two identical leaves in the world.
>
> 世界上没有两片完全相同的树叶。
>
> ​								— 莱布尼茨

[中文文档](./README.md) | [English Document](./README.md)

## 根据美团开源leaf改造
1. 沿用leaf雪花id生成算法
2. 移除mysql，zookeeper相关依赖功能，改用redis并发锁控制服务发布生成唯一workerId
3. 增加nacos服务注册功能（workerId 0-1000）
4. 防止http服务不可用，增加本地core依赖启动（启动参数配置workerId 1001-1023）

