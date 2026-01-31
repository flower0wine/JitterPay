/**
 * JitterPay 本地更新服务器
 *
 * 功能：
 * 1. 监听 APK 构建产物变化
 * 2. 自动提取版本信息并更新 version.json
 * 3. 提供版本检查 API 和 APK 下载服务
 *
 * 使用方法：
 *   npm install                    # 安装依赖
 *   npm run build                  # 构建 APK
 *   npm start                      # 启动服务器
 *   npm run dev                    # 构建并启动（一步到位）
 *
 * 配置：
 *   修改 .env 文件来配置服务器地址、端口等
 */

// 加载 .env 配置文件
require('dotenv').config();

const express = require('express');
const fs = require('fs');
const path = require('path');
const chokidar = require('chokidar');

// 配置
const CONFIG = {
    // 项目根目录
    projectRoot: path.join(__dirname, '..'),
    // APK 输出目录（支持 Product Flavors: apk/{flavor}/{buildType}）
    apkOutputDir: path.join(__dirname, '../app/build/outputs/apk/dev'),
    // APK 版本目录（服务器存储）
    apksDir: path.join(__dirname, 'apks'),
    // version.json 文件路径
    versionJsonPath: path.join(__dirname, 'version.json'),
    // 服务器端口 (默认 8080)
    port: parseInt(process.env.PORT) || 8080,
    // 服务器地址 (用于生成 version.json 中的 cdn_base_url)
    // 模拟器: 10.0.2.2, 真机: 电脑 IP, 本地: localhost
    serverHost: process.env.SERVER_HOST || '10.0.2.2',
    // 构建类型 (debug / release)
    buildType: process.env.BUILD_TYPE || 'debug',
    // 构建风味 (dev / prod)
    flavor: process.env.FLAVOR || 'dev',
    // 本地版本号（用于测试更新，不为空时优先使用）
    localVersionName: process.env.LOCAL_VERSION_NAME || null
};

const app = express();

// ABI 映射：APK 文件名中的 ABI -> 更新 API 期望的 ABI
const ABI_MAP = {
    'arm64-v8a': 'arm64-v8a',
    'armeabi-v7a': 'armeabi-v7a',
    'x86': 'x86',
    'x86_64': 'x86_64',
    'universal': 'universal'
};

/**
 * 从 output-metadata.json 读取版本信息
 */
function readVersionFromMetadata() {
    const metadataPath = path.join(
        CONFIG.apkOutputDir,
        CONFIG.buildType,
        'output-metadata.json'
    );

    if (!fs.existsSync(metadataPath)) {
        return null;
    }

    try {
        const metadata = JSON.parse(fs.readFileSync(metadataPath, 'utf8'));
        const element = metadata.elements[0];
        return {
            versionCode: element.versionCode,
            versionName: element.versionName
        };
    } catch (e) {
        console.error('读取 metadata 失败:', e.message);
        return null;
    }
}

/**
 * 从 build.gradle.kts 读取版本信息
 */
function readVersionFromGradle() {
    const gradlePath = path.join(CONFIG.projectRoot, 'app/build.gradle.kts');

    if (!fs.existsSync(gradlePath)) {
        return null;
    }

    try {
        const content = fs.readFileSync(gradlePath, 'utf8');
        const versionNameMatch = content.match(/versionName\s*=\s*"([^"]+)"/);
        const versionCodeMatch = content.match(/versionCode\s*=\s*(\d+)/);

        if (versionNameMatch) {
            return {
                versionName: versionNameMatch[1],
                versionCode: versionCodeMatch ? parseInt(versionCodeMatch[1]) : 1
            };
        }
    } catch (e) {
        console.error('读取 gradle 配置失败:', e.message);
    }
    return null;
}

/**
 * 获取当前版本信息
 */
function getCurrentVersion() {
    // 优先使用本地版本号（用于测试更新）
    if (CONFIG.localVersionName) {
        console.log(`  使用本地版本号: ${CONFIG.localVersionName} (来自 .env)`);
        return {
            versionName: CONFIG.localVersionName,
            versionCode: parseInt(CONFIG.localVersionName.split('.').join('')) || 9900
        };
    }

    // 优先从 metadata 读取（更准确）
    let version = readVersionFromMetadata();
    if (!version) {
        // 备用：从 gradle 读取
        version = readVersionFromGradle();
    }
    return version;
}

/**
 * 获取 APK 文件大小
 */
function getApkSize(filePath) {
    try {
        const stats = fs.statSync(filePath);
        return stats.size;
    } catch (e) {
        return 0;
    }
}

/**
 * 查找并准备 APK 文件
 */
