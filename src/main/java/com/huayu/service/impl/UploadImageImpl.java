package com.huayu.service.impl;

import com.huayu.dto.Result;
import com.huayu.service.IUploadImageService;
import com.huayu.utils.UploadUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 上传图片服务层实现类
 */
@Service
public class UploadImageImpl implements IUploadImageService {
    @Override
    public Result uploadImage(MultipartFile file) {
        //判断文件是否符合格式
        if (UploadUtil.isImage(file)) {
            try {
                String uploadImage = UploadUtil.uploadImage(file);
                return Result.ok("上传图片成功", uploadImage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return Result.fail("图片格式错误，上传图片失败");


    }
}
