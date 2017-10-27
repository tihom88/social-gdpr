package com.adobe.cq.social.gdpr.core.servlets;

import com.adobe.cq.social.gdpr.core.UserUgcCommons.ComponentEnum;
import com.adobe.cq.social.gdpr.core.factory.UserUgcComponentFactory;
import com.adobe.cq.social.gdpr.core.gdprService.GdprService;
import com.adobe.cq.social.scf.OperationException;
import com.adobe.cq.social.srp.SocialResourceProvider;
import com.adobe.cq.social.srp.config.SocialResourceConfiguration;
import com.adobe.cq.social.srp.utilities.api.SocialResourceUtilities;
import com.adobe.cq.social.ugc.api.SearchResults;
import com.adobe.cq.social.ugc.api.UgcFilter;
import com.adobe.cq.social.ugc.api.UgcSearch;
import org.apache.felix.scr.annotations.*;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
//import org.apache.sling.commons.json.JSONObject;
//import org.apache.sling.servlets.post.JSONResponse;


/**
 * Created by mokatari on 10/11/17.
 */

@Component
@Service
@Properties({@Property(name = "sling.servlet.paths", value = "/services/social/gdpr/deleteuseraccount")})
public class UserAccountDeleteService extends SlingAllMethodsServlet {

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
        final SocialResourceConfiguration storageConfig = socialResourceUtilities.getStorageConfig(resource);
        final SocialResourceProvider srp = socialResourceUtilities.getSocialResourceProvider(resource);
        srp.setConfig(storageConfig);

        List<ComponentEnum> componentEnumList = Arrays.asList(ComponentEnum.values());
        try {
            gdprService.deleteUserUgc(resourceResolver,componentEnumList, user);
            gdprService.deleteUserAccount(resourceResolver,user);
        }catch (RepositoryException e) {
            throw new ServletException(e);
        } catch (OperationException e) {
            throw new ServletException(e);
        }
        resp.getOutputStream().println("UGC for user"+user+ " deleted");
    }
}
