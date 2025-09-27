package com.example.security;

import com.example.model.entity.Student;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Реализация интерфейса UserDetails для представления данных студента в Spring Security.
 * Оборачивает объект Student и предоставляет необходимую информацию для системы безопасности.
 */
public record PersonDetails(Student student) implements UserDetails {

    /**
     * Возвращает права доступа (роли) пользователя.
     * Все студенты имеют роль ROLE_STUDENT.
     *
     * @return коллекция с одной ролью ROLE_STUDENT
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT"));
    }

    /**
     * Возвращает хеш пароля пользователя для проверки аутентификации.
     *
     * @return хеш пароля студента
     */
    @Override
    public String getPassword() {
        return this.student.getPasswordHash();
    }

    /**
     * Возвращает имя пользователя (номер студенческого билета) для идентификации.
     *
     * @return номер студенческого билета
     */
    @Override
    public String getUsername() {
        return this.student.getStudentTicketNumber();
    }

    /**
     * Показывает, не истек ли срок действия учетной записи.
     * В данной реализации учетные записи никогда не истекают.
     *
     * @return всегда true
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Показывает, не заблокирована ли учетная запись.
     * В данной реализации учетные записи никогда не блокируются.
     *
     * @return всегда true
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Показывает, не истек ли срок действия учетных данных (пароля).
     * В данной реализации учетные данные никогда не истекают.
     *
     * @return всегда true
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Показывает, активна ли учетная запись.
     * В данной реализации все учетные записи активны.
     *
     * @return всегда true
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}