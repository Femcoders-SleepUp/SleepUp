package com.SleepUp.SU.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;

@ActiveProfiles("tests")
@ExtendWith(MockitoExtension.class)
public class CloudinaryServiceTest {

    private CloudinaryService cloudinaryService;
    private Cloudinary cloudinary;
    private Uploader uploader;

    @BeforeEach
    void setUp() {
        cloudinary = Mockito.mock(Cloudinary.class);
        uploader = Mockito.mock(Uploader.class);
        cloudinaryService = new CloudinaryService(cloudinary);
    }

    @Nested
    class uploadFile {

        @Test
        void uploadFile_success() throws IOException {
            MockMultipartFile image =  new MockMultipartFile(
                    "image",
                    "test-image.jpg",
                    "image/jpeg",
                    "Test Image Content".getBytes()
            );

            Map<String, String> response = new HashMap<>();
            response.put("secure_url", "http://cloudinary.com/image/upload/example.jpg");

            Mockito.when(cloudinary.uploader()).thenReturn(uploader);
            Mockito.when(uploader.upload(any(), anyMap())).thenReturn(response);

            Map result = cloudinaryService.uploadFile(image);

            assertEquals("http://cloudinary.com/image/upload/example.jpg", result.get("secure_url"));
        }
    }

    @Nested
    class deleteFile {
        @Test
        void deleteFile_success() throws IOException {

            Map<String, String> response = new HashMap<>();
            response.put("result", "ok");

            Mockito.when(cloudinary.uploader()).thenReturn(uploader);
            Mockito.when(uploader.destroy(eq("image"), anyMap())).thenReturn(response);

            assertDoesNotThrow(() -> cloudinaryService.deleteFile("image"));
        }
    }
}