package com.itheima.pinda.file.service;

import com.itheima.pinda.file.dto.AttachmentDTO;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

    /**
     * 刪除附件
     * @param ids
     */
    public void remove(Long[] ids);

    /**
     * 根据业务类型或者业务Id删除附件
     * @param bizId
     * @param bizType
     */
    void removeByBizIdAndBizType(String bizId, String bizType);

    public void download(HttpServletRequest request, HttpServletResponse response,Long[] ids) throws Exception;
}
