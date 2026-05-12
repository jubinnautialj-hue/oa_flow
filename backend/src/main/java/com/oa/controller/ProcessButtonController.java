package com.oa.controller;

import com.oa.dto.ProcessButtonDTO;
import com.oa.entity.ProcessButton;
import com.oa.service.ProcessButtonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/process-buttons")
public class ProcessButtonController {

    @Autowired
    private ProcessButtonService processButtonService;

    @GetMapping
    public ResponseEntity<List<ProcessButton>> list(@RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return ResponseEntity.ok(processButtonService.search(keyword));
        }
        return ResponseEntity.ok(processButtonService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcessButton> getById(@PathVariable Long id) {
        return processButtonService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/process/{processDefinitionKey}")
    public ResponseEntity<List<ProcessButton>> getByProcessDefinitionKey(@PathVariable String processDefinitionKey) {
        return ResponseEntity.ok(processButtonService.findByProcessDefinitionKey(processDefinitionKey));
    }

    @GetMapping("/process/{processDefinitionKey}/task/{taskDefinitionKey}")
    public ResponseEntity<List<ProcessButton>> getByProcessAndTask(
            @PathVariable String processDefinitionKey,
            @PathVariable String taskDefinitionKey) {
        return ResponseEntity.ok(processButtonService.findByProcessAndTask(processDefinitionKey, taskDefinitionKey));
    }

    @PostMapping
    public ResponseEntity<ProcessButton> create(@RequestBody ProcessButtonDTO dto) {
        return ResponseEntity.ok(processButtonService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProcessButton> update(@PathVariable Long id, @RequestBody ProcessButtonDTO dto) {
        return ResponseEntity.ok(processButtonService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        processButtonService.delete(id);
        return ResponseEntity.ok().build();
    }
}
