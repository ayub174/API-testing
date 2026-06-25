package com.apitesting.security;

import com.apitesting.model.Account;
import com.apitesting.service.AccountService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Används av AuthenticationManager vid login för att verifiera BCrypt-lösenord
 * och bygga upp authorities från kontots behörigheter.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountService accountService;

    public CustomUserDetailsService(AccountService accountService) {
        this.accountService = accountService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account;
        try {
            account = accountService.findByUsername(username);
        } catch (Exception ex) {
            throw new UsernameNotFoundException("Kontot '" + username + "' hittades inte");
        }
        List<SimpleGrantedAuthority> authorities = account.getPermissions().stream()
                .map(p -> new SimpleGrantedAuthority(p.name()))
                .toList();
        return User.withUsername(account.getUsername())
                .password(account.getPasswordHash())
                .authorities(authorities)
                .build();
    }
}
