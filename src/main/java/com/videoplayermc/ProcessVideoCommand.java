package com.videoplayermc;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.File;

/**
 * 预处理视频命令处理器
 * 命令格式: /processvideo <视频文件名> [输出名称]
 */
public class ProcessVideoCommand implements CommandExecutor {

    private final VideoPlayerMC plugin;
    private final VideoProcessor videoProcessor;

    public ProcessVideoCommand(VideoPlayerMC plugin, VideoProcessor videoProcessor) {
        this.plugin = plugin;
        this.videoProcessor = videoProcessor;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 检查权限
        if (!sender.hasPermission("videoplayermc.process")) {
            sender.sendMessage("§c你没有权限使用此命令！");
            return true;
        }

        // 检查参数数量
        if (args.length < 1) {
            sender.sendMessage("§c用法: /processvideo <视频文件名> [输出名称]");
            return true;
        }

        String videoFileName = args[0];
        String outputName = args.length > 1 ? args[1] : videoFileName.substring(0, videoFileName.lastIndexOf('.'));

        // 获取视频文件路径
        File videoDir = new File(plugin.getDataFolder(), "videos");
        if (!videoDir.exists()) {
            videoDir.mkdirs();
        }

        File videoFile = new File(videoDir, videoFileName);
        if (!videoFile.exists()) {
            sender.sendMessage("§c视频文件不存在: " + videoFileName);
            sender.sendMessage("§7请将视频文件放入: " + videoDir.getAbsolutePath());
            return true;
        }

        // 创建处理后的数据目录
        File processedDir = new File(plugin.getDataFolder(), "processed");
        if (!processedDir.exists()) {
            processedDir.mkdirs();
        }

        sender.sendMessage("§e正在处理视频: " + videoFileName);
        sender.sendMessage("§7这可能需要一些时间，请稍候...");

        // 异步处理视频
        new Thread(() -> {
            try {
                long startTime = System.currentTimeMillis();

                // 使用进度监听器
                int frameCount = videoProcessor.saveFramesAsImages(
                    videoFile.getAbsolutePath(),
                    processedDir,
                    outputName,
                    (count, height) -> {
                        if (count % 10 == 0) { // 每10帧显示一次进度
                            sender.sendMessage("§7已处理 " + count + " 帧...");
                        }
                    }
                );

                long endTime = System.currentTimeMillis();
                long duration = (endTime - startTime) / 1000;

                plugin.getLogger().info("视频预处理完成: " + outputName + " (共 " + frameCount + " 帧, 耗时: " + duration + "秒)");
                sender.sendMessage("§a视频预处理完成！");
                sender.sendMessage("§7输出目录: " + outputName + "/");
                sender.sendMessage("§7帧数: " + frameCount);
                sender.sendMessage("§7耗时: " + duration + "秒");
                sender.sendMessage("§7使用命令播放: /playfile " + outputName + " <x> <y> <z>");

            } catch (Exception e) {
                plugin.getLogger().severe("视频预处理失败: " + e.getMessage());
                sender.sendMessage("§c视频预处理失败: " + e.getMessage());
                e.printStackTrace();
            }
        }, "VideoProcessor-Thread").start();

        return true;
    }
}