package com.huayu.controller;

import com.huayu.dto.Result;
import com.huayu.service.IUploadImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * uploadImage表现层对象
 */
@RestController
@RequestMapping("/upload")
public class UploadImageController {

    @Autowired
    private IUploadImageService uploadImageService;

    /**
     * 上传图片
     *
     * @param file 要上传的图片
     * @return 图片的地址信息
     */
    @PostMapping("/add")
    public Result uploadImage(@RequestBody MultipartFile file) {
        return uploadImageService.uploadImage(file);
    }
}
