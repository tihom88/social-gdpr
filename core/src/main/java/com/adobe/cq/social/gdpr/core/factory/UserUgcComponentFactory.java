package com.adobe.cq.social.gdpr.core.factory;

import com.adobe.cq.social.gdpr.core.UserUgcCommons.ComponentEnum;
import com.adobe.cq.social.gdpr.core.UserUgcCommons.UserUgcFilter;
import com.adobe.cq.social.commons.comments.endpoints.CommentOperations;

/**
 * Created by mokatari on 10/13/17.
 */
public interface UserUgcComponentFactory<T> {
    public UserUgcFilter getUserUgcFilter(Class T);
    public UserUgcFilter getUserUgcFilter(ComponentEnum componentEnum);

    public CommentOperations getOperation(ComponentEnum componentEnum);
}
