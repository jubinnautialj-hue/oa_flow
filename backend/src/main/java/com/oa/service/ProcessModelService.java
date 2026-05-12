package com.oa.service;

import com.oa.entity.ProcessModel;
import com.oa.entity.User;
import com.oa.repository.ProcessModelRepository;
import com.oa.repository.UserRepository;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProcessModelService {

    @Autowired
    private ProcessModelRepository processModelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RepositoryService repositoryService;

    public List<ProcessModel> list(String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return processModelRepository.search(keyword);
        }
        return processModelRepository.findAll();
    }

    public ProcessModel getById(Long id) {
        return processModelRepository.findById(id).orElse(null);
    }

    @Transactional
    public ProcessModel create(ProcessModel model, User currentUser) {
        if (processModelRepository.existsByProcessKey(model.getProcessKey())) {
            throw new RuntimeException("流程Key已存在");
        }
        model.setCreateUser(currentUser);
        model.setStatus(0);
        return processModelRepository.save(model);
    }

    @Transactional
    public ProcessModel update(Long id, ProcessModel model) {
        ProcessModel exist = processModelRepository.findById(id).orElse(null);
        if (exist == null) {
            throw new RuntimeException("流程模型不存在");
        }
        
        ProcessModel byKey = processModelRepository.findByProcessKey(model.getProcessKey()).orElse(null);
        if (byKey != null && !byKey.getId().equals(id)) {
            throw new RuntimeException("流程Key已存在");
        }
        
        exist.setName(model.getName());
        exist.setProcessKey(model.getProcessKey());
        exist.setDescription(model.getDescription());
        return processModelRepository.save(exist);
    }

    @Transactional
    public void updateBpmnXml(Long id, String bpmnXml) {
        ProcessModel exist = processModelRepository.findById(id).orElse(null);
        if (exist == null) {
            throw new RuntimeException("流程模型不存在");
        }
        exist.setBpmnXml(bpmnXml);
        processModelRepository.save(exist);
    }

    @Transactional
    public void delete(Long id) {
        ProcessModel model = processModelRepository.findById(id).orElse(null);
        if (model != null && model.getDeploymentId() != null) {
            try {
                repositoryService.deleteDeployment(model.getDeploymentId(), true);
            } catch (Exception e) {
                throw new RuntimeException("删除已部署的流程失败: " + e.getMessage());
            }
        }
        processModelRepository.deleteById(id);
    }

    @Transactional
    public Map<String, Object> deploy(Long id) {
        ProcessModel model = processModelRepository.findById(id).orElse(null);
        if (model == null) {
            throw new RuntimeException("流程模型不存在");
        }
        if (model.getBpmnXml() == null || model.getBpmnXml().isEmpty()) {
            throw new RuntimeException("流程XML为空，请先设计流程");
        }
        
        if (model.getDeploymentId() != null) {
            try {
                repositoryService.deleteDeployment(model.getDeploymentId(), true);
            } catch (Exception e) {
                System.err.println("删除旧部署失败: " + e.getMessage());
            }
        }
        
        Deployment deployment = repositoryService.createDeployment()
                .addString(model.getName() + ".bpmn20.xml", model.getBpmnXml())
                .name(model.getName())
                .key(model.getProcessKey())
                .deploy();
        
        model.setDeploymentId(deployment.getId());
        model.setStatus(1);
        processModelRepository.save(model);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("deploymentId", deployment.getId());
        result.put("deploymentName", deployment.getName());
        return result;
    }

    @Transactional
    public void undeploy(Long id) {
        ProcessModel model = processModelRepository.findById(id).orElse(null);
        if (model == null) {
            throw new RuntimeException("流程模型不存在");
        }
        if (model.getDeploymentId() != null) {
            repositoryService.deleteDeployment(model.getDeploymentId(), true);
            model.setDeploymentId(null);
            model.setStatus(0);
            processModelRepository.save(model);
        }
    }
}
