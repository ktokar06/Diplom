package com.example.repository;

import com.example.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с учебными предметами.
 * Предоставляет методы для выполнения CRUD-операций и специализированных запросов
 * к сущности {@link Subject}.
 */
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    /**
     * Находит предмет по идентификатору.
     *
     * @param id идентификатор предмета
     * @return {@link Optional} с найденным предметом, если существует, или пустой {@link Optional}
     */
    Optional<Subject> findById(Long id);

    /**
     * Находит все предметы.
     *
     * @return список всех предметов
     */
    List<Subject> findAll();
}