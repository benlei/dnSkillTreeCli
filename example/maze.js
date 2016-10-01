var fetch = function(rs) {
    var rsmd = rs.getMetaData();
    var cols = rsmd.getColumnCount();
    var dict = {};

    for (var i = 0; i < cols; i++) {
        var name = rsmd.getColumnName(i + 1);

        if (name.charAt(0) == '_') {
            name = name.substring(1);
        }

        dict[name] = rs.getObject(i + 1);
    }

    return dict;
};

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
        var row = fetch(rs);
        java.lang.System.out.println(row['JobID'] + " = " + row["EnglishName"]);
    }

    connection.close();
};

var QUERY_GET_ALL_JOBS = "SELECT * FROM job";