package com.broadcom.wbi.service.elasticSearch.implementation;

import com.broadcom.wbi.model.elasticSearch.RevisionSearch;
import com.broadcom.wbi.model.mysql.Segment;
import com.broadcom.wbi.repository.elasticSearch.RevisionSearchRepository;
import com.broadcom.wbi.service.elasticSearch.RevisionSearchService;
import com.broadcom.wbi.service.jpa.SegmentService;
import com.broadcom.wbi.util.ProjectConstant.EnumProgramType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@SuppressWarnings({"rawtypes"})
@Service
public class RevisionSearchServiceImpl implements RevisionSearchService {
    private final RevisionSearchRepository repo;
    private final SegmentService segmentServ;
    private final ElasticsearchTemplate template;

    @Autowired
    public RevisionSearchServiceImpl(RevisionSearchRepository repo, SegmentService segmentServ, ElasticsearchTemplate template) {
        this.repo = repo;
        this.segmentServ = segmentServ;
        this.template = template;
    }

    @Override
    public RevisionSearch saveOrUpdate(RevisionSearch rev) {
        return repo.save(rev);
    }

    @Override
    public void saveBulk(List<RevisionSearch> revs) {
        repo.save(revs);
    }

    @Override
    public void delete(String rev) {
        repo.delete(rev);
    }

    @Override
    public long count() {
        return repo.count();
    }

    @Override
    public void emptyData() {
        repo.deleteAll();
    }

    @Override
    public void deleteIndex() {
        template.deleteIndex(RevisionSearch.class);
    }

    @Override
    public List<RevisionSearch> findByDateTime(DateTime dt) {
        return null;
    }

    @Override
    public RevisionSearch findById(String id) {
        return repo.findOne(id);
    }

    @Override
    public Iterator<RevisionSearch> findAll() {
        return repo.findAll().iterator();
    }

    @Override
    public List<RevisionSearch> findBySegment(String segment) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1000))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("segment", segment))
                ).build();
        List<RevisionSearch> revs = repo.search(searchQuery).getContent();
        return revs;
    }


    @Override
    public List<RevisionSearch> findByType(String type) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1000))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("program_type", type))
                ).build();
        List<RevisionSearch> revs = repo.search(searchQuery).getContent();
        return revs;
    }

    @Override
    public List<RevisionSearch> findBySegment(List<Segment> segments) {
        final List<RevisionSearch> revisions = Collections.synchronizedList(new ArrayList<RevisionSearch>());
        if (segments != null && !segments.isEmpty()) {
            ExecutorService executor = Executors.newFixedThreadPool(5);
            for (final Object seg : segments) {
                executor.submit(new Runnable() {
                    public void run() {
                        Segment segment = null;
                        if (seg instanceof Segment) {
                            segment = (Segment) seg;
                        } else if (seg instanceof LinkedHashMap) {
                            LinkedHashMap tm = (LinkedHashMap) seg;
                            if (tm.containsKey("id")) {
                                String id = tm.get("id").toString();
                                segment = segmentServ.findById(Integer.parseInt(id));
                            }
                        }
                        if (segment != null) {
                            List<RevisionSearch> rsl = findBySegment(segment.getName().toLowerCase());
                            if (rsl != null && rsl.size() > 0) {
                                revisions.addAll(rsl);
                            }
                        }
                    }
                });
            }
            executor.shutdown();
            try {
                executor.awaitTermination(10, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return revisions;
    }

    @Override
    public List<RevisionSearch> findBySegment(String segment, String type) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1000))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("segment", segment))
                        .must(QueryBuilders.termQuery("program_type", type))
                ).build();
        List<RevisionSearch> revs = repo.search(searchQuery).getContent();
        return revs;
    }

    @Override
    public List<RevisionSearch> findBySegment(String segment, String type, Boolean isActive) {
        SearchQuery searchQuery = null;
        if (isActive != null) {
            searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1000))
                    .withQuery(QueryBuilders.boolQuery()
                            .must(QueryBuilders.termQuery("segment", segment))
                            .must(QueryBuilders.termQuery("program_type", type))
                            .must(QueryBuilders.termQuery("is_active", isActive))
                    ).build();
        } else {
            searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1000))
                    .withQuery(QueryBuilders.boolQuery()
                            .must(QueryBuilders.termQuery("segment", segment))
                            .must(QueryBuilders.termQuery("program_type", type))
                    ).build();
        }
        List<RevisionSearch> revs = repo.search(searchQuery).getContent();
        return revs;
    }

    @Override
    public List<RevisionSearch> findByProgram(int pid) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1000))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("program_id", pid))
                ).build();
        List<RevisionSearch> revs = repo.search(searchQuery).getContent();
        return revs;
    }

    @Override
    public List<RevisionSearch> findByProgram(int pid, Boolean isActive) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1000))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("program_id", pid))
                        .must(QueryBuilders.termQuery("is_active", isActive))
                ).build();
        List<RevisionSearch> revs = repo.search(searchQuery).getContent();
        return revs;
    }

    @Override
    public RevisionSearch findByProgram(Integer pid, String revname) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1))
                .withQuery(QueryBuilders.boolQuery()
                        .must(QueryBuilders.termQuery("program_id", pid))
                        .must(QueryBuilders.termQuery("rev_name", revname))
                ).build();
        List<RevisionSearch> revs = repo.search(searchQuery).getContent();
        if (revs != null && !revs.isEmpty())
            return revs.get(0);
        return null;
    }

    @Override
    public List<RevisionSearch> findByProgram(String pname) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        pname = pname.replace(".", "").replaceAll("\\+", "plus").trim();
        query.must(QueryBuilders.termQuery("program_name", pname));
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 100))
                .withQuery(query).build();
        System.out.println(pname + "---");
        List<RevisionSearch> revs = repo.search(searchQuery).getContent();
        return revs;
    }

    @Override
    public List<RevisionSearch> findByProgram(String baseNum, String pname) {
        BoolQueryBuilder query = QueryBuilders.boolQuery();
        pname = pname.replace(".", "").replaceAll("\\+", "plus").trim();
        query.must(QueryBuilders.termQuery("program_name", pname));
        query.must(QueryBuilders.termQuery("base_num", baseNum.toLowerCase().trim()));
        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 100))
                .withQuery(query).build();
        List<RevisionSearch> revs = repo.search(searchQuery).getContent();
        return revs;
    }

