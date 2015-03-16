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

import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Field;
import com.google.appengine.api.search.GeoPoint;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.Query;
import com.google.appengine.api.search.QueryOptions;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.search.SortExpression;
import com.google.appengine.api.search.SortOptions;
import com.google.sample.mobileassistantbackend.models.PlaceInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.google.appengine.api.utils.
        SystemProperty.Environment.Value.Development;
import static com.google.appengine.api.utils.SystemProperty.environment;

/**
 * Helper class for geo-proximity related management of Places.
 */
public final class PlacesHelper {

    /**
     * Log output.
     */
    private static final Logger LOG = Logger
            .getLogger(PlacesHelper.class.getName());

    /**
     * The datastore index containing the places that we will use to search.
     */
    private static final String INDEX_NAME = "Places";

    /**
     * The double precision to use for comparisons.
     */
    private static final double EPSILON = 0.0001;

    /**
     * The number of meters in a kilometer.
     */
    private static final int METERS_IN_KILOMETER = 1000;

    /**
     * The radius of the earth, in kilometers.
     */
    private static final double EARTH_RADIUS = 6378.1;

    /**
     * A fake distance used in the dev environment.
     */
    private static  final int FAKE_DISTANCE_FOR_DEV = 5;

    /**
     * Default constructor, never called.
     */
    private PlacesHelper() {
    }

    /**
     * Returns the Places index in the datastore.
     * @return The index to use to search places in the datastore.
     */
    public static Index getIndex() {
        IndexSpec indexSpec = IndexSpec.newBuilder().setName(INDEX_NAME)
                .build();
        return SearchServiceFactory.getSearchService().getIndex(indexSpec);
    }

    /**
     * Builds a new Place document to insert in the Places index.
     * @param placeId      the identifier of the place in the database.
     * @param placeName    the name of the place.
     * @param placeAddress the address of the place.
     * @param location     the GPS location of the place, as a GeoPt.
     * @return the Place document created.
     */
    public static Document buildDocument(
            final Long placeId, final String placeName,
            final String placeAddress, final GeoPt location) {
        GeoPoint geoPoint = new GeoPoint(location.getLatitude(),
                location.getLongitude());

        Document.Builder builder = Document.newBuilder()
                .addField(Field.newBuilder().setName("id")
                        .setText(placeId.toString()))
                .addField(Field.newBuilder().setName("name").setText(placeName))
                .addField(Field.newBuilder().setName("address")
                        .setText(placeAddress))
                .addField(Field.newBuilder().setName("place_location")
                        .setGeoPoint(geoPoint));

        // geo-location doesn't work under dev_server, so let's add another
        // field to use for retrieving documents
        if (environment.value() == Development) {
            builder.addField(Field.newBuilder().setName("value").setNumber(1));
        }

        return builder.build();
    }

    /**
     * Returns the nearest places to the location of the user.
     * @param location the location of the user.
     * @param distanceInMeters the maximum distance to the user.
     * @param resultCount the maximum number of places returned.
     * @return List of up to resultCount places in the datastore ordered by
     *      the distance to the location parameter and less than
     *      distanceInMeters meters to the location parameter.
     */
    public static List<PlaceInfo> getPlaces(final GeoPt location,
            final long distanceInMeters, final int resultCount) {

        // Optional: use memcache

        String geoPoint = "geopoint(" + location.getLatitude() + ", " + location
                .getLongitude()
                + ")";
        String locExpr = "distance(place_location, " + geoPoint + ")";

        // Build the SortOptions with 2 sort keys
        SortOptions sortOptions = SortOptions.newBuilder()
                .addSortExpression(SortExpression.newBuilder()
                        .setExpression(locExpr)
                        .setDirection(SortExpression.SortDirection.ASCENDING)
                        .setDefaultValueNumeric(distanceInMeters + 1))
                .setLimit(resultCount)
                .build();
        // Build the QueryOptions
        QueryOptions options = QueryOptions.newBuilder()
                .setSortOptions(sortOptions)
                .build();
        // Query string
        String searchQuery = "distance(place_location, " + geoPoint + ") < "
                + distanceInMeters;

        Query query = Query.newBuilder().setOptions(options).build(searchQuery);

        Results<ScoredDocument> results = getIndex().search(query);

        if (results.getNumberFound() == 0) {
            // geo-location doesn't work under dev_server
            if (environment.value() == Development) {
                // return all documents
                results = getIndex().search("value > 0");
            }
        }

        List<PlaceInfo> places = new ArrayList<>();

        for (ScoredDocument document : results) {
            if (places.size() >= resultCount) {
                break;
            }

            GeoPoint p = document.getOnlyField("place_location").getGeoPoint();

            PlaceInfo place = new PlaceInfo();
            place.setPlaceId(Long.valueOf(document.getOnlyField("id")
                    .getText()));
            place.setName(document.getOnlyField("name").getText());
            place.setAddress(document.getOnlyField("address").getText());

            place.setLocation(new GeoPt((float) p.getLatitude(),
                    (float) p.getLongitude()));

            // GeoPoints are not implemented on dev server and latitude and
            // longitude are set to zero
            // But since those are doubles let's play safe
            // and use double comparison with epsilon set to EPSILON
            if (Math.abs(p.getLatitude()) <= EPSILON
                    && Math.abs(p.getLongitude()) <= EPSILON) {
                // set a fake distance of 5+ km
                place.setDistanceInKilometers(FAKE_DISTANCE_FOR_DEV + places
                        .size());
            } else {
                double distance = distanceInMeters / METERS_IN_KILOMETER;
                try {
                    distance = getDistanceInKm(
                            p.getLatitude(), p.getLongitude(),
                            location.getLatitude(),
                            location.getLongitude());
                } catch (Exception e) {
                    LOG.warning("Exception when calculating a distance: " + e
                            .getMessage());
                }

                place.setDistanceInKilometers(distance);
            }

            places.add(place);
        }
        return places;
    }

    /**
     * Computes the geodesic distance between two GPS coordinates.
     * @param latitude1 the latitude of the first point.
     * @param longitude1 the longitude of the first point.
     * @param latitude2 the latitude of the second point.
     * @param longitude2 the longitude of the second point.
     * @return the geodesic distance between the two points, in kilometers.
     */
    static double getDistanceInKm(
            final double latitude1, final double longitude1,
            final double latitude2, final double longitude2) {

        double lat1 = Math.toRadians(latitude1);
        double lat2 = Math.toRadians(latitude2);
        double long1 = Math.toRadians(longitude1);
        double long2 = Math.toRadians(longitude2);

        return EARTH_RADIUS * Math
                .acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1)
                        * Math.cos(lat2) * Math.cos(Math.abs(long1 - long2)));
    }
}
