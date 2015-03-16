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

import com.google.appengine.repackaged.com.google.common.collect.ImmutableMap;
import com.google.sample.mobileassistantbackend.models.Recommendation;
import com.google.sample.mobileassistantbackend.utils.CheckInUtil;

import java.io.IOException;
import java.util.Calendar;
import java.util.IllegalFormatException;
import java.util.Random;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.google.sample.mobileassistantbackend.OfyService.ofy;

/**
 * HttpServlet for processing request to generate personalized recommendations
 * and pushing info about them to user's devices.
 */
public class RecommendationServlet extends HttpServlet {

    /**
     * Recommendation expiration time.
     * Two minutes for recommendation expiration is reasonable for demo.
     * For production it will more likely be several hours.
     */
    private static final int RECOMMENDATION_EXPIRATION_IN_MINUTES = 2;

    /**
     * Delay to simulate the creation time of recommendations, only for demo.
     */
    private static final int SLEEP_TIME_IN_MILLISECONDS = 15 * 1000;

    /**
     * Log output.
     */
    private static final Logger LOG = Logger
            .getLogger(CheckInEndpoint.class.getName());

    /**
     * Random generator.
     */
    private static final Random RANDOM = new Random();


    @Override
    public final void doPost(final HttpServletRequest req,
            final HttpServletResponse resp)
            throws IOException {
        String placeId = req.getParameter("placeId");
        String userEmail = req.getParameter("userEmail");

        // Skip generating new recommendations if user checked into the same
        // place within the recommendation expiration time.
        // In other words, if the number of checkins in this time window is
        // larger than 1 (one checkin is the "current" one).
        Calendar validityTimeWindow = Calendar
                .getInstance(TimeZone.getTimeZone("UTC"));
        validityTimeWindow
                .add(Calendar.MINUTE, -RECOMMENDATION_EXPIRATION_IN_MINUTES);
        if (CheckInUtil.getCheckInsForUser(userEmail, placeId,
                validityTimeWindow.getTime()).size()
                > 1) {
            LOG.info("Skipping generating recommendations for user " + userEmail
                    + " checked into place "
                    + placeId);

            return;
        }

        LOG.info("Generating recommendations for user " + userEmail
                + " checked into place "
                + placeId);

        // This sample, instead of actually generating personalized
        // recommendations, only pretends to crunch data for some time (by
        // having a SLEEP_TIME_IN_MILLISECONDS milliseconds delay - long enough
        // for demo :-) ), and then it inserts two recommendations based on
        // a recommendation  template with a specific key. The recommendation
        // from the template is then customized using randomly generated
        // prices and one of a few available product recommendation images.
        try {
            Thread.sleep(SLEEP_TIME_IN_MILLISECONDS);
        } catch (InterruptedException e1) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        // Let Task Queue handle any exceptions through normal retry logic and
        // error logging, so the code only catches InvalidFormatException and
        // allows the other exception pass through.

        String[] recommendationData = null;

        try {
            // Retrieve the recommendation template.
            Recommendation recommendationTemplate = ofy().load()
                    .type(Recommendation.class).id("template1").now();

            if (recommendationTemplate == null) {
                LOG.warning(
                        "No recommendation template found. Skipping generating "
                                + "personalized recommendations");
                return;
            }

            recommendationData = recommendationTemplate.getTitle().split(";");
            if (recommendationData.length != 2) {
                LOG.warning("Invalid format of the recommendation template. "
                        + "The title property should have two parts separated "
                        + "by a semicolon. "
                        + "Skipping generating personalized recommendations.");

                return;
            }

            for (int i = 0; i < 2; i++) {
                Recommendation r = new Recommendation();
                r.generateId();
                r.setTitle(recommendationData[0]);
                r.setDescription(String.format(
                        recommendationTemplate.getDescription(),
                        110 + RANDOM.nextInt(90),
                        80 + RANDOM.nextInt(20)));
                r.setImageUrl(
                        String.format(recommendationTemplate.getImageUrl(),
                                3 + RANDOM.nextInt(6)));

                // set recommendation expiration
                Calendar expirationTime = Calendar
                        .getInstance(TimeZone.getTimeZone("UTC"));
                expirationTime.add(Calendar.MINUTE,
                        RECOMMENDATION_EXPIRATION_IN_MINUTES);
                r.setExpiration(expirationTime.getTime());

                ofy().save().entity(r).now();
            }
        } catch (IllegalFormatException e) {
            LOG.warning("IllegalFormatException caught. This indicates that "
                    + "the format of the recommendation template is invalid. "
                    + "Skipping generating personalized recommendations");
            return;
        }

        long numberOfItemsWithReducedPrices = 2;
        String firstItem = recommendationData[1];

        try {
            ImmutableMap<String, String> payload = ImmutableMap.<String,
                    String>builder()
                    .put("NotificationKind", "PriceCheckLowerPrices1")
                    .put("ProductCount",
                            Long.toString(numberOfItemsWithReducedPrices))
                    .put("ProductName", firstItem)
                    .build();

            MessagingEndpoint messagingEndpoint = new MessagingEndpoint();
            messagingEndpoint.sendMessage(payload);
            //Optional: change function to push to one specific device
        } catch (IOException e) {
            LOG.info("Exception when sending push notification for user "
                    + userEmail
                    + " checked into a place " + placeId);
        }
    }
}
