package com.SleepUp.SU.accommodation.utilis;

import com.SleepUp.SU.accommodation.dto.AccommodationRequest;
import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.accommodation.exceptions.AccommodationAlreadyExistsByNameException;
import com.SleepUp.SU.accommodation.exceptions.AccommodationNotFoundByIdException;
import com.SleepUp.SU.accommodation.repository.AccommodationRepository;
import com.SleepUp.SU.accommodation.utils.AccommodationServiceHelper;
import com.SleepUp.SU.cloudinary.CloudinaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccommodationServiceHelperTest {

    @Mock
    private AccommodationRepository accommodationRepository;

    @Mock
    private CloudinaryService cloudinaryService;

    @InjectMocks
    private AccommodationServiceHelper helper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void validateAccommodationNameDoesNotExist_nameExists_shouldThrowException() {
        String name = "ExistingName";
        when(accommodationRepository.existsByName(name)).thenReturn(true);

        AccommodationAlreadyExistsByNameException exception = assertThrows(
                AccommodationAlreadyExistsByNameException.class,
                () -> helper.validateAccommodationNameDoesNotExist(name));

        assertEquals("Accommodation with name '" + name+ "' already exists", exception.getMessage());
    }

    @Test
    void validateAccommodationNameDoesNotExist_nameNotExists_shouldNotThrow() {
        when(accommodationRepository.existsByName(anyString())).thenReturn(false);
        assertDoesNotThrow(() -> helper.validateAccommodationNameDoesNotExist("NewName"));
    }

    @Test
    void getAccommodationEntityById_found_shouldReturnAccommodation() {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        when(accommodationRepository.findById(1L)).thenReturn(Optional.of(accommodation));

        Accommodation result = helper.getAccommodationEntityById(1L);
        assertEquals(accommodation, result);
    }

    @Test
    void getAccommodationEntityById_notFound_shouldThrowException() {
        when(accommodationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(AccommodationNotFoundByIdException.class, () -> helper.getAccommodationEntityById(99L));
    }

    @Test
    void cloudinaryManagement_withImage_shouldDeleteAndPostImage() throws IOException {
        AccommodationRequest request = mock(AccommodationRequest.class);
        Accommodation accommodation = Accommodation.builder()
                .imageUrl("mockurl").build();

        MockMultipartFile imageFile = new MockMultipartFile("image", "filename.jpg",
                "image/jpeg", "dummy data".getBytes());

        when(request.image()).thenReturn(imageFile);

        doNothing().when(cloudinaryService).deleteFile(anyString());
        when(cloudinaryService.uploadFile(any(), anyString())).thenReturn(Map.of("secure_url", "http://example.com/image.jpg"));

        helper.cloudinaryManagement(request, accommodation);

        verify(cloudinaryService).deleteFile(anyString());
        verify(cloudinaryService).uploadFile(any(), eq("accommodations"));
        assertEquals("http://example.com/image.jpg", accommodation.getImageUrl());
    }


    @Test
    void cloudinaryManagement_withNullOrEmptyImage_shouldDoNothing() {
        AccommodationRequest request = mock(AccommodationRequest.class);
        Accommodation accommodation = new Accommodation();

        when(request.image()).thenReturn(null);
        helper.cloudinaryManagement(request, accommodation);
        verifyNoInteractions(cloudinaryService);

        MockMultipartFile emptyFile = mock(MockMultipartFile.class);
        when(request.image()).thenReturn(emptyFile);
        when(emptyFile.isEmpty()).thenReturn(true);
        helper.cloudinaryManagement(request, accommodation);
        verifyNoMoreInteractions(cloudinaryService);
    }

    @Test
    void getPublicIdCloudinary_shouldReturnCorrectPublicId() {
        String urlWithVersion = "http://res.cloudinary.com/demo/image/upload/v1234/accommodation1.jpg";
        String urlWithoutVersion = "http://res.cloudinary.com/demo/image/upload/accommodation2.png";

        String publicIdWithVersion = AccommodationServiceHelper.getPublicIdCloudinary(urlWithVersion);
        String publicIdWithoutVersion = AccommodationServiceHelper.getPublicIdCloudinary(urlWithoutVersion);

        assertEquals("accommodation1", publicIdWithVersion);
        assertEquals("accommodation2", publicIdWithoutVersion);
    }

    @Test
    void postImageCloudinary_success_shouldSetImageUrl() throws Exception {
        AccommodationRequest request = mock(AccommodationRequest.class);
        Accommodation accommodation = new Accommodation();
        MockMultipartFile image = new MockMultipartFile("image", "file.jpg", "image/jpeg", "content".getBytes());
        when(request.image()).thenReturn(image);

        when(cloudinaryService.uploadFile(any(), anyString())).thenReturn(Map.of("secure_url", "http://example.com/image.jpg"));

        helper.postImageCloudinary(request, accommodation);

        assertEquals("http://example.com/image.jpg", accommodation.getImageUrl());
    }

    @Test
    void postImageCloudinary_failure_shouldSetDefaultImageUrl() throws Exception {
        AccommodationRequest request = mock(AccommodationRequest.class);
        Accommodation accommodation = new Accommodation();
        MockMultipartFile image = new MockMultipartFile("image", "file.jpg", "image/jpeg", "content".getBytes());
        when(request.image()).thenReturn(image);

        when(cloudinaryService.uploadFile(any(), anyString())).thenThrow(new RuntimeException("Any exception"));

        helper.postImageCloudinary(request, accommodation);

        assertEquals("http://localhost:8080/images/LOGO.png", accommodation.getImageUrl());
    }

    @Test
    void deleteImageCloudinary_success_shouldInvokeDeleteFile() throws IOException {
        String url = "http://res.cloudinary.com/demo/image/upload/v1/sample.jpg";

        doNothing().when(cloudinaryService).deleteFile("sample");

        helper.deleteImageCloudinary(url);

        verify(cloudinaryService).deleteFile("sample");
    }

    @Test
    void deleteImageCloudinary_failure_shouldThrowRuntimeException() throws IOException {
        String url = "http://res.cloudinary.com/demo/image/upload/v1/sample.jpg";

        doThrow(new IOException("Delete failed")).when(cloudinaryService).deleteFile("sample");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> helper.deleteImageCloudinary(url));
        assertTrue(ex.getMessage().contains("Error deleting image from Cloudinary"));
    }
}
