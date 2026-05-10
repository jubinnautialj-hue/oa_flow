package com.oa.service;

import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProcessService {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    public List<ProcessDefinition> getProcessDefinitions() {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery()
                .latestVersion()
                .orderByProcessDefinitionName().asc();
        return query.list();
    }

    @Transactional
    public Deployment deployByString(String bpmnXml, String processName) {
        return repositoryService.createDeployment()
                .addString(processName + ".bpmn20.xml", bpmnXml)
                .name(processName)
                .deploy();
    }

    @Transactional
    public Deployment deployByJson(String jsonString, String processName) {
        throw new RuntimeException("JSON格式部署功能暂未启用，请使用XML格式部署");
    }

    public Map<String, Object> startProcess(String processDefinitionKey, String businessKey, String initiator, Map<String, Object> variables) {
        if (variables == null) {
            variables = new HashMap<>();
        }
        variables.put("initiator", initiator);
        String processInstanceId = runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, variables).getId();
        
        Map<String, Object> result = new HashMap<>();
        result.put("processInstanceId", processInstanceId);
        result.put("success", true);
        return result;
    }

    @Transactional
    public void deleteDeployment(String deploymentId) {
        repositoryService.deleteDeployment(deploymentId, true);
    }

    public ProcessDefinition getProcessDefinitionById(String processDefinitionId) {
        return repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .singleResult();
    }

    public List<Deployment> getDeployments() {
        return repositoryService.createDeploymentQuery()
                .orderByDeploymentTime().desc()
                .list();
    }
}
