package com.broadcom.wbi.model.mssql;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


@NamedStoredProcedureQuery(name = "generateReport",
        procedureName = "Reporting.dbo.usp_Report_093_1_T_PT_2",
        resultClasses = ResourceReader.class,
        parameters = {
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "bu", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "lob_project", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "lob_employee", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "project", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "parent_project", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "ace_hierarchy", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "begin_date", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "end_date", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "forecast_type", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "file_path", type = String.class)
        }
)
@Getter
@Setter
@Entity
public class ResourceReader implements Serializable {

    //	@GeneratedValue(strategy = GenerationType.AUTO)
//    private Long id;
    @EmbeddedId
    private ResourceReaderId id;

    @Column(name = "SEGMENT_NAME")
    private String segment_name;
    @Column(name = "DIRECTOR")
    private String director;
    @Column(name = "MANAGER")
    private String manager;
    @Column(name = "EMPLOYEE")
    private String employee;
    @Column(name = "PROJECT_TYPE")
    private String project_type;
    @Column(name = "PARENT_PROJECT")
    private String parent_project;
    @Column(name = "REVISION")
    private String revision;
    @Column(name = "PROJECT_OWNER")
    private String project_owner;
    @Column(name = "EMPLOYEE_TYPE")
    private String employee_type;
    @Column(name = "CATEGORY")
    private String category;
    @Column(name = "SUB_CATEGORY")
    private String sub_category;
    @Column(name = "PROFIT_CENTER_PROJECT")
    private String profit_center_project;
    @Column(name = "COST_CENTER")
    private String cost_center;

    @Column(name = "PROFIT_CENTER_GROUP")
    private String profit_center_group;

    @Temporal(TemporalType.DATE)
    @Column(name = "FM_BEGIN_DATE")
    private Date date;
    @Column(name = "YEARS")
    private String year;
    @Column(name = "QUARTERS")
    private String quarter;

    @Column(name = "PRIMARY_LOC_COUNTRY_NAME")
    private String loc_country;
    @Column(name = "PRIMARY_LOC_STATE")
    private String loc_state;
    @Column(name = "PRIMARY_LOC_NAME")
    private String loc_name;
    @Column(name = "PERSON_STATUS")
    private String person_status;

    @Id
    @Column(name = "EMPLOYEE_ID")
    private String employee_id;
    @Column(name = "MANAGER_ID")
    private String manager_id;

    @Override
    public String toString() {
        return employee + "--" + id.getProject() + "--" + id.getSkill()
                + "--" + date.toString() + "--" + id.getCount();
    }

}
