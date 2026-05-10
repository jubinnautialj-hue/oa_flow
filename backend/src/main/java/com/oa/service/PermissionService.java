package com.oa.service;

import com.oa.dto.PermissionDTO;
import com.oa.entity.Permission;
import com.oa.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PermissionService {

    @Autowired
    private PermissionRepository permissionRepository;

    public List<Permission> findAll() {
        return permissionRepository.findAll();
    }

    public Optional<Permission> findById(Long id) {
        return permissionRepository.findById(id);
    }

    @Transactional
    public Permission create(PermissionDTO dto) {
        if (permissionRepository.existsByCode(dto.getCode())) {
            throw new RuntimeException("权限编码已存在");
        }
        Permission permission = new Permission();
        permission.setCode(dto.getCode());
        permission.setName(dto.getName());
        permission.setDescription(dto.getDescription());
        permission.setStatus(dto.getStatus());
        return permissionRepository.save(permission);
    }

    @Transactional
    public Permission update(Long id, PermissionDTO dto) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("权限不存在"));
        permission.setName(dto.getName());
        permission.setDescription(dto.getDescription());
        permission.setStatus(dto.getStatus());
        return permissionRepository.save(permission);
    }

    @Transactional
    public void delete(Long id) {
        permissionRepository.deleteById(id);
    }
}
