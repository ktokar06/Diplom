package com.example.repository;

import com.example.model.TeacherSubject;
import com.example.model.TeacherSubjectId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Репозиторий для работы со связями преподавателей и предметов.
 * Предоставляет методы для выполнения CRUD-операций и специализированных запросов
 * к сущности {@link TeacherSubject} с составным ключом {@link TeacherSubjectId}.
 */
public interface TeacherSubjectRepository extends JpaRepository<TeacherSubject, TeacherSubjectId> {

    /**
     * Находит все связи преподавателей с предметами по идентификатору предмета.
     *
     * @param subjectId идентификатор предмета
     * @return список связей преподавателей с указанным предметом
     */
    List<TeacherSubject> findBySubjectSubjectId(Long subjectId);
}