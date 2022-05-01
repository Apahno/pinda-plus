package com.itheima.pinda.file.service;

import com.itheima.pinda.file.dto.AttachmentDTO;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

public interface AttachmentService {
    /**
     * 文件上传
     * @param file
     * @param bizId
     * @param bizType
     * @param isSingle
     * @param id
     * @return
     */
    public AttachmentDTO upload(
            MultipartFile file,
            Long bizId,
            String bizType,
            Boolean isSingle,
            Long id);
}
