CREATE TABLE FINGERPRINTS(
    victims_id  INTEGER         NOT NULL, 
    algorithm   VARCHAR(255)    NOT NULL, 
    combined    VARCHAR(255)    NOT NULL, 
    filename    VARCHAR(255)    NOT NULL, 
    hash        VARCHAR(255)    NOT NULL,
    PRIMARY KEY(hash), FOREIGN KEY(victims_id) REFERENCES VICTIMS(id));


CREATE TABLE metadata(
    victims_id  INTEGER         NOT NULL, 
    id          INTEGER         NOT NULL PRIMARY KEY, 
    source      VARCHAR(255)    NOT NULL, 
    property    VARCHAR(255)    NOT NULL, 
    value       VARCHAR(255)    NOT NULL,
    FOREIGN KEY(victims_id) REFERENCES VICTIMS(id));


CREATE TABLE VICTIMS(
    id          INTEGER         NOT NULL, 
    cves        VARCHAR(255)    NOT NULL, 
    vendor      VARCHAR(255)    NOT NULL, 
    name        VARCHAR(255)    NOT NULL, 
    created     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP, 
    version     VARCHAR(255)    NOT NULL, 
    submitter   VARCHAR(255)    NOT NULL, 
    format      VARCHAR(255)    NOT NULL, 
    status      VARCHAR(255)    NOT NULL, 
    PRIMARY KEY(id));

