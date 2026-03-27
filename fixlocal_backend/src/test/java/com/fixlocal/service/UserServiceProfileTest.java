package com.fixlocal.service;

import com.fixlocal.dto.ServiceOfferingRequest;
import com.fixlocal.dto.UpdateUserRequest;
import com.fixlocal.exception.ResourceNotFoundException;
import com.fixlocal.exception.UnauthorizedException;
import com.fixlocal.model.Role;
import com.fixlocal.model.ServiceOffering;
import com.fixlocal.model.Status;
import com.fixlocal.model.User;
import com.fixlocal.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceProfileTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User tradesperson;

    @BeforeEach
    void setup() {
        tradesperson = User.builder()
                .id("tp-1")
                .email("tp@mail.com")
                .role(Role.TRADESPERSON)
                .status(Status.AVAILABLE)
                .skillTags(new ArrayList<>())
                .serviceOfferings(new ArrayList<>())
                .build();
    }

    @Test
    void updateMyProfile_updatesBioPhoneAndSkillTags() {

        when(userRepository.findByEmail("tp@mail.com"))
                .thenReturn(Optional.of(tradesperson));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Jane TP");
        request.setWorkingCity("New York");
        request.setBio("Plumber & electrician");
        request.setPhone("555-0000");
        request.setSkillTags(List.of("plumbing", "outdoor"));

        var dto = userService.updateMyProfile("tp@mail.com", request);

        assertThat(dto.getBio()).isEqualTo("Plumber & electrician");
        assertThat(dto.getPhone()).isEqualTo("555-0000");
        assertThat(dto.getSkillTags()).containsExactly("plumbing", "outdoor");
    }

    @Test
    void updateSkillTags_requiresTradesperson() {

        User user = User.builder()
                .email("user@mail.com")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail("user@mail.com"))
                .thenReturn(Optional.of(user));

        assertThrows(UnauthorizedException.class,
                () -> userService.updateSkillTags("user@mail.com", List.of("tag")));
    }

    @Test
    void addServiceOffering_generatesIdAndPersists() {

        when(userRepository.findByEmail("tp@mail.com"))
                .thenReturn(Optional.of(tradesperson));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ServiceOfferingRequest request = new ServiceOfferingRequest();
        request.setName("Water heater install");
        request.setDescription("Install standard heaters");
        request.setBasePrice(250.0);
        request.setDurationMinutes(90);

        var dto = userService.addServiceOffering("tp@mail.com", request);

        assertThat(dto.getServiceOfferings()).hasSize(1);
        var offering = dto.getServiceOfferings().get(0);
        assertThat(offering.getId()).isNotBlank();
        assertThat(offering.getName()).isEqualTo("Water heater install");
    }

    @Test
    void updateServiceOffering_throwsWhenNotFound() {

        when(userRepository.findByEmail("tp@mail.com"))
                .thenReturn(Optional.of(tradesperson));

        ServiceOfferingRequest request = new ServiceOfferingRequest();
        request.setName("Fix wiring");
        request.setDescription("desc");
        request.setBasePrice(100.0);
        request.setDurationMinutes(60);

        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateServiceOffering("tp@mail.com", "missing", request));
    }

    @Test
    void deleteServiceOffering_removesExisting() {

        ServiceOffering existing = ServiceOffering.builder()
                .id("svc-1")
                .name("Basic plumbing")
                .build();
        tradesperson.getServiceOfferings().add(existing);

        when(userRepository.findByEmail("tp@mail.com"))
                .thenReturn(Optional.of(tradesperson));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var dto = userService.deleteServiceOffering("tp@mail.com", "svc-1");

        assertThat(dto.getServiceOfferings()).isEmpty();
    }
}
