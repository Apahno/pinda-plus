package com.itheima.pinda.file.manage;

import com.itheima.pinda.file.dto.chunk.FileUploadDTO;
import com.itheima.pinda.log.annotation.SysLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * 封装分片操作的工具类，主要用于创建分片临时文件，分片存放目录
 */
@Component
@Slf4j
public class WebUploader {
    /**
     * 为分片上传创建对应的保存位置，同时还可以创建临时文件 .tmp
     * @param fileUploadDTO
     * @param path
     * @return
     */
    public File getReadySpace(FileUploadDTO fileUploadDTO, String path){
        boolean b = createFileFolder(path, false);
        if(!b){
            return null;
        }
        // 将分片文件保存到文件名对应的md5构成的目录中
        String fileFolder = fileUploadDTO.getName();
        if(fileFolder==null){
            return null;
        }
        path+="/"+fileFolder;
        // 创建临时文件和存放分片目录
        b = createFileFolder(path, true);
        if(!b){
            return null;
        }
        // 构造需要上传的分片文件对应的路径
        String newFileName = String.valueOf(fileUploadDTO.getChunk());
        return new java.io.File(path, newFileName);
    }

    public boolean createFileFolder(String file,boolean hasTmp){
        File tmpFile = new File(file);
        if(!tmpFile.exists()){
            // 不存在，直接创建目录
            try {
                tmpFile.mkdirs();
            }catch (Exception e){
                log.error("创建分片所在目录失败",e);
                return false;
            }
        }
        if (hasTmp){
            // 需要创建临时文件
            tmpFile = new File(file + ".tmp");
            if(tmpFile.exists()){
                // 临时文件已经存在，修改临时文件的最后更新时间为当前系统时间
                return tmpFile.setLastModified(System.currentTimeMillis());
            }else {
                // 临时文件
                try {
                    tmpFile.createNewFile(); // 创建文件
                } catch (Exception e) {
                    log.error("创建分片对应的临时文件失败");
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return true;
    }

}
