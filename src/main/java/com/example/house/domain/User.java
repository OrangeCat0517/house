package com.example.house.domain;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Data
public class User implements UserDetails {
    private Long id;
    private String name;
    private String password;//pwd
    private String email;
    private String phoneNumber;
    private Integer status;
    private LocalDateTime createTime; //DATETIME/TIMESTAMP
    private LocalDateTime lastLoginTime;
    private LocalDateTime lastUpdateTime;
    private String avatar;
    private List<GrantedAuthority> authorityList;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorityList;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return name;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
