package com.itheima.pinda.file.controller;

import com.itheima.pinda.base.BaseController;
import com.itheima.pinda.base.R;
import com.itheima.pinda.dozer.DozerUtils;
import com.itheima.pinda.file.dto.chunk.FileChunksMergeDTO;
import com.itheima.pinda.file.dto.chunk.FileUploadDTO;
import com.itheima.pinda.file.entity.File;
import com.itheima.pinda.file.manage.WebUploader;
import com.itheima.pinda.file.properties.FileServerProperties;
import com.itheima.pinda.file.service.FileService;
import com.itheima.pinda.file.strategy.FileStrategy;
import com.itheima.pinda.file.utils.FileDataTypeUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 分片上传
 */
@RestController
@RequestMapping("/chunk")
@Slf4j
@CrossOrigin
@Api(value = "分片上传",tags = "分片上传")
public class FileChunkController extends BaseController {
    @Autowired
    private FileStrategy fileStrategy;
    @Autowired
    private FileService fileService;
    @Autowired
    private FileServerProperties fileServerProperties;
    @Autowired
    private WebUploader webUploader;
    @Autowired
    private DozerUtils dozerUtils;

    @ApiOperation(value = "分片上传",notes = "分片上传")
    @PostMapping("/upload")
    // R<FileChunksMergeDTO> 为什么返回这个类型？是因为分片上传完之后要进行合并
    public R<FileChunksMergeDTO> uploadFile(
            @RequestParam(value = "file") MultipartFile multipartFile, FileUploadDTO fileUploadDTO) throws IOException {
        log.info("接收到分片："+multipartFile + "分片信息:"+fileUploadDTO);
        if(multipartFile == null || multipartFile.isEmpty()){
            log.error("分片上传文件为空");
            return this.fail("分片上传文件为空 ");
        }
        // 判断当前文件是否需要分片
        if(fileUploadDTO.getChunks()==null || fileUploadDTO.getChunks()<=0){
            // 当前文件上传无需分片，按照普通文件上传处理
            File file = fileStrategy.upload(multipartFile);
            file.setFileMd5(fileUploadDTO.getMd5());
            fileService.save(file);
            return this.success(null);
        }else {
            String storagePath = fileServerProperties.getStoragePath();
            // 获取配置的存放分片的临时目录
            String uploadFolder = FileDataTypeUtil.getUploadPathPrefix(storagePath);
            // 为需要上传的分片文件准备对应的存储位置
            java.io.File targetFile = webUploader.getReadySpace(fileUploadDTO, storagePath);

            if(targetFile==null){
                return this.fail("分片上传失败");
            }
            // 保存上传分片文件
            multipartFile.transferTo(targetFile);
            // 封装信息给前端，用于后面的分片合并
            FileChunksMergeDTO fileChunksMergeDTO = new FileChunksMergeDTO();
            fileChunksMergeDTO.setSubmittedFileName(multipartFile.getOriginalFilename());
            dozerUtils.map(fileUploadDTO,fileChunksMergeDTO);
        }

        return  null;
    }

}
