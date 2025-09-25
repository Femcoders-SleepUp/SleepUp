package com.SleepUp.SU.accommodation.entity;

import com.SleepUp.SU.reservation.entity.Reservation;
import com.SleepUp.SU.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "accommodations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Accommodation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false, name = "guest_number")
    private int guestNumber;

    @Column(nullable = false, name = "pet_friendly")
    private Boolean petFriendly;

    @Column(nullable = false, length = 50)
    private String location;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, name = "image_url")
    private String imageUrl;

    @Column(name = "check_in_time")
    private LocalTime checkInTime;

    @Column(name = "check_out_time")
    private LocalTime checkOutTime;

    @Column(nullable = false, name = "available_from")
    private LocalDate availableFrom;

    @Column(nullable = false, name = "available_to")
    private LocalDate availableTo;

    @ManyToOne
    @JoinColumn(name = "managed_by_user_id")
    private User managedBy;

    @OneToMany(mappedBy = "accommodation", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private Set<Reservation> reservations = new HashSet<>();
}