<h1 align="center">胶片坊 RecipeLab 中文汉化版</h1>

<p align="center">
索尼相机机内胶片配方应用 <b>Recipe Lab</b> 的简体中文汉化衍生版<br>
77 种胶片色彩配方，实时预览并写入相机，在所有拍摄模式长期生效
</p>

<p align="center">
<a href="https://github.com/voxivoid/recipe-lab-sony-pmca">上游原版项目</a>
·
<a href="https://github.com/voxivoid/recipe-lab-sony-pmca/releases">原版 Releases</a>
</p>

---

## 这是什么

**Recipe Lab（胶片坊）** 是一个运行在索尼相机本机的小型应用，内置 77 种色彩配方，可模拟其他相机与经典胶卷的色彩风格：富士胶片模拟、理光 GR 影像控制、徕卡、哈苏、佳能、尼康色彩，以及柯达、富士、CineStill、伊尔福等经典胶卷。

拨动拨轮，实时画面随之变化，按确定键写入相机；此后无论照片还是视频、任何拍摄模式，相机都会以该风格成像。关闭应用、关机重启后依然生效。

本仓库是 [voxivoid/recipe-lab-sony-pmca](https://github.com/voxivoid/recipe-lab-sony-pmca) 的简体中文汉化衍生版，界面全部中文化，并修复了相机固件字体缺字导致部分汉字显示为方框的问题。在A6500上验证OK无异常，可正常写入使用。

## 功能与配方

应用内配方按品牌分组：索尼、富士模拟、富士胶卷、柯达、电影卷、理光 GR、徕卡、哈苏、佳能/尼康、松下/奥巴、其他胶卷、伊尔福等，共 77 种。

- **CS 配方**：基于创意风格（Creative Style），可调整风格、饱和度、对比度、锐度与色彩矩阵
- **PE 配方**：基于照片效果（Picture Effect），仅在 JPEG 画质下生效
- 画质、白平衡、曝光补偿、DRO 等参数均可在应用内查看与调整
- 长按确定键可将配方加入收藏

完整配方说明见上游项目文档。

## 兼容性

应用本身不做机型限制，凡支持 PlayMemories 应用的索尼机型理论上均可安装。已确认可运行的机型包括：A6000、A6300、**A6500**、A5100、A7、A7 II、A7R、A7R II、RX100 V、NEX-5T 等。

> 判断方法：相机菜单中有 `MENU → 应用程序` 列表，即支持安装。2016 年底之后发布的机型（A6400、A7 III 及更新机型）固件已签名，无法安装任何 PlayMemories 应用。

## 安装方法

需要：相机、USB 数据线、存储卡、一台电脑（Windows / macOS / Linux）。

1. **下载安装工具 Sony-PMCA-RE**（作者 ma1co）：前往
   [Sony-PMCA-RE Releases](https://github.com/ma1co/Sony-PMCA-RE/releases)，
   Windows 用户下载 `pmca-gui.exe`，无需安装，直接运行。
2. **下载本应用 APK**：在本仓库的 [Releases](../../releases) 页面下载最新 APK 文件。
3. **设置相机**：开机，菜单中进入 `设置 → USB连接`，选择 **MTP**，用  micro usb（老机型基本上都是这个接口，按实际为准） 线连接电脑。
4. **安装**：运行 `pmca-gui.exe` → 选择 **Install app from file** → 选中下载的 APK → 等待完成。
5. 安装结束后拔线，关机再开机。应用位于 `MENU → 应用程序 → 应用程序列表`。

## 使用简介

| 按键 | 功能 |
|---|---|
| 拨轮 / 左右方向键 | 切换配方，实时画面立即变化 |
| Fn | 打开目录（左侧品牌、右侧lut） |
| 确定键（中央） | 应用当前配方并写入相机 |
| 长按确定键 | 收藏 / 取消收藏配方 |
| 上下方向键 | 在lut行与参数行之间切换 |
| AEL | 循环切换面板显示（完整面板 → 小标签 → 隐藏） |
| 删除键 | 载入出厂值 |
| MENU | 退出应用 |

应用配方后请**关机再开机**，风格即在所有模式下成为默认设置。

## 注意事项（安装前必读）

1. **签名不同，无法覆盖安装**：本汉化版签名与原版 / 其他汉化版不同，不能直接覆盖升级。安装前需先在相机上卸载旧版本，再全新安装。
2. **卸载会清除数据**：卸载应用会同时清除收藏列表与应用内配置，请先记录需要保留的内容。
3. **PE 配方需要 JPEG**：使用照片效果类配方时，画质必须为 JPEG；设为 RAW 或 RAW+JPEG 时相机会自动停用效果。
4. **部分配方超出菜单范围**：个别配方的饱和度超出菜单滑块的常规范围（±3），若手动触碰该滑块会回弹到常规范围，重新在应用内选择配方即可恢复。
5. **预览为临时效果**：应用内的实时预览在关闭应用后消失，只有按确定键写入的设置会保留。
6. 本汉化版已内置精简中文字体，不依赖相机固件字体；如界面仍出现方框，请确认安装的是本仓库最新 Release。

## 与上游版本的差异

- 界面全部简体中文化，照片效果等术语对齐索尼官方简体中文命名
- 内置精简中文字体（基于思源黑体 Noto Sans CJK SC，OFL 协议，仅收录应用实际用字），修复「徕」等汉字在相机固件字体下显示为方框的问题
- 收紧面板字体度量，底部信息面板占用面积与原版一致
- 未改动任何配方参数与核心功能逻辑

## 致谢

- 原应用作者：[voxivoid](https://github.com/voxivoid)，配方、逆向与应用本体
- PlayMemories 平台逆向：[ma1co](https://github.com/ma1co) 及 [Sony-PMCA-RE](https://github.com/ma1co/Sony-PMCA-RE)
- 内置字体：[Noto Sans CJK](https://fonts.google.com/noto/specimen/Noto+Sans+CJK)（SIL Open Font License 1.1）

## 许可证

本衍生版沿用上游 MIT 许可证。Recipe Lab 原项目 © voxivoid。