//    @Override
//    public List<RevisionSearch> searchByProgram(String pname) {
//        SearchQuery searchQuery = new NativeSearchQueryBuilder().withPageable(new PageRequest(0, 1000))
//                .withQuery(QueryBuilders.boolQuery()
//                        .must(QueryBuilders.wildcardQuery("program", "*" + pname.trim() + "*"))
//                ).build();
//        List<RevisionSearch> revs = repo.search(searchQuery).getContent();
//        return revs;
//    }

    @Override
    public List<RevisionSearch> findByProgramType(EnumProgramType ptype, Boolean isActive) {
        List<Segment> segments = new ArrayList<Segment>();
        final List<RevisionSearch> revisions = Collections.synchronizedList(new ArrayList<RevisionSearch>());
        if (ptype.equals(EnumProgramType.CHIP)) {
            segments = segmentServ.findAllActive();
            if (segments != null && !segments.isEmpty()) {
                ExecutorService executor = Executors.newFixedThreadPool(5);
                for (final Object seg : segments) {
                    executor.submit(new Runnable() {
                        public void run() {
                            Segment segment = null;
                            if (seg instanceof Segment) {
                                segment = (Segment) seg;
                            } else if (seg instanceof LinkedHashMap) {
                                LinkedHashMap tm = (LinkedHashMap) seg;
                                if (tm.containsKey("id")) {
                                    String id = tm.get("id").toString();
                                    segment = segmentServ.findById(Integer.parseInt(id));
                                }
                            }
                            if (segment != null) {
                                if (segment.getName().toLowerCase().indexOf("customer") != -1)
                                    return;
                                List<RevisionSearch> rsl = null;
                                if (segment.getName().toLowerCase().indexOf("software") != -1) {
                                    rsl = findBySegment(segment.getName().toLowerCase(), "software", isActive);
                                } else if (segment.getName().toLowerCase().indexOf("ip") == 0) {
                                    rsl = findBySegment(segment.getName().toLowerCase(), "ip", isActive);
                                } else {
                                    rsl = findBySegment(segment.getName().toLowerCase(), "chip", isActive);
                                }
                                if (rsl != null && rsl.size() > 0) {
                                    revisions.addAll(rsl);
                                }
                            }
                        }
                    });
                }
                executor.shutdown();
                try {
                    executor.awaitTermination(10, TimeUnit.MINUTES);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        } else if (ptype.equals(EnumProgramType.CUSTOMER)) {
            List<RevisionSearch> rsl = findBySegment("customer", "customer", true);
            revisions.addAll(rsl);
        } else if (ptype.equals(EnumProgramType.IP)) {
            List<RevisionSearch> rsl = findBySegment("ip", "ip", true);
            revisions.addAll(rsl);
        }
        if (revisions.size() > 0)
            return revisions;
        return null;
    }

}