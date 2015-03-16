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

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiClass;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.api.server.spi.response.CollectionResponse;
import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.users.User;
import com.google.sample.mobileassistantbackend.Constants;
import com.google.sample.mobileassistantbackend.models.Registration;
import com.google.sample.mobileassistantbackend.utils.EndpointUtil;

import java.util.List;
import java.util.logging.Logger;

import static com.google.sample.mobileassistantbackend.OfyService.ofy;

/**
 * <p>A registration endpoint class we are exposing for a device's GCM
 * registration id on the backend</p>
 *
 * <p>For more information, see
 * https://developers.google.com/appengine/docs/java/endpoints/
 * </p>
 *
 * <p>NOTE: This endpoint does not use any form of authorization or
 * authentication! If this app is deployed, anyone can access this endpoint! If
 * you'd like to add authentication, take a look at the documentation.</p>
 */
@Api(name = "shoppingAssistant", version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = Constants.API_OWNER,
                ownerName = Constants.API_OWNER,
                packagePath = Constants.API_PACKAGE_PATH
        )
)
@ApiClass(resource = "registrations",
        clientIds = {
                Constants.ANDROID_CLIENT_ID,
                Constants.IOS_CLIENT_ID,
                Constants.WEB_CLIENT_ID},
        audiences = {Constants.AUDIENCE_ID}
)
public class RegistrationEndpoint {

    /**
     * Log output.
     */
    private static final Logger LOG = Logger
            .getLogger(CheckInEndpoint.class.getName());


    /**
     * Registers a device to the backend.
     * @param regId The Google Cloud Messaging registration id to add.
     * @param user the user registering a device.
     * @throws com.google.api.server.spi.response.UnauthorizedException if
     * user is unauthenticated
     */
    @ApiMethod(httpMethod = "POST")
    public final void registerDevice(@Named("regId") final String regId,
            final User user) throws UnauthorizedException {
        EndpointUtil.throwIfNotAuthenticated(user);
        if (findRecord(regId) != null) {
            LOG.info("Device " + regId
                    + " already registered, skipping register");
            return;
        }
        Registration record = new Registration();
        record.setRegId(regId);
        ofy().save().entity(record).now();
    }

    /**
     * Unregisters a device from the backend.
     * @param regId The Google Cloud Messaging registration Id to remove     *
     * @param user the user unregistering a device.
     * @throws com.google.api.server.spi.response.UnauthorizedException if
     * user is unauthorized
     */
    @ApiMethod(httpMethod = "DELETE")
    public final void unregisterDevice(@Named("regId") final String regId,
            final User user) throws UnauthorizedException {
        EndpointUtil.throwIfNotAdmin(user);
        Registration record = findRecord(regId);
        if (record == null) {
            LOG.info(
                    "Device " + regId + " not registered, skipping unregister");
            return;
        }
        ofy().delete().entity(record).now();
    }

    /**
     * Returns a collection of registered devices.
     * @param count The number of devices to list
     * @param user the user listing registered devices.*
     * @return a list of Google Cloud Messaging registration Ids
     * @throws com.google.api.server.spi.response.UnauthorizedException if
     * user is unauthorized
     */
    @ApiMethod(httpMethod = "GET")
    public final CollectionResponse<Registration> listDevices(
            @Named("count") final int count,
            final User user) throws UnauthorizedException {
        EndpointUtil.throwIfNotAdmin(user);
        List<Registration> records = ofy().load()
                .type(Registration.class).limit(count)
                .list();
        return CollectionResponse.<Registration>builder()
                .setItems(records).build();
    }

    /**
     * Searches an entity by ID.
     * @param regId the registration ID to search
     * @return the Registration associated to regId
     */
    private Registration findRecord(final String regId) {
        return ofy().load().type(Registration.class)
                .filter("regId", regId).first().now();
    }

}
