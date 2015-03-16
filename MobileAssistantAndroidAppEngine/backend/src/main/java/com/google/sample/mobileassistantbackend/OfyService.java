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

package com.google.sample.mobileassistantbackend;

import com.google.sample.mobileassistantbackend.models.CheckIn;
import com.google.sample.mobileassistantbackend.models.Offer;
import com.google.sample.mobileassistantbackend.models.Place;
import com.google.sample.mobileassistantbackend.models.Recommendation;
import com.google.sample.mobileassistantbackend.models.Registration;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;

/**
 * Objectify service wrapper so we can statically register our persistence
 * classes.
 * More on Objectify here : https://code.google.com/p/objectify-appengine/
 */
public final class OfyService {
    /**
     * Default constructor, never called.
     */
    private OfyService() {
    }

    static {
            factory().register(Registration.class);
            factory().register(CheckIn.class);
            factory().register(Offer.class);
            factory().register(Recommendation.class);
            factory().register(Place.class);
    }

    /**
     * Returns the Objectify service wrapper.
     * @return The Objectify service wrapper.
     */
    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }

    /**
     * Returns the Objectify factory service.
     * @return The factory service.
     */
    public static ObjectifyFactory factory() {
        return ObjectifyService.factory();
    }
}
