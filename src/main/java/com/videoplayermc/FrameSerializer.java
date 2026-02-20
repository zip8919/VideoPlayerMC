package com.videoplayermc;

import java.io.*;

/**
 * 帧数据序列化工具
 * 用于将处理后的帧数据保存到文件和从文件加载
 */
public class FrameSerializer {

    private static final int MAGIC_NUMBER = 0x564D4652; // "VMFR" - VideoMC Frame
    private static final int VERSION = 1;

    /**
     * 保存帧数据到文件
     *
     * @param frames 帧数据列表
     * @param outputFile 输出文件
     * @throws IOException 如果写入失败
     */
    public static void saveFrames(java.util.List<Palette[][]> frames, File outputFile) throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)))) {
            // 写入文件头
            dos.writeInt(MAGIC_NUMBER);
            dos.writeInt(VERSION);

            // 写入帧数
            dos.writeInt(frames.size());

            // 写入宽度
            dos.writeInt(VideoProcessor.getTargetWidth());

            // 写入高度
            dos.writeInt(VideoProcessor.getTargetHeight());

            // 写入每一帧
            for (Palette[][] frame : frames) {
                saveFrame(dos, frame);
            }
        }
    }

    /**
     * 保存单帧数据
     */
    private static void saveFrame(DataOutputStream dos, Palette[][] frame) throws IOException {
        int width = frame.length;
        int height = frame[0].length;

        // 写入宽度和高度
        dos.writeInt(width);
        dos.writeInt(height);

        // 写入每个像素的颜色索引
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                dos.writeByte(frame[x][y].ordinal());
            }
        }
    }

    /**
     * 从文件加载帧数据
     *
     * @param inputFile 输入文件
     * @return 帧数据列表
     * @throws IOException 如果读取失败
     */
    public static java.util.List<Palette[][]> loadFrames(File inputFile) throws IOException {
        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(inputFile)))) {
            // 读取并验证文件头
            int magic = dis.readInt();
            if (magic != MAGIC_NUMBER) {
                throw new IOException("无效的帧数据文件格式");
            }

            int version = dis.readInt();
            if (version != VERSION) {
                throw new IOException("不支持的帧数据版本: " + version);
            }

            // 读取帧数
            int frameCount = dis.readInt();

            // 读取宽度和高度
            int width = dis.readInt();
            int height = dis.readInt();

            // 读取每一帧
            java.util.List<Palette[][]> frames = new java.util.ArrayList<>(frameCount);
            for (int i = 0; i < frameCount; i++) {
                frames.add(loadFrame(dis, width, height));
            }

            return frames;
        }
    }

    /**
     * 加载单帧数据
     */
    private static Palette[][] loadFrame(DataInputStream dis, int width, int height) throws IOException {
        Palette[][] frame = new Palette[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int colorIndex = dis.readByte() & 0xFF;
                frame[x][y] = Palette.getByIndex(colorIndex);
            }
        }

        return frame;
    }

    /**
     * 检查文件是否是有效的帧数据文件
     */
    public static boolean isValidFrameFile(File file) {
        if (!file.exists() || file.length() < 16) {
            return false;
        }

        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            int magic = dis.readInt();
            return magic == MAGIC_NUMBER;
        } catch (IOException e) {
            return false;
        }
    }
}