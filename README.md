# Leaf

> There are no two identical leaves in the world.
>
> 世界上没有两片完全相同的树叶。
>
> ​								— 莱布尼茨

[中文文档](./README.md) | [English Document](./README.md)

## 根据美团开源leaf改造
1. 沿用leaf雪花id生成算法
2. 增加nacos服务注册（workerId 0-1000）
3. 移除mysql，zookeeper相关依赖功能，改用redis控制服务注册时的workerId
4. 为防止http服务不可用，可引入core组件,指定workerId并实例化本地工具类,暂分配1001-1023（启动参数配置增加配置-DworkerId=1001）

