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
import com.google.appengine.api.users.User;
import com.google.sample.mobileassistantbackend.Constants;
import com.google.sample.mobileassistantbackend.models.Offer;
import com.google.sample.mobileassistantbackend.utils.EndpointUtil;

import java.util.List;
import java.util.logging.Logger;

import static com.google.sample.mobileassistantbackend.OfyService.ofy;

/**
 * Exposes REST API over Offer resources.
 */
@Api(name = "shoppingAssistant", version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = Constants.API_OWNER,
                ownerName = Constants.API_OWNER,
                packagePath = Constants.API_PACKAGE_PATH
        )
)
@ApiClass(resource  = "offers",
        clientIds = {
                Constants.ANDROID_CLIENT_ID,
                Constants.IOS_CLIENT_ID,
                Constants.WEB_CLIENT_ID},
        audiences = {Constants.AUDIENCE_ID}
)
public class OfferEndpoint {

    /**
     * Log output.
     */
    private static final Logger LOG = Logger
            .getLogger(OfferEndpoint.class.getName());


    /**
     * Lists all the entities inserted in datastore.
     * @param user the user requesting the entities.
     * @return List of all Offer entities persisted.
     */
    @SuppressWarnings({"cast", "unchecked"})
    @ApiMethod(httpMethod = "GET")
    public final List<Offer> listOffers(final User user) {
        return ofy().load().type(Offer.class).list();
    }

    /**
     * Gets the entity having primary key id.
     * @param id the primary key of the java bean Offer entity.
     * @param user the user requesting the entity.
     * @return The entity with primary key id.
     * @throws com.google.api.server.spi.ServiceException if user is not
     * authorized
     */
    @ApiMethod(httpMethod = "GET")
    public final Offer getOffer(@Named("id") final Long id, final User user)
            throws ServiceException {
        EndpointUtil.throwIfNotAdmin(user);

        return findOffer(id);
    }

    /**
     * Inserts the entity into App Engine datastore. It uses HTTP POST method.
     * @param offer the entity to be inserted.
     * @param user the user inserting the entity.
     * @return The inserted entity.
     * @throws com.google.api.server.spi.ServiceException if user is not
     * authorized
     */
    @ApiMethod(httpMethod = "POST")
    public final Offer insertOffer(final Offer offer, final User user) throws
            ServiceException {
        EndpointUtil.throwIfNotAdmin(user);

        ofy().save().entity(offer).now();

        return offer;
    }

    /**
     * Updates an entity. It uses HTTP PUT method.
     * @param offer the entity to be updated.
     * @param user the user modifying the entity.
     * @return The updated entity.
     * @throws com.google.api.server.spi.ServiceException if user is not
     * authorized
     */
    @ApiMethod(httpMethod = "PUT")
    public final Offer updateOffer(final Offer offer, final User user) throws
            ServiceException {
        EndpointUtil.throwIfNotAdmin(user);

        ofy().save().entity(offer).now();

        return offer;
    }

    /**
     * Removes the entity with primary key id. It uses HTTP DELETE method.
     * @param id the primary key of the entity to be deleted.
     * @param user the user deleting the entity.
     * @throws com.google.api.server.spi.ServiceException if user is not
     * authorized
     */
    @ApiMethod(httpMethod = "DELETE")
    public final void removeOffer(@Named("id") final Long id, final User user)
            throws ServiceException {
        EndpointUtil.throwIfNotAdmin(user);

        Offer offer = findOffer(id);
        if (offer == null) {
            LOG.info(
                    "Offer " + id + " not found, skipping deletion.");
            return;
        }
        ofy().delete().entity(offer).now();
    }

    /**
     * Searches an entity by ID.
     * @param id the offer ID to search
     * @return the Offer associated to id
     */
    private Offer findOffer(final Long id) {
        return ofy().load().type(Offer.class).id(id).now();
    }
}
