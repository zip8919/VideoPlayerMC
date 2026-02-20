package com.videoplayermc;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * VideoPlayerMC 主插件类
 * 用于在 Minecraft 中使用混凝土方块播放视频
 */
public class VideoPlayerMC extends JavaPlugin {

    // 静态初始化块，在类加载时立即设置无头模式
    static {
        System.setProperty("java.awt.headless", "true");
    }

    private VideoManager videoManager;

    @Override
    public void onEnable() {
        // 设置无头模式，避免 AWT 尝试连接 X11 显示服务器
        System.setProperty("java.awt.headless", "true");

        // 创建视频目录
        File videoDir = new File(getDataFolder(), "videos");
        if (!videoDir.exists()) {
            videoDir.mkdirs();
            getLogger().info("创建视频目录: " + videoDir.getAbsolutePath());
        }

        // 创建处理后的视频目录
        File processedDir = new File(getDataFolder(), "processed");
        if (!processedDir.exists()) {
            processedDir.mkdirs();
            getLogger().info("创建处理后的视频目录: " + processedDir.getAbsolutePath());
        }

        // 初始化视频管理器
        videoManager = new VideoManager(this);
        VideoProcessor videoProcessor = new VideoProcessor();

        // 注册命令
        getCommand("playvideo").setExecutor(new PlayVideoCommand(this, videoManager));
        getCommand("processvideo").setExecutor(new ProcessVideoCommand(this, videoProcessor));
        getCommand("playfile").setExecutor(new PlayFileCommand(this, videoManager));

        getLogger().info("VideoPlayerMC 已启用！");
        getLogger().info("使用 /processvideo <视频文件> [名称] 预处理视频");
        getLogger().info("使用 /playfile <名称> <x> <y> <z> 播放预处理后的视频");
        getLogger().info("视频文件请放入: " + videoDir.getAbsolutePath());
    }

    @Override
    public void onDisable() {
        // 停止所有播放
        if (videoManager != null) {
            videoManager.stop();
        }

        getLogger().info("VideoPlayerMC 已禁用！");
    }

    /**
     * 获取视频管理器
     */
    public VideoManager getVideoManager() {
        return videoManager;
    }
}