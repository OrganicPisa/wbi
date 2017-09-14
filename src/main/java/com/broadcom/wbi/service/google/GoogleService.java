package com.broadcom.wbi.service.google;


import com.google.api.client.auth.oauth2.Credential;
import com.google.api.services.drive.Drive;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.sheets.v4.Sheets;

import java.io.IOException;

public interface GoogleService {

    Drive getDriveService(Credential credential) throws IOException;

    Gmail getGmailService(Credential credential) throws IOException;

    Sheets getSheetService(Credential credential) throws IOException;

    Credential authorize() throws IOException;

}
