package org.fao.geonet.api.es.queryrewrite;

 import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
 import jeeves.server.UserSession;
 import net.sf.json.JSONObject;
 import org.fao.geonet.api.ApiUtils;
 import org.fao.geonet.domain.Profile;
 import org.fao.geonet.domain.User;
 import org.fao.geonet.domain.UserMetadataSelectionList;
 import org.fao.geonet.repository.UserMetadataSelectionListRepository;
 import org.fao.geonet.repository.UserMetadataSelectionRepository;
 import org.fao.geonet.repository.UserRepository;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.data.domain.Pageable;
 import org.springframework.stereotype.Component;

 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Optional;
 import java.util.stream.Collectors;

 import static org.fao.geonet.api.selections.UserSelectionApi.SESSION_COOKIE_NAME;


@Component
public class UserSelectionESQueryRewriter implements ESQueryRewriter{


    @Autowired
    UserMetadataSelectionRepository userMetadataSelectionRepo;

    @Autowired
    UserMetadataSelectionListRepository userMetadataSelectionListRepo;

    @Autowired
    UserRepository userRepository;

    @Override
    public String rewriteQuery(HttpSession httpSession, HttpServletRequest request, String jsonESQuery)  {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonESQuery);

            ArrayNode queryItems = (ArrayNode) root.get("query").get("function_score").get("query").get("bool").get("must");
            int index = -1;
            for (JsonNode node: queryItems) {
                index++;
                if (!node.isObject()) {
                    continue;
                }
                ObjectNode objectNode = (ObjectNode) node;
                if (!objectNode.has("terms")) {
                    continue;
                }
                ObjectNode termsNode = (ObjectNode) objectNode.get("terms");
                if (!termsNode.has("userselection")) {
                    continue;
                }
                int selectionId =  ((IntNode)termsNode.get("userselection").get(0)).asInt();
//                int from = root.get("from").asInt();
//                int size = root.get("size").asInt();
                ObjectNode queryString = replacement(selectionId,mapper, httpSession, request);
                queryItems.remove(index);
                queryItems.add(queryString);
                fixsort((ObjectNode) root);
               // ((ObjectNode)root).putArray("sort").add("_id");
                String result = mapper.writeValueAsString(root);
                return result;
            }
            return jsonESQuery;
        }
        catch (Exception e) {
            //don't process
            return jsonESQuery;
        }
    }

    private void fixsort(ObjectNode root) {
        if (!root.has("sort")) {
            root.putArray("sort").add("_id");
        }
    }
//
//    Pageable getPageable(int from, int size) throws Exception {
//        if (from % size != 0) {
//            throw new Exception("paging isn't consistent!");
//        }
//        return PageRequest.of( from/size, size);
//    }

    ObjectNode replacement(int selectionId,ObjectMapper mapper,HttpSession httpSession, HttpServletRequest request) throws Exception {
        Optional<UserMetadataSelectionList> selectionList= userMetadataSelectionListRepo.findById(selectionId);
4        if (!selectionList.isPresent()) {
            throw new Exception("could not find selectionId="+selectionId);
        }
        //check security
        boolean isAdmin = isAdmin(httpSession);
        String sessionId = getSessionId(request);
        User user =  getUser(httpSession);

        if (!permittedRead(selectionList.get(),user,sessionId,isAdmin)) {
            throw new Exception("not permitted to read");
        }

        List<String>  uuids = userMetadataSelectionRepo.queryByParent(selectionId);
        List<String> statements = uuids.stream()
            .map(x->"_id:("+x+")")
            .collect(Collectors.toList());
        String queryText = String.join(" OR ",statements);
        ObjectNode query = mapper.createObjectNode();
        ObjectNode queryString = mapper.createObjectNode();
        queryString.put("query",queryText);
        query.put("query_string",queryString);
        return query;
    }

    /**
     * Gets the user in the httpSession
     */
    User getUser(HttpSession httpSession) {
        UserSession session = ApiUtils.getUserSession(httpSession);
        String userId = session.getUserId(); // null = not logged on
        User user = null;
        if (userId != null) {
            user = userRepository.findOne(userId);
        }
        return user;
    }

    /**
     * returns true if the session represents an admin
     */
    boolean isAdmin(HttpSession httpSession) {
        UserSession session = ApiUtils.getUserSession(httpSession);
        return Profile.Administrator.equals(session.getProfile());
    }

    /**
     *   Get the `SESSION_COOKIE_NAME` cookie value from the request.
     *   If there isn't one, then returns null.
     */
    String getSessionId(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies ==null) {
            return null;
        }
        Optional<Cookie> sessionCookie = Arrays.stream(cookies)
            .filter(x->x.getName().equals(SESSION_COOKIE_NAME))
            .findFirst();

        return sessionCookie.isPresent() ?  sessionCookie.get().getValue() : null;
    }

    /**
     * @return true if the user/session is permitted to read the list
     */
    boolean permittedRead(UserMetadataSelectionList list, User user, String sessionId, boolean isAdmin) {
        // admin can always read
        if (isAdmin) {
            return true;
        }
        //owned by same user
        if ( (user != null) && (list.getUser() !=null) && (user.equals(list.getUser()))) {
            return true;
        }
        //owned by same session
        if ( (sessionId != null) && (list.getSessionId() !=null) && (sessionId.equals(list.getSessionId()))) {
            return true;
        }
        //public
        if (list.getIsPublic()) {
            return true;
        }
        //otherwise its private and owned by someone else
        return false;
    }

}
