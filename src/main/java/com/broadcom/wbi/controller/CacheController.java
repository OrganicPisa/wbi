package com.broadcom.wbi.controller;

import com.broadcom.wbi.exception.IDNotFoundException;
import com.broadcom.wbi.service.elasticSearch.RevisionSearchService;
import com.broadcom.wbi.service.jpa.RedisCacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.concurrent.Callable;

@RestController
@RequestMapping("/api/cache")
public class CacheController {

    @Autowired
    private RedisCacheRepository redisCacheRepository;

    @Autowired
    private RevisionSearchService revisionSearchService;

    @PreAuthorize("hasAnyRole('PM', 'ADMIN')")
//    @PreAuthorize("@employeePermissionServiceImpl.hasPermission(authentication, 'pm,ipm,cpm,swpm,ippm,admin')")
    @RequestMapping(value = {"/clear/program"}, method = {RequestMethod.GET})
    public Callable<HashMap> clearCache(HttpServletRequest req,
                                        @RequestParam(value = "pid", defaultValue = "0") final int pid,
                                        @RequestParam(value = "key", defaultValue = "") final String key) {
        return new Callable<HashMap>() {
            public HashMap call() {
                if (pid < 1)
                    throw new IDNotFoundException(pid, "program");
                redisCacheRepository.clearCache(pid, key, "program");
                return null;
            }
        };
    }
}
