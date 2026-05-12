package com.oa.controller;

import com.oa.entity.ProcessModel;
import com.oa.entity.User;
import com.oa.service.ProcessModelService;
import com.oa.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/process-model")
public class ProcessModelController {

    @Autowired
    private ProcessModelService processModelService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<ProcessModel>> list(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(processModelService.list(keyword));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcessModel> getById(@PathVariable Long id) {
        ProcessModel model = processModelService.getById(id);
        if (model == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(model);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ProcessModel model) {
        try {
            User currentUser = userService.getCurrentUser();
            ProcessModel saved = processModelService.create(model, currentUser);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", saved);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ProcessModel model) {
        try {
            ProcessModel saved = processModelService.update(id, model);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("data", saved);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PutMapping("/{id}/bpmn-xml")
    public ResponseEntity<?> updateBpmnXml(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String bpmnXml = request.get("bpmnXml");
            processModelService.updateBpmnXml(id, bpmnXml);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "保存成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            processModelService.delete(id);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "删除成功");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/{id}/deploy")
    public ResponseEntity<?> deploy(@PathVariable Long id) {
        try {
            Map<String, Object> result = processModelService.deploy(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/{id}/undeploy")
    public ResponseEntity<?> undeploy(@PathVariable Long id) {
        try {
            processModelService.undeploy(id);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "已取消部署");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}
