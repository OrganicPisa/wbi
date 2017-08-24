package com.broadcom.wbi.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;


@SuppressWarnings("resource")
public class FileUtil {

    public static void deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }

    public static void deleteDirectory2(String path) {
        File dir = new File(path);
        if (dir.exists()) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteFile(files[i].getPath());
            }
            dir.delete();
        }
    }

    public static void deleteDirectory(String path) {
        if (path != null) {
            File dir = new File(path);
            if (dir.exists()) {
                File[] files = dir.listFiles();
                for (int i = 0; i < files.length; i++) {
                    if (files[i].isFile()) {
                        deleteFile(files[i].getPath());
                    } else {
                        deleteDirectory(files[i].getPath());
                    }
                }
                dir.delete();
            }
        }
    }

    public static void copyFile(String in, String out) throws Exception {
        FileChannel inputChannel = new FileInputStream(in).getChannel();
        FileChannel outputChannel = new FileOutputStream(out).getChannel();
        outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        inputChannel.close();
        outputChannel.close();
    }

    public static void copyFolder(String in, String out) throws Exception {
        File newdir = new File(out);
        if (!newdir.exists()) {
            newdir.mkdir();
        }

        File orgdir = new File(in);
        File[] files = orgdir.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                copyFile(files[i].getPath(), newdir.getPath() + "\\" + files[i].getName());
            }
        }
    }

    public static String getPrefix(String fileName) {
        if (fileName == null) {
            return null;
        }
        int point = fileName.lastIndexOf(".");
        if (point != -1) {
            return fileName.substring(0, point);
        }
        return fileName;
    }

    public static String getFileSizeString(int fileSize) {
        int Mb = fileSize / 1024 / 1024;
        if (Mb > 0)
            return String.valueOf((fileSize / 1024 / 1024)) + "MB";
        else
            return String.valueOf((fileSize / 1024)) + "KB";
    }
}
