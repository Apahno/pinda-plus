package com.itheima.pinda.file.utils;

import cn.hutool.core.util.StrUtil;
import com.itheima.pinda.file.domain.FileDO;
import com.itheima.pinda.file.enumeration.DataType;
import com.itheima.pinda.utils.NumberHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 提供文件下载的公共方法
 */
@Component
@Slf4j
public class FileBiz {
    private static String buildNewFileName(String fileName,Integer order){
        // 同名文件加编号(1)
        return StrUtil.strBuilder(fileName).insert(fileName.lastIndexOf("."),"("+order+")").toString();

    }

    /**
     * 文件下载方法
     * @param fileDOList
     * @param request
     * @param response
     */
    public void down(List<FileDO> fileDOList, HttpServletRequest request, HttpServletResponse response) throws Exception {
        int fileSize = fileDOList.stream().filter(
                (file) -> file != null &&
                        !DataType.DIR.eq(file.getDataType()) &&
                        StringUtils.isNotEmpty(file.getUrl()))
                .mapToInt(
                        (file) -> NumberHelper.intValueOf0(file.getSize())).sum(); // 计算要下载的文件总大小

        // 确定要下载的文件名名称
        String downLoadFileName = fileDOList.get(0).getSubmittedFileName();
        if(fileDOList.size()>1){
            // 要下载多个文件，生成一个zip压缩文件
            downLoadFileName = StringUtils.substring(downLoadFileName,0,StringUtils.lastIndexOf(downLoadFileName,"."))+"等.zip";

        }
        // fileDoList ---> Map<String, String>
        LinkedHashMap<String, String> fileMap = new LinkedHashMap<>(fileDOList.size());
        // 处理下载文件名称重名的情况
        HashMap<String, Integer> duplicateFile = new HashMap<>(fileDOList.size());
        fileDOList.stream().filter((file)->file!=null&&!file.getDataType().eq(DataType.DIR)&&StringUtils.isNotEmpty(file.getUrl()))
                .forEach((file)->{
                    // 获得原始文件名
                    String submittedFileName = file.getSubmittedFileName();
                    if(fileMap.containsKey(submittedFileName)){
                        if(duplicateFile.containsKey(submittedFileName)){
                            // 将重复文件名的次数+1
                            duplicateFile.put(submittedFileName,duplicateFile.get(submittedFileName)+1);
                        }else {
                            duplicateFile.put(submittedFileName,1);
                        }
                        submittedFileName = buildNewFileName(submittedFileName,duplicateFile.get(submittedFileName));
                    }
                    fileMap.put(submittedFileName,file.getUrl());
                });
        ZipUtils.zipFilesByInputStream(fileMap,Long.valueOf(fileSize),downLoadFileName,request,response);
    }
}
