package com.tiv.rating.system.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.tiv.rating.system.common.BusinessException;
import com.tiv.rating.system.enums.BusinessCodeEnum;
import com.tiv.rating.system.service.FileStorageService;
import com.tiv.rating.system.service.UploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class UploadServiceImpl implements UploadService {

    /**
     * 图片存储目录
     */
    private static final String IMAGE_DIR = "image";

    /**
     * 支持的图片格式
     */
    private static final Set<String> SUPPORTED_IMAGE_TYPES = new HashSet<>(
            Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp"));

    /**
     * 存储目录的日期格式
     */
    private static final DateTimeFormatter DATE_PATH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    @Resource
    private FileStorageService fileStorageService;

    @Override
    public String uploadImage(MultipartFile file) {
        // 1. 校验图片
        String extension = validateImage(file);
        // 2. 生成对象名称
        String objectName = buildObjectName(IMAGE_DIR, extension);
        // 3. 上传至对象存储
        try (InputStream inputStream = file.getInputStream()) {
            String contentType = StrUtil.blankToDefault(file.getContentType(), "image/" + extension);
            return fileStorageService.upload(inputStream, file.getSize(), objectName, contentType);
        } catch (IOException e) {
            log.error("读取上传文件失败", e);
            throw new BusinessException(BusinessCodeEnum.SYSTEM_ERROR, "图片上传失败");
        }
    }

    /**
     * 校验图片文件
     *
     * @param file
     * @return 文件扩展名
     */
    private String validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(BusinessCodeEnum.PARAMS_ERROR, "上传图片不能为空");
        }
        String extension = FileUtil.extName(file.getOriginalFilename());
        if (StrUtil.isBlank(extension) || !SUPPORTED_IMAGE_TYPES.contains(extension.toLowerCase())) {
            throw new BusinessException(BusinessCodeEnum.PARAMS_ERROR, "不支持的图片格式");
        }
        return extension.toLowerCase();
    }

    /**
     * 生成对象名称: 按业务目录和日期分层, UUID重命名避免文件名冲突
     *
     * @param dir
     * @param extension
     * @return 对象名称
     */
    private String buildObjectName(String dir, String extension) {
        String datePath = LocalDate.now().format(DATE_PATH_FORMATTER);
        return String.format("%s/%s/%s.%s", dir, datePath, IdUtil.simpleUUID(), extension);
    }

}
