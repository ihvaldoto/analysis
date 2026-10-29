package ru.student.dataanalysis.expense.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.student.dataanalysis.expense.entity.AppUser;
import ru.student.dataanalysis.expense.entity.Role;
import ru.student.dataanalysis.expense.service.AppUserService;

/**
 * Преобразует пользователя приложения в {@link UserDetails} для Spring Security.
 */
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserService userService;

    public AppUserDetailsService(AppUserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser appUser = userService.getByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден: " + username));
        if (appUser.getPassword() == null || appUser.getPassword().isEmpty()) {
            throw new UsernameNotFoundException("У пользователя не задан пароль: " + username);
        }
        Role role = appUser.getRole() == null ? Role.USER : appUser.getRole();
        // roles() сам добавляет префикс ROLE_
        return org.springframework.security.core.userdetails.User.withUsername(appUser.getLogin())
                .password(appUser.getPassword())
                .roles(role.name())
                .build();
    }
}
