package com.oa.service;

import com.oa.dto.DeploymentDTO;
import com.oa.dto.ProcessDefinitionDTO;
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
import java.util.stream.Collectors;

@Service
public class ProcessService {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    public List<ProcessDefinitionDTO> getProcessDefinitions() {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery()
                .latestVersion()
                .orderByProcessDefinitionName().asc();
        return query.list().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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

    public ProcessDefinitionDTO getProcessDefinitionById(String processDefinitionId) {
        ProcessDefinition pd = repositoryService.createProcessDefinitionQuery()
                .processDefinitionId(processDefinitionId)
                .singleResult();
        return pd != null ? convertToDTO(pd) : null;
    }

    public List<DeploymentDTO> getDeployments() {
        return repositoryService.createDeploymentQuery()
                .orderByDeploymentTime().desc()
                .list().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ProcessDefinitionDTO convertToDTO(ProcessDefinition pd) {
        ProcessDefinitionDTO dto = new ProcessDefinitionDTO();
        dto.setId(pd.getId());
        dto.setName(pd.getName());
        dto.setKey(pd.getKey());
        dto.setVersion(pd.getVersion());
        dto.setDeploymentId(pd.getDeploymentId());
        dto.setDescription(pd.getDescription());
        return dto;
    }

    private DeploymentDTO convertToDTO(Deployment deployment) {
        DeploymentDTO dto = new DeploymentDTO();
        dto.setId(deployment.getId());
        dto.setName(deployment.getName());
        dto.setDeploymentTime(deployment.getDeploymentTime());
        dto.setCategory(deployment.getCategory());
        dto.setTenantId(deployment.getTenantId());
        return dto;
    }
}
