package com.itheima.pinda.file.controller;

import com.itheima.pinda.base.BaseController;
import com.itheima.pinda.base.R;
import com.itheima.pinda.file.dto.AttachmentDTO;
import com.itheima.pinda.file.service.AttachmentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务附件处理控制器
 */
@RestController
@RequestMapping("/attachment")
@Slf4j
@Api(value = "附件",tags = "附件")
public class AttachmentController extends BaseController {
    //    注入接口
    @Autowired
    private AttachmentService attachmentService;

    /**
     * 文件上传
     * @param file
     * @param bizId
     * @param bizType
     * @param isSingle
     * @param id
     * @return
     */
    @ApiOperation(value = "附件上传",notes = "附件上传")
    @ApiImplicitParams({
            // 参数说明
            @ApiImplicitParam(name = "isSingle", value = "是否单文件", dataType = "boolean", paramType = "query"),
            @ApiImplicitParam(name = "id", value = "文件id", dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "bizId", value = "业务id", dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "bizType", value = "业务类型", dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "file", value = "附件", dataType = "MultipartFile", allowMultiple = true, required = true),
    })
    @PostMapping("/upload")
    public R<AttachmentDTO> upload(
            @RequestParam(value = "file")
            MultipartFile file,
            @RequestParam(value = "bizId",required = false)
            Long bizId,
            @RequestParam(value = "bizType",required = false)
            String bizType,
            @RequestParam(value = "isSingle",required = false, defaultValue = "false")
            Boolean isSingle,
            @RequestParam(value = "id",required = false)
            Long id
    ){
        // 判断上传文件是否为空
        if(file==null|| file.isEmpty()){
            return this.fail("请求中必须包含一个有效文件");
        }
        // 执行文件上传逻辑
        AttachmentDTO attachmentDTO = attachmentService.upload(file, bizId, bizType, isSingle, id);
        return this.success(attachmentDTO);
    }
}
