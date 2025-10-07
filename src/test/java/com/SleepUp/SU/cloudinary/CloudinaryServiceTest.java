package com.SleepUp.SU.cloudinary;

import com.SleepUp.SU.config.properties.AppProperties;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.cloudinary.utils.ObjectUtils;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CloudinaryServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @Mock
    private MultipartFile multipartFile;

    @Mock
    private AppProperties appProperties;

    @Mock
    private AppProperties.CloudinaryProperties cloudinaryProperties;

    private CloudinaryService cloudinaryService;

    @BeforeEach
    public void setUp() {

        when(appProperties.getCloudinary()).thenReturn(cloudinaryProperties);
        when(cloudinaryProperties.getCloudName()).thenReturn("testCloudName");
        when(cloudinaryProperties.getApiKey()).thenReturn("testApiKey");
        when(cloudinaryProperties.getApiSecret()).thenReturn("testApiSecret");

        cloudinaryService = new CloudinaryService(appProperties);
    }

    @Test
    public void testUploadFile_withFolder_shouldReturnUploadResult() throws IOException {
        byte[] fileBytes = "file content".getBytes();
        when(multipartFile.getBytes()).thenReturn(fileBytes);

        Map<String, Object> mockUploadResult = Map.of("public_id", "12345", "url", "http://test.url/image.jpg");

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(eq(fileBytes), any(Map.class))).thenReturn(mockUploadResult);

        setCloudinaryMock(cloudinaryService, cloudinary);

        Map result = cloudinaryService.uploadFile(multipartFile, "testFolder");

        verify(uploader, times(1)).upload(eq(fileBytes), any(Map.class));
        assertEquals(mockUploadResult, result);
    }

    @Test
    public void testUploadFile_withoutFolder_shouldReturnUploadResult() throws IOException {
        byte[] fileBytes = "file content".getBytes();
        when(multipartFile.getBytes()).thenReturn(fileBytes);

        Map<String, Object> mockUploadResult = Map.of("public_id", "12345", "url", "http://test.url/image.jpg");

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(eq(fileBytes), eq(ObjectUtils.emptyMap()))).thenReturn(mockUploadResult);

        setCloudinaryMock(cloudinaryService, cloudinary);

        Map result = cloudinaryService.uploadFile(multipartFile, null);

        verify(uploader, times(1)).upload(eq(fileBytes), eq(ObjectUtils.emptyMap()));
        assertEquals(mockUploadResult, result);
    }

    @Test
    public void testDeleteFile_shouldThrowIOException() throws Exception {
        String publicId = "abc123";

        when(cloudinary.uploader()).thenReturn(uploader);

        doThrow(new IOException("Failed to delete file")).when(uploader)
                .destroy(eq(publicId), eq(ObjectUtils.emptyMap()));

        setCloudinaryMock(cloudinaryService, cloudinary);
        assertThrows(IOException.class, () -> cloudinaryService.deleteFile(publicId));
    }

    private void setCloudinaryMock(CloudinaryService service, Cloudinary cloudinaryMock) {
        try {
            java.lang.reflect.Field cloudinaryField = CloudinaryService.class.getDeclaredField("cloudinary");
            cloudinaryField.setAccessible(true);
            cloudinaryField.set(service, cloudinaryMock);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}