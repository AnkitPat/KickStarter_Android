package com.example.ankitpatidar.democheck.database;

import com.raizlabs.android.dbflow.annotation.Database;

/**
 * Used DB Flow library for Database.
 * As it is most efficient library for database in android. and it decrease our burden for handling
 * cursor at runtime.
 *
 * Here @Database annotattion is used for specifying the database name and version
 */
@Database(name = DatabaseModule.NAME, version = DatabaseModule.VERSION)
 class DatabaseModule {

     static final String NAME = "project_database";
     static final int VERSION = 2;
}
