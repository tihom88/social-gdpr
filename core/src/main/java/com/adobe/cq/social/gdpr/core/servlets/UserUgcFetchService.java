package com.adobe.cq.social.gdpr.core.servlets;

import com.adobe.cq.social.gdpr.core.UserUgcCommons.ComponentEnum;
import com.adobe.cq.social.gdpr.core.factory.UserUgcComponentFactory;
import com.adobe.cq.social.gdpr.core.gdprService.GdprService;
import com.adobe.cq.social.gdpr.core.util.ZipCreator;
import com.adobe.cq.social.srp.SocialResourceProvider;
import com.adobe.cq.social.srp.config.SocialResourceConfiguration;
import com.adobe.cq.social.srp.utilities.api.SocialResourceUtilities;
import com.adobe.cq.social.ugc.api.*;
import org.apache.commons.io.IOUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.commons.json.io.JSONWriter;
import org.apache.sling.commons.json.jcr.JsonItemWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.ServletException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipOutputStream;
import javax.jcr.Node;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.felix.scr.annotations.Reference;
//import org.apache.sling.commons.json.JSONObject;
//import org.apache.sling.servlets.post.JSONResponse;


/**
 * Created by mokatari on 10/11/17.
 */

@Component
@Service
@Properties({@Property(name = "sling.servlet.paths", value = "/services/social/gdpr/getuserugc")})
public class UserUgcFetchService extends SlingAllMethodsServlet {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Reference
    private SocialResourceUtilities socialResourceUtilities;

    @Reference
    private UgcSearch ugcSearch;

    @Reference
    private UserUgcComponentFactory userUgcComponentFactory;

    @Reference
    GdprService gdprService;

    @Override
    protected void doGet(final SlingHttpServletRequest req,
                         final SlingHttpServletResponse resp) throws ServletException, IOException {
        final Resource resource = req.getResource();
        String user = req.getParameter("user");
        resp.setContentType("text/plain");
        final ResourceResolver resourceResolver = req.getResourceResolver();
        final SocialResourceConfiguration storageConfig = socialResourceUtilities.getStorageConfig(resource); //socialUtils.getStorageConfig(resource);
        final SocialResourceProvider srp = socialResourceUtilities.getSocialResourceProvider(resource);
        srp.setConfig(storageConfig);


        List<ComponentEnum> componentEnumList = getComponentEnumList();
        Map<ComponentEnum, SearchResults<Resource>> resultsList = gdprService.getUserUgc(resourceResolver, componentEnumList, user);


        List<String> attachmentPaths = new ArrayList<String>();
        for (Map.Entry<ComponentEnum, SearchResults<Resource>> entry : resultsList.entrySet()) {
            for (Resource resourceNode : entry.getValue().getResults()) {
                String[] images = (String[]) resourceNode.getValueMap().get("social:attachments");
                for (String imagePath : images) {
                    attachmentPaths.add(imagePath);
                }
            }
        }

        resp.setContentType("application/octet-stream");
        final String headerKey = "Content-Disposition";
        final String headerValue = "attachment; filename=\""+user+"-UgcData.zip" +"\"";
        resp.setHeader(headerKey, headerValue);

        String userUgcJson = null;
        userUgcJson = createJsonResponse(resultsList);
        createZip(req, resp, userUgcJson, attachmentPaths);
    }

    private void createZip(final SlingHttpServletRequest req,
                           final SlingHttpServletResponse resp, String ugcTextData, List<String> attachmentPaths) throws IOException, ServletException {

        final Resource resource = req.getResource();
        final ResourceResolver resourceResolver = req.getResourceResolver();
        final SocialResourceConfiguration storageConfig = socialResourceUtilities.getStorageConfig(resource); //socialUtils.getStorageConfig(resource);
        final SocialResourceProvider srp = socialResourceUtilities.getSocialResourceProvider(resource);
        srp.setConfig(storageConfig);


        File outFile = File.createTempFile(UUID.randomUUID().toString(), ".zip");
        FileOutputStream fos = new FileOutputStream(outFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        ZipOutputStream zip = new ZipOutputStream(bos);


        ZipCreator.addTextToZip("ugcTextData.json", zip, ugcTextData);
        ZipCreator.addAttachmentsToZip(attachmentPaths, zip, srp, resourceResolver);

        OutputStream outStream = null;
        InputStream inStream = null;
        try {
            IOUtils.closeQuietly(zip);
            IOUtils.closeQuietly(bos);
            IOUtils.closeQuietly(fos);
            // obtains response's output stream
            outStream = resp.getOutputStream();
            inStream = new FileInputStream(outFile);
            // copy from file to output
            IOUtils.copy(inStream, outStream);
        } catch (final IOException e) {
            throw new ServletException(e);
        } catch (final Exception e) {
            throw new ServletException(e);
        } finally {
            IOUtils.closeQuietly(zip);
            IOUtils.closeQuietly(bos);
            IOUtils.closeQuietly(fos);
            IOUtils.closeQuietly(inStream);
            IOUtils.closeQuietly(outStream);
            if (outFile != null) {
                outFile.delete();
            }
        }

    }

    private List<ComponentEnum> getComponentEnumList() {
        List<ComponentEnum> componentEnumList = new ArrayList<ComponentEnum>();
        componentEnumList.add(ComponentEnum.BLOG_COMMENT);
        componentEnumList.add(ComponentEnum.BLOG_ENTRY);

        return componentEnumList;
    }

    private String createJsonResponse(Map<ComponentEnum, SearchResults<Resource>> resultsList) throws ServletException {
        StringBuilder response = new StringBuilder();
        final StringWriter stringWriter = new StringWriter();
        boolean isEmpty = true;
        response.append("[");
        final JsonItemWriter jsonWriter = new JsonItemWriter(null);
        try {
            for (Map.Entry<ComponentEnum, SearchResults<Resource>> entry : resultsList.entrySet()) {
                for (Resource resultRes : entry.getValue().getResults()) {
                    Node node = null;

                    node = resultRes.getResourceResolver().adaptTo(Session.class).getNode(resultRes.getPath());

                    // max recursion level set to 0 to get info of current node itself
                    jsonWriter.dump(node, stringWriter, 0);
                    response.append(stringWriter.toString() + ",");
                    isEmpty = false;
                }
            }
        } catch (RepositoryException e) {
            throw new ServletException(e);
        } catch (JSONException e) {
            throw new ServletException(e);
        }
        if (!isEmpty) {
            response.setLength(response.length() - 1);
        }
        response.append("]");
        return response.toString();
    }
}
