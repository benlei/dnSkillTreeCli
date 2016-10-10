// imports
var System = java.lang.System;
var DriverManager = java.sql.DriverManager;

// fields
var connection;

/** Normalize file name to table name. */
var normalizeName = function (name) {
    if (name == 'uistring') {
        return 'message';
    }

    var idx = name.indexOf('table');

    if (idx == -1) {
      idx = name.indexOf('_');
    }

    if (idx == -1) {
      return name;
    }

    name = name.substring(0, idx);

    // special cases
    switch (name) {
        case 'union':
            return 'uniontable';
        default:
            return name;
    }
};

/** Gets a JDBC connection. */
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

    // connection = DriverManager.getConnection("jdbc:h2:mem:test;MODE=MYSQL;IGNORECASE=TRUE");

    return connection;
};

/** Process stuff, if desired. Otherwise you can give it an empty body. */
var process = function () {
    var connection = getConnection();
    var stmt = connection.createStatement();

    // Get all existing jobs
    var rs = stmt.executeQuery("SELECT * FROM job WHERE _Service IS TRUE");

    // List
    while (rs.next()) {
        System.out.println(rs.getInt('ID') + ' = ' + rs.getString('_EnglishName'));
    }

    // Don't forget to close a statement
    stmt.close();
};

/** Close the JDBC connection (automatically run at end of program). */
var close = function () {
    connection.close();
};
