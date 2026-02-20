# VideoPlayerMC 项目说明

## 项目概述

VideoPlayerMC 是一个 Minecraft Paper 服务器插件，允许玩家在游戏世界中使用混凝土方块播放视频。该插件通过将视频帧转换为 16 种混凝土颜色的方块矩阵，实现视频在 Minecraft 中的可视化播放。

### 核心功能
- 视频解码与处理：使用 FFmpeg 解码视频文件
- 视频预处理：支持预先生成视频帧数据，实现快速播放
- 颜色量化：将 RGB 颜色映射到 Minecraft 的 16 种混凝土颜色
- 方块渲染：在指定位置动态放置混凝土方块来显示视频帧
- 增量更新优化：仅更新颜色变化的方块，提升性能约 80%
- 无头模式支持：在无显示服务器的环境中运行

### 技术栈
- **语言**: Java 17
- **构建工具**: Maven
- **游戏平台**: Paper 1.20.4
- **视频处理**: JavaCV + FFmpeg 6.0 (ARM64 Linux 优化版本)
- **颜色量化**: 预计算查找表 (O(1) 时间复杂度)

### 项目结构
```
videoplayermc/
├── src/main/
│   ├── java/com/videoplayermc/
│   │   ├── VideoPlayerMC.java       # 主插件类
│   │   ├── PlayVideoCommand.java    # 直接播放视频命令（已弃用）
│   │   ├── PlayFileCommand.java     # 播放预处理视频命令
│   │   ├── ProcessVideoCommand.java # 预处理视频命令
│   │   ├── VideoManager.java        # 视频播放管理器
│   │   ├── VideoProcessor.java      # 视频处理（解码、缩放、量化）
│   │   ├── FrameSerializer.java     # 帧数据序列化工具
│   │   ├── BlockPlacer.java         # 方块放置器
│   │   ├── Palette.java             # 16色混凝土调色板
│   │   └── ColorQuantizer.java      # 颜色量化器
│   └── resources/
│       └── plugin.yml               # 插件配置文件
├── pom.xml                          # Maven 配置
└── target/                          # 构建输出目录
```

## 构建和运行

### 前置要求
- Java 17 或更高版本
- Maven 3.x
- Paper/Spigot 1.20.4 服务器

### 构建命令
```bash
mvn clean package
```

构建完成后，生成的 JAR 文件位于：
- `target/VideoPlayerMC-1.0.0.jar` (包含依赖的完整版本)
- `target/original-VideoPlayerMC-1.0.0.jar` (不含依赖的原始版本)

### 安装和使用
1. 将 `target/VideoPlayerMC-1.0.0.jar` 复制到 Paper 服务器的 `plugins` 目录
2. 重启服务器或加载插件
3. 在插件数据目录创建 `videos` 文件夹：`plugins/VideoPlayerMC/videos/`
4. 将视频文件放入 `videos` 文件夹
5. 在游戏中使用命令播放视频

#### 命令说明

**推荐方式：预处理后播放**
```bash
# 1. 预处理视频（生成帧数据文件）
/processvideo <视频文件名> [输出名称]
# 别名: /proc

# 2. 播放预处理后的视频
/playfile <输出名称> <x> <y> <z>
# 别名: /pf
```

**直接播放（已弃用，性能较差）**
```bash
/playvideo <视频文件名> <x> <y> <z>
# 别名: /pv
```

#### 示例
```bash
# 预处理视频
/processvideo test.mp4 myvideo

# 播放预处理后的视频
/playfile myvideo 100 64 200
```
这将在坐标 (100, 64, 200) 处开始播放 `myvideo` 视频。

### 权限配置
- `videoplayermc.play`: 允许使用播放视频命令（默认：true）
- `videoplayermc.process`: 允许预处理视频（默认：true）

## 开发规范

### 技术约束
- **禁止使用 OpenCV**: 项目禁止使用 OpenCV 进行图像处理
- **可使用 JavaCV**: 允许使用 JavaCV 进行视频解码和图像处理
- **禁止使用 AWT**: 由于无头模式限制，避免使用 java.awt.Graphics2D 等 AWT 组件

### 视频参数
- **目标分辨率**: 114 × 64 方块
- **目标帧率**: 20 FPS
- **颜色数量**: 16 种（混凝土颜色）

