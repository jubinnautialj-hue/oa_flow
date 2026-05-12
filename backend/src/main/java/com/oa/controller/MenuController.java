package com.oa.controller;

import com.oa.dto.MenuDTO;
import com.oa.entity.Menu;
import com.oa.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/menus")
public class MenuController {

    @Autowired
    private MenuService menuService;

    @GetMapping
    public ResponseEntity<List<Menu>> list(@RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return ResponseEntity.ok(menuService.search(keyword));
        }
        return ResponseEntity.ok(menuService.findAll());
    }

    @GetMapping("/tree")
    public ResponseEntity<List<Map<String, Object>>> getMenuTree() {
        return ResponseEntity.ok(menuService.getMenuTree());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Menu> getById(@PathVariable Long id) {
        return menuService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Menu> create(@RequestBody MenuDTO dto) {
        return ResponseEntity.ok(menuService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Menu> update(@PathVariable Long id, @RequestBody MenuDTO dto) {
        return ResponseEntity.ok(menuService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return ResponseEntity.ok().build();
    }
}
