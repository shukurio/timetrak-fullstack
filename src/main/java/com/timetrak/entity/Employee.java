package com.timetrak.entity;


import com.timetrak.enums.EmployeeStatus;
import com.timetrak.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name="employee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Employee extends BaseEntity {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 30, message = "Username must be between 3 and 30 characters")
    @Column(name = "username", nullable = false, unique = true, length = 30)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length =20)
    private EmployeeStatus status;

    @NotNull(message = "Role is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "company_id",nullable = false)
    @NotNull(message = "Company is required")
    private Company company;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @Override
    public void markAsDeleted() {
        super.markAsDeleted();
        this.status = EmployeeStatus.DELETED;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public boolean isActive(){
        return status == EmployeeStatus.ACTIVE;
    }

    public boolean isDeactivated(){
        return status == EmployeeStatus.DEACTIVATED;
    }
    public boolean isPending(){
        return status == EmployeeStatus.PENDING;
    }

    //Overrides IsDeleted from BAseEntity
    public boolean isDeleted(){
        return status == EmployeeStatus.DELETED;
    }
    public boolean isRejected() {
        return this.status == EmployeeStatus.REJECTED;
    }
}