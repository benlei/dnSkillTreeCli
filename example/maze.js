var normalizeName = function (name) {
    if (name == 'uistring') {
        return 'message';
    }

    var tableIdx = name.indexOf('table');

    /* Capitalize the words */
    return name.substring(0, tableIdx)
        .replace('skill', 'Skill')
        .replace('tree', 'Tree')
        .replace('level', 'Level');
};

var getConnection = function () {
    return java.sql
        .DriverManager
        .getConnection("jdbc:mysql://localhost/maze?user=root&"
            + "password=root&"
            + "useUnicode=true&"
            + "characterEncoding=utf-8&"
            + "useSSL=false");
};

var complete = function () {
    var connection = getConnection();
    var stmt = connection.createStatement();
    var rs = stmt.executeQuery(QUERY_GET_ALL_JOBS);

    while (rs.next()) {
        java.lang.System.out.println(rs.getInt("_JobId") + " = " + rs.getString("_EnglishName"));
    }

    connection.close();
};

var QUERY_GET_ALL_JOBS = "SELECT * FROM job";