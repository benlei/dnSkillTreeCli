// imports
var System = java.lang.System;
var DriverManager = java.sql.DriverManager;

// static final fields
var QUERY_GET_ALL_JOBS = "SELECT * FROM job";

// fields
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
        .replace('weapon', 'Weapon');
};

var getConnection = function () {
    if (connection) {
        return connection;
    }

    // return DriverManager.getConnection("jdbc:mysql://localhost/maze?"
    //     + "user=root&"
    //     + "password=root&"
    //     + "useUnicode=true&"
    //     + "characterEncoding=utf-8&"
    //     + "useSSL=false");

    connection = DriverManager.getConnection("jdbc:h2:mem:test;MODE=MYSQL;IGNORECASE=TRUE", "sa", "sa");
    return connection;
};

var complete = function () {
    var connection = getConnection();
    var stmt = connection.createStatement();


    // 1. Get all existing jobs
    var rs = stmt.executeQuery(QUERY_GET_ALL_JOBS);

    while (rs.next()) {
        System.out.println(rs.getInt('_JobID') + " = " + rs.getString('_EnglishName'));
    }

    // 2. For every job in jobs, get all their skills, their skill levels, and their skill tree

    // 3. Find out all names used for weapons

    // 4. Determine max SP possible for this cap. Include the Hero Level stuff.

    // 5.
};

var close = function () {
    var connection = getConnection();
    connection.close();
};