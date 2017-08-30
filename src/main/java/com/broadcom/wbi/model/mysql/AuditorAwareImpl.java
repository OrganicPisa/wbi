package com.broadcom.wbi.model.mysql;

import com.broadcom.wbi.security.model.JwtUser;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public String getCurrentAuditor() {
        return (((JwtUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername());
    }
}
