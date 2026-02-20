package com.videoplayermc;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 * 视频处理器
 * 使用 FFmpeg 解码视频并提取帧数据
 * 保存每一帧为 PNG 图片
 */
public class VideoProcessor {

    private static final int TARGET_WIDTH = 114;
    private static final int TARGET_HEIGHT = 64;
    private static final int TARGET_FPS = 20;

    private final Java2DFrameConverter frameConverter;

    static {
        // 设置无头模式，避免 X11 依赖
        System.setProperty("java.awt.headless", "true");
    }

    public VideoProcessor() {
        this.frameConverter = new Java2DFrameConverter();
        // 初始化颜色量化器
        ColorQuantizer.initialize();
    }

    /**
     * 处理视频文件并保存每一帧为 PNG 图片
     *
     * @param videoPath 视频文件路径
     * @param outputDir 输出目录
     * @param name 视频名称
     * @param progressListener 进度监听器（可选）
     * @return 保存的帧数量
     * @throws Exception 如果处理或保存失败
     */
    public int saveFramesAsImages(String videoPath, File outputDir, String name, ProgressListener progressListener) throws Exception {
        File videoFile = new File(videoPath);
        if (!videoFile.exists()) {
            throw new IllegalArgumentException("视频文件不存在: " + videoPath);
        }

        // 创建输出目录
        File frameDir = new File(outputDir, name);
        if (!frameDir.exists()) {
            frameDir.mkdirs();
        }

        int savedFrameCount = 0;

        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoFile)) {
            // 设置 FFmpeg 输出分辨率，让 FFmpeg 自动缩放
            grabber.setImageWidth(TARGET_WIDTH);
            grabber.setImageHeight(TARGET_HEIGHT);

            // 不设置像素格式，让 Java2DFrameConverter 自动处理
            // grabber.setPixelFormat(0);

            grabber.start();

            double videoFps = grabber.getVideoFrameRate();
            long frameInterval = (long) (videoFps / TARGET_FPS);

            // 如果视频帧率小于目标帧率，逐帧处理
            if (frameInterval < 1) {
                frameInterval = 1;
            }

            long sourceFrameCount = 0;
            Frame frame;

            while ((frame = grabber.grabImage()) != null) {
                // 按 20fps 提取帧
                if (sourceFrameCount % frameInterval == 0) {
                    // 保存当前帧为 PNG
                    File outputFile = new File(frameDir, String.format("frame_%05d.png", savedFrameCount));
                    saveFrameAsImage(frame, outputFile);
                    savedFrameCount++;

                    // 通知进度
                    if (progressListener != null) {
                        progressListener.onProgress(savedFrameCount, frame.imageHeight > 0 ? (int)frame.imageHeight : TARGET_HEIGHT);
                    }
                }
                sourceFrameCount++;
            }

            grabber.stop();
        }

        return savedFrameCount;
    }

    /**
     * 保存单帧为 PNG 图片
     */
    private void saveFrameAsImage(Frame frame, File outputFile) throws IOException {
        // 使用 Java2DFrameConverter 转换为 BufferedImage
        BufferedImage image = frameConverter.convert(frame);

        if (image != null) {
            // 转换为 RGB 格式，确保是彩色图片
            BufferedImage rgbImage = new BufferedImage(TARGET_WIDTH, TARGET_HEIGHT, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < TARGET_HEIGHT; y++) {
                for (int x = 0; x < TARGET_WIDTH; x++) {
                    int rgb = image.getRGB(x, y);
                    rgbImage.setRGB(x, y, rgb);
                }
            }

            // 垂直翻转图像
            BufferedImage flipped = new BufferedImage(TARGET_WIDTH, TARGET_HEIGHT, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < TARGET_HEIGHT; y++) {
                for (int x = 0; x < TARGET_WIDTH; x++) {
                    int flippedY = TARGET_HEIGHT - 1 - y;
                    int rgb = rgbImage.getRGB(x, flippedY);
                    flipped.setRGB(x, y, rgb);
                }
            }
            image = flipped;

            // 保存为 PNG
            ImageIO.write(image, "PNG", outputFile);
        }
    }

    /**
     * 进度监听器接口
     */
    public interface ProgressListener {
        void onProgress(int frameCount, int height);
    }

    /**
     * 获取目标宽度
     */
    public static int getTargetWidth() {
        return TARGET_WIDTH;
    }

    /**
     * 获取目标高度
     */
    public static int getTargetHeight() {
        return TARGET_HEIGHT;
    }

    /**
     * 获取目标帧率
     */
    public static int getTargetFps() {
        return TARGET_FPS;
    }
}