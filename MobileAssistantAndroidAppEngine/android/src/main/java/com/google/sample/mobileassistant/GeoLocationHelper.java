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

package com.google.sample.mobileassistant;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.logging.Logger;

/**
 * Helper class for retrieving location from multiple providers and determining
 * the current
 * location.
 */
class GeoLocationHelper {

    /**
     * Two minutes in milliseconds.
     */
    private static final int TWO_MINUTES_IN_MILLISECONDS = 1000 * 60 * 2;

    /**
     * The location accuracy in meters that remains acceptable.
     */
    private static final int LOCATION_ACCURACY = 200;

    /**
     * The refresh rate for the location provider, in milliseconds.
     */
    private static final long LOCATION_PROVIDER_UPDATE_RATE = 5000;

    /**
     * The distance delta after which the location provider should send a
     * new location.
     */
    private static final float LOCATION_PROVIDER_UPDATE_DISTANCE = 100;

    /**
     * Log output.
     */
    private Logger log = Logger.getLogger(GeoLocationHelper.class.getName());

    /**
     * The current best known location for the user.
     */
    private Location currentBestLocation = null;

    /**
     * Listener that responds to location updates.
     */
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            // Called when a new location is found by a location provider.
            if (isBetterLocation(location)) {
                currentBestLocation = location;
            }
        }

        @Override
        public void onStatusChanged(final String provider, final int status,
                final Bundle extras) {
        }

        @Override
        public void onProviderEnabled(final String provider) {
        }

        @Override
        public void onProviderDisabled(final String provider) {
        }
    };

    /**
     * The location manager to retrieve the user location.
     */
    private LocationManager locationManager;

    /**
     * Determines whether a new location is "better" that then current best
     * location, taking into account when the location was retrieved and its
     * accuracy.
     * @param location a candidate location
     * @return boolean indicating whether the new location is better than the
     * * current best one or not
     */
    private boolean isBetterLocation(final Location location) {
        if (location == null) {
            return false;
        }

        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isNewer = timeDelta > 0;

        // If the current location fix has been taken more than two minutes
        // prior to the new location fix then use the new location because
        // the user has likely moved.
        if (timeDelta > TWO_MINUTES_IN_MILLISECONDS) {
            return true;
        }

        // If the "new" location fix is more than two minutes older, we assume
        // it is worse
        if (timeDelta < -TWO_MINUTES_IN_MILLISECONDS) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        // The accuracy returned by Location.getAccuracy() is expressed in
        // meters and the lower the value the more accurate the location is.
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
                .getAccuracy());
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isEquallyAccurate = accuracyDelta == 0;
        boolean isSlightlyLessAccurate = (accuracyDelta > 0) && (accuracyDelta
                <= LOCATION_ACCURACY);

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider =
                isSameProvider(location.getProvider(),
                        currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and
        // accuracy
        return (isMoreAccurate
                || (isNewer && (isEquallyAccurate || (isFromSameProvider
                && isSlightlyLessAccurate))));
    }

    /**
     * *
     * @param provider1 the first provider
     * @param provider2 the second provider
     * @return bollean indicating whether two providers are the same
     */
    private boolean isSameProvider(final String provider1,
            final String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    /**
     * Starts retrieving location updates.
     *
     * @param context Activity context
     */
    void startRetrievingLocation(final Context context) {
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);

        try {
            currentBestLocation = locationManager
                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            Location gpsLocation = locationManager
                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (isBetterLocation(gpsLocation)) {
                currentBestLocation = gpsLocation;
            }
        } catch (SecurityException e) {
            log.warning(
                    "SecurityException when retrieving the last known "
                            + "location: "
                            + e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warning(
                    "IllegalArgumentException when retrieving the last known "
                            + "location: "
                            + e
                            .getMessage());
        }

        // Register the listener with the Location Manager to receive
        // location updates as often as every 5 seconds and every 100 meters
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, LOCATION_PROVIDER_UPDATE_RATE,
                LOCATION_PROVIDER_UPDATE_DISTANCE, locationListener);
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                LOCATION_PROVIDER_UPDATE_RATE,
                LOCATION_PROVIDER_UPDATE_DISTANCE, locationListener);
    }

    /**
     * Stops retrieving location updates.
     */
    void stopRetrievingLocation() {
        locationManager.removeUpdates(locationListener);
    }

    /**
     * Returns the current location.
     * @return the current location
     */
    Location getCurrentLocation() {
        return currentBestLocation;
    }
}
