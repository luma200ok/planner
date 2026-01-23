package com.planner.template.repository;

import com.planner.template.domain.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;


public interface TemplateRepository extends JpaRepository<Template,Long> {

    List<Template> findAllByActiveTrue();

    @Query("""
                    select t from Template t
                     left  join fetch t.items
                     where t.id = :id
            """)
    Optional<Template> findByIdWithItems(@Param("id") Long id);
}