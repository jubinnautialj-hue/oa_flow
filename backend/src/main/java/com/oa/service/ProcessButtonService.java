package com.oa.service;

import com.oa.dto.ProcessButtonDTO;
import com.oa.entity.ProcessButton;
import com.oa.repository.ProcessButtonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@Service
public class ProcessButtonService {

    @Autowired
    private ProcessButtonRepository processButtonRepository;

    public List<ProcessButton> findAll() {
        return processButtonRepository.findAllByOrderBySortAsc();
    }

    public List<ProcessButton> search(String keyword) {
        if (StringUtils.hasText(keyword)) {
            return processButtonRepository.search(keyword);
        }
        return processButtonRepository.findAllByOrderBySortAsc();
    }

    public Optional<ProcessButton> findById(Long id) {
        return processButtonRepository.findById(id);
    }

    public List<ProcessButton> findByProcessDefinitionKey(String processDefinitionKey) {
        return processButtonRepository.findByProcessDefinitionKeyOrderBySortAsc(processDefinitionKey);
    }

    public List<ProcessButton> findByProcessAndTask(String processDefinitionKey, String taskDefinitionKey) {
        return processButtonRepository.findByProcessDefinitionKeyAndTaskDefinitionKeyOrderBySortAsc(processDefinitionKey, taskDefinitionKey);
    }

    @Transactional
    public ProcessButton create(ProcessButtonDTO dto) {
        ProcessButton button = new ProcessButton();
        button.setButtonCode(dto.getButtonCode());
        button.setButtonName(dto.getButtonName());
        button.setDescription(dto.getDescription());
        button.setSort(dto.getSort() != null ? dto.getSort() : 0);
        button.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        button.setProcessDefinitionKey(dto.getProcessDefinitionKey());
        button.setTaskDefinitionKey(dto.getTaskDefinitionKey());
        button.setButtonType(dto.getButtonType());
        return processButtonRepository.save(button);
    }

    @Transactional
    public ProcessButton update(Long id, ProcessButtonDTO dto) {
        ProcessButton button = processButtonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("按钮不存在"));
        button.setButtonCode(dto.getButtonCode());
        button.setButtonName(dto.getButtonName());
        button.setDescription(dto.getDescription());
        if (dto.getSort() != null) {
            button.setSort(dto.getSort());
        }
        if (dto.getStatus() != null) {
            button.setStatus(dto.getStatus());
        }
        button.setProcessDefinitionKey(dto.getProcessDefinitionKey());
        button.setTaskDefinitionKey(dto.getTaskDefinitionKey());
        button.setButtonType(dto.getButtonType());
        return processButtonRepository.save(button);
    }

    @Transactional
    public void delete(Long id) {
        processButtonRepository.deleteById(id);
    }
}
