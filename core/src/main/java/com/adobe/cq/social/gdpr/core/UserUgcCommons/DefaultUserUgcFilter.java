package com.adobe.cq.social.gdpr.core.UserUgcCommons;

import com.adobe.cq.social.ugc.api.*;

import java.util.Map;

/**
 * Created by mokatari on 10/12/17.
 */
public abstract class  DefaultUserUgcFilter implements  UserUgcFilter{

    public  abstract Map<String, String>  getComponentfilters();
    public abstract String getUserIdentifierKey();

    public UgcFilter getUgcFilter(String user) {

        UgcFilter ugcShowcaseFilter = new UgcFilter();
        ConstraintGroup resourceGroupConstraint = new ConstraintGroup(Operator.And);
        Map<String, String> resourceTypeList = getComponentfilters();
        for (Map.Entry<String, String> entry : resourceTypeList.entrySet()) {
            resourceGroupConstraint.addConstraint(new ValueConstraint<String>(entry.getKey(), entry.getValue(), ComparisonType.Equals,
                    Operator.Or));
        }
        ConstraintGroup userGroup = new ConstraintGroup(Operator.And);
        String userIdentifierKey = getUserIdentifierKey();
        userGroup.addConstraint(new ValueConstraint<String>(userIdentifierKey, user, ComparisonType.Equals,
                    Operator.Or));
        ConstraintGroup andcons = new ConstraintGroup(Operator.Or); // doesn't matter
        andcons.addConstraint(resourceGroupConstraint);
        andcons.addConstraint(userGroup);
        ugcShowcaseFilter.and(andcons);
        return ugcShowcaseFilter;
    }

}

