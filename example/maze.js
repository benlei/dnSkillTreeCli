var normalizeName = function(name) {
    if (name == 'uistring') {
        return 'Message';
    }

    var tableIdx = name.indexOf('table');
    return name.substring(0, tableIdx);
};

var getConnection = function() {
    return java.sql
        .DriverManager
        .getConnection("jdbc:mysql://localhost/maze?user=root&"
                                                 + "password=root&"
                                                 + "useUnicode=true&"
                                                 + "characterEncoding=utf-8&"
                                                 + "useSSL=false");
};

var complete = function() {
};