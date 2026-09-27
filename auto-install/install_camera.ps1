<#
========================================================================
 胶片坊 RecipeLab 中文汉化版 — 索尼相机自动安装脚本 (PowerShell)
========================================================================
 功能：
   1. 自动下载 Sony-PMCA-RE 命令行工具 pmca-console（官方逆向工具，GPL）
   2. 获取汉化 APK：优先使用本地已打包的 APK（离线可用），
      本地没有时才通过 GitHub API 下载最新 Release 的 APK
   3. 检测相机 USB 连接
   4. 检测相机上是否已安装旧版（签名不同需先卸载）
   5. 自动安装 APK 到相机

 前置条件：
   - 索尼相机（支持 PlayMemories Camera Apps 的老机型，如 A6500/A6000/A7 等）
   - USB 数据线连接相机，相机开机
   - 相机 USB 连接模式建议设为"MTP 模式"（见 README）
   - 需要管理员权限（pmca-console 通过 WPD 驱动访问相机 USB）

 用法：
   直接双击 install_camera.bat（会自动请求管理员权限），
   或手动运行：powershell -NoProfile -ExecutionPolicy Bypass -File install_camera.ps1
========================================================================
#>

# ---------- 管理员检查 + 自动提权（pmca-console 访问 USB 需要管理员权限）----------
if (-not ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)) {
    Write-Host "需要管理员权限才能访问相机 USB，正在请求提权 ..." -ForegroundColor Yellow
    Start-Process powershell -Verb RunAs -ArgumentList "-NoProfile -ExecutionPolicy Bypass -File `"$PSCommandPath`""
    exit
}

$ErrorActionPreference = "Continue"
$OutputEncoding = [System.Text.Encoding]::UTF8

# ---------- 路径与常量 ----------
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ToolsDir  = Join-Path $ScriptDir "tools"
$ApkDir    = Join-Path $ScriptDir "apk"
New-Item -ItemType Directory -Force $ToolsDir | Out-Null
New-Item -ItemType Directory -Force $ApkDir  | Out-Null

# 官方逆向工具发布信息
$ConsoleTag  = "v0.18"
$ConsoleUrl  = "https://github.com/ma1co/Sony-PMCA-RE/releases/download/$ConsoleTag/pmca-console-$ConsoleTag-win.exe"
$ConsolePath = Join-Path $ToolsDir "pmca-console.exe"

# 中文汉化项目
$RepoOwner = "wslzgf"
$RepoName  = "recipe-lab-sony-pmca-chinese"
$ReleaseApi = "https://api.github.com/repos/$RepoOwner/$RepoName/releases/latest"
$PackageName = "com.voxivoid.recipelab"

Write-Host ""
Write-Host "======== 胶片坊 RecipeLab 中文汉化版 · 相机自动安装 ========" -ForegroundColor Cyan
Write-Host ""

# ---------- 1. 下载 pmca-console（如不存在）----------
if (-not (Test-Path $ConsolePath)) {
    Write-Host "[1/5] 下载安装工具 pmca-console $ConsoleTag ..." -ForegroundColor Yellow
    try {
        Invoke-WebRequest -Uri $ConsoleUrl -OutFile $ConsolePath -UseBasicParsing -ErrorAction Stop
        Write-Host "      工具已下载: $([math]::Round((Get-Item $ConsolePath).Length / 1KB)) KB" -ForegroundColor Green
    } catch {
        Write-Host "      [错误] 下载安装工具失败: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "      请手动下载后放到: $ConsolePath" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "[1/5] 安装工具已存在: pmca-console $ConsoleTag" -ForegroundColor Green
}

# ---------- 2. 获取 APK（优先使用本地已打包的 APK，离线可用；没有时才联网下载）----------
Write-Host "[2/5] 获取汉化 APK ..." -ForegroundColor Yellow
$LocalApk = Get-ChildItem $ApkDir -Filter *.apk -ErrorAction SilentlyContinue | Select-Object -First 1
if ($LocalApk) {
    $ApkPath = $LocalApk.FullName
    Write-Host "      使用本地已打包的 APK: $($LocalApk.Name) ($([math]::Round($LocalApk.Length/1KB)) KB)" -ForegroundColor Green
    Write-Host "      （如需在线获取更新版，请先删除 apk 文件夹内的旧 APK 再重新运行）" -ForegroundColor DarkGray
} else {
    Write-Host "      本地无 APK，正在查询中文汉化项目最新 Release ..." -ForegroundColor Yellow
    $Release = $null
    try {
        $Release = Invoke-RestMethod -Uri $ReleaseApi -Headers @{Accept="application/vnd.github+json"} -TimeoutSec 30 -ErrorAction Stop
    } catch {
        Write-Host "      [错误] 查询 Release 失败: $($_.Exception.Message)" -ForegroundColor Red
        Write-Host "      （网络不可用或 GitHub API 限流。可手动下载 APK 放入 $ApkDir 目录后重试）" -ForegroundColor Red
        exit 1
    }

    $ApkAsset = $Release.assets | Where-Object { $_.name -like "*.apk" } | Select-Object -First 1
    if (-not $ApkAsset) {
        Write-Host "      [错误] 最新 Release ($($Release.tag_name)) 中未找到 APK 文件" -ForegroundColor Red
        exit 1
    }

    $ApkPath = Join-Path $ApkDir $ApkAsset.name
    Write-Host "      最新版本: $($Release.tag_name)  ($($Release.name))" -ForegroundColor Green
    Write-Host "      APK 文件: $($ApkAsset.name)"
    Write-Host "      下载 APK ..." -ForegroundColor Yellow
    try {
        Invoke-WebRequest -Uri $ApkAsset.browser_download_url -OutFile $ApkPath -UseBasicParsing -ErrorAction Stop
        Write-Host "      APK 已下载: $([math]::Round((Get-Item $ApkPath).Length/1KB)) KB" -ForegroundColor Green
    } catch {
        Write-Host "      [错误] 下载 APK 失败: $($_.Exception.Message)" -ForegroundColor Red
        exit 1
    }
}

# ---------- 3. 检测相机连接 ----------
Write-Host ""
Write-Host "------------------------------------------------------------" -ForegroundColor DarkGray
Write-Host " 请确认相机已满足以下条件：" -ForegroundColor White
Write-Host "   · 相机开机"
Write-Host "   · USB 数据线连接电脑"
Write-Host "   · 相机 USB 连接模式设为『MTP 模式』"
Write-Host "------------------------------------------------------------" -ForegroundColor DarkGray
Write-Host ""

Write-Host "[3/5] 检测相机 USB 连接 ..." -ForegroundColor Yellow
$connectOutput = ""
try { $connectOutput = (& $ConsolePath install 2>&1 | Out-String) } catch { }
$connectExit = $LASTEXITCODE
Write-Host $connectOutput

# 注意：pmca-console 未找到设备时退出码仍为 0，须同时解析输出文本
$connectFail = ($connectExit -ne 0) -or ($connectOutput -match "No devices found") -or ($connectOutput -match "(?i)\berror\b|\bfailed\b|not found")
if ($connectFail) {
    Write-Host ""
    Write-Host "      [错误] 未能连接相机。请检查：" -ForegroundColor Red
    Write-Host "       · USB 线是否插好、相机是否开机" -ForegroundColor Red
    Write-Host "       · 相机 USB 连接模式是否设为『MTP 模式』" -ForegroundColor Red
    Write-Host "       · 更换 USB 线 / USB 接口后再试" -ForegroundColor Red
    exit 1
}
Write-Host "      ✔ 相机连接成功" -ForegroundColor Green

# ---------- 4. 检测相机上是否已安装旧版 ----------
Write-Host "[4/5] 检查相机上是否已安装旧版应用 ..." -ForegroundColor Yellow
$infoOutput = ""
try { $infoOutput = (& $ConsolePath info 2>&1 | Out-String) } catch { }
if ($infoOutput -match "recipelab") {
    Write-Host "      [警告] 检测到相机上已安装 RecipeLab（$PackageName）" -ForegroundColor Yellow
    Write-Host "      本汉化版签名与原版/其他版本不同，无法直接覆盖安装。" -ForegroundColor Yellow
    Write-Host "      请在相机上先卸载旧版本（MENU → 应用程序 → 管理 → 删除），再继续。" -ForegroundColor Yellow
    Write-Host ""
    $ans = Read-Host "      请卸载后按 Y 继续，或按 N 退出 [Y/N]"
    if ($ans -notmatch "^[Yy]") { Write-Host "已取消安装。"; exit 0 }
} else {
    Write-Host "      未检测到已安装的旧版 RecipeLab，可直接安装" -ForegroundColor Green
}

# ---------- 5. 执行安装 ----------
Write-Host "[5/5] 正在安装 $([System.IO.Path]::GetFileName($ApkPath)) 到相机 ..." -ForegroundColor Yellow
Write-Host ""
Write-Host "      安装过程中请勿拔线或关闭相机电源 ..."
$installOutput = ""
try { $installOutput = (& $ConsolePath install -f $ApkPath 2>&1 | Out-String) } catch { }
$installExit = $LASTEXITCODE
Write-Host $installOutput

# 判定失败：退出码非0 或 输出含未找到设备/错误/失败（注意未找到设备时退出码仍为0）
$installFail = ($installExit -ne 0) -or ($installOutput -match "No devices found") -or ($installOutput -match "(?i)\berror\b|\bfailed\b|not found")
if (-not $installFail) {
    Write-Host ""
    Write-Host "============================================================" -ForegroundColor Green
    Write-Host "  ✔ 安装成功！" -ForegroundColor Green
    Write-Host "  接下来：拔下 USB 线，关机再开机。" -ForegroundColor White
    Write-Host "  打开 MENU → 应用程序 → 应用程序列表，即可看到『胶片坊』。" -ForegroundColor White
    Write-Host "============================================================" -ForegroundColor Green
} else {
    Write-Host ""
    Write-Host "      [错误] 安装失败。常见原因：" -ForegroundColor Red
    Write-Host "       · 相机上已有旧版且签名不同 → 请先在相机卸载再装" -ForegroundColor Red
    Write-Host "       · 相机 USB 连接模式不正确 → 设为『MTP 模式』" -ForegroundColor Red
    Write-Host "       · USB 线未连接 / 相机未开机" -ForegroundColor Red
    exit 1
}
