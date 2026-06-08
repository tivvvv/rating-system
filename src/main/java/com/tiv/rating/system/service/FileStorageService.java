package com.tiv.rating.system.service;

import java.io.InputStream;

public interface FileStorageService {

    /**
     * 上传文件
     *
     * @param inputStream
     * @param size
     * @param objectName
     * @param contentType
     * @return 文件访问地址
     */
    String upload(InputStream inputStream, long size, String objectName, String contentType);

}