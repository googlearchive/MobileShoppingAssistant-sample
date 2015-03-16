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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Helper class for displaying images retrieved asynchronously from Internet
 * locations.
 */
class ImageUrlViewBinder implements SimpleAdapter.ViewBinder {

    /**
     * The image view ids.
     */
    private Set<Integer> imageViewIds;

    /**
     * @param pImageViewIds set of resource ids of ImageViews that will have
     *                     images downloaded from Internet
     */
    ImageUrlViewBinder(final Set<Integer> pImageViewIds) {
        this.imageViewIds = pImageViewIds;
    }

    /**
     * @param imageViewId the resource id of ImageView that will have images
     *                    downloaded from
     *                    Internet
     */
    ImageUrlViewBinder(final int imageViewId) {
        imageViewIds = new HashSet<>();
        imageViewIds.add(imageViewId);
    }


    /**
     * If the view has been configured to display images downloaded from
     * Internet, the method
     * interprets the data argument as an Url, downloads the image from that Url
     * asynchronously and
     * binds it to the specified view.
     */
    @Override
    public boolean setViewValue(final View view, final Object data,
            final String textRepresentation) {
        if (!imageViewIds.contains(view.getId())) {
            return false;
        }

        // Optional: Implement local caching on the device.
        String uri = (String) data;
        ImageView imageView = (ImageView) view;
        new DownloadImageAsyncTask(imageView, R.drawable.ic_launcher)
                .execute(uri);
        return true;
    }
}


/**
 * AsyncTask that asynchronously downloads an image from an url and sets the
 * image as a bitmap for a specified ImageView.
 */
class DownloadImageAsyncTask extends AsyncTask<String, Void, Bitmap> {

    /**
     * * Log output.
     */
    private static final Logger LOG = Logger
            .getLogger(ImageUrlViewBinder.class.getName());

    /**
     * The image view to put the image in.
     */
    private ImageView imageView;

    /**
     * The fallback image resource id, if the download fails for any reason.
     */
    private int fallbackResId;


    /**
     * @param pImageView     imageView that will have the downloaded image set
     *                      to
     * @param pFallbackResId a bitmap resource id to be used when downloading
     *                       *                       the
     *                      image from the url
     *                      fails
     */
    public DownloadImageAsyncTask(final ImageView pImageView,
            final int pFallbackResId) {
        this.imageView = pImageView;
        this.fallbackResId = pFallbackResId;
    }

    /**
     * Downloads the image from Url and creates a bitmap out of it.
     */
    @Override
    protected Bitmap doInBackground(final String... urls) {
        String url = urls[0];
        Bitmap bitmap = null;
        try {
            InputStream in = new java.net.URL(url).openStream();
            bitmap = BitmapFactory.decodeStream(in);
        } catch (IOException e) {
            LOG.warning("Downloading image failed: " + e.getMessage());
        }
        return bitmap;
    }

    /**
     * If the download succeeded, it binds the downloaded bitmap to the view.
     * Otherwise it binds a
     * static bitmap passed as fallbackResId to the constructor
     */
    @Override
    protected void onPostExecute(final Bitmap result) {
        if (result == null) {
            imageView.setImageResource(fallbackResId);
        } else {
            imageView.setImageBitmap(result);
        }
    }
}
