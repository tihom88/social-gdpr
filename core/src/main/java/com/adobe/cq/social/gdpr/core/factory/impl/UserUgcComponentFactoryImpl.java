package com.adobe.cq.social.gdpr.core.factory.impl;

import com.adobe.cq.social.gdpr.core.UserUgcCommons.ComponentEnum;
import com.adobe.cq.social.gdpr.core.UserUgcCommons.UserUgcFilter;
import com.adobe.cq.social.gdpr.core.blog.blogComment.BlogCommentUserUgcFilter;
import com.adobe.cq.social.gdpr.core.blog.blogEntry.BlogEntryUserUgcFilter;
import com.adobe.cq.social.gdpr.core.factory.UserUgcComponentFactory;
import com.adobe.cq.social.calendar.client.endpoints.CalendarOperations;
import com.adobe.cq.social.commons.comments.endpoints.CommentOperations;
import com.adobe.cq.social.filelibrary.client.endpoints.FileLibraryOperations;
import com.adobe.cq.social.forum.client.endpoints.ForumOperations;
import com.adobe.cq.social.journal.client.endpoints.JournalOperations;
import com.adobe.cq.social.qna.client.endpoints.QnaForumOperations;
import com.adobe.cq.social.srp.SocialResourceProvider;
import com.adobe.cq.social.tally.client.endpoints.TallyOperationsService;
import com.adobe.cq.social.ugcbase.SocialUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;


/**
 * Created by mokatari on 10/12/17.
 */

@Component
@Service
public class UserUgcComponentFactoryImpl implements UserUgcComponentFactory {

    @Reference
    private ForumOperations forumOperations;

    @Reference
    private QnaForumOperations qnaForumOperations;

    @Reference
    private CommentOperations commentOperations;

    @Reference
    private TallyOperationsService tallyOperationsService;

    @Reference
    private CalendarOperations calendarOperations;

    @Reference
    private JournalOperations journalOperations;

    @Reference
    private FileLibraryOperations fileLibraryOperations;

    @Reference
    private SocialUtils socialUtils;

    private SocialResourceProvider resProvider;

    private SlingHttpServletRequest request;


    public UserUgcFilter getUserUgcFilter(ComponentEnum componentEnum){

        UserUgcFilter userUgcFilter;


        switch(componentEnum){
            case BLOG_ENTRY:
                userUgcFilter = new BlogEntryUserUgcFilter();
                break;
            case BLOG_COMMENT:
                userUgcFilter = new BlogCommentUserUgcFilter();
                break;
            default:
                throw new RuntimeException("ComponentEnum not defined for fetching userContent");
        }
        return userUgcFilter;
    }

    @Override
    public CommentOperations getOperation(ComponentEnum componentEnum) {
        CommentOperations commentOperations;

        switch (componentEnum){
            case BLOG_ENTRY:
            case BLOG_COMMENT:
                commentOperations = journalOperations;
                break;
            default:
                throw new RuntimeException("ComponentEnum not defined for operation");
        }
        return commentOperations;
    }


    @Override
    public UserUgcFilter getUserUgcFilter(Class T) {
        return null;
    }
}
