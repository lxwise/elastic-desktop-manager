package com.lxwise.elastic.utils;

import com.podigua.path.Paths;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.stream.Collectors;

/**
 * @author lstar
 * @create 2025-02
 * @description: 文件工具类
 */
public class FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    /**
     * 根路径
     *
     * @return {@link File}
     */
    public static File root() {
        File root = new File(Paths.appData());
        if (!root.exists()) {
            if (root.mkdirs()) {
                logger.info("创建目录成功");
            }
        }
        return root;
    }

    /**
     * 根路径
     *
     * @return {@link File}
     */
    public static File file(String file) {
        File result = new File(root(), file);
        if (!result.getParentFile().exists()) {
            result.getParentFile().mkdirs();
        }
        return result;
    }

    /**
     * 根路径
     *
     * @return {@link File}
     */
    public static void write(File file, String content) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根路径
     *
     * @return {@link File}
     */
    public static String read(File file) {
        if (file.exists()) {
            try (BufferedReader writer = new BufferedReader(new FileReader(file))) {
                return writer.lines().collect(Collectors.joining(System.lineSeparator()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    public static File guess(File folder, String filename) {
        File target = new File(folder, filename);
        if (!target.exists()) {
            return target;
        }
        String name = FilenameUtils.getBaseName(filename);
        int index = 1;
        while (true){
            File result = new File(folder, name+"-"+(index++)+"."+FilenameUtils.getExtension(filename));
            if (!result.exists()) {
                return result;
            }
        }

    }

    public static void copy(InputStream input, FileOutputStream output) {
        try {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = input.read(buffer)) != -1) {
                output.write(buffer, 0, len);
            }
        } catch (Exception e) {
            logger.error("复制失败", e);
        } finally {
            try {
                input.close();
                output.close();
            } catch (IOException e) {
                logger.error("关闭", e);
            }
        }
    }
}
