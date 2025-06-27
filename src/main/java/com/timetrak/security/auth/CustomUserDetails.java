package com.timetrak.security.auth;

import com.timetrak.entity.Employee;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;


public class CustomUserDetails implements UserDetails {
    private final Employee employee;

    public CustomUserDetails(Employee employee) {
        this.employee = employee;
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + employee.getRole().name()));
    }

    public Employee getEmployee() {
        return employee;
    }

    public Long getCompanyId() {
        return employee.getCompany() != null ? employee.getCompany().getId() : null;
    }

    @Override
    public String getPassword() {
        return employee.getPassword();
    }

    @Override
    public String getUsername() {
        return employee.getUsername();
    }

}