### 性能优化策略
1. **增量更新**: BlockPlacer 仅放置颜色变化的方块，减少约 80% 的方块操作
2. **预计算查找表**: ColorQuantizer 使用 16MB 的查找表实现 O(1) 颜色量化
3. **无物理更新**: 方块放置时不触发物理更新 (`false` 参数)
4. **帧缓存**: 保存上一帧状态用于增量比较

### 代码风格
- 包名使用小写字母：`com.videoplayermc`
- 类名使用 PascalCase：`VideoPlayerMC`, `PlayVideoCommand`
- 方法名使用 camelCase：`playVideo`, `processFrame`
- 常量使用 UPPER_SNAKE_CASE：`TARGET_WIDTH`, `TARGET_HEIGHT`
- 中文注释为主，关键逻辑添加详细说明

### 关键类说明

#### VideoPlayerMC
- 主插件类，继承自 `JavaPlugin`
- 在静态块中设置无头模式，避免 AWT 尝试连接 X11
- 创建视频目录和预处理数据目录
- 初始化 VideoManager 和 VideoProcessor
- 注册所有命令处理器

#### VideoManager
- 协调视频播放的时序控制
- 使用 BukkitScheduler 实现定时帧播放（每 tick 一帧，20 FPS）
- 管理播放状态和帧索引
- 支持从预处理的帧数据加载和播放

#### VideoProcessor
- 使用 FFmpegFrameGrabber 解码视频
- 将视频帧缩放到 114×64
- 调用 ColorQuantizer 进行颜色量化
- 按目标帧率提取帧（支持视频帧率下采样）
- 支持将处理后的帧数据保存为文件（用于快速播放）

#### FrameSerializer
- 帧数据序列化工具
- 负责将处理后的帧数据保存到二进制文件
- 支持从文件加载帧数据
- 使用自定义文件格式（Magic Number: 0x564D4652 "VMFR"）

#### PlayFileCommand
- 播放预处理视频的命令处理器
- 从 `processed` 目录加载帧数据文件
- 支持指定坐标进行播放

#### ProcessVideoCommand
- 预处理视频的命令处理器
- 调用 VideoProcessor 处理视频并保存帧数据
- 显示处理进度
- 异步处理，避免阻塞主线程

#### PlayVideoCommand
- 直接播放视频的命令处理器（已弃用）
- 实时解码和播放视频文件
- 性能较差，推荐使用预处理方式

#### BlockPlacer
- 负责将 Palette 数据转换为 Minecraft 方块
- 实现增量更新优化
- 支持清除所有方块

#### Palette
- 定义 16 种混凝土颜色的枚举
- 每种颜色包含对应的 Material 和 RGB 值

#### ColorQuantizer
- 使用加权欧氏距离计算颜色差异
- 预计算 16M 种 RGB 组合的查找表
- 考虑人眼对不同颜色的敏感度（绿色权重最高）

### 注意事项
1. 插件依赖 JavaCV 的 ARM64 Linux 版本，仅适用于 Linux ARM64 平台
2. 插件运行时设置无头模式，无需图形界面
3. 视频文件必须放入 `plugins/VideoPlayerMC/videos/` 目录
4. 预处理后的帧数据存储在 `plugins/VideoPlayerMC/processed/` 目录
5. 推荐使用预处理方式播放视频（`/processvideo` + `/playfile`），性能更佳
6. 直接播放命令（`/playvideo`）已弃用，实时解码性能较差
7. 播放新视频前会自动停止当前播放
8. 停止播放后保留最后一帧的方块显示

## 故障排除

### 视频播放失败
- 检查视频文件是否存在于 `videos` 目录
- 确认视频格式是否被 FFmpeg 支持
- 查看服务器日志获取详细错误信息

### 性能问题
- 降低视频分辨率或帧率
- 检查服务器性能（CPU 和 TPS）
- 减少同时播放的视频数量

### 颜色显示不准确
- ColorQuantizer 使用加权欧氏距离优化颜色映射
- 受限于 16 种混凝土颜色，无法完全还原原始颜色
- 可调整权重参数以获得更好的视觉效果

## 测试建议
- 测试不同格式的视频文件（MP4、AVI、MKV 等）
- 测试不同分辨率和帧率的视频
- 测试视频预处理功能的性能和正确性
- 测试预处理后视频的播放速度
- 对比直接播放和预处理播放的性能差异
- 测试长时间播放的内存占用
- 测试多人同时播放的性能表现
- 测试停止和清除功能
- 测试预处理数据的加载和保存