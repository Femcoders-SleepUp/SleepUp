package com.SleepUp.SU.accommodation.utils;

import com.SleepUp.SU.accommodation.entity.Accommodation;
import com.SleepUp.SU.accommodation.repository.AccommodationRepository;
import com.SleepUp.SU.accommodation.dto.AccommodationRequest;
import com.SleepUp.SU.accommodation.exceptions.AccommodationAlreadyExistsByNameException;
import com.SleepUp.SU.accommodation.exceptions.AccommodationNotFoundByIdException;
import com.SleepUp.SU.cloudinary.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccommodationServiceHelper {
    private final AccommodationRepository accommodationRepository;
    private final CloudinaryService cloudinaryService;

    public void validateAccommodationNameDoesNotExist(String name) {
        if (accommodationRepository.existsByName(name)) {
            throw new AccommodationAlreadyExistsByNameException(name);
        }
    }

    public Accommodation getAccommodationEntityById(Long id) {
        return accommodationRepository.findById(id).orElseThrow(() -> new AccommodationNotFoundByIdException(id));
    }

    public boolean isAccommodationOwnedByUser(Long accommodationId, Long userId){
        return accommodationRepository.existsByIdAndManagedBy_Id(accommodationId, userId);
    }

    public void cloudinaryManagement(AccommodationRequest accommodationRequest, Accommodation accommodationIsExisting) {
        if (accommodationRequest.image() != null && !accommodationRequest.image().isEmpty()) {
            deleteImageCloudinary(accommodationIsExisting.getImageUrl());
            postImageCloudinary(accommodationRequest, accommodationIsExisting);
        }
    }

    public static String getPublicIdCloudinary(String imageUrl) {
        String withoutPrefix = imageUrl.substring(imageUrl.indexOf("/upload/") + 8);
        if (withoutPrefix.matches("v\\d+/.+")) {
            withoutPrefix = withoutPrefix.substring(withoutPrefix.indexOf('/') + 1);
        }
        int dotIndex = withoutPrefix.lastIndexOf('.');
        String publicId = (dotIndex != -1) ? withoutPrefix.substring(0, dotIndex) : withoutPrefix;
        return publicId;
    }

    public void postImageCloudinary(AccommodationRequest request, Accommodation accommodation) {
        try {
            Map uploadResult = cloudinaryService.uploadFile(request.image(), "accommodations");
            String imageUrl = (String) uploadResult.get("secure_url");
            accommodation.setImageUrl(imageUrl);
        } catch (Exception e) {
            accommodation.setImageUrl("http://localhost:8080/images/LOGO.png");
        }
    }

    public void deleteImageCloudinary(String url) {
        try {
            String publicId = getPublicIdCloudinary(url);
            cloudinaryService.deleteFile(publicId);
        } catch (IOException e) {
            throw new RuntimeException("Error deleting image from Cloudinary: " + e.getMessage());
        }
    }
}