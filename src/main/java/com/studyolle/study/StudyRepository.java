package com.studyolle.study;

import com.studyolle.domain.Study;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface StudyRepository extends JpaRepository<Study, Long> {
    boolean existsByPath(String path);

    @EntityGraph(attributePaths = {"tags", "zones", "managers", "members"}, type = EntityGraph.EntityGraphType.LOAD)
    Study findStudyWithAllByPath(String path);

    @EntityGraph(attributePaths = {"managers"}, type = EntityGraph.EntityGraphType.FETCH)
    Study findStudyWithManagersByPath(String path);

    @EntityGraph(attributePaths = {"managers", "tags"}, type = EntityGraph.EntityGraphType.LOAD)
    Study findStudyWithManagersAndTagsByPath(String path);

    @EntityGraph(attributePaths = {"managers", "zones"}, type = EntityGraph.EntityGraphType.LOAD)
    Study findStudyWithManagersAndZonesByPath(String path);
}
