package com.oa.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wf_process_model")
public class ProcessModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, nullable = false, length = 100)
    private String processKey;

    @Column(length = 500)
    private String description;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String bpmnXml;

    @Column(nullable = false)
    private Integer status = 0;

    @Column(name = "deployment_id")
    private String deploymentId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "create_user_id")
    private User createUser;

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
