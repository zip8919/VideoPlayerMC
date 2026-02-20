package com.videoplayermc;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitTask;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * 视频播放管理器
 * 负责协调视频播放的时序控制
 */
public class VideoManager {

    private final VideoPlayerMC plugin;

    private List<Palette[][]> frames;
    private List<File> frameFiles;
    private BlockPlacer blockPlacer;
    private BukkitTask playTask;
    private int currentFrameIndex;
    private boolean isPlaying;

    public VideoManager(VideoPlayerMC plugin) {
        this.plugin = plugin;
        this.currentFrameIndex = 0;
        this.isPlaying = false;
        // 初始化颜色量化器
        ColorQuantizer.initialize();
    }

    /**
     * 从图片文件夹播放视频
     *
     * @param frameDir 帧图片目录
     * @param location  播放起始位置
     * @throws Exception 如果加载失败
     */
    public void playFromImages(File frameDir, Location location) throws Exception {
        // 停止当前播放
        stop();

        // 检查目录是否存在
        if (!frameDir.exists() || !frameDir.isDirectory()) {
            throw new IllegalArgumentException("帧图片目录不存在: " + frameDir);
        }

        // 加载所有帧图片文件
        plugin.getLogger().info("正在加载帧图片: " + frameDir);
        frameFiles = new ArrayList<>();

        // 按文件名排序加载
        File[] files = frameDir.listFiles((dir, name) -> name.endsWith(".png"));
        if (files != null) {
            java.util.Arrays.sort(files, (f1, f2) -> {
                String n1 = f1.getName().replace("frame_", "").replace(".png", "");
                String n2 = f2.getName().replace("frame_", "").replace(".png", "");
                return Integer.compare(Integer.parseInt(n1), Integer.parseInt(n2));
            });
            frameFiles.addAll(java.util.Arrays.asList(files));
        }

        if (frameFiles.isEmpty()) {
            throw new IllegalArgumentException("没有找到帧图片文件");
        }

        plugin.getLogger().info("帧图片加载完成，共 " + frameFiles.size() + " 帧");

        // 创建方块放置器
        World world = location.getWorld();
        int width = VideoProcessor.getTargetWidth();
        int height = VideoProcessor.getTargetHeight();
        blockPlacer = new BlockPlacer(world, location, width, height);

        // 开始播放
        currentFrameIndex = 0;
        isPlaying = true;

        // 每tick（50ms）播放一帧，实现 20fps
        playTask = Bukkit.getScheduler().runTaskTimer(plugin, this::playNextImageFrame, 0, 1);

        plugin.getLogger().info("开始播放视频");
    }

    /**
     * 播放下一帧（从图片）
     */
    private void playNextImageFrame() {
        if (!isPlaying || frameFiles == null || currentFrameIndex >= frameFiles.size()) {
            // 播放结束
            stop();
            plugin.getLogger().info("视频播放完成");
            return;
        }

        // 加载并放置当前帧
        File frameFile = frameFiles.get(currentFrameIndex);
        try {
            BufferedImage image = ImageIO.read(frameFile);
            if (image != null) {
                Palette[][] frame = convertImageToPalette(image);
                blockPlacer.placeFrame(frame);
            } else {
                plugin.getLogger().warning("无法读取帧图片: " + frameFile.getName());
            }
        } catch (IOException e) {
            plugin.getLogger().warning("读取帧图片失败: " + frameFile.getName() + " - " + e.getMessage());
            e.printStackTrace();
        }

        // 移动到下一帧
        currentFrameIndex++;
    }

    /**
     * 将 BufferedImage 转换为 Palette 数组
     */
    private Palette[][] convertImageToPalette(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        Palette[][] frameData = new Palette[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                frameData[x][y] = ColorQuantizer.quantize(r, g, b);
            }
        }

        return frameData;
    }

    /**
     * 停止播放
     */
    public void stop() {
        if (playTask != null) {
            playTask.cancel();
            playTask = null;
        }

        isPlaying = false;
        currentFrameIndex = 0;

        // 不清除方块，保留最后一帧
    }

    /**
     * 完全清除播放区域
     */
    public void clear() {
        stop();
        if (blockPlacer != null) {
            blockPlacer.clear();
            blockPlacer = null;
        }
        frames = null;
        frameFiles = null;
    }

    /**
     * 检查是否正在播放
     */
    public boolean isPlaying() {
        return isPlaying;
    }

    /**
     * 获取当前帧索引
     */
    public int getCurrentFrameIndex() {
        return currentFrameIndex;
    }

    /**
     * 获取总帧数
     */
    public int getTotalFrames() {
        return frameFiles != null ? frameFiles.size() : 0;
    }

    /**
     * 获取方块放置器
     */
    public BlockPlacer getBlockPlacer() {
        return blockPlacer;
    }
}