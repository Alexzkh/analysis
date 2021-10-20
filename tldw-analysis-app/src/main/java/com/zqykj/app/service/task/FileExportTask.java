package com.zqykj.app.service.task;

import com.zqykj.infrastructure.task.Task;
import com.zqykj.infrastructure.task.TaskStatus;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;

/**
 * @Description: 文件导出任务
 * @Author zhangkehou
 * @Date 2021/10/14
 */
public abstract class FileExportTask extends Task<File> {

    protected static final Logger logger = LoggerFactory.getLogger(FileExportTask.class);

    /**
     * 文件大小
     */
    private long fileSize;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件导出限制
     */
    private int exportCountLimit = 5000;


    public void downLoadAndClearFile(HttpServletResponse response) throws Exception {
        OutputStream out = null;
        File file = this.result;
        InputStream in = null;
        BufferedInputStream bin = null;
        if (file.isDirectory()) {
            return;
        }
        String originalFileName = file.getName();
        String fileName = originalFileName.substring(0, originalFileName.lastIndexOf("."));
        String type = originalFileName.substring(originalFileName.lastIndexOf("."));
        try {
            in = new FileInputStream(file);
            bin = new BufferedInputStream(in);
            response.setContentType("application/json;charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename*=UTF-8''" + URLEncoder.encode(fileName, "UTF-8") + type);
            out = response.getOutputStream();
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = bin.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            response.flushBuffer();
        } finally {
            if (in != null) {
                IOUtils.closeQuietly(in);
            }
            if (bin != null) {
                IOUtils.closeQuietly(bin);
            }
            if (out != null) {
                IOUtils.closeQuietly(out);
            }
            file.delete();
        }
    }

    @Override
    public void run() throws Exception {
        {
            long begin = System.currentTimeMillis();
            try {
                this.setStatus(TaskStatus.Executing);
                File file = exportData2LocalFile();
                this.setResult(file);
                this.setStatus(TaskStatus.Finish);
            } catch (Exception e) {
                logger.error("", e);
                this.setMessage(e.getMessage());
                this.setStatus(TaskStatus.Error);
                throw new RuntimeException(e);
            } finally {
                logger.info(" Export Data to excel time spent : {} ms, taskId:{}, fileName:{}",
                        (System.currentTimeMillis() - begin), getTaskId(), getFileName());
            }
        }
    }

    protected abstract File exportData2LocalFile();


    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    protected int getExportCountLimit() {
        return exportCountLimit;
    }
}
