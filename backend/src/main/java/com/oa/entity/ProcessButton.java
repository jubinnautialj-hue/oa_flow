package com.oa.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "sys_process_button")
public class ProcessButton {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String buttonCode;

    @Column(nullable = false, length = 100)
    private String buttonName;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private Integer sort = 0;

    @Column(nullable = false)
    private Integer status = 1;

    @Column(name = "process_definition_key", length = 100)
    private String processDefinitionKey;

    @Column(name = "task_definition_key", length = 100)
    private String taskDefinitionKey;

    @Column(name = "button_type", length = 50)
    private String buttonType;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }
}
