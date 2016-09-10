CREATE TABLE file (
    path VARCHAR(255) PRIMARY KEY,
    file_size INT,
    zdata BLOB,
    zfile_size INT
);