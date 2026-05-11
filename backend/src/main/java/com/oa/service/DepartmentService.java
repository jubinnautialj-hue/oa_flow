package com.oa.service;

import com.oa.dto.DepartmentDTO;
import com.oa.entity.Department;
import com.oa.repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    public List<Department> findAll() {
        return departmentRepository.findAll();
    }

    public List<Department> search(String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return departmentRepository.search(keyword);
        }
        return departmentRepository.findAll();
    }

    public Optional<Department> findById(Long id) {
        return departmentRepository.findById(id);
    }

    @Transactional
    public Department create(DepartmentDTO dto) {
        Department department = new Department();
        department.setName(dto.getName());
        department.setDescription(dto.getDescription());
        department.setStatus(dto.getStatus());
        
        if (dto.getParentId() != null) {
            departmentRepository.findById(dto.getParentId()).ifPresent(department::setParent);
        }
        return departmentRepository.save(department);
    }

    @Transactional
    public Department update(Long id, DepartmentDTO dto) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("部门不存在"));
        department.setName(dto.getName());
        department.setDescription(dto.getDescription());
        department.setStatus(dto.getStatus());
        
        if (dto.getParentId() != null) {
            departmentRepository.findById(dto.getParentId()).ifPresent(department::setParent);
        } else {
            department.setParent(null);
        }
        return departmentRepository.save(department);
    }

    @Transactional
    public void delete(Long id) {
        departmentRepository.deleteById(id);
    }
}
