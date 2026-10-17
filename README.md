# 中医考试系统 Web 应用

依据 `tcm-exam-prompts.html` 分阶段提示词文档开发的完整中医在线考试系统，内置 **10000+ 道中医试题**。

## ✨ 功能总览

| 角色 | 功能 |
|------|------|
| **学生** | 参加考试（答题卡/倒计时/标记/防切屏/断点续答/自动交卷）、成绩查询、错题本（间隔重复复习） |
| **教师** | 题库管理（CRUD/筛选/审核流转）、手工/智能组卷、发布考试、阅卷台（主观题人工批阅）、班级成绩分析（ECharts） |
| **管理员** | 用户/角色管理、院系班级管理、全局统计看板 |

## 📚 内置题库（10000+ 道）

- **六大学科**：中医基础理论(138)、中医诊断学(248)、中药学(4750)、方剂学(2541)、针灸学(1702)、中医内科学(622)
- **九大题型**：单选(4576)、判断(2883)、填空(1440)、多选(544)、配伍(320)、名词解释(104)、简答(70)、论述(20)、病案分析(44)
- **数据来源**：基于中医药规划教材考点，由 `tools/` 下脚本从事实数据表程序化生成

## 🛠 技术栈

- **后端**：Java 17 / Spring Boot 3.x / MyBatis-Plus / Spring Security + JWT / PostgreSQL 16 / Redis
- **前端**：Vue 3 + Vite + TypeScript + Pinia + Element Plus + ECharts
- **部署**：Docker Compose（后端 + 前端 + PostgreSQL + Redis + Nginx）

## 🚀 快速开始

### 方式一：Docker 一键部署（推荐）

```bash
docker-compose up -d --build
```

访问 `http://localhost` （前端），API 位于 `http://localhost:8080/api`。
首次启动自动初始化数据库（含 10000+ 题库）。

### 方式二：本地开发

**1. 准备数据库**

```sql
-- 创建数据库
CREATE DATABASE tcm_exam;
-- 依次执行（PostgreSQL 16）
\i backend/src/main/resources/sql/schema.sql
\i backend/src/main/resources/sql/seed_basic.sql
\i backend/src/main/resources/sql/seed_questions.sql
```

**2. 启动后端**

```bash
cd backend
# 配置 application.yml 中的数据库/Redis 连接（默认 localhost:5432 / localhost:6379）
mvn spring-boot:run
# 或 mvn package && java -jar target/exam-backend-1.0.0.jar
```

**3. 启动前端**

```bash
cd frontend
npm install
npm run dev    # 开发模式，代理 /api 到 localhost:8080
npm run build  # 生产构建
```

## 🔑 演示账号（密码均为 `123456`）

| 账号 | 角色 | 说明 |
|------|------|------|
| `admin` | 管理员 | 系统管理 |
| `teacher` | 教师 | 题库/组卷/阅卷 |
| `student1` | 学生 | 参加考试 |

## 📁 项目结构

```
├── backend/                    # Spring Boot 后端
│   ├── src/main/java/com/tcm/exam/
│   │   ├── controller/         # REST 接口
│   │   ├── service/            # 业务逻辑
│   │   ├── mapper/             # MyBatis-Plus Mapper
│   │   ├── entity/             # 实体
│   │   ├── dto/ vo/            # 传输/视图对象
│   │   ├── security/           # JWT/RBAC
│   │   ├── config/             # 配置
│   │   └── common/             # 统一响应/异常
│   └── src/main/resources/
│       ├── application.yml
│       └── sql/                # schema + 种子数据（10000+ 题）
├── frontend/                   # Vue 3 前端
│   └── src/
│       ├── views/              # 学生/教师/管理员三端页面
│       ├── api/                # 接口封装
│       ├── stores/             # Pinia 状态
│       ├── router/             # 路由（角色守卫）
│       └── components/         # 布局等
├── tools/                      # 题库生成脚本
│   ├── tcm_data.py             # 中药/方剂/腧穴/脉象/内科事实表
│   ├── tcm_hand_data.py        # 名解/简答/论述/病案手写题库
│   └── generate_questions.py   # 生成器（输出 SQL）
├── docker-compose.yml
└── README.md
```

## 🧪 测试与压测参考

- 后端单元/集成测试：`mvn test`
- 压测：JMeter/k6 500 人并发，目标组卷 < 2s、判分 < 1s（见 `tcm-exam-prompts.html` 阶段6）

## 🔒 安全特性

- JWT 无状态鉴权 + RBAC 注解式权限控制
- BCrypt 密码加密
- 考试防作弊：题目/选项乱序、切屏检测上报、超时自动交卷、禁用复制
- 操作审计日志表预留

## 📄 文档依据

`tcm-exam-prompts.html` 提供六阶段提示词（需求→架构→数据库→后端→前端→部署），本工程即按该流程落地实现。
