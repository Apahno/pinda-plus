package com.itheima.pinda.file.storage;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.itheima.pinda.file.domain.FileDeleteDO;
import com.itheima.pinda.file.entity.File;
import com.itheima.pinda.file.properties.FileServerProperties;
import com.itheima.pinda.file.strategy.impl.AbstractFileStrategy;
import com.itheima.pinda.utils.StrPool;
import io.minio.MinioClient;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rx.BackpressureOverflow;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Minio配置
 */
@Configuration
@Slf4j
@EnableConfigurationProperties(FileServerProperties.class)
@ConditionalOnProperty(name = "pinda.file.type", havingValue = "MINIO")
public class MinioAutoConfig {

    @Service
    public class MinioServiceImpl extends AbstractFileStrategy {
        MinioClient minioClient;
        private MinioClient buildClient() throws InvalidPortException, InvalidEndpointException {
            properties = fileProperties.getMinio();
            log.info("新建了一次minioClient");
            return new MinioClient(properties.getEndpoint(),properties.getAccessKeyId(),properties.getAccessKeySecret());
        }

        // 参数为 ip端口，账号。密码
        @Override
        protected void uploadFile(File file, MultipartFile multipartFile) throws Exception {
            if (minioClient==null) {
                minioClient = buildClient();
            }
            InputStream inputStream = multipartFile.getInputStream();
            String contentType = multipartFile.getContentType();
            // 生成文件名
            try {
                String bucketName = properties.getBucketName();
                // 校验存储桶是否存在
                boolean bucket = minioClient.bucketExists(bucketName);
                if (!bucket) {
                    minioClient.makeBucket("bucket");
                }
                minioClient.putObject(bucketName, file.getSubmittedFileName(), inputStream, contentType);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void delete(FileDeleteDO fileDeleteDO) throws Exception {
            if (minioClient==null) {
                minioClient = buildClient();
            }
            String bucketName = properties.getBucketName();
            try {
                minioClient.removeObject(bucketName, fileDeleteDO.getFileName());
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }
}
