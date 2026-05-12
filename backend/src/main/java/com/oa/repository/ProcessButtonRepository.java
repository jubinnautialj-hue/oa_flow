package com.oa.repository;

import com.oa.entity.ProcessButton;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProcessButtonRepository extends JpaRepository<ProcessButton, Long> {

    List<ProcessButton> findAllByOrderBySortAsc();

    @Query("SELECT pb FROM ProcessButton pb WHERE LOWER(pb.buttonName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(pb.buttonCode) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<ProcessButton> search(@Param("keyword") String keyword);

    List<ProcessButton> findByProcessDefinitionKeyOrderBySortAsc(String processDefinitionKey);

    List<ProcessButton> findByProcessDefinitionKeyAndTaskDefinitionKeyOrderBySortAsc(String processDefinitionKey, String taskDefinitionKey);
}
