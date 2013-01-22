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


INSERT INTO VICTIMS (id, cves, vendor, name, submitter, format, status, version) 
    VALUES(1234, "CVE-2001", "Apache", "commons-http", "gmurphy@redhat.com", "jar", "SUBMITTED", "0.0.1");

INSERT INTO FINGERPRINTS (victims_id, algorithm, combined, filename, hash) VALUES
    (1234, "SHA-1", "244c282f7f23986b7a47dd242b1da7209b067b9b", "a.class", "55139ec6ceb9fc1f0e00f62de9a673059057a50f"),
    (1234, "SHA-1", "244c282f7f23986b7a47dd242b1da7209b067b9b", "b.class", "6face70414fcbee77be9444b154d77984ff112bc"),
    (1234, "SHA-1", "244c282f7f23986b7a47dd242b1da7209b067b9b", "c.class", "1c3f5c07bf366c9a1c19c5f3e300948b0da79835"),
    (1234, "SHA-1", "244c282f7f23986b7a47dd242b1da7209b067b9b", "d.class", "bfd3ba911196e9412002ab78ef834c0cb0392c25"),
    (1234, "SHA-1", "244c282f7f23986b7a47dd242b1da7209b067b9b", "e.class", "731968475b83c11be4bcd5db1eb6400def3789f0"),
    (1234, "SHA-512", "826285a3cd2fbd07235a4c79552c1848a55e38e248c8d817cd27900351106516713f9a31ebb581e6d3b2b2cf504328aa924131101477345f82d3338702f4960a", "a.class", "c52e1284c4ddc4f425bb93ad464750593908cb767cd588454e28276afad155583962c2715401e9417504765c0b288453c908e285fee28a12d128451ae25b7267"),
    (1234, "SHA-512", "826285a3cd2fbd07235a4c79552c1848a55e38e248c8d817cd27900351106516713f9a31ebb581e6d3b2b2cf504328aa924131101477345f82d3338702f4960a", "b.class", "0cd266a113dd5fdce394b3f6a64885ce867f794382ee2eba84bf60cb3d5cfafa78089b53ef456b6cd318fc0cacdf14a08fa26d4dd915f54262777a3a6050b0e2"),
    (1234, "SHA-512", "826285a3cd2fbd07235a4c79552c1848a55e38e248c8d817cd27900351106516713f9a31ebb581e6d3b2b2cf504328aa924131101477345f82d3338702f4960a", "c.class", "cfdafb91fef1ce98513379d380a80269099e1811c4ccedef8f332ec6e58c5bb10914dd121987102983aecc9e14bf224d7dfa1c7024ba39dc46ddd850e2330880"),
    (1234, "SHA-512", "826285a3cd2fbd07235a4c79552c1848a55e38e248c8d817cd27900351106516713f9a31ebb581e6d3b2b2cf504328aa924131101477345f82d3338702f4960a", "d.class", "7055dd416118b0d99119047c770291dd0582d7a6972159df74e4838021c6cdbf30aa1d11b9f0854ce5f7ba55da2f69fe6f31310adca3cada2642e2b88dfda443"),
    (1234, "SHA-512", "826285a3cd2fbd07235a4c79552c1848a55e38e248c8d817cd27900351106516713f9a31ebb581e6d3b2b2cf504328aa924131101477345f82d3338702f4960a", "e.class", "cada38c3ef604ac121ac20184b69b2ffb7859975df3b55f4c3a3d6fc00df305dd0fef7faafab564f5923da4cdc3cb22348453d11dc4d825004150f55b2837cd7");


INSERT INTO metadata (victims_id, source, property, value) VALUES
    ("1234", "META-INF/MANIFEST.MF", "apple", "red"),
    ("1234", "META-INF/MANIFEST.MF", "orange", "orange"),
    ("1234", "META-INF/MANIFEST.MF", "grass", "green"),
    ("1234", "META-INF/MANIFEST.MF", "grape", "purple"),
    ("1234", "META-INF/pom.properties", "artifactId", "red"),
    ("1234", "META-INF/pom.properties", "groupdId", "orange"),
    ("1234", "META-INF/pom.properties", "version", "purple");


