package com.tiv.rating.system.service.impl;

import com.tiv.rating.system.common.BusinessException;
import com.tiv.rating.system.config.MinioProperties;
import com.tiv.rating.system.enums.BusinessCodeEnum;
import com.tiv.rating.system.service.FileStorageService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.InputStream;

/**
 * 基于Minio的对象存储实现
 */
@Slf4j
@Service
public class MinioFileStorageServiceImpl implements FileStorageService {

    @Resource
    private MinioClient minioClient;

    @Resource
    private MinioProperties minioProperties;

    @Override
    public String upload(InputStream inputStream, long size, String objectName, String contentType) {
        String bucket = minioProperties.getBucket();
        // 1. 上传至Minio
        try {
            ensureBucketExists(bucket);
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(inputStream, size, -1)
                    .contentType(contentType)
                    .build());
        } catch (Exception e) {
            log.error("文件上传失败, objectName: {}", objectName, e);
            throw new BusinessException(BusinessCodeEnum.SYSTEM_ERROR, "文件上传失败");
        }
        // 2. 拼接文件访问地址
        String url = String.format("%s/%s/%s", minioProperties.getEndpoint(), bucket, objectName);
        log.debug("文件上传成功, url: {}", url);
        return url;
    }

    /**
     * 确保存储桶存在, 不存在则创建
     *
     * @param bucket
     */
    private void ensureBucketExists(String bucket) throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            log.info("创建存储桶: {}", bucket);
        }
    }

}
