package com.itheima.pinda.file.storage;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.itheima.pinda.file.domain.FileDeleteDO;
import com.itheima.pinda.file.entity.File;
import com.itheima.pinda.file.properties.FileServerProperties;
import com.itheima.pinda.file.strategy.impl.AbstractFileStrategy;
import com.itheima.pinda.utils.DateUtils;
import com.itheima.pinda.utils.StrPool;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 阿里云oss配置
 */
@Configuration
@Slf4j
@EnableConfigurationProperties(FileServerProperties.class)
@ConditionalOnProperty(name="pinda.file.type",havingValue = "ALI")
public class AliOssAutoConfig {
    /**
     * 阿里云oss文件策略处理类
     */
    @Service
    public class AliServiceImpl extends AbstractFileStrategy{
        /**
         * 构建阿里云OSS客户端
         * @return
         */
        private OSS buildClient() {
            properties = fileProperties.getAli();
            return new OSSClientBuilder().
                    build(properties.getEndpoint(),
                            properties.getAccessKeyId(),
                            properties.getAccessKeySecret());
        }

        /**
         * 上传文件
         * @param file
         * @param multipartFile
         * @throws Exception
         */
        @Override
        protected void uploadFile(File file, MultipartFile multipartFile) throws Exception {
            OSS client = buildClient();

            // 获得OSS空间名称
            String bucketName = properties.getBucketName();
            if (!client.doesBucketExist(bucketName)) {
                // 创建存储空间
                client.createBucket(bucketName);
            }
            // 生成文件名
            String fileName = UUID.randomUUID().toString() + StrPool.DOT + file.getExt();

            // Paths的话由于系统原因会变成2022\05 而不是2022/05
            String relativePath = LocalDate.now().format(DateTimeFormatter.ofPattern(DateUtils.DEFAULT_MONTH_FORMAT_SLASH));    //这样才会是这种/

            // 服务器绝对路径
            String relativeFileName = relativePath + StrPool.SLASH + fileName;
            // 防止有反斜杠
            relativeFileName = StrUtil.replace(relativeFileName, "\\\\", StrPool.SLASH);
            relativeFileName = StrUtil.replace(relativeFileName, "\\", StrPool.SLASH);

            //对象元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentDisposition("attachment;fileName=" +
                    file.getSubmittedFileName());
            metadata.setContentType(file.getContextType());

            //上传请求对象
            PutObjectRequest request =
                    new PutObjectRequest(bucketName, relativeFileName,
                            multipartFile.getInputStream(),
                            metadata);

            // 上传文件到阿里云oss空间
            PutObjectResult result = client.putObject(request);

            // 文件上传完成后，需要设置上传文件相关信息，用于保存到数据库
            log.info("result={}", JSONObject.toJSONString(result));

            String url = getUriPrefix() + StrPool.SLASH + relativeFileName;
            url = StrUtil.replace(url, "\\\\", StrPool.SLASH);
            url = StrUtil.replace(url, "\\", StrPool.SLASH);
            // 写入文件表
            file.setUrl(url);
            file.setFilename(fileName);
            file.setRelativePath(relativePath);

            file.setGroup(result.getETag());
            file.setPath(result.getRequestId());

            //关闭阿里云OSS客户端
            client.shutdown();
        }

        protected String getUriPrefix() {
            if (StringUtils.isNotEmpty(properties.getUriPrefix())) {
                return properties.getUriPrefix();
            } else {
                String prefix = properties.getEndpoint().contains("https://")?"https://":"http";

                return prefix + properties.getBucketName() + "." + properties.getEndpoint().replaceFirst(prefix,"");
            }
        }

        /**
         * 删除文件
         * @param fileDeleteDO
         */
        @Override
        public void delete(FileDeleteDO fileDeleteDO) {
            OSS client = buildClient();
            //获得OSS空间名称
            String bucketName = properties.getBucketName();
            // 删除文件
            client.deleteObject(bucketName, fileDeleteDO.getRelativePath() + StrPool.SLASH + fileDeleteDO.getFileName());
            //关闭阿里云OSS客户端
            client.shutdown();
        }
    }
}
