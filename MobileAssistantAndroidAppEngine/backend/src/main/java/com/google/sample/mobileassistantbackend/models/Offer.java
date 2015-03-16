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

/**
 * Offer entity.
 */
@Entity
public class Offer {

    /**
     * Unique identifier of this Entity in the database.
     */
    @Id
    private Long offerId;

    /**
     * The offer title.
     */
    private String title;

    /**
     * The offer description.
     */
    private String description;

    /**
     * The URL to the image associated to the offer.
     */
    private String imageUrl;

    /**
     * Returns the unique identifier of this offer.
     * @return the unique identifier associated to this offer.
     */
    public final Long getOfferID() {
        return offerId;
    }

    /**
     * Sets the unique identifier of this offer.
     * @param offerID the unique identifier to associate to this offer.
     */
    public final void setOfferID(final Long offerID) {
        this.offerId = offerID;
    }

    /**
     * Returns the offer title.
     * @return The offer title.
     */
    public final String getTitle() {
        return title;
    }

    /**
     * Sets the offer title.
     * @param pTitle The title to set for this offer.
     */
    public final void setTitle(final String pTitle) {
        this.title = pTitle;
    }

    /**
     * Returns the offer description.
     * @return The offer description.
     */
    public final String getDescription() {
        return description;
    }

    /**
     * Sets the offer description.
     * @param pDescription The offer description to set.
     */
    public final void setDescription(final String pDescription) {
        this.description = pDescription;
    }

    /**
     * Returns the URL of the image for this offer.
     * @return The URL of the image associated to this offer.
     */
    public final String getImageUrl() {
        return imageUrl;
    }

    /**
     * Sets the URL of the image for this offer.
     * @param pImageUrl The URL of the image to associate to this offer.
     */
    public final void setImageUrl(final String pImageUrl) {
        this.imageUrl = pImageUrl;
    }

}
