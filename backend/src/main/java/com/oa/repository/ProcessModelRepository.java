package com.oa.repository;

import com.oa.entity.ProcessModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessModelRepository extends JpaRepository<ProcessModel, Long> {
    Optional<ProcessModel> findByProcessKey(String processKey);
    boolean existsByProcessKey(String processKey);

    @Query("SELECT m FROM ProcessModel m WHERE m.name LIKE %:keyword% OR m.processKey LIKE %:keyword% OR m.description LIKE %:keyword%")
    List<ProcessModel> search(@Param("keyword") String keyword);
}
