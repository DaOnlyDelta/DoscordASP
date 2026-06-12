package com.example.doscord.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.doscord.R;
import com.example.doscord.adapters.MessagesAdapter;
import com.example.doscord.models.Message;

import java.util.List;

public class MessageSwipeController extends ItemTouchHelper.Callback {

    public interface OnSwipeListener {
        void onSwipeToEdit(int position);
        void onSwipeToDelete(int position);
    }

    private final Context context;
    private final OnSwipeListener listener;
    private final MessagesAdapter adapter;
    private final Drawable editIcon;
    private final Drawable deleteIcon;
    private final int intrinsicWidth;
    private final int intrinsicHeight;
    private final Paint circlePaint;
    private final Paint deleteCirclePaint;

    // Configuration variables for swiping and scaling
    private float maxSlideDp = 40f;        // Max distance the message moves off the wall
    private float scaleDenominator = 120f; // Higher = slower scaling
    private float circleRadiusDp = 18f;    // Final radius of the blue circle
    private float iconSizeDp = 16f;        // Final size of the pencil icon
    private float iconCenterOffsetDp = 40f; // Position from the right wall
    private float slideResistance = 0.2f;   // 1.0 = normal, < 1.0 = more resistance (slower)
    private long animationDuration = 250L; // Duration of snap-back/swipe animation in ms

    public MessageSwipeController(Context context, MessagesAdapter adapter, OnSwipeListener listener) {
        this.context = context;
        this.adapter = adapter;
        this.listener = listener;
        this.editIcon = ContextCompat.getDrawable(context, R.drawable.pencil);
        this.deleteIcon = ContextCompat.getDrawable(context, R.drawable.trash);
        this.intrinsicWidth = editIcon.getIntrinsicWidth();
        this.intrinsicHeight = editIcon.getIntrinsicHeight();
        this.circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.circlePaint.setColor(context.getColor(R.color.blue));
        this.deleteCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.deleteCirclePaint.setColor(context.getColor(R.color.red));
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int position = viewHolder.getBindingAdapterPosition();
        if (position == RecyclerView.NO_POSITION) return makeMovementFlags(0, 0);

        List<Object> displayList = adapter.getDisplayList();
        if (position >= displayList.size()) return makeMovementFlags(0, 0);

        Object item = displayList.get(position);
        if (item instanceof Message) {
            Message msg = (Message) item;
            Integer activeUserId = GlobalData.getActiveUserId();
            
            // System messages should never be swipeable
            if ("system".equalsIgnoreCase(msg.getType())) {
                return makeMovementFlags(0, 0);
            }

            // Ownership check: You can only swipe your own messages
            if (activeUserId != null && activeUserId.equals(msg.getSenderId())) {
                int swipeFlags = ItemTouchHelper.RIGHT; // Delete allowed for all our messages
                
                // Edit only allowed for normal text messages (not voice)
                if ("normal".equalsIgnoreCase(msg.getType())) {
                    swipeFlags |= ItemTouchHelper.LEFT;
                }
                
                return makeMovementFlags(0, swipeFlags);
            }
        }
        
        return makeMovementFlags(0, 0);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getBindingAdapterPosition();
        if (position == RecyclerView.NO_POSITION) return;

        if (direction == ItemTouchHelper.LEFT) {
            listener.onSwipeToEdit(position);
        } else if (direction == ItemTouchHelper.RIGHT) {
            listener.onSwipeToDelete(position);
        }
    }

    @Override
    public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
        return 0.3f;
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        View itemView = viewHolder.itemView;
        int itemHeight = itemView.getBottom() - itemView.getTop();
        float density = context.getResources().getDisplayMetrics().density;
        
        // Apply resistance to dX
        float actualDX = dX * slideResistance;
        
        // Limit translation
        float maxTranslationX = maxSlideDp * density;
        float translationX;
        if (actualDX > 0) {
            translationX = Math.min(maxTranslationX, actualDX);
        } else {
            translationX = Math.max(-maxTranslationX, actualDX);
        }

        float swipeProgress = Math.min(1f, Math.abs(actualDX) / (maxSlideDp * density));
        
        if (Math.abs(actualDX) > 10) {
            int iconCenterX;
            int iconCenterY = itemView.getTop() + (itemHeight / 2);
            Paint activeCirclePaint;
            Drawable activeIcon;

            if (actualDX > 0) {
                // Swipe Right -> Delete (Red Trash) on the LEFT
                iconCenterX = itemView.getLeft() + (int) (iconCenterOffsetDp * density);
                activeCirclePaint = deleteCirclePaint;
                activeIcon = deleteIcon;
            } else {
                // Swipe Left -> Edit (Blue Pencil) on the RIGHT
                iconCenterX = itemView.getRight() - (int) (iconCenterOffsetDp * density);
                activeCirclePaint = circlePaint;
                activeIcon = editIcon;
            }
            
            activeCirclePaint.setAlpha((int) (swipeProgress * 255));
            float currentRadius = (circleRadiusDp * density) * swipeProgress;
            c.drawCircle(iconCenterX, iconCenterY, currentRadius, activeCirclePaint);

            int iconSize = (int) (iconSizeDp * density * swipeProgress);
            activeIcon.setBounds(iconCenterX - iconSize / 2, iconCenterY - iconSize / 2, iconCenterX + iconSize / 2, iconCenterY + iconSize / 2);
            activeIcon.setAlpha((int) (swipeProgress * 255));
            activeIcon.setTint(Color.WHITE);
            activeIcon.draw(c);
        }

        super.onChildDraw(c, recyclerView, viewHolder, translationX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public long getAnimationDuration(@NonNull RecyclerView recyclerView, int animationType, float animateDx, float animateDy) {
        return animationDuration;
    }
}
