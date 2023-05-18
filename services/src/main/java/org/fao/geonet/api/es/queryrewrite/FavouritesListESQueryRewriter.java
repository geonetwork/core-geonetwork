/*
 * Copyright (C) 2023 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */
package org.fao.geonet.api.es.queryrewrite;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jeeves.server.UserSession;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.FavouriteMetadataList;
import org.fao.geonet.repository.FavouriteMetadataListRepository;
import org.fao.geonet.repository.FavouriteMetadataListItemRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.utils.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.fao.geonet.api.selections.FavouriteMetadataListApi.SESSION_COOKIE_NAME;

/**
 * typically, an incoming elastic request will be very large (see below for an example, with 90% removed).
 * The important part is:
 *           {
 *                 "terms": {
 *                   "favouritesList": [
 *                        104
 *                    ]
 *                }
 *            }
 *
 *  If this is seen in the elastic query, that section will be removed and replaced with something like this:
 *
 *  {
 *    query:
 *     {
 *         query_string: "_id(uuid1) OR _id(uuid2) ..."
 *     }
 *  }
 *
 *  If the there is no sort, then an _id sort is added (for paging).
 *
 * None of the rest of the query is changed.  This means you can add more queries (in the GN4 UI) and they will
 * work on the backend.
 *
 *

{
    "from": 0,
    "size": 30,
    "sort": [
    "_score"
    ],
    "query": {
    "function_score": {
    ...
    "score_mode": "multiply",
    "query": {
       "bool": {
          "must": [
             {
               "terms": {
                  "isTemplate": [
                     "n"
                  ]
               }
            },
            {
                "terms": {
                  "favouritesList": [
                       104
                   ]
               }
           }
    ]
    }
    }
    }
    },
    "aggregations": {
    ...
    },
    "_source": {
    ...

    },
    "track_total_hits": true
    }
 */
@Component
public class FavouritesListESQueryRewriter implements ESQueryRewriter{


    @Autowired
    FavouriteMetadataListItemRepository userMetadataSelectionRepo;

    @Autowired
    FavouriteMetadataListRepository userMetadataSelectionListRepo;

    @Autowired
    UserRepository userRepository;

    /**
     * Rewrites an elasticsearch query.
     *
     * If there is an exception, it is logged.  However, this will return with an unmodified query.
     *
     * @param httpSession - used to get the current User (for security)
     * @param request - used to get the current http session (for security)
     * @param jsonESQuery - original query
     * @return
     */
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
                if (!termsNode.has("favouritesList")) {
                    continue;
                }
                int selectionId =  ((IntNode)termsNode.get("favouritesList").get(0)).asInt();
                ObjectNode queryString = replacement(selectionId,mapper, httpSession, request);
                queryItems.remove(index);
                queryItems.add(queryString);
                fixsort((ObjectNode) root);
                String result = mapper.writeValueAsString(root);
                return result;
            }
            return jsonESQuery;
        }
        catch (Exception e) {
            //don't modify
            Log.debug(Geonet.SEARCH_ENGINE, "FavouritesListESQueryRewriter",e);
            return jsonESQuery;
        }
    }

    private void fixsort(ObjectNode root) {
        if (!root.has("sort")) {
            root.putArray("sort").add("_id");
        }
    }


    /**
     * constructs the
     *
     *  {
     *    query:
     *     {
     *         query_string: "_id(uuid1) OR _id(uuid2) ..."
     *     }
     *  }
     *
     *  that will replace the
     *
     *           {
     *                 "terms": {
     *                   "favouritesList": [
     *                        104
     *                    ]
     *                }
     *            }
     *
     *   This checks security (i.e. does the user/session have read permission on the favourites list?)
     */
    private ObjectNode replacement(int favouritesListId, ObjectMapper mapper, HttpSession httpSession, HttpServletRequest request) throws Exception {
        Optional<FavouriteMetadataList> selectionList= userMetadataSelectionListRepo.findById(favouritesListId);
        if (!selectionList.isPresent()) {
            throw new Exception("could not find favouritesListId="+ favouritesListId);
        }
        //check security
        boolean isAdmin = isAdmin(httpSession);
        String sessionId = getSessionId(request);
        User user =  getUser(httpSession);

        if (!permittedRead(selectionList.get(),user,sessionId,isAdmin)) {
            throw new Exception("not permitted to read");
        }

        List<String>  uuids = userMetadataSelectionRepo.queryByParent(favouritesListId);
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
    private boolean isAdmin(HttpSession httpSession) {
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
    boolean permittedRead(FavouriteMetadataList list, User user, String sessionId, boolean isAdmin) {
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
