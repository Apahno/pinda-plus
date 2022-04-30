package com.itheima.pinda.file.storage;

import com.itheima.pinda.file.domain.FileDeleteDO;
import com.itheima.pinda.file.entity.File;
import com.itheima.pinda.file.properties.FileServerProperties;
import com.itheima.pinda.file.strategy.impl.AbstractFileStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 本地上传策略配置类
 */
@Configuration  // 配置类一定要注解
@Slf4j  // 日志记录

// 用于加载配置文件里的属性类
@EnableConfigurationProperties(FileServerProperties.class)

// 配置类生效条件，基于某个属性，在配置文件里面配置
@ConditionalOnProperty(name = "pinda.file.type",havingValue = "LOCAL")  // type是LOCAL的时候配置文件生效
public class LocalAutoConfig {
    /**
     * 本地文件策略处理类
     */
    @Service
    public class LocalServiceImpl extends AbstractFileStrategy{

        /**
         * 文件上传
         * @param file
         * @param multipartFile
         * @throws Exception
         */
        @Override
        protected void uploadFile(File file, MultipartFile multipartFile) throws Exception {

        }

        /**
         * 文件删除
         * @param fileDeleteDO
         */
        @Override
        public void delete(FileDeleteDO fileDeleteDO) {

        }
    }
}
