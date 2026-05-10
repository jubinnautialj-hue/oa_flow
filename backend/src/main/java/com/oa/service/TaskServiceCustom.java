package com.oa.service;

import com.oa.dto.CompleteTaskDTO;
import com.oa.dto.HistoricActivityInstanceDTO;
import com.oa.dto.HistoricProcessInstanceDTO;
import com.oa.dto.HistoricTaskInstanceDTO;
import com.oa.dto.TaskDTO;
import com.oa.entity.User;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TaskServiceCustom {

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private UserService userService;

    public List<TaskDTO> getTodoTasks(String assignee) {
        TaskQuery query = taskService.createTaskQuery()
                .taskAssignee(assignee)
                .active()
                .orderByTaskCreateTime().desc();
        return query.list().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<TaskDTO> getTodoTasksByCandidate(String candidateUser) {
        TaskQuery query = taskService.createTaskQuery()
                .taskCandidateUser(candidateUser)
                .active()
                .orderByTaskCreateTime().desc();
        return query.list().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<TaskDTO> getTasksByProcessInstanceId(String processInstanceId) {
        return taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .list().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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

    public List<HistoricTaskInstanceDTO> getHistoricTasks(String assignee) {
        return historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(assignee)
                .finished()
                .orderByHistoricTaskInstanceEndTime().desc()
                .list().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<HistoricProcessInstanceDTO> getHistoricProcessInstances(String startedBy) {
        return historyService.createHistoricProcessInstanceQuery()
                .startedBy(startedBy)
                .orderByProcessInstanceStartTime().desc()
                .list().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<HistoricActivityInstanceDTO> getHistoricActivities(String processInstanceId) {
        return historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime().asc()
                .list().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TaskDTO getTaskById(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        return task != null ? convertToDTO(task) : null;
    }

    public Map<String, Object> getTaskVariables(String taskId) {
        return taskService.getVariables(taskId);
    }

    private TaskDTO convertToDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setName(task.getName());
        dto.setDescription(task.getDescription());
        dto.setProcessInstanceId(task.getProcessInstanceId());
        dto.setProcessDefinitionId(task.getProcessDefinitionId());
        dto.setAssignee(task.getAssignee());
        dto.setOwner(task.getOwner());
        dto.setCreateTime(task.getCreateTime());
        dto.setDueDate(task.getDueDate());
        dto.setPriority(String.valueOf(task.getPriority()));
        dto.setCategory(task.getCategory());
        dto.setFormKey(task.getFormKey());
        dto.setParentTaskId(task.getParentTaskId());
        dto.setTenantId(task.getTenantId());
        
        if (task.getProcessDefinitionId() != null) {
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(task.getProcessDefinitionId())
                    .singleResult();
            if (pd != null) {
                dto.setProcessDefinitionName(pd.getName());
                dto.setProcessDefinitionKey(pd.getKey());
            }
        }
        
        return dto;
    }

    private HistoricTaskInstanceDTO convertToDTO(HistoricTaskInstance task) {
        HistoricTaskInstanceDTO dto = new HistoricTaskInstanceDTO();
        dto.setId(task.getId());
        dto.setName(task.getName());
        dto.setDescription(task.getDescription());
        dto.setProcessInstanceId(task.getProcessInstanceId());
        dto.setProcessDefinitionId(task.getProcessDefinitionId());
        dto.setAssignee(task.getAssignee());
        dto.setOwner(task.getOwner());
        dto.setStartTime(task.getStartTime());
        dto.setEndTime(task.getEndTime());
        dto.setDurationInMillis(task.getDurationInMillis());
        dto.setWorkTimeInMillis(task.getWorkTimeInMillis());
        dto.setDeleteReason(task.getDeleteReason());
        dto.setTaskDefinitionKey(task.getTaskDefinitionKey());
        dto.setPriority(task.getPriority());
        dto.setDueDate(task.getDueDate());
        dto.setParentTaskId(task.getParentTaskId());
        dto.setCategory(task.getCategory());
        dto.setTenantId(task.getTenantId());
        
        if (task.getProcessDefinitionId() != null) {
            ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(task.getProcessDefinitionId())
                    .singleResult();
            if (pd != null) {
                dto.setProcessDefinitionName(pd.getName());
                dto.setProcessDefinitionKey(pd.getKey());
            }
        }
        
        return dto;
    }

    private HistoricProcessInstanceDTO convertToDTO(HistoricProcessInstance pi) {
        HistoricProcessInstanceDTO dto = new HistoricProcessInstanceDTO();
        dto.setId(pi.getId());
        dto.setName(pi.getName());
        dto.setBusinessKey(pi.getBusinessKey());
        dto.setProcessDefinitionId(pi.getProcessDefinitionId());
        dto.setProcessDefinitionKey(pi.getProcessDefinitionKey());
        dto.setProcessDefinitionName(pi.getProcessDefinitionName());
        dto.setProcessDefinitionVersion(pi.getProcessDefinitionVersion());
        dto.setStartTime(pi.getStartTime());
        dto.setEndTime(pi.getEndTime());
        dto.setDurationInMillis(pi.getDurationInMillis());
        dto.setStartUserId(pi.getStartUserId());
        dto.setStartActivityId(pi.getStartActivityId());
        dto.setEndActivityId(pi.getEndActivityId());
        dto.setDeleteReason(pi.getDeleteReason());
        dto.setTenantId(pi.getTenantId());
        return dto;
    }

    private HistoricActivityInstanceDTO convertToDTO(HistoricActivityInstance ai) {
        HistoricActivityInstanceDTO dto = new HistoricActivityInstanceDTO();
        dto.setId(ai.getId());
        dto.setActivityId(ai.getActivityId());
        dto.setActivityName(ai.getActivityName());
        dto.setActivityType(ai.getActivityType());
        dto.setProcessDefinitionId(ai.getProcessDefinitionId());
        dto.setProcessInstanceId(ai.getProcessInstanceId());
        dto.setExecutionId(ai.getExecutionId());
        dto.setTaskId(ai.getTaskId());
        dto.setAssignee(ai.getAssignee());
        dto.setStartTime(ai.getStartTime());
        dto.setEndTime(ai.getEndTime());
        dto.setDurationInMillis(ai.getDurationInMillis());
        dto.setDeleteReason(ai.getDeleteReason());
        dto.setTenantId(ai.getTenantId());
        return dto;
    }
}
