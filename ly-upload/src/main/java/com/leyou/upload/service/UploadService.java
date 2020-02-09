package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.upload.config.UploadProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * @author leoso
 * @create 2019-12-30 11:21
 */
@Service
@Slf4j
@EnableConfigurationProperties(UploadProperties.class)
public class UploadService {

    //private static final List<String> ALLOW_TYPES =Arrays.asList("image/bmp","image/jpg","image/png");

    @Autowired
    private FastFileStorageClient storageClient;

    @Autowired
    private UploadProperties prop;

    public String uploadImage(MultipartFile file) {
        try{
            //校验文件类型
            String contentType = file.getContentType();
            if(!prop.getAllowTypes().contains(contentType)){
                throw new LyException(ExceptionEnum.INVALID_IMAGE_TYPE);
            }
            //检验是不是图片
            BufferedImage image = ImageIO.read(file.getInputStream());
            if(image==null){
                throw new LyException(ExceptionEnum.INVALID_IMAGE_TYPE);
            }
            //保存文件
            String extension = StringUtils.substringAfterLast(file.getOriginalFilename(), ".");
            StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(),
                    extension, null);
            /*File destFile = new File("D:\\leyou\\upload",file.getOriginalFilename());
            file.transferTo(destFile);*/
            System.out.println(storePath.getFullPath());
            return prop.getBaseUrl()+storePath.getFullPath();
        }catch(IOException e){
            log.error("[上传失败] 图片上传失败");
            throw new LyException(ExceptionEnum.IMAGE_UPLOAD_ERROR);
        }
    }
}
