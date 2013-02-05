package fr.ippon.tatami.web.rest;

import fr.ippon.tatami.domain.User;
import fr.ippon.tatami.security.AuthenticationService;
import fr.ippon.tatami.service.SearchService;
import fr.ippon.tatami.service.SuggestionService;
import fr.ippon.tatami.service.UserService;
import fr.ippon.tatami.service.util.DomainUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;

/**
 * REST controller for managing users.
 *
 * @author Julien Dubois
 */
@Controller
public class UserController {

    private final Log log = LogFactory.getLog(UserController.class);

    @Inject
    private UserService userService;

    @Inject
    private AuthenticationService authenticationService;

    @Inject
    private SearchService searchService;

    @Inject
    private SuggestionService suggestionService;

    /**
     * GET  /users/show?screen_name=jdubois -> get the "jdubois" user
     */
    @RequestMapping(value = "/rest/users/show",
            method = RequestMethod.GET,
            produces = "application/json")
    @ResponseBody
    public User getUser(@RequestParam("screen_name") String username) {
        if (this.log.isDebugEnabled()) {
            this.log.debug("REST request to get Profile : " + username);
        }
        User user = userService.getUserByUsername(username);
        return user;
    }

    /**
     * GET  /users/suggestions -> suggest users to follow
     */
    @RequestMapping(value = "/rest/users/suggestions",
            method = RequestMethod.GET,
            produces = "application/json")
    @ResponseBody
    public Collection<User> suggestions() {
        String login = authenticationService.getCurrentUser().getLogin();
        return suggestionService.suggestUsers(login);
    }

    /**
     * GET  /users/searchStatus -> searchStatus user by username<br>
     * Should return a collection of users matching the query.<br>
     * The collection doesn't contain the current user even if he matches the query.<br>
     * If nothing matches, an empty collection (but not null) is returned.<br>
     *
     * @param query the query
     * @return a Collection of User
     */
    @RequestMapping(value = "/rest/users/search",
            method = RequestMethod.GET,
            produces = "application/json")
    @ResponseBody
    public Collection<User> searchUsers(@RequestParam("q") String query) {
        String prefix = query.toLowerCase();
        if (this.log.isDebugEnabled()) {
            this.log.debug("REST request to find users starting with : " + prefix);
        }
        User currentUser = authenticationService.getCurrentUser();
        String domain = DomainUtil.getDomainFromLogin(currentUser.getLogin());
        Collection<String> logins = searchService.searchUserByPrefix(domain, prefix);
        return userService.getUsersByLogin(logins);
    }

    /**
     * GET  /users -> Get all users of domain
     */
    @RequestMapping(value = "/rest/users",
            method = RequestMethod.GET,
            produces = "application/json")
    @ResponseBody
    public Collection<User> getAll(@RequestParam(required = false) Integer pagination) {
        if (pagination == null) {
            pagination = 0;
        }
        List<User> users = userService.getUsersForCurrentDomain(pagination);
        return users;
    }
}