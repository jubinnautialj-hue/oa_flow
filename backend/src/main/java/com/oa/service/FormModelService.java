package com.oa.service;

import com.oa.entity.FormModel;
import com.oa.repository.FormModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FormModelService {

    @Autowired
    private FormModelRepository formModelRepository;

    public List<FormModel> list(String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return formModelRepository.search(keyword);
        }
        return formModelRepository.findAll();
    }

    public FormModel getById(Long id) {
        return formModelRepository.findById(id).orElse(null);
    }

    public FormModel getByFormKey(String formKey) {
        return formModelRepository.findByFormKey(formKey).orElse(null);
    }

    @Transactional
    public FormModel create(FormModel model, Long userId) {
        if (formModelRepository.existsByFormKey(model.getFormKey())) {
            throw new RuntimeException("表单Key已存在");
        }
        model.setCreateUserId(userId);
        return formModelRepository.save(model);
    }

    @Transactional
    public FormModel update(Long id, FormModel model) {
        FormModel exist = formModelRepository.findById(id).orElse(null);
        if (exist == null) {
            throw new RuntimeException("表单模型不存在");
        }
        
        FormModel byKey = formModelRepository.findByFormKey(model.getFormKey()).orElse(null);
        if (byKey != null && !byKey.getId().equals(id)) {
            throw new RuntimeException("表单Key已存在");
        }
        
        exist.setName(model.getName());
        exist.setFormKey(model.getFormKey());
        exist.setDescription(model.getDescription());
        return formModelRepository.save(exist);
    }

    @Transactional
    public void updateFormSchema(Long id, String formSchema) {
        FormModel exist = formModelRepository.findById(id).orElse(null);
        if (exist == null) {
            throw new RuntimeException("表单模型不存在");
        }
        exist.setFormSchema(formSchema);
        formModelRepository.save(exist);
    }

    @Transactional
    public void delete(Long id) {
        formModelRepository.deleteById(id);
    }
}
