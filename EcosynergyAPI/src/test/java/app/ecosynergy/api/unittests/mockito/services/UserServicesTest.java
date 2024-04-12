package app.ecosynergy.api.unittests.mockito.services;

import app.ecosynergy.api.data.vo.v1.UserVO;
import app.ecosynergy.api.exceptions.RequiredObjectIsNullException;
import app.ecosynergy.api.models.User;
import app.ecosynergy.api.repositories.UserRepository;
import app.ecosynergy.api.services.UserServices;
import app.ecosynergy.api.unittests.mapper.mocks.MockUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class UserServicesTest {
    MockUser input;

    @InjectMocks
    private UserServices service;

    @Mock
    UserRepository repository;
    @BeforeEach
    void setUpMocks() {
        input = new MockUser();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findAll() {

    }

    @Test
    void findById() {
        User entity = input.mockEntity(1);

        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
        UserVO result = service.findById(entity.getId());
        assertNotNull(result);
        assertNotNull(result.getKey());
        assertNotNull(result.getLinks());
        assertTrue(result.toString().contains("links: [</api/user/v1/1>;rel=\"self\"]"));
        assertEquals("User1", result.getFullName());
        assertEquals("Email1", result.getEmail());
        assertEquals("Password1", result.getPassword());
        assertEquals("Female", result.getGender());
        assertEquals("Brazilian1", result.getNationality());
    }

    @Test
    void update() {
        Exception exception = assertThrows(RequiredObjectIsNullException.class, () -> service.create(null));

        String expectedMessage = "It is not allowed to persist a null object!";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void delete() {
        User entity = input.mockEntity(0);

        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
        service.delete(entity.getId());
        assertTrue(true);
    }
}