package com.broadcom.wbi.service.indexing;

import org.joda.time.DateTime;
import org.springframework.scheduling.annotation.Async;

import java.util.concurrent.Future;

public interface IndexSearchService {

    @Async
    Future<Boolean> indexAllSku(int reload);

    @Async
    Future<Boolean> indexAllRevision(DateTime dt);

    @Async
    Future<Boolean> indexAllRevisionInformation(int reload);

    @Async
    Future<Boolean> indexProgramInformationByRevision(int rid);

    @Async
    Future<Boolean> indexAllRevisionContact(int reload);

    @Async
    Future<Boolean> indexAllIndicatorGroup(DateTime dt);

    @Async
    Future<Boolean> indexAllIndicatorTask(DateTime dt);

    @Async
    Future<Boolean> indexAllIndicatorDate(DateTime dt);

    @Async
    Future<Boolean> indexAllHeadline(DateTime dt);

    @Async
    Future<Boolean> indexAllTemplate(int reload);
}
