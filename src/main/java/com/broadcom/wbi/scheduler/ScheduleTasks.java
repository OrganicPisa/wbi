package com.broadcom.wbi.scheduler;

import com.broadcom.wbi.model.elasticSearch.ResourceActualSearch;
import com.broadcom.wbi.service.jpa.RedisCacheRepository;
import com.broadcom.wbi.service.report.ReportService;
import com.broadcom.wbi.service.resource.parser.DataResourceParseService;
import com.google.common.base.Stopwatch;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ScheduleTasks {

    static final DateTimeFormatter dtfm = DateTimeFormat.forPattern("M/d/yyyy");

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedisCacheRepository redisCacheRepository;
    @Autowired
    private DataResourceParseService dataResourceParseService;
    @Autowired
    private ReportService reportService;

    @Scheduled(cron = "0 0 1,13 * * *")
    public void insertProjectTracker() throws IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        if (!elasticsearchTemplate.indexExists(ResourceActualSearch.class)) {
            elasticsearchTemplate.createIndex(ResourceActualSearch.class);
            elasticsearchTemplate.putMapping(ResourceActualSearch.class);
        }
        DateTime lastmonth = new DateTime().minusMonths(1).dayOfMonth().withMinimumValue().withTimeAtStartOfDay();
        DateTime stopdt = new DateTime().dayOfYear().withMaximumValue().withTimeAtStartOfDay().plusYears(1);
        DateTime current = lastmonth;
        while (current.getMillis() < stopdt.getMillis()) {
            dataResourceParseService.cleanup(current);
            dataResourceParseService.doCollectAndInsertData(current);
            current = current.plusMonths(1);
        }
    }

    @Scheduled(fixedRate = 1000 * 60 * 60 * 3)
    public void clearCache() throws IOException {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Scheduled(cron = "0 0 12 ? * SAT")
    public void weekendEmail() throws IOException {
        reportService.generateCustomerHeadlineWeekendEmail("cisco", null, "zavsoh.mostoufi@broadcom.com");

        reportService.generateCustomerHeadlineWeekendEmail(null, "cisco", "delany.kou@broadcom.com");

        reportService.generateInternalHeadlineWeekendEmail("david.crosbie@broadcom.com");
    }

}
