package com.example.repository;

import com.example.model.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Репозиторий для работы с учебными группами.
 * Предоставляет методы для выполнения CRUD-операций с сущностью {@link Group}.
 */
public interface GroupRepository extends JpaRepository<Group, Long> {
    // доступны по умолчанию
}