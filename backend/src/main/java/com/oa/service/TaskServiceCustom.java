package com.oa.service;

import com.oa.dto.CompleteTaskDTO;
import com.oa.entity.User;
import org.flowable.engine.HistoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TaskServiceCustom {

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private UserService userService;

    public List<Task> getTodoTasks(String assignee) {
        TaskQuery query = taskService.createTaskQuery()
                .taskAssignee(assignee)
                .active()
                .orderByTaskCreateTime().desc();
        return query.list();
    }

    public List<Task> getTodoTasksByCandidate(String candidateUser) {
        TaskQuery query = taskService.createTaskQuery()
                .taskCandidateUser(candidateUser)
                .active()
                .orderByTaskCreateTime().desc();
        return query.list();
    }

    public List<Task> getTasksByProcessInstanceId(String processInstanceId) {
        return taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .list();
    }

    @Transactional
    public Map<String, Object> completeTask(CompleteTaskDTO dto) {
        Task task = taskService.createTaskQuery().taskId(dto.getTaskId()).singleResult();
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        if (dto.getComment() != null && !dto.getComment().isEmpty()) {
            taskService.addComment(dto.getTaskId(), task.getProcessInstanceId(), dto.getComment());
        }
        Map<String, Object> variables = dto.getVariables();
        if (variables == null) {
            variables = new HashMap<>();
        }
        if (dto.getOutcome() != null && !dto.getOutcome().isEmpty()) {
            variables.put("outcome", dto.getOutcome());
        }
        taskService.complete(dto.getTaskId(), variables);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "任务完成");
        return result;
    }

    @Transactional
    public Map<String, Object> claimTask(String taskId) {
        User user = userService.getCurrentUser();
        if (user == null) {
            throw new RuntimeException("用户未登录");
        }
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        taskService.claim(taskId, user.getUsername());
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "任务已签收");
        return result;
    }

    @Transactional
    public Map<String, Object> unclaimTask(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        taskService.unclaim(taskId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "任务已退回");
        return result;
    }

    public List<HistoricTaskInstance> getHistoricTasks(String assignee) {
        return historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(assignee)
                .finished()
                .orderByHistoricTaskInstanceEndTime().desc()
                .list();
    }

    public List<HistoricProcessInstance> getHistoricProcessInstances(String startedBy) {
        return historyService.createHistoricProcessInstanceQuery()
                .startedBy(startedBy)
                .orderByProcessInstanceStartTime().desc()
                .list();
    }

    public List<HistoricActivityInstance> getHistoricActivities(String processInstanceId) {
        return historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime().asc()
                .list();
    }

    public Task getTaskById(String taskId) {
        return taskService.createTaskQuery().taskId(taskId).singleResult();
    }

    public Map<String, Object> getTaskVariables(String taskId) {
        return taskService.getVariables(taskId);
    }
}
