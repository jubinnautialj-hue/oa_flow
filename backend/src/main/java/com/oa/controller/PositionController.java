package com.oa.controller;

import com.oa.dto.PositionDTO;
import com.oa.entity.Position;
import com.oa.service.PositionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/positions")
public class PositionController {

    @Autowired
    private PositionService positionService;

    @GetMapping
    public ResponseEntity<List<Position>> list(@RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return ResponseEntity.ok(positionService.search(keyword));
        }
        return ResponseEntity.ok(positionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Position> getById(@PathVariable Long id) {
        return positionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Position> create(@RequestBody PositionDTO dto) {
        return ResponseEntity.ok(positionService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Position> update(@PathVariable Long id, @RequestBody PositionDTO dto) {
        return ResponseEntity.ok(positionService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        positionService.delete(id);
        return ResponseEntity.ok().build();
    }
}
