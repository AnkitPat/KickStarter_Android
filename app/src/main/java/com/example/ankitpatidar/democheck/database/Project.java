package com.example.ankitpatidar.democheck.database;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Project class to define the table of database.
 *
 * Here @table specify table name and class variables define the column names
 */

@Table(databaseName = DatabaseModule.NAME)
public class Project extends BaseModel {

    @PrimaryKey
    @Column
    public Integer project_sno ;
    
    @Column
    public Integer project_amt_pledged ;


    @Column
    public String project_blurb ;

    @Column
    public String project_by ;
    @Column
    public String project_country ;
    @Column
    public String project_end_time ;
    @Column
    public String project_currency ;
    @Column
    public String project_location ;
    @Column
    public Integer project_percentage ;
    @Column
    public String project_num_backers;
    @Column
    public String project_state ;
    @Column
    public String project_title;
    @Column
    public String project_type ;
    @Column
    public String project_url ;

    @Column
    public String img_url;
    
}
