package com.itheima.pinda.file.service.impl;

import com.baomidou.mybatisplus.core.toolkit.ArrayUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.pinda.base.id.IdGenerate;
import com.itheima.pinda.database.mybatis.conditions.Wraps;
import com.itheima.pinda.dozer.DozerUtils;
import com.itheima.pinda.file.dao.AttachmentMapper;
import com.itheima.pinda.file.domain.FileDeleteDO;
import com.itheima.pinda.file.dto.AttachmentDTO;
import com.itheima.pinda.file.entity.Attachment;
import com.itheima.pinda.file.entity.File;
import com.itheima.pinda.file.properties.FileServerProperties;
import com.itheima.pinda.file.service.AttachmentService;
import com.itheima.pinda.file.strategy.FileStrategy;
import com.itheima.pinda.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AttachmentServiceImpl extends ServiceImpl<AttachmentMapper,Attachment> implements AttachmentService {
    @Autowired
    private FileStrategy fileStrategy;
    // 生成业务id
    @Autowired
    private IdGenerate<Long> idGenerate;
    @Autowired
    private DozerUtils  dozerUtils;
    @Autowired
    private FileServerProperties properties;

    /**
     * 文件上传
     * @param multipartFile
     * @param bizId
     * @param bizType
     * @param isSingle
     * @param id
     * @return
     */
    @Override
    public AttachmentDTO upload(MultipartFile multipartFile, Long bizId, String bizType, Boolean isSingle, Long id) {
        String bizIdStr = String.valueOf(bizId);
        // 判断bizId是否为空，如果为空需要产生业务Id
        if(bizId==null){
            bizIdStr = idGenerate.generate().toString();
        }
        // 调用策略处理对象实现真正文件上传
        File file = fileStrategy.upload(multipartFile);

        //对象转换
        Attachment attachment = dozerUtils.map(file, Attachment.class);
        attachment.setBizId(bizIdStr);
        attachment.setBizType(bizType);
        LocalDateTime now = LocalDateTime.now();
        attachment.setCreateMonth(DateUtils.formatAsYearMonth(now));
        attachment.setCreateDay(DateUtils.formatAsDateEn(now));
        attachment.setCreateWeek(DateUtils.formatAsYearWeekEn(now));

        // 判断当前业务是否是单一文件
        if(isSingle){
            // 需要将当前业务下其他的文件信息从数据库删除
            super.remove(Wraps.<Attachment>lbQ().eq(Attachment::getBizId,bizIdStr).eq(Attachment::getBizType,bizType));
        }

        // 完成文件上传后将文件信息保存到数据库
        if(id!=null && id>0){
            attachment.setId(id);
            // 执行数据库修改操作
            super.updateById(attachment);
        }else {
            // 执行数据库新增操作
            attachment.setId(idGenerate.generate());
            super.save(attachment);
        }

        return dozerUtils.map(attachment,AttachmentDTO.class);
    }

    /**
     * 刪除附件
     * @param ids
     */
    @Override
    public void remove(Long[] ids) {
        if(ArrayUtils.isEmpty(ids))
        {
            return;
        }
        // 查询文件路径
        // select * from pd_attachment where id in
        List<Attachment> list = super.list(Wrappers.<Attachment>lambdaQuery().in(Attachment::getId, ids));
        // 从数据库中删除文件信息记录
        super.removeByIds(Arrays.asList(ids));

        // 对象格式转换处理
        // list.stream.map((xx)-> ,意思是把list转成流，map就是把集合里面的每个元素取出来做加工
        List<FileDeleteDO> fileDeleteDOList = list.stream().map((fi)->
            FileDeleteDO.builder().relativePath(fi.getRelativePath())
                    .fileName(fi.getFilename())
                    .group(fi.getGroup())
                    .path(fi.getPath())
                    .build()
        ).collect(Collectors.toList());

        // 删除文件
        fileStrategy.delete(fileDeleteDOList);
    }

    /**
     * 根据业务类型或者业务Id删除附件
     * @param bizId
     * @param bizType
     */
    @Override
    public void removeByBizIdAndBizType(String bizId, String bizType) {
        //根据业务类和业务id查询数据库
        List<Attachment> list = super.list(
                Wraps.<Attachment>lbQ()
                        .eq(Attachment::getBizId, bizId)
                        .eq(Attachment::getBizType, bizType));
        if (list.isEmpty()) {
            return;
        }

        //根据id删除文件
        remove(list.stream().mapToLong(
                Attachment::getId).boxed().toArray(Long[]::new));
    }
}
