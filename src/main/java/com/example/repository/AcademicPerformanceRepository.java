package com.example.repository;

import com.example.model.AcademicPerformance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Репозиторий для работы с академической успеваемостью студентов.
 * Предоставляет методы для выполнения CRUD-операций и специализированных запросов
 * к сущности {@link AcademicPerformance}.
 */
public interface AcademicPerformanceRepository extends JpaRepository<AcademicPerformance, Long> {

    /**
     * Находит все записи об академической успеваемости по идентификатору студента.
     *
     * @param studentId идентификатор студента
     * @return список записей академической успеваемости для указанного студента
     */
    List<AcademicPerformance> findByStudentId(Long studentId);

    /**
     * Находит записи об академической успеваемости по идентификатору студента и предмета.
     *
     * @param studentId идентификатор студента
     * @param subjectId идентификатор предмета
     * @return список записей академической успеваемости для указанного студента и предмета
     */
    List<AcademicPerformance> findByStudentIdAndSubjectSubjectId(Long studentId, Long subjectId);
}