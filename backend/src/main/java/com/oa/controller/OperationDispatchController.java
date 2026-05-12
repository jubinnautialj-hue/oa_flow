package com.oa.controller;

import com.oa.dto.HistoricProcessInstanceDTO;
import com.oa.dto.TaskDTO;
import com.oa.service.ProcessService;
import com.oa.service.TaskServiceCustom;
import org.flowable.engine.history.HistoricProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/operation")
public class OperationDispatchController {

    @Autowired
    private ProcessService processService;

    @Autowired
    private TaskServiceCustom taskServiceCustom;

    @GetMapping("/process-instances")
    public ResponseEntity<List<Map<String, Object>>> getProcessInstances(
            @RequestParam(required = false) String status) {
        List<HistoricProcessInstance> instances;
        if ("running".equals(status)) {
            instances = processService.getRunningProcessInstances();
        } else {
            instances = processService.getAllProcessInstances();
        }
        
        List<Map<String, Object>> result = instances.stream().map(hpi -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", hpi.getId());
            map.put("processDefinitionId", hpi.getProcessDefinitionId());
            map.put("processDefinitionKey", hpi.getProcessDefinitionKey());
            map.put("processDefinitionName", hpi.getProcessDefinitionName());
            map.put("processDefinitionVersion", hpi.getProcessDefinitionVersion());
            map.put("businessKey", hpi.getBusinessKey());
            map.put("startTime", hpi.getStartTime());
            map.put("endTime", hpi.getEndTime());
            map.put("startUserId", hpi.getStartUserId());
            map.put("deleteReason", hpi.getDeleteReason());
            map.put("durationInMillis", hpi.getDurationInMillis());
            map.put("status", hpi.getEndTime() == null ? "RUNNING" : "ENDED");
            return map;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }

    @GetMapping("/process-instance/{processInstanceId}")
    public ResponseEntity<?> getProcessInstance(@PathVariable String processInstanceId) {
        HistoricProcessInstance hpi = processService.getProcessInstance(processInstanceId);
        if (hpi == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> map = new HashMap<>();
        map.put("id", hpi.getId());
        map.put("processDefinitionId", hpi.getProcessDefinitionId());
        map.put("processDefinitionKey", hpi.getProcessDefinitionKey());
        map.put("processDefinitionName", hpi.getProcessDefinitionName());
        map.put("processDefinitionVersion", hpi.getProcessDefinitionVersion());
        map.put("businessKey", hpi.getBusinessKey());
        map.put("startTime", hpi.getStartTime());
        map.put("endTime", hpi.getEndTime());
        map.put("startUserId", hpi.getStartUserId());
        map.put("deleteReason", hpi.getDeleteReason());
        map.put("durationInMillis", hpi.getDurationInMillis());
        map.put("status", hpi.getEndTime() == null ? "RUNNING" : "ENDED");
        
        return ResponseEntity.ok(map);
    }

    @GetMapping("/process-instance/{processInstanceId}/tasks")
    public ResponseEntity<List<TaskDTO>> getProcessInstanceTasks(@PathVariable String processInstanceId) {
        return ResponseEntity.ok(taskServiceCustom.getTasksByProcessInstanceId(processInstanceId));
    }

    @GetMapping(value = "/process-instance/{processInstanceId}/diagram", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getProcessInstanceDiagram(@PathVariable String processInstanceId) {
        try {
            InputStream inputStream = processService.getProcessDiagram(processInstanceId);
            if (inputStream == null) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] bytes = inputStream.readAllBytes();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            return ResponseEntity.ok().headers(headers).body(bytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/process-definition/{processDefinitionId}/diagram", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getProcessDefinitionDiagram(@PathVariable String processDefinitionId) {
        try {
            InputStream inputStream = processService.getProcessDefinitionDiagram(processDefinitionId);
            if (inputStream == null) {
                return ResponseEntity.notFound().build();
            }
            
            byte[] bytes = inputStream.readAllBytes();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            return ResponseEntity.ok().headers(headers).body(bytes);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/task/{taskId}/complete")
    public ResponseEntity<?> completeTask(
            @PathVariable String taskId,
            @RequestBody Map<String, String> request) {
        try {
            String comment = request.get("comment");
            String outcome = request.getOrDefault("outcome", "agree");
            Map<String, Object> result = taskServiceCustom.completeTaskByAdmin(taskId, comment, outcome);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/task/{taskId}/reject")
    public ResponseEntity<?> rejectTask(
            @PathVariable String taskId,
            @RequestBody Map<String, String> request) {
        try {
            String comment = request.get("comment");
            Map<String, Object> result = taskServiceCustom.rejectTask(taskId, comment);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/task/{taskId}/assignee")
    public ResponseEntity<?> setAssignee(
            @PathVariable String taskId,
            @RequestBody Map<String, String> request) {
        try {
            String assignee = request.get("assignee");
            if (assignee == null || assignee.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "审核人不能为空");
                return ResponseEntity.badRequest().body(result);
            }
            Map<String, Object> result = taskServiceCustom.setAssignee(taskId, assignee);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/task/{taskId}/transfer")
    public ResponseEntity<?> transferTask(
            @PathVariable String taskId,
            @RequestBody Map<String, String> request) {
        try {
            String assignee = request.get("assignee");
            if (assignee == null || assignee.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "转办人不能为空");
                return ResponseEntity.badRequest().body(result);
            }
            Map<String, Object> result = taskServiceCustom.transferTask(taskId, assignee);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/task/{taskId}/delegate")
    public ResponseEntity<?> delegateTask(
            @PathVariable String taskId,
            @RequestBody Map<String, String> request) {
        try {
            String assignee = request.get("assignee");
            if (assignee == null || assignee.isEmpty()) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("message", "委托人不能为空");
                return ResponseEntity.badRequest().body(result);
            }
            Map<String, Object> result = taskServiceCustom.delegateTask(taskId, assignee);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @DeleteMapping("/process-instance/{processInstanceId}")
    public ResponseEntity<?> terminateProcessInstance(
            @PathVariable String processInstanceId,
            @RequestParam(required = false) String reason) {
        try {
            processService.deleteProcessInstance(processInstanceId, reason != null ? reason : "运维终止");
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "流程已终止");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}
