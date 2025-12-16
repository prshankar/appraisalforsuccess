package com.cfs.pojo.entity;

import static javax.persistence.TemporalType.TIMESTAMP;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.Temporal;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class EntityLog {
	
    @CreatedDate
    @Temporal(TIMESTAMP)
    @Column(name = "creation_date")
    protected Date creationDate;

    @LastModifiedDate
    @Temporal(TIMESTAMP)
    @Column(name = "update_date")
    protected Date updateDate;
    
    @CreatedBy
    @Column(name = "created_by", nullable = true, updatable = false)
    protected Long createdBy;
    
    @LastModifiedBy
    @Column(name = "updated_by")
    protected Long updatedBy;

    @PrePersist
    public void onPrePersist() {
    	//setCreatedBy("");
    }
}
