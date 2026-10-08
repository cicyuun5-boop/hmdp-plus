# 邻里优选 · 分布式本地生活服务平台

多模块 Spring Cloud Alibaba 微服务项目（模块前缀 `hmdp-`），覆盖商户浏览、优惠券秒杀、达人探店、好友关注等本地生活业务，并围绕高并发秒杀链路补齐稳定性与数据一致性方案。

## 技术栈

| 分类 | 技术 |
| --- | --- |
| 语言 / 运行时 | Java 17 |
| 框架 | Spring Boot 3.5.4、Spring Cloud 2025.0.0、Spring Cloud Alibaba 2025.0.0.0 |
| 服务治理 | Nacos（注册中心 + 配置中心）、Spring Cloud Gateway、OpenFeign |
| 数据访问 | MyBatis-Plus 3.5.7、MySQL、ShardingSphere 5.3.2 |
| 中间件 | Redis、Redisson 3.52.0、Kafka |
| 前端 | Vue 3.5、Vite 6、Element Plus、Pinia、Vue Router、Axios |

## 模块结构

### 业务服务

| 模块 | 职责 |
| --- | --- |
| `hmdp-gateway` | 统一网关：路由转发；拦截外部对 `/xxx/inner/**` 内部接口的访问 |
| `hmdp-user-service` | 用户服务：登录鉴权、用户信息、手机号 |
| `hmdp-trade-service` | 交易服务：优惠券、秒杀下单、库存扣减、订单、对账 |
| `hmdp-shop-service` | 商铺服务：商铺与商铺类型查询、缓存 |
| `hmdp-community-service` | 社区服务：探店笔记、点赞、关注 |
| `hmdp-api` | 服务间契约：OpenFeign Client 与内部调用鉴权拦截器 |

### 公共与中间件框架

| 模块 | 职责 |
| --- | --- |
| `hmdp-common` | 通用常量、DTO、异常与工具类 |
| `hmdp-parameter` | 公共参数与配置 |
| `hmdp-sharding` | 分库分表支撑 |
| `hmdp-id-generator-framework` | 全局唯一 ID 生成（雪花算法） |
| `hmdp-redis-tool-framework` | Redis 封装：通用客户端、限流（令牌桶 / 滑动窗口） |
| `hmdp-redisson-framework` | Redisson 封装：分布式锁、布隆过滤器、幂等、延迟队列 |
| `hmdp-mq-framework` | Kafka 封装：生产端可靠投递、消费端幂等与重试、死信处理 |

## 核心能力

- **服务治理**：Nacos 同时承担注册中心与配置中心，端口 / 数据源 / Redis / Kafka / 内部凭据等集中托管，仓库内本地配置只保留启动引导项，实现配置集中管理与环境隔离。
- **服务间调用安全**：网关层直接拒绝外部访问内部路径，服务内再校验服务间共享凭据，调用方由 Feign 拦截器自动携带凭据，形成「网关拦截 + 服务校验」两层防御。
- **秒杀链路**：Redis 限流控制入口流量 → Lua 脚本原子扣减库存与「一人一单」判重 → 下单消息经 Kafka 异步落库；生产端幂等，消费端手动 ack + 幂等防重，失败走补偿与死信队列。
- **缓存治理**：本地缓存 + Redis 二级缓存；布隆过滤器与空值缓存防穿透，分布式锁重建防击穿。
- **数据一致性**：以 Redis 扣减流水与数据库订单做定时对账，识别「已扣减未落单」并回补库存、修正订单状态；对账任务用分布式锁保证多实例下不重复执行，回滚失败单独落库并告警。
- **数据层扩展**：ShardingSphere 分库分表（2 库 × 2 表），订单表按分片键写入、订单路由表定位，主键采用全局 ID 生成器。

## 目录结构

```
hmdp-plus
├── hmdp-common                    # 通用模块
├── hmdp-parameter                 # 公共参数
├── hmdp-sharding                  # 分库分表支撑
├── hmdp-api                       # 服务间契约（OpenFeign）
├── hmdp-gateway                   # 统一网关
├── hmdp-user-service              # 用户服务
├── hmdp-trade-service             # 交易服务
├── hmdp-shop-service              # 商铺服务
├── hmdp-community-service         # 社区服务
├── hmdp-id-generator-framework    # 全局 ID 生成
├── hmdp-redis-tool-framework      # Redis 封装 / 限流
├── hmdp-redisson-framework        # 分布式锁 / 布隆过滤器 / 幂等 / 延迟队列
├── hmdp-mq-framework              # Kafka 可靠消息
├── hmdp-vue3                      # 前端工程
├── sql                            # 建库建表脚本
└── pom.xml
```

## 环境依赖

- JDK 17
- Maven 3.9+
- MySQL 8.x
- Redis
- Kafka
- Nacos 2.x

## 启动步骤

1. **初始化数据库**：依次执行 `sql/1_create_database.sql`、`sql/hmdp_0.sql`、`sql/hmdp_1.sql`。
2. **启动中间件**：Nacos（默认 `127.0.0.1:8848`）、Redis、Kafka、MySQL。
3. **导入 Nacos 配置**：在配置中心 `DEFAULT_GROUP` 下创建
   `hmdp-common.yaml`，以及 `hmdp-gateway.yaml`、`hmdp-user-service.yaml`、
   `hmdp-trade-service.yaml`、`hmdp-shop-service.yaml`、`hmdp-community-service.yaml`。
   这些配置承载端口、数据源、Redis、Kafka、内部凭据等内容，并开启 `refreshEnabled` 支持动态刷新。
4. **编译**：`mvn -DskipTests clean package`
   > 项目 `java.version` 为 17，请确认 `JAVA_HOME` 指向 JDK 17。
5. **启动服务**：先启动 `hmdp-gateway`，再启动各业务服务。
6. **启动前端**：`cd hmdp-vue3 && npm install && npm run dev`。

## 说明

- 服务配置集中在 Nacos，仓库内 `application.yml` 只保留应用名、Nacos 地址与需要导入的配置列表，便于环境隔离与集中管理。
- 数据层按 `hmdp_0` / `hmdp_1` 两个库分片，对应 `sql/hmdp_0.sql` 与 `sql/hmdp_1.sql`。
