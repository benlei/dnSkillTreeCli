// imports
var System = java.lang.System;
var DriverManager = java.sql.DriverManager;
var FileOutputStream = java.io.FileOutputStream;
var FileInputStream = java.io.FileInputStream;
var File = java.io.File;
var IOUtils = org.apache.commons.io.IOUtils;
var StandardCharsets = java.nio.charset.StandardCharsets;

// static final fields
var QUERY_GET_ALL_JOBS = "SELECT j.*, m._Message as $JobName FROM Job j JOIN Message m ON m.ID = j._JobName WHERE _Service IS TRUE";

// fields
var config = JSON.parse(
    IOUtils.toString(
        new FileInputStream(System.getProperty("dnt.config")),
        StandardCharsets.UTF_8));

var connection;

var normalizeName = function (name) {
    if (name == 'uistring') {
        return 'message';
    }

    var tableIdx = name.indexOf('table');

    /* Capitalize the words */
    return name.substring(0, tableIdx)
        .replace('skill', 'Skill')
        .replace('tree', 'Tree')
        .replace('level', 'Level')
        .replace('glyph', 'Glyph')
        .replace('weapon', 'Weapon')
        .replace('hero', 'Hero')
        .replace('potential', 'Potential')
        .replace('battlefield', 'Battlefield')
        .replace('rank', 'Rank')
        .replace('reward', 'Reward');
};

var getConnection = function () {
    if (connection) {
        return connection;
    }

    connection = DriverManager.getConnection("jdbc:mysql://localhost/maze?"
        + "user=root&"
        + "password=root&"
        + "useUnicode=true&"
        + "characterEncoding=utf-8&"
        + "useSSL=false");

    // connection = DriverManager.getConnection("jdbc:h2:mem:test;MODE=MYSQL;IGNORECASE=TRUE",
    //     "sa", "sa");

    return connection;
};

var process = function () {
    var connection = getConnection();
    var stmt = connection.createStatement();


    // 1. Get all existing jobs
    var rs = stmt.executeQuery(QUERY_GET_ALL_JOBS);

    var jobs = {"jobs": {}, "classes": []};
    while (rs.next()) {
        var job = {
            "id": rs.getInt('ID'),
            "jobName": rs.getString('$JobName'),
            "jobNumber": rs.getInt('_JobNumber'),
            "baseClass": rs.getInt('_BaseClass'),
            "parentJob": rs.getInt('_ParentJob'),
            "englishName": rs.getString('_EnglishName').toLowerCase(),
            "jobIcon": rs.getInt('_JobIcon'),
            "maxSPJob0": rs.getFloat('_MaxSPJob0'),
            "maxSPJob1": rs.getFloat('_MaxSPJob1'),
            "maxSPJob2": rs.getFloat('_MaxSPJob2'),
            "awakened": rs.getInt('_AwakeningItem') > 0
        };

        if (job.jobNumber == 2) {
            jobs.classes.push(job.id.toString());
        }

        jobs.jobs[job.id] = job;
    }

    stmt.close();

    write("job.json", jobs);




    // 2. For every job in jobs, get all their skills, their skill levels, and their skill tree

    // 3. Find out all names used for weapons

    // 4. Determine max SP possible for this cap. Include the Hero Level stuff.

    // 5.
};

var close = function () {
    var connection = getConnection();
    connection.close();
};


var write = function (path, json) {
    var out = new FileOutputStream(new File(config.output, path));
    out.write(JSON.stringify(json).getBytes(StandardCharsets.UTF_8));
    out.close();
};