function prepareApkFiles(versionName) {
    // 去掉 flavor 后缀，保持版本目录干净
    const cleanVersionName = versionName.replace(/-dev$/, '');
    const versionDir = path.join(CONFIG.apksDir, `v${cleanVersionName}`);

    // 确保版本目录存在
    if (!fs.existsSync(versionDir)) {
        fs.mkdirSync(versionDir, { recursive: true });
    }

    // 支持 Product Flavors: apk/{flavor}/{buildType} 或直接 apk/{buildType}
    let apkDir = path.join(CONFIG.apkOutputDir, CONFIG.buildType);
    if (CONFIG.flavor && fs.existsSync(path.join(CONFIG.apkOutputDir, CONFIG.flavor, CONFIG.buildType))) {
        apkDir = path.join(CONFIG.apkOutputDir, CONFIG.flavor, CONFIG.buildType);
    }

    const apkFiles = [];

    console.log(`  查找 APK 目录: ${apkDir}`);
    console.log(`  目录存在: ${fs.existsSync(apkDir)}`);

    if (fs.existsSync(apkDir)) {
        const files = fs.readdirSync(apkDir);
        console.log(`  找到 ${files.length} 个文件`);

        for (const file of files) {
            if (!file.endsWith('.apk')) continue;

            // 解析 APK 文件名: app-[flavor]-[abi]-[buildType].apk
            // 例如: app-dev-arm64-v8a-debug.apk 或 app-arm64-v8a-debug.apk
            // 支持带 flavor (app-dev-xxx) 和不带 flavor (app-xxx) 两种格式
            const match = file.match(/^app-(?:dev-)?(arm64-v8a|armeabi-v7a|x86|x86_64|universal)-(\w+)\.apk$/);
            if (!match) continue;

            let abi = match[1];
            const buildType = match[2];

            // 映射 ABI 名称
            const targetAbi = ABI_MAP[abi];
            if (!targetAbi) continue;

            // 目标文件名: jitterpay-[abi]-[version].apk
            // 例如: jitterpay-arm64-v8a-1.0.apk
            // 注意：去掉 flavor 后缀（如 -dev），保持版本号干净
            const targetFileName = `jitterpay-${targetAbi}-${cleanVersionName}.apk`;
            const targetPath = path.join(versionDir, targetFileName);
            const sourcePath = path.join(apkDir, file);

            // 复制/链接 APK 文件
            if (!fs.existsSync(targetPath)) {
                console.log(`  复制: ${file} -> ${targetFileName}`);
                try {
                    // 使用硬链接以节省空间（如果支持）
                    fs.linkSync(sourcePath, targetPath);
                } catch (e) {
                    // 如果硬链接失败，使用复制
                    fs.copyFileSync(sourcePath, targetPath);
                }
            }

            apkFiles.push({
                abi: targetAbi,
                fileName: targetFileName,
                size: getApkSize(targetPath),
                path: targetPath
            });
        }
    }

    return apkFiles;
}

/**
 * 生成 version.json
 */
function generateVersionJson(versionInfo, apkFiles) {
    // 去掉 flavor 后缀，保持版本号干净
    const cleanVersionName = versionInfo.versionName.replace(/-dev$/, '');

    // 计算 APK 总大小（取最大的一个）
    const totalSize = apkFiles.reduce((max, apk) => Math.max(max, apk.size), 0);

    const versionJson = {
        latest_version: `v${cleanVersionName}`,
        release_date: new Date().toISOString().split('T')[0],
        cdn_base_url: `http://${CONFIG.serverHost}:${CONFIG.port}`,
        apk_size: totalSize,
        apks: apkFiles.map(apk => ({
            abi: apk.abi,
            file_name: apk.fileName,
            size: apk.size
        })),
        generated_at: new Date().toISOString(),
        build_type: CONFIG.buildType
    };

    fs.writeFileSync(CONFIG.versionJsonPath, JSON.stringify(versionJson, null, 2));
    console.log(`\n已更新 version.json (版本: v${cleanVersionName})`);

    return versionJson;
}

/**
 * 更新服务器状态
 */
function updateServerState() {
    console.log('\n=== 检查更新 ===');

    const versionInfo = getCurrentVersion();
    const source = CONFIG.localVersionName ? '(来自 .env LOCAL_VERSION_NAME)' : '(来自 APK)';
    console.log(`当前版本: ${versionInfo.versionName} (code: ${versionInfo.versionCode}) ${source}`);

    // 检查是否有 APK 文件
    const apkFiles = prepareApkFiles(versionInfo.versionName);

    if (apkFiles.length === 0) {
        console.log('未找到 APK 文件，请先运行: npm run build');
        return null;
    }

    console.log(`找到 ${apkFiles.length} 个 APK 文件`);

    // 生成 version.json
    const versionJson = generateVersionJson(versionInfo, apkFiles);

    return versionJson;
}

