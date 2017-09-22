package com.broadcom.wbi.controller;

import com.broadcom.wbi.model.mysql.Program;
import com.broadcom.wbi.model.mysql.Revision;
import com.broadcom.wbi.service.elasticSearch.RevisionSearchService;
import com.broadcom.wbi.service.jpa.ProgramService;
import com.broadcom.wbi.service.jpa.RedisCacheRepository;
import com.broadcom.wbi.service.jpa.RevisionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.WebAsyncTask;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('ADMIN')")
public class AdminController {
    @Autowired
    RedisCacheRepository redisCacheRepository;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RevisionService revisionService;
    @Autowired
    private RevisionSearchService revisionSearchService;
    @Autowired
    private ProgramService programService;

    @RequestMapping(value = {"/cache/clear/all"}, method = {RequestMethod.GET})
    public Callable<HashMap> clearCache(HttpServletRequest req) {
        return new Callable<HashMap>() {
            public HashMap call() {
                redisTemplate.getConnectionFactory().getConnection().flushAll();
                return null;
            }
        };
    }

    @RequestMapping(value = {"/delete/revision"}, method = {RequestMethod.POST})
    @ResponseBody
    public WebAsyncTask<ResponseEntity> deleteRevision(HttpServletRequest req, HttpServletResponse res,
                                                       @RequestParam(value = "rid", defaultValue = "0") final int rid) {
        Callable<ResponseEntity> callable = new Callable<ResponseEntity>() {
            @Override
            public ResponseEntity call() throws Exception {
                Map ret = new HashMap();
                ret.put("url", "deleted");
                if (rid < 1)
                    return null;

                Revision revision = revisionService.findById(rid);
                if (revision == null)
                    return null;
                revisionSearchService.delete(Integer.toString(rid));
                Program program = revision.getProgram();
                if (program != null)
                    redisCacheRepository.clearCache(program.getId(), "", "program");

                return ResponseEntity.ok(ret);
            }
        };
        return new WebAsyncTask<ResponseEntity>(1800000, callable);
    }

    @RequestMapping(value = {"/delete/program"}, method = {RequestMethod.POST})
    @ResponseBody
    public WebAsyncTask<ResponseEntity> deleteProgram(HttpServletRequest req, HttpServletResponse res,
                                                      @RequestParam(value = "pid", defaultValue = "0") final int pid) {
        Callable<ResponseEntity> callable = new Callable<ResponseEntity>() {
            @Override
            public ResponseEntity call() throws Exception {
                Map ret = new HashMap();
                ret.put("url", "deleted");
                if (pid < 1)
                    return null;

                Program program = programService.findById(pid);
                if (program != null)
                    redisCacheRepository.clearCache(program.getId(), "", "program");

                List<Revision> revisionList = revisionService.findByProgram(program, null);
                if (revisionList != null && !revisionList.isEmpty()) {
                    return ResponseEntity.ok(HttpStatus.NOT_ACCEPTABLE);
                }
                programService.delete(pid);

                return ResponseEntity.ok(ret);
            }
        };
        return new WebAsyncTask<ResponseEntity>(1800000, callable);
    }


}
