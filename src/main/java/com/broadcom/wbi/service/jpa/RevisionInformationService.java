package com.broadcom.wbi.service.jpa;

import com.broadcom.wbi.model.mysql.Revision;
import com.broadcom.wbi.model.mysql.RevisionInformation;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;

import java.util.List;

@SuppressWarnings("rawtypes")
public interface RevisionInformationService extends CRUDService<RevisionInformation> {

    List<RevisionInformation> findByRevision(Revision rev);

    List<RevisionInformation> findByRevision(Revision rev, boolean onDashboard);

    List<RevisionInformation> findByRevisionPhase(Revision rev, String phase);

    List<RevisionInformation> findByRevisionPhaseName(Revision rev, String phase, String name);

    @Async
    void cloneFromAnotherRevision(Revision oldRev, Revision rev, Authentication authentication);

    void cloneFromTemplate(Revision rev, Authentication authentication);

//    void initInfoNewRevision(Revision rev, String createtypestring, String username);
//
//    void cloneInformation(Revision oldRev, Revision rev, String username);
//
//    HashMap parseInfoSpreadSheet(String username, File file);
//
//    void updateValueByRevisionPhaseName(Revision rev, String phase, HashMap infomap);
}
