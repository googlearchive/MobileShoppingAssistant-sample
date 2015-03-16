/*
 * Copyright 2015 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.sample.mobileassistantbackend.apis;

import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.users.User;
import com.google.sample.mobileassistantbackend.Constants;
import com.google.sample.mobileassistantbackend.models.CheckIn;
import com.google.sample.mobileassistantbackend.utils.EndpointUtil;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;
import static com.google.sample.mobileassistantbackend.OfyService.ofy;


/**
 * Exposes REST API over CheckIn resources.
 */
@Api(name = "shoppingAssistant", version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = Constants.API_OWNER,
                ownerName = Constants.API_OWNER,
                packagePath = Constants.API_PACKAGE_PATH
        )
)
@ApiClass(resource = "checkins",
        clientIds = {
                Constants.ANDROID_CLIENT_ID,
                Constants.IOS_CLIENT_ID,
                Constants.WEB_CLIENT_ID},
        audiences = {Constants.AUDIENCE_ID}
)
/**
 * An endpoint class we are exposing.
 */
public class CheckInEndpoint {

    /**
     * Log output.
     */
    private static final Logger LOG =
            Logger.getLogger(CheckInEndpoint.class.getName());

    /**
     * Lists all the entities inserted in datastore.
     * @param user the user requesting the entities.
     * @return the list of all entities persisted.
     * @throws com.google.api.server.spi.ServiceException if user is not
     * authorized
     */
    @SuppressWarnings({"cast", "unchecked"})
    public final List<CheckIn> listCheckIn(final User user) throws
            ServiceException {
        EndpointUtil.throwIfNotAdmin(user);

        return ofy().load().type(CheckIn.class).list();
    }

    /**
     * Gets the entity having primary key id.
     * @param id the primary key of the java bean.
     * @param user the user requesting the entity.
     * @return The entity with primary key id.
     * @throws com.google.api.server.spi.ServiceException if user is not
     * authorized
     */
    @ApiMethod(httpMethod = "GET")
    public final CheckIn getCheckIn(@Named("id") final Long id, final User user)
            throws ServiceException {
        EndpointUtil.throwIfNotAdmin(user);

        return findCheckIn(id);
    }

    /**
     * Inserts the entity into App Engine datastore. It uses HTTP POST method.
     * @param checkin the entity to be inserted.
     * @param user the user trying to insert the entity.
     * @return The inserted entity.
     * @throws com.google.api.server.spi.ServiceException if user is not
     * authorized
     */
    @ApiMethod(httpMethod = "POST")
    public final CheckIn insertCheckIn(final CheckIn checkin, final User user)
            throws ServiceException {
        EndpointUtil.throwIfNotAuthenticated(user);

        checkin.setUserEmail(user.getEmail());
        checkin.setCheckInDate(new Date());

        // Do not use the key provided by the caller; use a generated key.
        checkin.clearKey();

        ofy().save().entity(checkin).now();

        // generate personalized offers when user checks into a place and send
        // the, to the user using push notification
        pushPersonalizedOffers(checkin.getPlaceId(), user);

        return checkin;
    }

    /**
     * Updates a entity. It uses HTTP PUT method.
     * @param checkin the entity to be updated.
     * @param user the user trying to update the entity.
     * @return The updated entity.
     * @throws com.google.api.server.spi.ServiceException if user is not
     * authorized
     */
    @ApiMethod(httpMethod = "PUT")
    public final CheckIn updateCheckIn(final CheckIn checkin, final User user)
            throws ServiceException {
        EndpointUtil.throwIfNotAdmin(user);

        ofy().save().entity(checkin).now();

        return checkin;
    }

    /**
     * Removes the entity with primary key id. It uses HTTP DELETE method.
     * @param id the primary key of the entity to be deleted.
     * @param user the user trying to delete the entity.
     * @throws com.google.api.server.spi.ServiceException if user is not
     * authorized
     */
    @ApiMethod(httpMethod = "DELETE")
    public final void removeCheckIn(@Named("id") final Long id, final User user)
            throws ServiceException {
        EndpointUtil.throwIfNotAdmin(user);

        CheckIn checkIn = findCheckIn(id);
        if (checkIn == null) {
            LOG.info(
                    "CheckIn " + id + " not found, skipping deletion.");
            return;
        }
        ofy().delete().entity(checkIn).now();
    }

    /**
     * Searches an entity by ID.
     * @param id the checkin ID to search
     * @return the CheckIn associated to id
     */
    private CheckIn findCheckIn(final Long id) {
        return ofy().load().type(CheckIn.class).id(id).now();
    }

    /**
     * Sends personalized offers to a user that checked in at a place.
     * @param placeId the place from which we want to retrieve offers.
     * @param user the user to whom we send the personalized offers.
     */
    private void pushPersonalizedOffers(final String placeId, final User user) {
        // insert a task to a queue
        LOG.info("adding a task to recommendations-queue");
        Queue queue = QueueFactory.getQueue("recommendations-queue");

        try {
            String userEmail = user.getEmail();
            queue.add(withUrl("/tasks/recommendations")
                    .param("userEmail", userEmail).param("placeId", placeId));
            LOG.info("task added");
        } catch (RuntimeException e) {
            LOG.severe(e.getMessage());
        }
    }
}
