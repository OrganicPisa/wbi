package com.broadcom.wbi.model.mysql;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@Getter
@Setter
@XmlRootElement
@JsonIgnoreProperties({"bytes"})
public class FileUpload implements Serializable {
    private String fileName;
    private String fileSize;
    private String fileType;
    private int pid;
    private byte[] bytes;

}
