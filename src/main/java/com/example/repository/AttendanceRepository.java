package com.example.repository;

import com.example.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Репозиторий для работы с посещаемостью студентов.
 * Предоставляет методы для выполнения CRUD-операций и специализированных запросов
 * к сущности {@link Attendance}.
 */
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    /**
     * Находит все записи о посещаемости по идентификатору студента.
     *
     * @param studentId идентификатор студента
     * @return список записей посещаемости для указанного студента
     */
    List<Attendance> findByStudentId(Long studentId);

    /**
     * Находит записи о посещаемости по идентификатору студента и предмета,
     * отсортированные по дате посещения в порядке убывания (от новых к старым).
     *
     * @param studentId идентификатор студента
     * @param subjectId идентификатор предмета
     * @return отсортированный список записей посещаемости для указанного студента и предмета
     */
    List<Attendance> findByStudentIdAndSubjectSubjectIdOrderByAttendanceDateDesc(Long studentId, Long subjectId);
}