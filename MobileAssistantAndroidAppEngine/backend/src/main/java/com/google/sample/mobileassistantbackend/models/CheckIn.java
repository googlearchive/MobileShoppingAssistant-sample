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

package com.google.sample.mobileassistantbackend.models;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.Date;



/**
 * CheckIn entity used to represent information about customers checking into
 * * places.
 */
@Entity
public class CheckIn {

    /**
     * Unique identifier of this Entity in the database.
     */
    @Id
    private Long key;

    /**
     * The identifier of the place the user checking in at.
     */
    private String placeId;

    /**
     * The email of the user checking in at this place.
     */
    private String userEmail;

    /**
     * The date of the check in at this place.
     */
    private Date checkinDate;

    /**
     *
     * @return the unique identifier of this Entity.
     */
    public final Long getKey() {
        return key;
    }

    /**
     * Resets the Entity key to null.
     */
    public final void clearKey() {
        key = null;
    }

    /**
     * Returns the unique identifier of this entity.
     * @return the identifier of the place.
     */
    public final String getPlaceId() {
        return placeId;
    }

    /**
     * Sets the place ID of the place the user is checking in at.
     * @param pPlaceId the identifier of the place the user is checking in at.
     */
    public final void setPlaceId(final String pPlaceId) {
        this.placeId = pPlaceId;
    }

    /**
     * Returns the email of the user that checked in.
     * @return the email of the user.
     */
    public final String getUserEmail() {
        return userEmail;
    }

    /**
     * Sets the email of the user that is checking in.
     * @param pUserEmail the email of the user that is checking in at this
     *      place.
     */
    public final void setUserEmail(final String pUserEmail) {
        this.userEmail = pUserEmail;
    }

    /**
     * Returns the date of the check in.
     * @return the date of the ckeck in.
     */
    public final Date getCheckInDate() {
        return checkinDate;
    }

    /**
     * Sets the date of the check in.
     * @param date the date of the check in at this place.
     */
    public final void setCheckInDate(final Date date) {
        checkinDate = date;
    }
}
