package com.example.repository;

import com.example.model.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Репозиторий для работы с преподавателями.
 * Предоставляет методы для выполнения CRUD-операций с сущностью {@link Teacher}.
 */
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    // доступны по умолчанию
}