// ==================== API 端点 ====================

// 版本检查 API
app.get('/version.json', (req, res) => {
    if (fs.existsSync(CONFIG.versionJsonPath)) {
        const versionJson = JSON.parse(fs.readFileSync(CONFIG.versionJsonPath, 'utf8'));
        res.json(versionJson);
    } else {
        res.status(404).json({ error: 'version.json not found. Run "npm run build" first.' });
    }
});

// APK 下载端点
app.get('/v:version/:fileName', (req, res) => {
    const { version, fileName } = req.params;
    const versionDir = path.join(CONFIG.apksDir, `v${version}`);

    // 安全检查：防止目录遍历
    const safeFileName = path.basename(fileName);
    const filePath = path.join(versionDir, safeFileName);

    if (fs.existsSync(filePath)) {
        res.download(filePath);
    } else {
        res.status(404).json({ error: 'APK not found' });
    }
});

// APK 列表 API
app.get('/apks', (req, res) => {
    if (!fs.existsSync(CONFIG.apksDir)) {
        return res.json({ versions: [] });
    }

    const versions = [];
    const dirs = fs.readdirSync(CONFIG.apksDir);

    for (const dir of dirs) {
        if (!dir.startsWith('v')) continue;
        const versionDir = path.join(CONFIG.apksDir, dir);
        if (!fs.statSync(versionDir).isDirectory()) continue;

        const files = fs.readdirSync(versionDir)
            .filter(f => f.endsWith('.apk'))
            .map(f => ({
                name: f,
                size: fs.statSync(path.join(versionDir, f)).size
            }));

        if (files.length > 0) {
            versions.push({
                version: dir,
                files: files
            });
        }
    }

    res.json({ versions: versions.sort() });
});

// 健康检查
app.get('/health', (req, res) => {
    res.json({
        status: 'ok',
        port: CONFIG.port,
        buildType: CONFIG.buildType
    });
});

// 静态文件服务
app.use('/static', express.static(CONFIG.apksDir));

// ==================== 初始化 ====================

function init() {
    // 确保目录存在
    if (!fs.existsSync(CONFIG.apksDir)) {
        fs.mkdirSync(CONFIG.apksDir, { recursive: true });
    }

    // 初始更新
    updateServerState();

    // 监听 APK 目录变化
    const watcher = chokidar.watch(CONFIG.apkOutputDir, {
        persistent: true,
        ignoreInitial: false
    });

    watcher.on('add', (filePath) => {
        if (filePath.endsWith('.apk')) {
            console.log(`\n检测到新 APK: ${path.basename(filePath)}`);
            updateServerState();
        }
    });

    watcher.on('change', (filePath) => {
        if (filePath.endsWith('.apk') || filePath.includes('output-metadata')) {
            console.log(`\nAPK 已更新: ${path.basename(filePath)}`);
            updateServerState();
        }
    });

    // 启动服务器
    app.listen(CONFIG.port, () => {
        const versionSource = CONFIG.localVersionName
            ? `测试版本: ${CONFIG.localVersionName}`
            : '版本: 来自 APK';
        console.log('\n╔════════════════════════════════════════════════╗');
        console.log('║      JitterPay 本地更新服务器已启动            ║');
        console.log('╠════════════════════════════════════════════════╣');
        console.log(`║  端口:       ${CONFIG.port}                               ║`);
        console.log(`║  服务器地址: http://${CONFIG.serverHost}:${CONFIG.port}              ║`);
        console.log(`║  构建类型:   ${CONFIG.buildType}                             ║`);
        console.log(`║  构建风味:   ${CONFIG.flavor || '自动检测'}                          ║`);
        console.log(`║  ${versionSource}                          ║`);
        console.log('╠════════════════════════════════════════════════╣');
        console.log(`║  版本检查:   http://${CONFIG.serverHost}:${CONFIG.port}/version.json    ║`);
        console.log(`║  APK 列表:   http://${CONFIG.serverHost}:${CONFIG.port}/apks              ║`);
        console.log('╠════════════════════════════════════════════════╣');
        console.log('║  命令:                                          ║');
        console.log('║    npm run build     - 构建 APK                ║');
        console.log('║    npm run watch     - 监听文件变化            ║');
        console.log('╚════════════════════════════════════════════════╝\n');
    });
}

// 导出供其他模块使用
module.exports = { app, updateServerState, CONFIG };

// 如果直接运行
if (require.main === module) {
    init();
}
