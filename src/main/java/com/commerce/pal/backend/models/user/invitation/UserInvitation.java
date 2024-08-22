package com.commerce.pal.backend.models.user.invitation;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "UserInvitation")
@Getter
@Setter
public class UserInvitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "PhoneNumber", unique = true, nullable = false)
    private String phoneNumber;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "InvitationCustomers", joinColumns = @JoinColumn(name = "InvitationId"))
    @Column(name = "LoginValidationId")
    private Set<Long> invitedBy = new HashSet<>();

    @Column(name = "CreatedAt", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UpdatedAt", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Set<Long> getInvitedBy() {
        return Collections.unmodifiableSet(invitedBy);
    }

    public void addInvitedBy(Long customerId) {
        this.invitedBy.add(customerId);
    }
}

