package com.huayu.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.apache.commons.io.FilenameUtils;
import org.apache.tika.Tika;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 上传工具类
 */
public class UploadUtil {
    //阿里域名
    public static final String ALI_DOMAIN = "https://huayu-cooperate.oss-cn-guangzhou.aliyuncs.com/";


    public static String uploadImage(MultipartFile file) throws IOException {
        //生成文件名
        String originalFilename = file.getOriginalFilename();
        String ext = "." + FilenameUtils.getExtension(originalFilename);
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String fileName = uuid + ext;
        //地域节点
        String endpoint = ConstantPropertiesUtils.END_POINT;
        String accessKeyId = ConstantPropertiesUtils.ACCESS_KEY_ID;
        String accessKeySecret = ConstantPropertiesUtils.ACCESS_KEY_SECRET;
        //OSS客户端对象
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        ossClient.putObject(
                "huayu-cooperate",
                fileName,
                file.getInputStream()
        );
        ossClient.shutdown();
        return ALI_DOMAIN + fileName;
    }

    public static boolean isImage(MultipartFile file) {
        Tika tika = new Tika();
        try {
            String fileContentType = tika.detect(file.getInputStream());
            return fileContentType.startsWith("image/");
        } catch (IOException e) {
            return false;
        }
    }
}
