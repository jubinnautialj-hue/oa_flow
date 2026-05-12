package com.oa.repository;

import com.oa.entity.FormModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FormModelRepository extends JpaRepository<FormModel, Long> {
    Optional<FormModel> findByFormKey(String formKey);
    boolean existsByFormKey(String formKey);

    @Query("SELECT m FROM FormModel m WHERE m.name LIKE %:keyword% OR m.formKey LIKE %:keyword% OR m.description LIKE %:keyword%")
    List<FormModel> search(@Param("keyword") String keyword);
}
