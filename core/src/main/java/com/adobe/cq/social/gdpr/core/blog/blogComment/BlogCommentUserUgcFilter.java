package com.adobe.cq.social.gdpr.core.blog.blogComment;

import com.adobe.cq.social.gdpr.core.UserUgcCommons.DefaultUserUgcFilter;
import com.adobe.cq.social.gdpr.core.UserUgcCommons.Identifiers;
import com.adobe.cq.social.gdpr.core.blog.BlogIdentifiers;
import com.adobe.cq.social.ugc.api.UgcFilter;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by mokatari on 10/13/17.
 */
public class BlogCommentUserUgcFilter extends DefaultUserUgcFilter{

    @Override
    public Map<String, String> getComponentfilters() {
        final Map<String, String>  filters = new HashMap<String, String>();
        filters.put(Identifiers.SLING_RESOURCE_TYPE, BlogIdentifiers.RESOURCE_TYPE_COMMENT);
        return filters;
    }

    @Override
    public String getUserIdentifierKey() {
        return Identifiers.AUTHORIZABLE_ID;
    }

    @Override
    public UgcFilter getUgcFilter(String user) {
        return super.getUgcFilter(user);
    }
}
