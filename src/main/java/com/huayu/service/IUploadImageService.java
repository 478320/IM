package com.huayu.service;

import com.huayu.dto.Result;
import org.springframework.web.multipart.MultipartFile;

/**
 * UploadImage服务层
 */
public interface IUploadImageService {

    Result uploadImage(MultipartFile file);
}
