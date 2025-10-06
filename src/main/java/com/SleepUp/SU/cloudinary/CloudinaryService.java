package com.SleepUp.SU.cloudinary;

import com.SleepUp.SU.config.properties.AppProperties;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public CloudinaryService(AppProperties appProperties) {
        AppProperties.CloudinaryProperties cloudinaryProps = appProperties.getCloudinary();
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudinaryProps.getCloudName(),
                "api_key", cloudinaryProps.getApiKey(),
                "api_secret", cloudinaryProps.getApiSecret()));
    }

    public Map uploadFile(MultipartFile file, String folder) throws IOException {

        Map params = folder != null ? ObjectUtils.asMap("folder", folder) : ObjectUtils.emptyMap();
        return cloudinary.uploader().upload(file.getBytes(), params);
    }

    public void deleteFile(String publicId) throws IOException {
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }
}