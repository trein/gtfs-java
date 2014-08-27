package com.trein.gtfs.jpa.entity.app;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity(name = "audit")
@Cache(region = "entity", usage = CacheConcurrencyStrategy.READ_WRITE)
public class Audit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "created_at", nullable = false)
    private Date createdAt;
    
    @Column(name = "requested_uri", nullable = false, columnDefinition = "TEXT")
    private String requestedUri;
    
    @Column(name = "remote_user", nullable = true)
    private String remoteUser;
    
    @Column(name = "remote_address", nullable = false, columnDefinition = "TEXT")
    private String remoteAddress;
    
    @Column(name = "remote_port", nullable = false)
    private String remotePort;
    
    @Column(name = "signature", columnDefinition = "TEXT")
    private String signature;
    
    @Column(name = "comment", nullable = false)
    private String comment;
    
    public Long getId() {
        return this.id;
    }
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
    }
    
    public Date getCreatedAt() {
        return this.createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getRequestedUri() {
        return this.requestedUri;
    }
    
    public void setRequestedUri(String requestedUri) {
        this.requestedUri = requestedUri;
    }
    
    public String getRemoteAddress() {
        return this.remoteAddress;
    }
    
    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }
    
    public String getRemoteUser() {
        return this.remoteUser;
    }
    
    public void setRemoteUser(String remoteUser) {
        this.remoteUser = remoteUser;
    }
    
    public String getRemotePort() {
        return this.remotePort;
    }
    
    public void setRemotePort(String remotePort) {
        this.remotePort = remotePort;
    }
    
    public String getSignature() {
        return this.signature;
    }
    
    public void setSignature(String signature) {
        this.signature = signature;
    }
    
    public String getComment() {
        return this.comment;
    }
    
    public void setComment(String comment) {
        this.comment = comment;
    }
    
    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }
    
    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }
    
    @Override
    public String toString() {
        return new ReflectionToStringBuilder(this).build();
    }
}
