package com.oa.service;

import com.oa.dto.PositionDTO;
import com.oa.entity.Position;
import com.oa.repository.PositionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PositionService {

    @Autowired
    private PositionRepository positionRepository;

    public List<Position> findAll() {
        return positionRepository.findAll();
    }

    public List<Position> search(String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return positionRepository.search(keyword);
        }
        return positionRepository.findAll();
    }

    public Optional<Position> findById(Long id) {
        return positionRepository.findById(id);
    }

    @Transactional
    public Position create(PositionDTO dto) {
        Position position = new Position();
        position.setName(dto.getName());
        position.setDescription(dto.getDescription());
        position.setStatus(dto.getStatus());
        return positionRepository.save(position);
    }

    @Transactional
    public Position update(Long id, PositionDTO dto) {
        Position position = positionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("岗位不存在"));
        position.setName(dto.getName());
        position.setDescription(dto.getDescription());
        position.setStatus(dto.getStatus());
        return positionRepository.save(position);
    }

    @Transactional
    public void delete(Long id) {
        positionRepository.deleteById(id);
    }
}
