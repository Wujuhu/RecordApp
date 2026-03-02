# RecordApp（TPAPP）

一个基于 **Android + Kotlin + Jetpack Compose** 的本地记录应用，面向“应用账号/密码/备注”等信息的结构化管理。

> 当前仓库主代码位于 `TPAPP/` 目录。

## 功能概览

- 应用维度管理（如微信、邮箱、银行等）
- 单应用下多账号记录
- 账号字段支持：用户名、密码、备注、标签
- 记录搜索与快速编辑
- 随机密码生成器
- 导入/导出（用于本地备份与迁移）
- 回收站（删除恢复相关能力）

## 技术栈

- **语言**：Kotlin
- **UI**：Jetpack Compose + Material 3
- **架构**：MVVM
- **数据层**：Room
- **安全**：Android Keystore + AES/GCM（密码加密）
- **构建**：Gradle (KTS)

## 目录结构

```text
RecordApp/
├── TPAPP/
│   ├── app/
│   │   └── src/main/java/com/tp/tpapp/
│   │       ├── data/           # 数据库、DAO、Repository、安全模块
│   │       ├── ui/             # 页面、导航、组件、ViewModel
│   │       └── MainActivity.kt
│   ├── build.gradle.kts
│   └── settings.gradle.kts
├── PROJECT_PLAYBOOK.md
└── README.md
```

## 环境要求

- Android Studio（建议最新稳定版）
- JDK 17（通常由 Android Studio 自带）
- Android SDK（`minSdk 26`）

## 快速开始

```bash
# 1) 进入项目
cd TPAPP

# 2) 构建调试包
./gradlew assembleDebug

# 3) 运行单元测试
./gradlew test
```

也可以直接用 Android Studio 打开 `TPAPP/` 目录后运行。

## 数据与安全说明

- 应用默认本地存储，不依赖云端账号系统
- 敏感密码字段通过 Keystore 派生密钥进行加密后存储
- 导入/导出文件请妥善保管，避免泄露

## 开发建议

- 业务与流程说明请先看：`PROJECT_PLAYBOOK.md`
- 协作约束与 AI 规则请看：`AGENTS.md`

## 路线图（可选）

- [ ] 生物识别/应用锁
- [ ] 更细粒度的导入导出策略
- [ ] 更完整的搜索与筛选
- [ ] UI/交互细节优化

## License

当前仓库未声明开源许可证（默认保留所有权利）。如需开源，请补充 `LICENSE` 文件。
