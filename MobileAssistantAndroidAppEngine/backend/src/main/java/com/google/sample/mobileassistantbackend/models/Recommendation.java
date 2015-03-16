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
import java.util.UUID;

/**
 * Product Recommendation entity.
 */
@Entity
public class Recommendation {

    /**
     * Unique identifier of this Entity in the database.
     */
    @Id
    private String id;

    /**
     * The recommendation title.
     */
    private String title;

    /**
     * The recommendation description.
     */
    private String description;

    /**
     * The URL to the image associated to this recommendation.
     */
    private String imageUrl;

    /**
     * The expiration Date for this recommendation.
     */
    private Date expiration;

    /**
     * Generates an id for the entity.
     */
    public final void generateId() {
        this.id = UUID.randomUUID().toString();
    }

    /**
     * Returns the recommendation title.
     * @return the recommendation title.
     */
    public final String getTitle() {
        return title;
    }

    /**
     * Sets the recommendation title.
     * @param pTitle the title to set for this recommendation.
     */
    public final void setTitle(final String pTitle) {
        this.title = pTitle;
    }

    /**
     * Returns the recommendation description.
     * @return the recommendation description.
     */
    public final String getDescription() {
        return description;
    }

    /**
     * Sets the recommendation description.
     * @param pDescription the description to set for this recommendation.
     */
    public final void setDescription(final String pDescription) {
        this.description = pDescription;
    }

    /**
     * Returns the URL of the image of this recommendation.
     * @return the URL of the image associated with this recommendation.
     */
    public final String getImageUrl() {
        return imageUrl;
    }

    /**
     * Sets the URL of the image of this recommendation.
     * @param pImageUrl the URL to set for the image associated to this
     *      recommendation.
     */
    public final void setImageUrl(final String pImageUrl) {
        this.imageUrl = pImageUrl;
    }

    /**
     * Returns the expiration date of this recommendation.
     * @return the expiration date of this recommendation.
     */
    public final Date getExpiration() {
        return expiration;
    }

    /**
     * Sets the expiration date of this recommendation.
     * @param pExpiration the expiration date to set for this recommendation.
     */
    public final void setExpiration(final Date pExpiration) {
        this.expiration = pExpiration;
    }

}
