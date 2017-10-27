package com.adobe.cq.social.gdpr.core.util;

import com.adobe.cq.social.srp.SocialResourceProvider;
import org.apache.commons.io.IOUtils;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.io.JSONWriter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by mokatari on 10/25/17.
 */
public class ZipCreator {
    //Logger logger = LoggerFactory.getLogger(this.getClass());

    public static void addTextToZip(String fileName, ZipOutputStream zip1, String data) throws IOException {
        zip1.putNextEntry(new ZipEntry(fileName));
        Writer zipWriter = new OutputStreamWriter(zip1);
        zipWriter.write(data);
        zipWriter.flush();
    }

    public static void addAttachmentsToZip(List<String> imagePaths, ZipOutputStream zipOutputStream, SocialResourceProvider srp, ResourceResolver resourceResolver) throws IOException {
        for (String imagePath: imagePaths) {
            zipOutputStream.putNextEntry(new ZipEntry(imagePath));
            InputStream inputStream = srp.getAttachmentInputStream(resourceResolver, imagePath);
            IOUtils.copy(inputStream, zipOutputStream);
        }
    }

}
