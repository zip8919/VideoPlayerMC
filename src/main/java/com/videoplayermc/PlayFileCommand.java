package com.videoplayermc;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;

/**
 * 播放预处理视频命令处理器
 * 命令格式: /playfile <视频名称> <x> <y> <z>
 */
public class PlayFileCommand implements CommandExecutor {

    private final VideoPlayerMC plugin;
    private final VideoManager videoManager;

    public PlayFileCommand(VideoPlayerMC plugin, VideoManager videoManager) {
        this.plugin = plugin;
        this.videoManager = videoManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // 检查权限
        if (!sender.hasPermission("videoplayermc.play")) {
            sender.sendMessage("§c你没有权限使用此命令！");
            return true;
        }

        // 检查参数数量
        if (args.length != 4) {
            sender.sendMessage("§c用法: /playfile <视频名称> <x> <y> <z>");
            return true;
        }

        String videoName = args[0];
        String xStr = args[1];
        String yStr = args[2];
        String zStr = args[3];

        // 解析坐标
        int x, y, z;
        try {
            x = Integer.parseInt(xStr);
            y = Integer.parseInt(yStr);
            z = Integer.parseInt(zStr);
        } catch (NumberFormatException e) {
            sender.sendMessage("§c坐标必须是整数！");
            return true;
        }

        // 获取处理后的数据目录
        File processedDir = new File(plugin.getDataFolder(), "processed");
        if (!processedDir.exists()) {
            sender.sendMessage("§c处理后的视频目录不存在！");
            return true;
        }

        // 查找对应的帧图片目录
        File frameDir = new File(processedDir, videoName);
        if (!frameDir.exists() || !frameDir.isDirectory()) {
            sender.sendMessage("§c预处理视频不存在: " + videoName);
            sender.sendMessage("§7请先使用 /processvideo 命令处理视频");
            return true;
        }

        // 如果有正在播放的视频，先停止
        if (videoManager.isPlaying()) {
            sender.sendMessage("§e正在停止之前的播放...");
            videoManager.stop();
        }

        // 开始播放
        try {
            Player player = sender instanceof Player ? (Player) sender : null;
            Location location;

            if (player != null) {
                // 使用玩家的世界
                location = new Location(player.getWorld(), x, y, z);
            } else {
                // 控制台命令，无法确定世界
                sender.sendMessage("§c请在游戏中执行此命令！");
                return true;
            }

            videoManager.playFromImages(frameDir, location);
            sender.sendMessage("§a开始播放视频: " + videoName);
            sender.sendMessage("§7位置: (" + x + ", " + y + ", " + z + ")");
            sender.sendMessage("§7总帧数: " + videoManager.getTotalFrames());

        } catch (Exception e) {
            sender.sendMessage("§c播放视频失败: " + e.getMessage());
            plugin.getLogger().severe("播放视频失败: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }
}