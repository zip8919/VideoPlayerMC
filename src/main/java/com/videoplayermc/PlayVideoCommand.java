package com.videoplayermc;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;

/**
 * 播放视频命令处理器
 * 命令格式: /playvideo <视频文件名> <x> <y> <z>
 * 注意: 此命令已弃用，请使用 /playfile 命令
 */
public class PlayVideoCommand implements CommandExecutor {

    private final VideoPlayerMC plugin;

    public PlayVideoCommand(VideoPlayerMC plugin, VideoManager videoManager) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage("§c/playvideo 命令已弃用！");
        sender.sendMessage("§7请先使用 /processvideo 处理视频，然后使用 /playfile 播放");
        sender.sendMessage("§7用法:");
        sender.sendMessage("§7  1. /processvideo <视频文件名> [名称] - 预处理视频");
        sender.sendMessage("§7  2. /playfile <名称> <x> <y> <z> - 播放预处理后的视频");
        return true;
    }
}