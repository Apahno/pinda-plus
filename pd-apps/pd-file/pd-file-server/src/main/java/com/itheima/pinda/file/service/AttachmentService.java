package com.itheima.pinda.file.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.pinda.base.R;
import com.itheima.pinda.file.dto.AttachmentDTO;
import com.itheima.pinda.file.dto.AttachmentResultDTO;
import com.itheima.pinda.file.dto.FilePageReqDTO;
import com.itheima.pinda.file.entity.Attachment;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

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

    /**
     * 根据业务类型/业务Id下载
     * @param request
     * @param response
     * @param bizTypes
     * @param bizIds
     */
    public void downloadByBiz(HttpServletRequest request, HttpServletResponse response, String[] bizTypes, String[] bizIds) throws Exception;

    IPage<Attachment> page(Page<Attachment> page, FilePageReqDTO data);

    List<AttachmentResultDTO> find(String[] bizTypes, String[] bizIds);
}
