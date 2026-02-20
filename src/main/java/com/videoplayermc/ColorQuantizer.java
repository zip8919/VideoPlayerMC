package com.videoplayermc;

/**
 * 颜色量化器
 * 将 RGB 颜色映射到最近的混凝土颜色
 */
public class ColorQuantizer {

    // 预计算的颜色查找表，将 RGB 映射到对应的 Palette 索引
    private static final byte[] colorLookupTable = new byte[256 * 256 * 256];
    private static boolean initialized = false;

    /**
     * 初始化颜色查找表
     * 预计算所有可能的 RGB 值到最近混凝土颜色的映射
     */
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }

        Palette[] colors = Palette.getAllColors();

        // 遍历所有可能的 RGB 组合（256^3 = 16,777,216 种组合）
        for (int r = 0; r < 256; r++) {
            for (int g = 0; g < 256; g++) {
                for (int b = 0; b < 256; b++) {
                    int index = (r << 16) | (g << 8) | b;
                    colorLookupTable[index] = (byte) findClosestColorIndex(r, g, b, colors);
                }
            }
        }

        initialized = true;
    }

    /**
     * 使用预计算的查找表量化颜色
     * 性能优化：O(1) 时间复杂度
     */
    public static Palette quantize(int r, int g, int b) {
        if (!initialized) {
            initialize();
        }

        // 确保 RGB 值在有效范围内
        r = clamp(r, 0, 255);
        g = clamp(g, 0, 255);
        b = clamp(b, 0, 255);

        int index = (r << 16) | (g << 8) | b;
        return Palette.getByIndex(colorLookupTable[index] & 0xFF);
    }

    /**
     * 找到最近的混凝土颜色索引
     * 使用欧氏距离计算颜色差异
     */
    private static int findClosestColorIndex(int r, int g, int b, Palette[] colors) {
        int closestIndex = 0;
        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < colors.length; i++) {
            Palette color = colors[i];
            double distance = calculateDistance(r, g, b, color.getR(), color.getG(), color.getB());

            if (distance < minDistance) {
                minDistance = distance;
                closestIndex = i;
            }
        }

        return closestIndex;
    }

    /**
     * 计算两个 RGB 颜色之间的欧氏距离
     * 考虑人眼对不同颜色的敏感度，给予绿色更高的权重
     */
    private static double calculateDistance(int r1, int g1, int b1, int r2, int g2, int b2) {
        double dr = r1 - r2;
        double dg = g1 - g2;
        double db = b1 - b2;

        // 人眼对绿色最敏感，对蓝色最不敏感
        // 使用加权欧氏距离提高颜色映射的准确性
        return Math.sqrt(2.0 * dr * dr + 4.0 * dg * dg + 3.0 * db * db);
    }

    /**
     * 将值限制在指定范围内
     */
    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * 检查是否已初始化
     */
    public static boolean isInitialized() {
        return initialized;
    }
}