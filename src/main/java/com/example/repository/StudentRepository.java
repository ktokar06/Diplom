package com.example.repository;

import com.example.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы со студентами.
 * Предоставляет методы для выполнения CRUD-операций и специализированных запросов
 * к сущности {@link Student}.
 */
public interface StudentRepository extends JpaRepository<Student, Long> {

    /**
     * Находит всех студентов по идентификатору учебной группы.
     *
     * @param groupId идентификатор учебной группы
     * @return список студентов, принадлежащих указанной группе
     */
    List<Student> findByGroupId(Long groupId);

    /**
     * Находит студента по номеру студенческого билета.
     *
     * @param studentTicketNumber номер студенческого билета
     * @return {@link Optional} с найденным студентом, если существует, или пустой {@link Optional}
     */
    Optional<Student> findByStudentTicketNumber(String studentTicketNumber);

    /**
     * Находит студента по идентификатору.
     *
     * @param id идентификатор студента
     * @return {@link Optional} с найденным студентом, если существует, или пустой {@link Optional}
     */
    Optional<Student> findById(Long id);
}