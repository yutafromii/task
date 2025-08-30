package com.example.ecapp.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import com.example.ecapp.domain.AppUser;
import com.example.ecapp.repository.UserRepository;

@Service
public class MyUserDetailsService implements UserDetailsService {

  private final UserRepository userRepository;
  @Autowired
  public MyUserDetailsService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }
  @Override
  public UserDetails loadUserByUsername(String email) {
    AppUser appUser = userRepository.findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません: " + email));

    return new User(
        appUser.getEmail(),
        appUser.getPassword(),
        Collections.singletonList(new SimpleGrantedAuthority(appUser.getRole())));
  }
}
