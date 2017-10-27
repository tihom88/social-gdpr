package com.adobe.cq.social.gdpr.core.UserUgcCommons;

import com.adobe.cq.social.ugc.api.UgcFilter;

/**
 * Created by mokatari on 10/12/17.
 */
public interface UserUgcFilter {
    UgcFilter getUgcFilter(String userId);
}
