package com.example.doscord.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.doscord.R;

public class PfpUtils {

    private static final String BASE_IMAGE_URL = "https://doscord.top/api/uploads/";

    /**
     * Loads a profile picture into an ImageView, handling local mapping for default avatars
     * and caching for remote ones.
     */
    public static void loadPfp(Context context, String path, ImageView imageView) {
        if (path == null) path = "";
        
        // Prevent redundant loads and flickering if the path is the same
        if (path.equals(imageView.getTag())) {
            return;
        }
        imageView.setTag(path);

        if (path.isEmpty() || path.equals("defaults/defaults0.png")) {
            Glide.with(context)
                    .load(R.drawable.icon)
                    .centerCrop()
                    .circleCrop()
                    .into(imageView);
            return;
        }

        int localResId = getLocalDefaultResId(path);
        if (localResId != -1) {
            Glide.with(context)
                    .load(localResId)
                    .centerCrop()
                    .circleCrop()
                    .into(imageView);
        } else {
            String fullPath = BASE_IMAGE_URL + path;
            Glide.with(context)
                    .load(fullPath)
                    .placeholder(R.drawable.icon)
                    .error(R.drawable.icon)
                    .centerCrop()
                    .circleCrop()
                    .into(imageView);
        }
    }

    /**
     * Loads a group server profile picture. Falls back to a group placeholder drawable
     * if no custom banner or icon path is provided by the server.
     */
    public static void loadGroupPfp(Context context, String path, ImageView imageView) {
        if (path == null) path = "";

        // Use a prefix to distinguish from user PFPs if using the same tag
        String tag = "group_" + path;
        if (tag.equals(imageView.getTag())) {
            return;
        }
        imageView.setTag(tag);

        if (path.isEmpty()) {
            Glide.with(context)
                    .load(R.drawable.pfp_group_placeholder)
                    .centerCrop()
                    .circleCrop()
                    .into(imageView);
            return;
        }

        String fullPath = BASE_IMAGE_URL + path;
        Glide.with(context)
                .load(fullPath)
                .placeholder(R.drawable.pfp_group_placeholder)
                .error(R.drawable.pfp_group_placeholder)
                .centerCrop()
                .circleCrop()
                .into(imageView);
    }

    /**
     * Special version for the current user that respects RegDataHolder for immediate updates.
     */
    public static void loadMyPfp(Context context, String path, ImageView... imageViews) {
        if (RegDataHolder.selectedImageUri != null) {
            String uriTag = RegDataHolder.selectedImageUri.toString();
            for (ImageView iv : imageViews) {
                if (uriTag.equals(iv.getTag())) continue;
                iv.setTag(uriTag);
                Glide.with(context)
                        .load(RegDataHolder.selectedImageUri)
                        .centerCrop()
                        .circleCrop()
                        .into(iv);
            }
        } else if (RegDataHolder.defaultPfpDrawable != -1) {
            String resTag = "res_" + RegDataHolder.defaultPfpDrawable;
            for (ImageView iv : imageViews) {
                if (resTag.equals(iv.getTag())) continue;
                iv.setTag(resTag);
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