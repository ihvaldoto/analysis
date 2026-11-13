package ru.student.dataanalysis.expense;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.student.dataanalysis.expense.entity.AppUser;
import ru.student.dataanalysis.expense.entity.Role;
import ru.student.dataanalysis.expense.exception.UserAlreadyExistsException;
import ru.student.dataanalysis.expense.repository.AppUserRepository;
import ru.student.dataanalysis.expense.service.AppUserServiceImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppUserServiceUnitTest {

    @Mock private AppUserRepository repository;
    @Mock private PasswordEncoder encoder;
    @InjectMocks private AppUserServiceImpl service;

    @Test
    void registrationTrimsLoginAndSavesEncodedPasswordWithUserRole() {
        when(repository.findByLogin("anna")).thenReturn(Optional.empty());
        when(encoder.encode("secret")).thenReturn("encoded-password");
        when(repository.save(any(AppUser.class))).thenAnswer(call -> call.getArgument(0));

        AppUser result = service.addUser(" anna ", "secret");

        ArgumentCaptor<AppUser> saved = ArgumentCaptor.forClass(AppUser.class);
        verify(repository).save(saved.capture());
        assertSame(saved.getValue(), result);
        assertEquals("anna", result.getLogin());
        assertEquals("anna", result.getDisplayName());
        assertEquals("encoded-password", result.getPassword());
        assertEquals(Role.USER, result.getRole());
    }

    @Test
    void findsExistingUserByLogin() {
        AppUser user = new AppUser();
        when(repository.findByLogin("anna")).thenReturn(Optional.of(user));

        assertEquals(Optional.of(user), service.getByLogin("anna"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void rejectsInvalidLoginBeforeTouchingDependencies(String login) {
        assertThrows(IllegalArgumentException.class, () -> service.addUser(login, "secret"));
        verifyNoInteractions(repository, encoder);
    }

    @Test
    void rejectsBlankPasswordBeforeTouchingDependencies() {
        assertThrows(IllegalArgumentException.class, () -> service.addUser("anna", " "));
        verifyNoInteractions(repository, encoder);
    }

    @Test
    void duplicateLoginDoesNotEncodePasswordOrSaveUser() {
        when(repository.findByLogin("anna")).thenReturn(Optional.of(new AppUser()));

        assertThrows(UserAlreadyExistsException.class, () -> service.addUser(" anna ", "secret"));
        verify(repository, never()).save(any(AppUser.class));
        verifyNoInteractions(encoder);
    }

    @Test
    void translatesDatabaseConflictDuringConcurrentRegistration() {
        when(repository.findByLogin("anna")).thenReturn(Optional.empty());
        when(encoder.encode("secret")).thenReturn("encoded-password");
        when(repository.save(any(AppUser.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate login"));

        assertThrows(UserAlreadyExistsException.class, () -> service.addUser("anna", "secret"));
    }
}
