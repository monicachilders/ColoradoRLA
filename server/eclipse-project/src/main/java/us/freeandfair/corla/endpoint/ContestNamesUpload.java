/*
 * Colorado RLA System
 *
 * @title ColoradoRLA
 * @copyright 2018 Colorado Department of State
 * @license SPDX-License-Identifier: AGPL-3.0-or-later
 */

package us.freeandfair.corla.endpoint;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.sql.Blob;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.persistence.PersistenceException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.log4j.Logger;
import org.apache.log4j.LogManager;

import spark.Request;
import spark.Response;

import us.freeandfair.corla.crypto.HashChecker;
import us.freeandfair.corla.model.County;
import us.freeandfair.corla.model.UploadedFile;
import us.freeandfair.corla.model.UploadedFile.FileStatus;
import us.freeandfair.corla.model.UploadedFile.HashStatus;
import us.freeandfair.corla.persistence.Persistence;
import us.freeandfair.corla.util.FileHelper;
import us.freeandfair.corla.util.SparkHelper;
import us.freeandfair.corla.util.SuppressFBWarnings;

/**
 * POST a list of standardized contest names
 *
 * 1. Respond with a 200 if everything worked
 * 2. Respond with a 400 if there are duplicates, no file, etc. Be
 *    descriptive.
 * 3. Respond with a 500 if the CSV doesn't parse. Be as descriptive as
 *    possible.
 */

public class ContestNamesUpload extends AbstractEndpoint {
  private static final Logger log = LogManager.getLogger(ContestNamesUpload.class);

  /**
   * The "file" form data field name.
   */
  public static final String FILE = "file";

  /**
   * The upload buffer size, in bytes.
   */
  private static final int BUFFER_SIZE = 1048576; // 1 MB

  /**
   * The maximum upload size, in bytes.
   */
  private static final int MAX_UPLOAD_SIZE = 1073741824; // 1 GB

 /**
  * {@inheritDoc}
  */
  @Override
  public EndpointType endpointType() {
    return EndpointType.POST;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String endpointName() {
    return "/contest-names";
  }

  /**
   * @return STATE authorization is necessary for this endpoint.
   */
  public AuthorizationType requiredAuthorization() {
    // FIXME return AuthorizationType.STATE;
    // once there's a good way to test this _with_ authn.
    return AuthorizationType.NONE;
  }

  private void handleUpload(final Request request,
                            final Response response,
                            final UploadInformation upload) {
    try {
        final HttpServletRequest raw = SparkHelper.getRaw(request);
        upload.ok = ServletFileUpload.isMultipartContent(raw);

        log.info("handling file upload request from " + raw.getRemoteHost());

        if (upload.ok) {
          final ServletFileUpload fileUpload = new ServletFileUpload();
          final FileItemIterator fii = fileUpload.getItemIterator(raw);

          while (fii.hasNext()) {
            final FileItemStream item = fii.next();
            final String name = item.getFieldName();
            final InputStream stream = item.openStream();

            if (item.isFormField()) {
              upload.formFields.put(item.getFieldName(), Streams.asString(stream));
            } else if (FILE.equals(name)) {
              upload.filename = item.getName();
              upload.file = File.createTempFile("upload", ".csv");

              final OutputStream os = new FileOutputStream(upload.file);
              final int total = FileHelper
                                .bufferedCopy(stream, os, BUFFER_SIZE, MAX_UPLOAD_SIZE);

              if (total >= MAX_UPLOAD_SIZE) {
                log.debug("attempt to upload file greater than max size from "
                          + raw.getRemoteHost());
                badDataContents(response, "Upload Failed");
                upload.ok = false;
              } else {
                log.debug("successfully saved file of size "
                         + total
                         + " from "
                         + raw.getRemoteHost());
              }
              os.close();
            }
          }
        }

        if (upload.file == null) {
          log.warn("No file was found in the POST");
          upload.ok = false;
          badDataContents(response, "No file was uploaded");
        }
    } catch (final IOException | FileUploadException e) {
      log.error("OMG: IOException!", e);
      upload.ok = false;
      badDataContents(response, "Upload Failed");
    }
  }

  public String endpointBody(final Request request, final Response response) {
      final UploadInformation info = new UploadInformation();
      info.timestamp = Instant.now();
      info.ok = true;

      try {
          handleUpload(request, response, info);

         if (info.ok) {
           // TODO persist something somewhere
            ok(response);
          }
          else {
            serverError(response, "OMG, something bad happened but I don't know what");
          }
      } finally {
          if (info.file != null) {
            try {
              if (!info.file.delete()) {
                log.error("Unable to delete temp file " + info.file);
              }
            } catch (final SecurityException e) {
              log.error("Uh oh, SecurityException that should never happen!", e);
            }
          }
      }

    return my_endpoint_result.get();
  }

  /**
   * A small class to encapsulate data dealt with during an upload.
   */
  private static class UploadInformation {
    /**
     * The uploaded file.
     */
    protected File file;

    /**
     * The original name of the uploaded file.
     */
     protected String filename;

    /**
     * The timestamp of the upload.
     */
    protected Instant timestamp;

    /**
     * A flag indicating whether the upload is "ok".
     */
    protected boolean ok = true;

    /**
     * A map of form field names and values.
     */
    protected Map<String, String> formFields = new HashMap<String, String>();
  }
}
