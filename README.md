# VideoPlayerMC

> 在 Minecraft 中用混凝土方块播放视频的 Paper 插件

## 功能特性

- **视频播放**：将视频文件转换为 16 种颜色的混凝土方块矩阵
- **视频预处理**：支持预先生成视频帧数据，实现快速播放
- **增量更新**：仅更新颜色变化的方块，减少约 80% 的方块操作
- **无头模式**：支持在无显示服务器的环境中运行

## 环境要求

- Java 17 或更高版本
- Paper/Spigot 1.20.4 服务器


## 安装步骤

1. 构建插件：
   ```bash
   mvn clean package
   ```

2. 将 `target/VideoPlayerMC-1.0.0.jar` 复制到 Paper 服务器的 `plugins` 目录

3. 重启服务器或加载插件

4. 在插件数据目录创建 `videos` 文件夹：
   ```
   plugins/VideoPlayerMC/videos/
   ```

5. 将视频文件放入 `videos` 文件夹

## 使用方法

### 命令说明

#### 预处理视频（推荐）
```bash
/processvideo <视频文件名> [输出名称]
```
别名：`/proc`

示例：
```bash
/processvideo test.mp4 myvideo
```
会在 `plugins/VideoPlayerMC/processed/` 目录生成帧数据文件，处理过程中会显示进度。

#### 播放预处理的视频
```bash
/playfile <输出名称> <x> <y> <z>
```
别名：`/pf`

示例：
```bash
/playfile myvideo 100 64 200
```
这将在坐标 (100, 64, 200) 处播放 `myvideo` 视频。

#### 直接播放视频（已弃用）
```bash
/playvideo <视频文件名> <x> <y> <z>
```
别名：`/pv`

示例：
```bash
/playvideo test.mp4 100 64 200
```

**注意**：直接播放命令已弃用，实时解码性能较差，推荐使用预处理方式。

### 权限

- `videoplayermc.play`：允许播放视频（默认：true）
- `videoplayermc.process`：允许预处理视频（默认：true）

## 视频参数

- **目标分辨率**：114 × 64 方块
- **目标帧率**：20 FPS
- **颜色数量**：16 种（混凝土颜色）

## 免责声明

**本项目仅供学习和展示目的，不保证能够流畅运行。**

- ⚠️ **性能不稳定**：视频播放可能会对服务器性能产生较大影响，导致卡顿或延迟
- ⚠️ **平台限制**：仅在 Linux ARM64 平台测试通过，其他平台可能无法运行
- ⚠️ **功能限制**：受限于 Minecraft 方块数量和服务器性能，视频效果有限
- ⚠️ **不提供长期维护**：开发者不会经常维护或更新此项目
- ⚠️ **仅供展示**：项目主要用于技术演示，不建议在生产环境中使用
- ⚠️ **AI 生成**：本项目代码由 AI 辅助生成，**不保证代码的安全性或可靠性**

使用本插件可能对服务器稳定性产生负面影响，请谨慎使用。

## 技术栈

- Java 17
- Maven
- Paper API 1.20.4
- JavaCV + FFmpeg 6.0

## 许可证

MIT License