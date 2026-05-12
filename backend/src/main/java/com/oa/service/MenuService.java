package com.oa.service;

import com.oa.dto.MenuDTO;
import com.oa.entity.Menu;
import com.oa.repository.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MenuService {

    @Autowired
    private MenuRepository menuRepository;

    public List<Menu> findAll() {
        return menuRepository.findAllByOrderBySortAsc();
    }

    public List<Menu> findByParentId(Long parentId) {
        return menuRepository.findByParentIdOrderBySortAsc(parentId);
    }

    public List<Menu> search(String keyword) {
        if (StringUtils.hasText(keyword)) {
            return menuRepository.search(keyword);
        }
        return menuRepository.findAllByOrderBySortAsc();
    }

    public Optional<Menu> findById(Long id) {
        return menuRepository.findById(id);
    }

    @Transactional
    public Menu create(MenuDTO dto) {
        Menu menu = new Menu();
        menu.setName(dto.getName());
        menu.setPath(dto.getPath());
        menu.setIcon(dto.getIcon());
        menu.setSort(dto.getSort() != null ? dto.getSort() : 0);
        menu.setParentId(dto.getParentId());
        menu.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        return menuRepository.save(menu);
    }

    @Transactional
    public Menu update(Long id, MenuDTO dto) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("菜单不存在"));
        menu.setName(dto.getName());
        menu.setPath(dto.getPath());
        menu.setIcon(dto.getIcon());
        if (dto.getSort() != null) {
            menu.setSort(dto.getSort());
        }
        menu.setParentId(dto.getParentId());
        if (dto.getStatus() != null) {
            menu.setStatus(dto.getStatus());
        }
        return menuRepository.save(menu);
    }

    @Transactional
    public void delete(Long id) {
        menuRepository.deleteById(id);
    }

    public List<Map<String, Object>> getMenuTree() {
        List<Menu> allMenus = menuRepository.findAllByOrderBySortAsc();
        List<Menu> enabledMenus = allMenus.stream()
                .filter(m -> m.getStatus() == 1)
                .collect(Collectors.toList());
        
        List<Menu> topLevelMenus = enabledMenus.stream()
                .filter(m -> m.getParentId() == null)
                .collect(Collectors.toList());
        
        return buildMenuTree(topLevelMenus, enabledMenus);
    }

    private List<Map<String, Object>> buildMenuTree(List<Menu> menus, List<Menu> allMenus) {
        List<Map<String, Object>> tree = new ArrayList<>();
        for (Menu menu : menus) {
            Map<String, Object> menuMap = new HashMap<>();
            menuMap.put("id", menu.getId());
            menuMap.put("name", menu.getName());
            menuMap.put("path", menu.getPath());
            menuMap.put("icon", menu.getIcon());
            menuMap.put("sort", menu.getSort());
            menuMap.put("status", menu.getStatus());
            menuMap.put("parentId", menu.getParentId());
            
            List<Menu> children = allMenus.stream()
                    .filter(m -> Objects.equals(m.getParentId(), menu.getId()))
                    .collect(Collectors.toList());
            
            if (!children.isEmpty()) {
                menuMap.put("children", buildMenuTree(children, allMenus));
            }
            tree.add(menuMap);
        }
        return tree;
    }
}
