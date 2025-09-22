package com.SleepUp.SU.user;

import com.SleepUp.SU.accommodation.Accommodation;
import com.SleepUp.SU.user.role.Role;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToMany(mappedBy = "managedBy", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Accommodation> accommodations = new HashSet<>();


    public User(long l, String usernameTest, String nameTest, String mail, String testPassword, Role role) {
    }
}
