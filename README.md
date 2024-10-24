# Leaf

> There are no two identical leaves in the world.
>
> 世界上没有两片完全相同的树叶。
>
> ​								— 莱布尼茨

[中文文档](./README.md) | [English Document](./README.md)

## 根据美团开源leaf改造
1.使用snowflake方式，移除mysql相关依赖
2.弃用zookeeper，改用redis控制并发生成workerId
3.增加nacos服务注册功能（workerId 0-1000）
4.增加本地core依赖启动（workerId 1001-1023 参数配置）

