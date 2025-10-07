package com.SleepUp.SU.user.dto;

import com.SleepUp.SU.user.entity.User;
import com.SleepUp.SU.user.role.Role;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperImplTest {

    private final UserMapperImpl userMapper = new UserMapperImpl();

    @Nested
    class ToResponseTests {
        @Test
        void toResponse_nullUser_shouldReturnNull() {
            UserResponse result = userMapper.toResponse(null);
            assertThat(result).isNull();
        }

        @Test
        void toResponse_userWithAllFieldsNull_shouldMapFieldsAsNull() {
            User user = new User();
            UserResponse response = userMapper.toResponse(user);

            assertThat(response).isNotNull();
            assertThat(response.id()).isNull();
            assertThat(response.username()).isNull();
            assertThat(response.name()).isNull();
            assertThat(response.email()).isNull();
            assertThat(response.role()).isNull();
        }
    }

    @Nested
    class ToEntityTests {
        @Test
        void toEntity_allNullParams_shouldReturnNull() {
            User result = userMapper.toEntity(null, null, null);
            assertThat(result).isNull();
        }

        @Test
        void toEntity_nullUserRequest_shouldSetOnlyPasswordAndRole() {
            String encodedPassword = "encodedPass";
            Role role = Role.USER;

            User result = userMapper.toEntity(null, encodedPassword, role);

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isNull();
            assertThat(result.getName()).isNull();
            assertThat(result.getEmail()).isNull();
            assertThat(result.getPassword()).isEqualTo(encodedPassword);
            assertThat(result.getRole()).isEqualTo(role);
        }

        @Test
        void toEntity_userRequestWithAllFields_shouldMapAll() {
            UserRequest request = new UserRequest("username", "Name", "email@test.com", "password");
            String encodedPassword = "encodedPass";
            Role role = Role.ADMIN;

            User result = userMapper.toEntity(request, encodedPassword, role);

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("username");
            assertThat(result.getName()).isEqualTo("Name");
            assertThat(result.getEmail()).isEqualTo("email@test.com");
            assertThat(result.getPassword()).isEqualTo(encodedPassword);
            assertThat(result.getRole()).isEqualTo(role);
        }

        @Test
        void toEntity_nullUserRequest_nonNullPasswordAndRole_shouldReturnUser() {
            String encodedPassword = "someEncodedPassword";
            Role role = Role.USER;

            User result = userMapper.toEntity(null, encodedPassword, role);

            assertThat(result).isNotNull();
            assertThat(result.getPassword()).isEqualTo(encodedPassword);
            assertThat(result.getRole()).isEqualTo(role);
            assertThat(result.getUsername()).isNull();
            assertThat(result.getEmail()).isNull();
            assertThat(result.getName()).isNull();
        }

        @Test
        void toEntity_nonNullUserRequest_nullPasswordAndRole_shouldReturnUser() {
            UserRequest userRequest = new UserRequest("username", "name", "email@test.com", "rawPass");

            User result = userMapper.toEntity(userRequest, null, null);

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("username");
            assertThat(result.getName()).isEqualTo("name");
            assertThat(result.getEmail()).isEqualTo("email@test.com");
            assertThat(result.getPassword()).isNull();
            assertThat(result.getRole()).isNull();
        }

        @Test
        void toEntity_nonNullUserRequest_nonNullPassword_nullRole_shouldReturnUser() {
            UserRequest userRequest = new UserRequest("username", "name", "email@test.com", "rawPass");
            String encodedPassword = "encodedPass";

            User result = userMapper.toEntity(userRequest, encodedPassword, null);

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("username");
            assertThat(result.getName()).isEqualTo("name");
            assertThat(result.getEmail()).isEqualTo("email@test.com");
            assertThat(result.getPassword()).isEqualTo(encodedPassword);
            assertThat(result.getRole()).isNull();
        }

        @Test
        void toEntity_nonNullUserRequest_nullPassword_nonNullRole_shouldReturnUser() {
            UserRequest userRequest = new UserRequest("username", "name", "email@test.com", "rawPass");
            Role role = Role.ADMIN;

            User result = userMapper.toEntity(userRequest, null, role);

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("username");
            assertThat(result.getName()).isEqualTo("name");
            assertThat(result.getEmail()).isEqualTo("email@test.com");
            assertThat(result.getPassword()).isNull();
            assertThat(result.getRole()).isEqualTo(role);
        }

        @Test
        void toEntity_nullUserRequest_nonNullPassword_nullRole_shouldReturnUser() {
            String encodedPassword = "encodedPass";
            Role role = null;

            User result = userMapper.toEntity(null, encodedPassword, role);

            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isNull();
            assertThat(result.getName()).isNull();
            assertThat(result.getEmail()).isNull();
            assertThat(result.getPassword()).isEqualTo(encodedPassword);
            assertThat(result.getRole()).isNull();
        }

        @Test
        void toEntity_nullUserRequest_nonNullPassword_nonNullRole_shouldReturnUser() {
            String encodedPassword = "encodedPass";
            Role role = Role.USER;

            User result = userMapper.toEntity(null, encodedPassword, role);

            assertThat(result).isNotNull();
            assertThat(result.getPassword()).isEqualTo(encodedPassword);
            assertThat(result.getRole()).isEqualTo(role);
            assertThat(result.getUsername()).isNull();
            assertThat(result.getEmail()).isNull();
            assertThat(result.getName()).isNull();
        }
    }
}