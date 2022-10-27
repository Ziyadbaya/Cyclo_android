package com.dev.cyclo;

import android.view.View;


/**
 * Interface to manage button interactions
 */
public interface IButton extends View.OnClickListener {

    /**
     * Interface for buttons to make the link between the parent activity and the button when clicked
     */
    interface OnButtonClickedListener {
        void onButtonClicked(View view);
        void onButtonMapTypeClicked(View v);

        void onButtonZoomTypeClicked(View v);
    }
}
