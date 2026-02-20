package com.videoplayermc;

import org.bukkit.Material;

/**
 * 16种混凝土颜色的调色板
 * 每种颜色都有对应的 Minecraft 方块类型和 RGB 值
 */
public enum Palette {
    WHITE(Material.WHITE_CONCRETE, 219, 219, 219),
    ORANGE(Material.ORANGE_CONCRETE, 235, 120, 60),
    MAGENTA(Material.MAGENTA_CONCRETE, 200, 80, 180),
    LIGHT_BLUE(Material.LIGHT_BLUE_CONCRETE, 120, 200, 220),
    YELLOW(Material.YELLOW_CONCRETE, 250, 220, 60),
    LIME(Material.LIME_CONCRETE, 130, 220, 90),
    PINK(Material.PINK_CONCRETE, 230, 150, 170),
    GRAY(Material.GRAY_CONCRETE, 95, 95, 95),
    LIGHT_GRAY(Material.LIGHT_GRAY_CONCRETE, 170, 170, 170),
    CYAN(Material.CYAN_CONCRETE, 60, 180, 180),
    PURPLE(Material.PURPLE_CONCRETE, 140, 70, 180),
    BLUE(Material.BLUE_CONCRETE, 60, 100, 220),
    BROWN(Material.BROWN_CONCRETE, 130, 90, 60),
    GREEN(Material.GREEN_CONCRETE, 100, 160, 70),
    RED(Material.RED_CONCRETE, 210, 60, 60),
    BLACK(Material.BLACK_CONCRETE, 40, 40, 40);

    private final Material blockType;
    private final int r, g, b;

    Palette(Material blockType, int r, int g, int b) {
        this.blockType = blockType;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public Material getBlockType() {
        return blockType;
    }

    public int getR() {
        return r;
    }

    public int getG() {
        return g;
    }

    public int getB() {
        return b;
    }

    /**
     * 获取所有颜色
     */
    public static Palette[] getAllColors() {
        return values();
    }

    /**
     * 根据索引获取颜色
     */
    public static Palette getByIndex(int index) {
        Palette[] colors = getAllColors();
        if (index >= 0 && index < colors.length) {
            return colors[index];
        }
        return WHITE; // 默认返回白色
    }
}