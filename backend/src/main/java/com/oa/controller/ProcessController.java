package com.oa.controller;

import com.oa.dto.DeploymentDTO;
import com.oa.dto.ProcessDefinitionDTO;
import com.oa.dto.StartProcessDTO;
import com.oa.entity.User;
import com.oa.service.ProcessService;
import com.oa.service.UserService;
import org.flowable.engine.repository.Deployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/process")
public class ProcessController {

    @Autowired
    private ProcessService processService;

    @Autowired
    private UserService userService;

    @GetMapping("/definitions")
    public ResponseEntity<List<ProcessDefinitionDTO>> getProcessDefinitions() {
        return ResponseEntity.ok(processService.getProcessDefinitions());
    }

    @GetMapping("/deployments")
    public ResponseEntity<List<DeploymentDTO>> getDeployments() {
        return ResponseEntity.ok(processService.getDeployments());
    }

    @PostMapping("/deploy/xml")
    public ResponseEntity<?> deployByXml(@RequestBody Map<String, String> request) {
        try {
            String bpmnXml = request.get("bpmnXml");
            String processName = request.get("processName");
            if (processName == null || processName.isEmpty()) {
                processName = "Process_" + System.currentTimeMillis();
            }
            Deployment deployment = processService.deployByString(bpmnXml, processName);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("deploymentId", deployment.getId());
            result.put("deploymentName", deployment.getName());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/deploy/json")
    public ResponseEntity<?> deployByJson(@RequestBody Map<String, String> request) {
        try {
            String jsonString = request.get("jsonString");
            String processName = request.get("processName");
            if (processName == null || processName.isEmpty()) {
                processName = "Process_" + System.currentTimeMillis();
            }
            Deployment deployment = processService.deployByJson(jsonString, processName);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("deploymentId", deployment.getId());
            result.put("deploymentName", deployment.getName());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/start")
    public ResponseEntity<?> startProcess(@RequestBody StartProcessDTO dto) {
        try {
            User currentUser = userService.getCurrentUser();
            String initiator = currentUser != null ? currentUser.getUsername() : "system";
            
            Map<String, Object> variables = new HashMap<>();
            variables.put("title", dto.getTitle());
            variables.put("businessKey", dto.getBusinessKey());
            
            Map<String, Object> result = processService.startProcess(
                    dto.getProcessDefinitionKey(),
                    dto.getBusinessKey(),
                    initiator,
                    variables
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @DeleteMapping("/deployments/{deploymentId}")
    public ResponseEntity<?> deleteDeployment(@PathVariable String deploymentId) {
        try {
            processService.deleteDeployment(deploymentId);
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

    @GetMapping("/definitions/{processDefinitionId}")
    public ResponseEntity<ProcessDefinitionDTO> getProcessDefinitionById(@PathVariable String processDefinitionId) {
        ProcessDefinitionDTO pd = processService.getProcessDefinitionById(processDefinitionId);
        if (pd == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(pd);
    }

    @PostMapping("/definitions/{processDefinitionId}/suspend")
    public ResponseEntity<?> suspendProcessDefinition(@PathVariable String processDefinitionId) {
        try {
            processService.suspendProcessDefinition(processDefinitionId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "流程定义已挂起");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/definitions/{processDefinitionId}/activate")
    public ResponseEntity<?> activateProcessDefinition(@PathVariable String processDefinitionId) {
        try {
            processService.activateProcessDefinition(processDefinitionId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "流程定义已激活");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }
}
