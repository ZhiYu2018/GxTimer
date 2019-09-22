package com.gexiang.io;

import java.io.File;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import com.gexiang.Util.Helper;
import com.google.common.io.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CDRWriter {
    private static Logger logger = LoggerFactory.getLogger(CDRWriter.class);
    private static final long MAX_FILE_SIZE = 1024*1024*2L;

    private static class LazyHodler{
        private final static CDRWriter INSTANCE = new CDRWriter();
    }

    private String dir;
    private MappedByteBuffer mappedByteBuffer;
    private long offset;
    private CDRWriter(){
        offset = 0;
    }

    public static CDRWriter getInstance(){
        return LazyHodler.INSTANCE;
    }

    public void init(String dir){
        this.dir = dir;
        try {
            Files.createParentDirs(new File(dir + "/child"));
        }catch (Throwable throwable){
            logger.error("Create dir exceptions:{}", throwable.getMessage());
        }
    }

    public void dump(){
        synchronized (this){
            close();
        }
    }

    public void close() {
        if(mappedByteBuffer != null){
            mappedByteBuffer.force();
            mappedByteBuffer.clear();
            Helper.umap(mappedByteBuffer);
            mappedByteBuffer = null;
        }
    }

    public void write(String jobType, String appId, String jobId, String status, String body){
        StringBuilder sb = new StringBuilder();
        sb.append(jobType).append("\t").append(appId).append("\t").append(jobId).append("\t");
        sb.append(status).append("\t").append(body).append("\n");
        byte[] content = sb.toString().getBytes();
        synchronized (this){
            if(((offset + content.length) >= MAX_FILE_SIZE) && (mappedByteBuffer == null)){
                open();
            }
            if(mappedByteBuffer != null){
                mappedByteBuffer.put(content);
            }
        }
    }

    private void open(){
        if(mappedByteBuffer != null){
            close();
        }

        String file = dir + Helper.getDateStr(System.currentTimeMillis()) + ".cdr";
        try {
            mappedByteBuffer = Files.map(new File(file), FileChannel.MapMode.READ_WRITE, MAX_FILE_SIZE);
        }catch (Throwable t){
            logger.error("Open file:{} exceptions:{}", file, t.getMessage());
        }
    }
}
