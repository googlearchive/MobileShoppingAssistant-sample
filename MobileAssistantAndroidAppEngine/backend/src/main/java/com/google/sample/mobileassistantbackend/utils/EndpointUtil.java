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

package com.google.sample.mobileassistantbackend.utils;

import com.google.api.server.spi.response.UnauthorizedException;
import com.google.appengine.api.users.User;
import com.google.sample.mobileassistantbackend.models.UserAccount;

/**
 * Utility class for Endpoints.
 */
public final class EndpointUtil {

    /**
     * Default constructor, never called.
     */
    private EndpointUtil() {
    }

    /**
     * Throws an exception if the user is not an admin.
     * @param user User object to be checked if it represents an admin.
     * @throws com.google.api.server.spi.response.UnauthorizedException when the
     *      user object does not represent an admin.
     */
    public static void throwIfNotAdmin(final User user) throws
            UnauthorizedException {
        if (!UserAccount.isAdmin(user)) {
            throw new UnauthorizedException(
                    "You are not authorized to perform this operation");
        }
    }

    /**
     * Throws an exception if the user object doesn't represent an authenticated
     * call.
     * @param user User object to be checked if it represents an authenticated
     *      caller.
     * @throws com.google.api.server.spi.response.UnauthorizedException when the
     *      user object does not represent an admin.
     */
    public static void throwIfNotAuthenticated(final User user) throws
            UnauthorizedException {
        if (user == null || user.getEmail() == null) {
            throw new UnauthorizedException(
                    "Only authenticated users may invoke this operation");
        }
    }
}
