AI START HERE: `C:\Users\WJH\Downloads\tp\PROJECT_PLAYBOOK.md`
AI RULES: `C:\Users\WJH\Downloads\tp\AGENTS.md`

# TP 笔记安卓软件 — 开始文档

## 目标与范围
TP 是一款用于记录不同应用账号/密码的安卓软件，强调“快速录入、结构化预留位、支持多账号、导入导出”。
这是一个纯本地应用，不需要用户账号系统或云同步。

核心目标：
- 为不同应用预留固定结构位置（如：应用名、账号、密码、备注等），便于快速记录
- 每个应用支持多个账号/密码
- 支持一键导入/导出（所有密码按统一格式导出为单个文件）
- 支持一键随机密码生成器

## 功能需求（第一版）
- 应用列表
  - 展示所有应用条目（如：微信、邮箱、银行等）
- 应用详情
  - 一个应用可包含多个账号
  - 账号字段至少包括：账号名/用户名、密码、备注、标签（可选）
- 快速新增
  - 进入应用后可快速新增账号条目
- 搜索
  - 按应用名、账号名、备注搜索
- 导入/导出
  - 一键导出为单个文件
  - 一键导入该文件并还原数据
- 随机密码生成器
  - 一键生成随机密码并可直接填入

## 数据模型（建议）
- AppEntity
  - id
  - appName
  - note (可选)
- AccountEntity
  - id
  - appId (关联 AppEntity)
  - username
  - password
  - note
  - tags (可选)
  - updatedAt

## 导入/导出格式（建议）
建议使用 JSON，结构清晰且易于扩展：

示例：
{
  "version": 1,
  "exportedAt": "2026-02-22T12:00:00Z",
  "apps": [
    {
      "appName": "微信",
      "note": "工作号",
      "accounts": [
        {
          "username": "user_a",
          "password": "pass_a",
          "note": "主账号",
          "tags": ["工作"],
          "updatedAt": "2026-02-22T12:00:00Z"
        }
      ]
    }
  ]
}

说明：
- version 用于未来兼容
- exportedAt 便于追踪导出时间

## 安全性注意事项
这是密码管理类软件，必须避免明文存储：
- 本地数据库建议加密（例如使用 Room + 加密方案）
- 密码字段使用加密后再存储
- 应用无需用户账号系统或云同步

## 技术栈（已确定）
- 语言：Kotlin
- UI：Jetpack Compose
- 数据库：Room
- 架构：MVVM
- 依赖管理：Gradle
- 最低系统版本：API 26 (Android 8.0)
- 应用内解锁：不需要

## Windows 11 准备步骤（小白路线）
1. 安装 Android Studio（标准安装即可，会自动带 JDK）
2. 打开 Android Studio，首次启动按提示安装 SDK 与模拟器组件
3. 新建项目：选择 `Empty Activity (Compose)`
4. 项目名填 `TP`，保存路径选择 `C:\Users\WJH\Downloads\tp`
5. 语言选 `Kotlin`，最低版本选 `API 26 (Android 8.0)`
6. 等待项目创建完成后，点击运行按钮启动模拟器并看到默认界面

## 第一阶段开发顺序（新手推荐）
1. 数据模型设计（App 与 Account）
2. Room 本地数据库
3. 页面：应用列表、应用详情（含账号列表）
4. 页面：新增/编辑账号
5. 一键随机密码生成器
6. 一键导入/导出（JSON 文件）

## 开发启动清单
- 确认需求范围是否增加：是否需要分类统计、更多字段（邮箱/手机号/网站）等
- 确认导入/导出格式（JSON/CSV）
- 确定 UI 结构：应用列表 -> 应用详情 -> 账号列表/编辑
- 定义数据模型并落地到 Room

## 下一步建议
- 我可以帮你初始化 Android 项目结构
- 也可以先从数据层（Room + 加密）开始

如需我继续，请告诉我：
- 是否现在就初始化 Android 项目
- 导入导出是否采用 JSON（推荐）

