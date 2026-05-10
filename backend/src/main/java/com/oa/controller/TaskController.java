package com.oa.controller;

import com.oa.dto.CompleteTaskDTO;
import com.oa.entity.User;
import com.oa.service.TaskServiceCustom;
import com.oa.service.UserService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/task")
public class TaskController {

    @Autowired
    private TaskServiceCustom taskService;

    @Autowired
    private UserService userService;

    @GetMapping("/todo")
    public ResponseEntity<List<Task>> getTodoTasks() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.badRequest().build();
        }
        List<Task> todoTasks = taskService.getTodoTasks(currentUser.getUsername());
        return ResponseEntity.ok(todoTasks);
    }

    @GetMapping("/candidate")
    public ResponseEntity<List<Task>> getCandidateTasks() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.badRequest().build();
        }
        List<Task> tasks = taskService.getTodoTasksByCandidate(currentUser.getUsername());
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<Task> getTaskById(@PathVariable String taskId) {
        Task task = taskService.getTaskById(taskId);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task);
    }

    @GetMapping("/{taskId}/variables")
    public ResponseEntity<Map<String, Object>> getTaskVariables(@PathVariable String taskId) {
        return ResponseEntity.ok(taskService.getTaskVariables(taskId));
    }

    @PostMapping("/complete")
    public ResponseEntity<?> completeTask(@RequestBody CompleteTaskDTO dto) {
        try {
            Map<String, Object> result = taskService.completeTask(dto);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/{taskId}/claim")
    public ResponseEntity<?> claimTask(@PathVariable String taskId) {
        try {
            Map<String, Object> result = taskService.claimTask(taskId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/{taskId}/unclaim")
    public ResponseEntity<?> unclaimTask(@PathVariable String taskId) {
        try {
            Map<String, Object> result = taskService.unclaimTask(taskId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/history/tasks")
    public ResponseEntity<List<HistoricTaskInstance>> getHistoricTasks() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(taskService.getHistoricTasks(currentUser.getUsername()));
    }

    @GetMapping("/history/processes")
    public ResponseEntity<List<HistoricProcessInstance>> getHistoricProcesses() {
        User currentUser = userService.getCurrentUser();
        if (currentUser == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(taskService.getHistoricProcessInstances(currentUser.getUsername()));
    }

    @GetMapping("/history/process/{processInstanceId}/activities")
    public ResponseEntity<List<HistoricActivityInstance>> getHistoricActivities(@PathVariable String processInstanceId) {
        return ResponseEntity.ok(taskService.getHistoricActivities(processInstanceId));
    }
}
