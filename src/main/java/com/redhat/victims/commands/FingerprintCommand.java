/*
 * Copyright (C) 2012 Red Hat Inc.
 *
 * This file is part of enforce-victims-rule for the Maven Enforcer Plugin.
 * enforce-victims-rule is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * enforce-victims-rule is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with enforce-victims-rule.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.redhat.victims.commands;

import com.redhat.victims.*;
import com.redhat.victims.archive.java.FingerprintClassfile;
import com.redhat.victims.archive.java.Jar;
import com.redhat.victims.db.VictimsRecord;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipFile;
import org.apache.commons.codec.binary.Hex;
import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * The fingerprint command resolves the supplied artifact and compares the
 * fingerprint of that item to entries within a database of vulnerable artifacts.
 *
 * @author gmurphy
 */
public final class FingerprintCommand implements Command {

    // FIXME - Retreive algorithms from the database or allow user
    // to set these values; NB. The database entries are in Python
    // format (sha1, sha512.. etc) so will need to normalize these.
    public static final String JAVA_ALGORITHMS[] = { "SHA-1", "SHA-512" };
    public static final String FILE_HASH_ALGORITHM = "SHA-512";

    public static final Map<String, String> Algorithms;
    static {
        Map<String, String> mappings = new HashMap<String, String>();
        mappings.put("SHA-1", "sha1");
        mappings.put("SHA-512", "sha512");
        Algorithms = Collections.unmodifiableMap(mappings);
    }


    public String computeHash(String algorithm, InputStream is) {

        String h = "";
        try {
            byte[] buf = new byte[1024];

            MessageDigest md = MessageDigest.getInstance(algorithm);
            while (is.read(buf) > 0)
                md.update(buf);

            h = new String(Hex.encodeHex(md.digest()));


        } catch (NoSuchAlgorithmException e){
        } catch (IOException e){
        }

        return h;
    }



    /**
     * Scan project dependencies and ensure no artifacts have the same
     * fingerprint as any entry in the database.
     *
     * FIXME: Cyclonic complexity of this is getting pretty high #refator
     *
     * @param ctx The execution context to run this command under.
     * @throws EnforcerRuleException
     */
    public void execute(final ExecutionContext ctx) throws EnforcerRuleException {

        try {

            File f = ctx.getArtifact().getFile();
            String fileHash = computeHash(FILE_HASH_ALGORITHM, new FileInputStream(f));
            VictimsRecord rec = ctx.getDatabase().findByJarHash(fileHash);
            failOnError(ctx, rec, fileHash);

            Jar jarfile = new Jar(new ZipFile(f));
            for (String algorithm : JAVA_ALGORITHMS) {

                // Visit each class file in the JAR.
                FingerprintClassfile visitor = new FingerprintClassfile(algorithm);
                jarfile.accept(visitor);
                JSONObject result = visitor.results().getJSONObject(0);


                // Create a master hash
                // FIXME: This is pointless really. The master hash should
                // just be replaced with a hash of the entired JAR content.
                Iterator iter = result.keys();
                MessageDigest md = MessageDigest.getInstance(algorithm);
                List<String> jarContent = new ArrayList<String>();
                while (iter.hasNext()){
                    // Update the master hash
                    String hash = iter.next().toString();
                    md.update(hash.getBytes());
                    jarContent.add(hash);
                }

                // Check the combined hash
                final String combined = new String(Hex.encodeHex(md.digest()));
                final VictimsRecord record = ctx.getDatabase().findByJarHash(combined);
                failOnError(ctx, record, combined);

                String[] hashes = jarContent.toArray(new String[jarContent.size()]);
                String withAlgorithm = Algorithms.get(algorithm);
                VictimsRecord firstMatch = ctx.getDatabase().findByClassSet(hashes, withAlgorithm);
                failOnError(ctx, firstMatch, firstMatch != null ? firstMatch.fileHash : ""); // Report file hash?

            }

        } catch (NoSuchAlgorithmException e) {
            ctx.getLog().error(e);

        } catch (IOException e) {
            ctx.getLog().error(e);

        } catch (JSONException e) {

            ctx.getLog().error(e);
        } catch (SQLException e){

            ctx.getLog().error(e);
        }
    }

    private void failOnError(final ExecutionContext ctx, VictimsRecord r, String h) throws EnforcerRuleException {

        final String fmt = IOUtils.fmt(Resources.INFO_FINGERPRINT_HEADING);
        final String execMode = ctx.getSettings().get(Settings.FINGERPRINT);
        final String artifactId = ctx.getArtifact().getArtifactId();
        try {
            final String filename = ctx.getArtifact().getFile().getCanonicalPath();

            if (r != null) {

                // For display purposes only
                JSONObject obj = new JSONObject(r.toJSON());
                obj.remove("hashes");
                obj.put("hash", h);

                // Notify of the error
                String info = IOUtils.prettyPrint(fmt, obj);
                IOUtils.report(ctx.getLog(), execMode, info);

                if (ctx.getSettings().inFatalMode(Settings.FINGERPRINT)) {
                    fatalError(artifactId, filename);
                }
            }
        } catch (IOException e){
        } catch (JSONException e){
        }
    }

    private void fatalError(final String artifactId, final String filename) throws EnforcerRuleException {

        StringBuilder err = new StringBuilder();
        err.append(IOUtils.box(IOUtils.fmt(Resources.FATAL_FINGERPRINT_HEADING)));
        err.append(IOUtils.wrap(80, IOUtils.fmt(Resources.FATAL_FINGERPRINT_BODY, filename, artifactId)));

        throw new EnforcerRuleException(err.toString());

    }

    public String getDefaultExecutionMode() {
        return Settings.MODE_FATAL;
    }
}
