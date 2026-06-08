package com.tiv.rating.system.service;

import org.springframework.web.multipart.MultipartFile;

public interface UploadService {

    /**
     * 上传图片
     *
     * @param file
     * @return 图片访问地址
     */
    String uploadImage(MultipartFile file);

}