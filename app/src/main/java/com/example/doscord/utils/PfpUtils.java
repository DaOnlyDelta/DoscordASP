package com.example.doscord.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.doscord.R;

public class PfpUtils {

    private static final String BASE_IMAGE_URL = "https://doscord.top/api/images/";

    /**
     * Loads a profile picture into an ImageView, handling local mapping for default avatars
     * and caching for remote ones.
     */
    public static void loadPfp(Context context, String path, ImageView imageView) {
        if (path == null || path.isEmpty() || path.equals("defaults/defaults0.png")) {
            // Default to app icon or a specific placeholder if defaults0 is not a real file
            Glide.with(context)
                    .load(R.drawable.icon)
                    .circleCrop()
                    .into(imageView);
            return;
        }

        // Check if it's a local default avatar
        int localResId = getLocalDefaultResId(path);
        if (localResId != -1) {
            Glide.with(context)
                    .load(localResId)
                    .circleCrop()
                    .into(imageView);
        } else {
            // Load from server with caching
            String fullPath = BASE_IMAGE_URL + path;
            Glide.with(context)
                    .load(fullPath)
                    .placeholder(R.drawable.pfp_placeholder)
                    .error(R.drawable.pfp_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Cache both original and resized
                    .circleCrop()
                    .into(imageView);
        }
    }

    /**
     * Special version for the current user that respects RegDataHolder for immediate updates.
     */
    public static void loadMyPfp(Context context, String path, ImageView... imageViews) {
        // Try to load from RegDataHolder first to avoid API wait after registration/update
        if (RegDataHolder.selectedImageUri != null) {
            for (ImageView iv : imageViews) {
                Glide.with(context)
                        .load(RegDataHolder.selectedImageUri)
                        .centerCrop()
                        .circleCrop()
                        .into(iv);
            }
        } else if (RegDataHolder.defaultPfpDrawable != -1) {
            for (ImageView iv : imageViews) {
                Glide.with(context)
                        .load(RegDataHolder.defaultPfpDrawable)
                        .centerCrop()
                        .circleCrop()
                        .into(iv);
            }
        } else {
            for (ImageView iv : imageViews) {
                loadPfp(context, path, iv);
            }
        }
    }

    private static int getLocalDefaultResId(String path) {
        if (path.startsWith("defaults/defaults")) {
            try {
                // Expecting "defaults/defaultsX.png"
                String numPart = path.substring("defaults/defaults".length(), path.lastIndexOf("."));
                int index = Integer.parseInt(numPart);
                
                switch (index) {
                    case 1: return R.drawable.defaults1;
                    case 2: return R.drawable.defaults2;
                    case 3: return R.drawable.defaults3;
                    case 4: return R.drawable.defaults4;
                    case 5: return R.drawable.defaults5;
                    case 6: return R.drawable.defaults6;
                    case 7: return R.drawable.defaults7;
                    case 8: return R.drawable.defaults8;
                    default: return -1;
                }
            } catch (Exception ignored) {}
        }
        return -1;
    }
}
