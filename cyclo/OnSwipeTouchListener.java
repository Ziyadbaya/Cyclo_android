package com.dev.cyclo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;

/**
 * Detects left and right swipes across a view.
 */
public class OnSwipeTouchListener implements View.OnTouchListener {

    private final GestureDetector gestureDetector;

    /**
     * Constructor of the OnSwipeTouchListener with a gestureDetector
     * @param context Context
     */
    public OnSwipeTouchListener(Context context) {
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    /**
     * To override in the class where the SwipeListener is used
     * Behavior when swipe left is made
     */
    public void onSwipeLeft() {
        Log.d("swipe", "left");
    }

    /**
     * To override in the class where the SwipeListener is used
     * Behavior when swipe right is made
     */
    public void onSwipeRight() {
        Log.d("swipe", "right");
    }

    /**
     * To override in the class where the SwipeListener is used
     * Behavior when swipe up is made
     */
    public void onSwipeUp() {
        Log.d("swipe", "up");
    }

    /**
     * To override in the class where the SwipeListener is used
     * Behavior when swipe down is made
     */
    public void onSwipeDown() {
        Log.d("swipe", "down");
    }

    /**
     * Detects if there is a gesture done
     * @param v View
     * @param event MotionEvent
     * @return boolean
     */
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouch(View v, MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    /**
     * Private inner class GestureListener
     */
    private final class GestureListener extends SimpleOnGestureListener {

        /**
         * Criteria for the movement (threshold of distance and velocity of the gesture)
         */
        private static final int SWIPE_DISTANCE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        /**
         * Detects if there is a gesture corresponding at a wipe and what type of swipe (up, down, right, left)
         * @param e1 MotionEvent (point at the start of the gesture)
         * @param e2 MotionEvent (point at the end of the gesture)
         * @param velocityX float (speed on the horizontal axis)
         * @param velocityY float (velocity on the vertical axis)
         * @return boolean (true if a gesture corresponding at a swipe is detected)
         */
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float distanceX = e2.getX() - e1.getX();
            float distanceY = e2.getY() - e1.getY();
            if (Math.abs(distanceX) > Math.abs(distanceY) && Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (distanceX > 0)
                    onSwipeRight();
                else
                    onSwipeLeft();
                return true;
            }
            if (Math.abs(distanceY) > Math.abs(distanceX) && Math.abs(distanceY) > SWIPE_DISTANCE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                if (distanceY > 0)
                    onSwipeDown();
                else
                    onSwipeUp();
                return true;
            }
            return false;
        }
    }
}