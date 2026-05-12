package com.oa.controller;

import com.oa.entity.FormModel;
import com.oa.service.FormModelService;
import com.oa.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/form-model")
public class FormModelController {

    @Autowired
    private FormModelService formModelService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<FormModel>> list(@RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(formModelService.list(keyword));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FormModel> getById(@PathVariable Long id) {
        FormModel model = formModelService.getById(id);
        if (model == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(model);
    }

    @GetMapping("/key/{formKey}")
    public ResponseEntity<FormModel> getByFormKey(@PathVariable String formKey) {
        FormModel model = formModelService.getByFormKey(formKey);
        if (model == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(model);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody FormModel model) {
        try {
            Long userId = null;
            try {
                userId = userService.getCurrentUser() != null ? userService.getCurrentUser().getId() : null;
            } catch (Exception e) {
                userId = 1L;
            }
            FormModel saved = formModelService.create(model, userId);
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
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody FormModel model) {
        try {
            FormModel saved = formModelService.update(id, model);
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

    @PutMapping("/{id}/schema")
    public ResponseEntity<?> updateFormSchema(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            String formSchema = request.get("formSchema") != null ? 
                (request.get("formSchema") instanceof String ? 
                    (String) request.get("formSchema") : 
                    request.get("formSchema").toString()) : "";
            formModelService.updateFormSchema(id, formSchema);
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
            formModelService.delete(id);
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
}
