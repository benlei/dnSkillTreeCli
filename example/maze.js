var normalizeName = function(name) {

};

var getConnection = function() {
    return java.sql
        .DriverManager
        .getConnection("jdbc:mysql://localhost/maze?user=root&"
                                                 + "password=root&"
                                                 + "useUnicode=true&"
                                                 + "characterEncoding=utf-8");
};

var complete = function() {
};