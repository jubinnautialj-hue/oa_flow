package com.oa.service;

import com.oa.dto.RoleDTO;
import com.oa.entity.Permission;
import com.oa.entity.Role;
import com.oa.repository.PermissionRepository;
import com.oa.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public List<Role> search(String keyword) {
        if (StringUtils.hasText(keyword)) {
            return roleRepository.search(keyword);
        }
        return roleRepository.findAll();
    }

    public Optional<Role> findById(Long id) {
        return roleRepository.findById(id);
    }

    @Transactional
    public Role create(RoleDTO dto) {
        if (roleRepository.existsByCode(dto.getCode())) {
            throw new RuntimeException("角色编码已存在");
        }
        Role role = new Role();
        role.setCode(dto.getCode());
        role.setName(dto.getName());
        role.setDescription(dto.getDescription());
        role.setStatus(dto.getStatus());
        
        if (dto.getPermissionIds() != null && !dto.getPermissionIds().isEmpty()) {
            Set<Permission> permissions = new HashSet<>();
            dto.getPermissionIds().forEach(permissionId -> {
                permissionRepository.findById(permissionId).ifPresent(permissions::add);
            });
            role.setPermissions(permissions);
        }
        return roleRepository.save(role);
    }

    @Transactional
    public Role update(Long id, RoleDTO dto) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("角色不存在"));
        role.setName(dto.getName());
        role.setDescription(dto.getDescription());
        role.setStatus(dto.getStatus());
        
        if (dto.getPermissionIds() != null) {
            Set<Permission> permissions = new HashSet<>();
            dto.getPermissionIds().forEach(permissionId -> {
                permissionRepository.findById(permissionId).ifPresent(permissions::add);
            });
            role.setPermissions(permissions);
        }
        return roleRepository.save(role);
    }

    @Transactional
    public void delete(Long id) {
        roleRepository.deleteById(id);
    }
}
