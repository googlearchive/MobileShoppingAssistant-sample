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
import com.google.api.server.spi.response.BadRequestException;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.users.User;
import com.google.sample.mobileassistantbackend.Constants;
import com.google.sample.mobileassistantbackend.models.Place;
import com.google.sample.mobileassistantbackend.models.PlaceInfo;
import com.google.sample.mobileassistantbackend.utils.EndpointUtil;
import com.google.sample.mobileassistantbackend.utils.PlacesHelper;

import java.util.List;
import java.util.logging.Logger;

import static com.google.sample.mobileassistantbackend.OfyService.ofy;

/**
 * Exposes REST API over Place resources.
 */
@Api(name = "shoppingAssistant", version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = Constants.API_OWNER,
                ownerName = Constants.API_OWNER,
                packagePath = Constants.API_PACKAGE_PATH
        )
)
@ApiClass(resource = "places",
        clientIds = {
                Constants.ANDROID_CLIENT_ID,
                Constants.IOS_CLIENT_ID,
                Constants.WEB_CLIENT_ID},
        audiences = {Constants.AUDIENCE_ID}
)
public class PlaceEndpoint {

    /**
     * Log output.
     */
    private static final Logger LOG = Logger
            .getLogger(PlaceEndpoint.class.getName());

    /**
     * Maximum number of places to return.
     */
    private static final int MAXIMUM_NUMBER_PLACES = 100;

    /**
     * Maximum distance to search places to return.
     */
    private static final int MAXIMUM_DISTANCE = 100;

    /**
     * The number of meters in a kilometer.
     */
    private static final int METERS_IN_KILOMETER = 1000;

    /**
     * Lists nearby places.
     * @param longitudeString the location longitude.
     * @param latitudeString  the location latitude.
     * @param pDistanceInKm   the maximum distance to search for nearby places.
     * @param pCount          the maximum number of places returned.
     * @param user            the user that requested the entities.
     * @return List of nearby places.
     * @throws com.google.api.server.spi.ServiceException if user is not
     * authorized
     */
    @ApiMethod(httpMethod = "GET")
    public final List<PlaceInfo> getPlaces(@Named("longitude") final String
            longitudeString,
            @Named("latitude") final String latitudeString,
            @Named("distanceInKm") final long pDistanceInKm,
            @Named("count") final int pCount, final User user) throws
            ServiceException {

        float latitude;
        float longitude;
        GeoPt location;
        int count = pCount;
        long distanceInKm = pDistanceInKm;

        try {
            latitude = (float) Double.parseDouble(latitudeString);
        } catch (Exception e) {
            throw new BadRequestException(
                    "Invalid value of 'latitude' argument");
        }

        try {
            longitude = (float) Double.parseDouble(longitudeString);
        } catch (Exception e) {
            throw new BadRequestException(
                    "Invalid value of 'longitude' argument");
        }

        try {
            location = new GeoPt(latitude, longitude);
        } catch (Exception e) {
            throw new BadRequestException(
                    "Invalid pair of 'latitude' and 'longitude' arguments");
        }

        // limit the result set to up to MAXIMUM_NUMBER_PLACES places within
        // up to MAXIMUM_DISTANCE km
        if (count > MAXIMUM_NUMBER_PLACES) {
            count = MAXIMUM_NUMBER_PLACES;
        } else if (count <= 0) {
            throw new BadRequestException("Invalid value of 'count' argument");
        }

        if (distanceInKm > MAXIMUM_DISTANCE) {
            distanceInKm = MAXIMUM_DISTANCE;
        } else if (distanceInKm < 0) {
            throw new BadRequestException(
                    "Invalid value of 'distanceInKm' argument");
        }

        List<PlaceInfo> places = PlacesHelper
                .getPlaces(location, METERS_IN_KILOMETER * distanceInKm, count);

        return places;
    }

    /**
     * Gets the entity having primary key id.
     * @param id the primary key of the java bean.
     * @param user the user that requested the entities.
     * @return The entity with primary key id.
     * @throws com.google.api.server.spi.ServiceException if user is not
     * authorized
     */
    @ApiMethod(httpMethod = "GET")
    public final Place getPlace(@Named("id") final Long id, final User user)
            throws ServiceException {
        EndpointUtil.throwIfNotAdmin(user);

        return findPlace(id);
    }

    /**
     * Inserts the entity into App Engine datastore. It uses HTTP POST method.
     * @param place the entity to be inserted.
     * @param user the user that requested to insert the entity.
     * @return The inserted entity.
     * @throws com.google.api.server.spi.ServiceException if user is not
     * authorized
     */
    @ApiMethod(httpMethod = "POST")
    public final Place insertPlace(final Place place, final User user) throws
            ServiceException {
        EndpointUtil.throwIfNotAdmin(user);

        ofy().save().entity(place).now();

        return place;
    }

    /**
     * Updates an entity. It uses HTTP PUT method.
     * @param place the entity to be updated.
     * @param user the user that requested the update to the entity.
     * @return The updated entity.
     * @throws com.google.api.server.spi.ServiceException if user is not
     * authorized
     */
    @ApiMethod(httpMethod = "PUT")
    public final Place updatePlace(final Place place, final User user) throws
            ServiceException {
        EndpointUtil.throwIfNotAdmin(user);

        ofy().save().entity(place).now();

        return place;
    }

    /**
     * Removes the entity with primary key id. It uses HTTP DELETE method.
     * @param id the primary key of the entity to be deleted.
     * @param user the user that requested the deletion of the entity.
     * @throws com.google.api.server.spi.ServiceException if user is not
     * authorized
     */
    @ApiMethod(httpMethod = "DELETE")
    public final void removePlace(@Named("id") final Long id, final User user)
            throws ServiceException {
        EndpointUtil.throwIfNotAdmin(user);

        Place place = findPlace(id);
        if (place == null) {
            LOG.info(
                    "Place " + id + " not found, skipping deletion.");
            return;
        }
        ofy().delete().entity(place).now();
    }

    /**
     * Searches an entity by ID.
     * @param id the place ID to search
     * @return the Place associated to id
     */
    private Place findPlace(final Long id) {
        return ofy().load().type(Place.class).id(id).now();
    }
}
