package com.itheima.pinda.file.storage;

import cn.hutool.core.util.StrUtil;
import com.itheima.pinda.file.domain.FileDeleteDO;
import com.itheima.pinda.file.entity.File;
import com.itheima.pinda.file.properties.FileServerProperties;
import com.itheima.pinda.file.strategy.impl.AbstractFileStrategy;
import com.itheima.pinda.utils.DateUtils;
import com.itheima.pinda.utils.StrPool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

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

        private void buildClient(){
            /**
             * 获取上传至本地时的配置
             */
            properties = fileProperties.getLocal();
        }

        /**
         * 文件上传
         * @param file
         * @param multipartFile
         * @throws Exception
         */
        @Override
        protected void uploadFile(File file, MultipartFile multipartFile) throws Exception {
            String endpoint = properties.getEndpoint();
            String bucketName = properties.getBucketName();
            String uriPrefix = properties.getUriPrefix();

            // 使用uuid为文件生成新文件名
            String fileName = UUID.randomUUID().toString() + StrPool.DOT+ file.getExt();

            // 按日期分割文件夹
            // 日期目录
            String relativePath = Paths.get(LocalDate.now().format(DateTimeFormatter.ofPattern(DateUtils.DEFAULT_MONTH_FORMAT_SLASH))).toString();

            // 上传文件存储的绝对目录
            String absolutePath = Paths.get(endpoint, bucketName, relativePath).toString();

            // 目标输出文件
            java.io.File outFile= new java.io.File(Paths.get(absolutePath,fileName).toString());
            // 向目标文件写入数据
            FileUtils.writeByteArrayToFile(outFile,multipartFile.getBytes());

            String url = new StringBuilder(getUriPrefix())
                    .append(StrPool.SLASH)
                    .append(properties.getBucketName())
                    .append(StrPool.SLASH)
                    .append(relativePath)
                    .append(StrPool.SLASH)
                    .append(fileName)
                    .toString();
            //替换掉windows环境的\路径
            url = StrUtil.replace(url, "\\\\", StrPool.SLASH);
            url = StrUtil.replace(url, "\\", StrPool.SLASH);

            // 文件上传完成后还需要设置File对象的属性(url,filename,relativePath)
            file.setUrl(url);  // http://ip:port/oss-file-service/2022/05/xxx.doc
            file.setFilename(fileName);
            file.setRelativePath(relativePath);

        }

        /**
         * 文件删除
         * @param fileDeleteDO
         */
        @Override
        public void delete(FileDeleteDO fileDeleteDO) {
            String endpoint = properties.getEndpoint();
            String bucketName = properties.getBucketName();
            // 拼接要删除的文件的绝对磁盘路径
            String filePath =  Paths.get(endpoint,bucketName,fileDeleteDO.getRelativePath(),fileDeleteDO.getFileName()).toString();
            java.io.File file = new java.io.File(filePath);
            FileUtils.deleteQuietly(file);

        }
    }
}
