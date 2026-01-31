# JitterPay 本地更新服务器

用于开发环境的本地更新服务器，模拟生产环境的更新检查和 APK 下载功能。

## 功能特性

- 自动监听 APK 构建产物变化
- 自动提取版本信息并生成 `version.json`
- 提供版本检查 API (`/version.json`)
- 提供 APK 下载服务
- 支持多 ABI 架构（arm64-v8a, armeabi-v7a, x86, x86_64, universal）

## 快速开始

### 1. 安装依赖

```bash
cd mock-server
npm install
```

### 2. 构建 APK

```bash
# 构建 debug 版本
npm run build

# 或构建 release 版本
BUILD_TYPE=release npm run build
```

### 3. 启动服务器

```bash
npm start
```

服务器将在 `http://localhost:8080` 启动。

### 4. 一键完成（推荐）

```bash
npm run dev
```

这将：构建 APK → 复制文件 → 启动服务器

## 使用方法

### 测试更新功能

1. 确保 Android 设备/模拟器与开发机在同一网络
2. 设置环境变量指向本地服务器：

```bash
# Windows PowerShell
$env:CDN_BASE_URL="http://<你的IP>:8080"
./gradlew installDebug

# Windows CMD
set CDN_BASE_URL=http://<你的IP>:8080
gradlew installDebug

# Linux/macOS
export CDN_BASE_URL="http://<你的IP>:8080"
./gradlew installDebug
```

> **注意**：Android 模拟器访问宿主机使用 `10.0.2.2`

3. 应用启动后会检查更新，检测到新版本后下载安装

### API 端点

| 端点 | 说明 |
|------|------|
| `GET /version.json` | 版本检查，返回最新版本信息 |
| `GET /v{version}/{file}` | 下载指定版本的 APK |
| `GET /apks` | 列出所有可用的 APK 版本 |
| `GET /health` | 服务器健康检查 |

### version.json 示例

```json
{
  "latest_version": "v1.0",
  "release_date": "2026-01-30",
  "cdn_base_url": "http://localhost:8080",
  "apk_size": 15000000,
  "apks": [
    {
      "abi": "arm64-v8a",
      "file_name": "jitterpay-arm64-v8a-1.0.apk",
      "size": 14500000
    },
    {
      "abi": "universal",
      "file_name": "jitterpay-universal-1.0.apk",
      "size": 28000000
    }
  ],
  "generated_at": "2026-01-30T10:00:00.000Z",
  "build_type": "debug"
}
```

## 目录结构

```
mock-server/
├── server.js          # 主服务器
├── watch.js           # 文件监听脚本
├── package.json       # npm 配置
├── .env.example       # 环境变量示例
├── README.md          # 本文档
├── version.json       # 自动生成的版本信息
└── apks/              # APK 存储目录
    └── v1.0/
        ├── jitterpay-arm64-v8a-1.0.apk
        ├── jitterpay-armeabi-v7a-1.0.apk
        ├── jitterpay-x86-1.0.apk
        ├── jitterpay-x86_64-1.0.apk
        └── jitterpay-universal-1.0.apk
```

## 环境变量

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `PORT` | 8080 | 服务器端口 |
| `BUILD_TYPE` | debug | 构建类型 (debug/release) |
| `ANDROID_PROJECT_DIR` | .. | Android 项目根目录 |

## 手动触发更新

修改版本号后，可以使用以下命令手动触发更新：

```bash
# 方式1: 使用内置命令
node server.js --update

# 方式2: 修改版本后重新构建
npm run build && node server.js
```

## 故障排除

### APK 文件未找到

确保已运行 `./gradlew assembleDebug` 构建 APK。

### 版本未更新

1. 检查 `app/build/outputs/apk/debug/output-metadata.json` 是否存在
2. 手动运行 `node server.js --update` 强制更新

### 网络连接问题

- 确保防火墙允许访问 `PORT` 端口
- 真机测试时使用电脑的实际 IP 地址，而非 `localhost`

## 与生产环境对比

| 功能 | 本地服务器 | 生产服务器 |
|------|----------|-----------|
| URL | `http://localhost:8080` | `https://store.flowerwine.dpdns.org` |
| 版本检查 | `/version.json` | `/version.json` |
| APK 下载 | 自动从本地 apks/ 目录 | 从 CDN 下载 |
| 监听变化 | 自动检测 | 需手动上传 |
