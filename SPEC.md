# MiniMax Token 查询 — App 规格说明书

## 1. 项目概述

- **名称**：MiniMax Token
- **类型**：Android 原生应用
- **一句话描述**：查询 MiniMax API Key 的 token 额度使用情况，支持桌面微件。
- **目标用户**：土豪本人
- **最低 SDK**：API 34（Android 14）
- **目标 SDK**：API 35

---

## 2. UI/UX 规格

### 2.1 屏幕结构

| 屏幕 | 路由 | 说明 |
|------|------|------|
| 主页 | `/` | API Key 输入 + 模型用量列表 |
| 设置 | `/settings` | 关于 |

### 2.2 视觉风格

- **设计语言**：Material Design 3（含 Dynamic Color / Monet）
- **配色**：亮/暗模式跟随系统，Monet 取色
- **字体**：系统默认Roboto
- **圆角**：MD3 标准

### 2.3 主页布局

- 顶部：API Key 输入框（密码样式，带显示/隐藏切换）
- Key 保存后自动查询，显示加载状态
- 模型用量列表（所有模型均显示）
- 下拉刷新

### 2.4 主页卡片

每个模型一行，显示：
- 模型名称
- 5小时窗口：时间范围 + 已用/总额度
- 周窗口：日期范围 + 已用/总额度

---

## 3. 功能规格

### 3.1 API Key 管理

- 输入框保存到 DataStore（加密存储）
- App 启动时自动加载已保存的 Key
- 有 Key 时自动查询，无 Key 时显示引导输入界面
- 每个 Key 绑定一个 GroupId（MiniMax 控制台可查），用于新接口的 GroupId 查询参数

### 3.2 数据查询

- 调用 `https://www.minimaxi.com/v1/api/openplatform/coding_plan/remains?GroupId={groupId}`
- Header：`Authorization: Bearer {apiKey}`、`accept: application/json`
- 返回 `model_remains[]`，每个模型含 `current_interval_remaining_percent` / `current_weekly_remaining_percent`（以 100% 为分母的剩余比例）、`remains_time`（5h 窗口剩余 ms）、`weekly_remains_time`（周窗口剩余 ms）、`start_time`/`end_time`/`weekly_start_time`/`weekly_end_time`（窗口起止时间戳）
- 注意：新接口的 `current_*_count` 字段恒为 0，UI 必须以百分比展示，不能再用「已用/总额」格式

### 3.3 Widget

- 显示 MiniMax-M* 模型的用量
- 5小时窗口：显示时间范围（24小时制，如 `15:00 - 20:00`）+ 剩余百分比
- 周窗口：显示日期范围（如 `2026-04-27 ~ 2026-05-04`）+ 剩余百分比
- 点击 Widget 跳转 App 主页

---

## 4. 技术栈

| 层次 | 技术 |
|------|------|
| 语言 | Kotlin 1.9+ |
| UI | Jetpack Compose + Material 3 |
| 架构 | MVVM + Clean Architecture |
| DI | Hilt |
| 数据 | Retrofit + OkHttp + DataStore |
| Widget | Glance (Jetpack Compose Widget) |
| 导航 | Compose Navigation |
| 异步 | Kotlin Coroutines + Flow |

---

## 5. 非功能要求

- APK 可直接安装，无需 Play Store
- 亮/暗模式自动跟随系统
- Monet 取色在 Android 12+ 自动生效
