# 学习项目：比联超市

## 1 技术栈

### 1.1 后端
- SpringBoot+MyBatis+MySQL
  - AOP
  - Swagger
  - JWT
  - WebSocket
- Redis
- Minio
- OnnxRuntime

### 1.2 前端
- Vue
- ElementUI
- 微信小程序
- Nginx

### 1.3 推荐系统
- Pytorch
- Transformer
- Onnx

## 2 部署流程

### 2.1 MySQL数据库
```shell
docker run -itd --name mysql57 -p 3306:3306 -p 33060:33060 -e MYSQL_ROOT_PASSWORD="root" mysql:5.7
```
运行billion.sql导入数据。
### 2.2 Redis
```shell
docker run -itd --name some-redis -p 6379:6379 redis
```

### 2.3 Minio
```shell
docker run -itd --name minio -p 9000:9000 -p 9001:9001 -e MINIO_ROOT_USER="admin" -e MINIO_ROOT_PASSWORD="admin123" bitnami/minio
```
1. 创建bucket：billion-supermarket。把图片文件上传进去。
2. 设置bucket权限，匿名可读。
3. 创建accesskey和secretkey，复制到application.yml中。

### 2.4 前端工程
进入nginx目录，运行：
```shell
.\nginx
```

### 2.5 后端工程
maven打包运行本项目。

## TODO
- 添加elasticsearch检索服务
