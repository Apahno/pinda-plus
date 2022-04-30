package com.itheima.pinda.file.storage;

import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.itheima.pinda.file.domain.FileDeleteDO;
import com.itheima.pinda.file.entity.File;
import com.itheima.pinda.file.properties.FileServerProperties;
import com.itheima.pinda.file.strategy.impl.AbstractFileStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * fastDfs配置
 */
@Configuration
@Slf4j
@EnableConfigurationProperties(FileServerProperties.class)
@ConditionalOnProperty(name = "pinda.file.type", havingValue = "FAST_DFS")
public class FastDfsAutoConfig {
    /**
     * fastDfs文件策略处理类
     */
    // 要加上@service注解否则不会被实例化
    @Service
    public class FastDfsServiceImpl extends AbstractFileStrategy {
        // 注入操作Fastdfs客户端对象
        @Autowired
        private FastFileStorageClient storageClient;

        /**
         * 上传文件
         *
         * @param file
         * @param multipartFile
         * @throws Exception
         */
        @Override
        protected void uploadFile(File file, MultipartFile multipartFile) throws Exception {
            // 调用FastDFS客户端对象将文件上传到服务器
            StorePath storePath = storageClient.uploadFile(multipartFile.getInputStream(), multipartFile.getSize(), file.getExt(), null);
            // 文件上传完成后需要设置文件的相关信息，用于保存到数据库
            file.setUrl(fileProperties.getInnerUriPrefix() + storePath.getFullPath());
            file.setGroup(storePath.getGroup());
            file.setPath(storePath.getPath());
        }

        @Override
        public void delete(FileDeleteDO fileDeleteDO) {
            // 调用客户端做删除
            storageClient.deleteFile(fileDeleteDO.getGroup(),fileDeleteDO.getPath());
        }
    }

}
