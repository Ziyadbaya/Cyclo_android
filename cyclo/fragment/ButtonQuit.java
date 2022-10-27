package com.dev.cyclo.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.dev.cyclo.IButton;
import com.dev.cyclo.R;
import org.jetbrains.annotations.NotNull;

/**
 * Fragment that manages when you quit the Ride Activity during a course
 * Come back to the menu
 */
public class ButtonQuit extends Fragment implements View.OnClickListener{

    /**
     * @see IButton
     */
    private IButton.OnButtonClickedListener mCallback;

    /**
     * Every fragment must have an empty public constructor
     */
    public ButtonQuit() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /**
     * Inflate the layout named fragment_button_quit for this fragment and suscribe the fragment to the listener
     * @param inflater inflate the layout
     * @param container container of the
     * @param savedInstanceState bundle saved
     * @return the view of the fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragment = inflater.inflate(R.layout.fragment_button_quit, container, false);
        LinearLayout quit = fragment.findViewById(R.id.quit);
        quit.setOnClickListener(this);
        return fragment;
    }

    /**
     * Call the method that creating callback after being attached to parent activity
     * @param context context of the activity that includes the fragment
     */
    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        this.createCallbackToParentActivity();
    }

    /**
     * Spread the click to the parent activity
     * @param v view
     */
    @Override
    public void onClick(View v) {
        mCallback.onButtonClicked(v);
    }


    /**
     * Create callback to the parent activity
     * Parent activity will automatically subscribe to callback
     */
    private void createCallbackToParentActivity(){
        try {
            mCallback = (IButton.OnButtonClickedListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(e + " must implement OnButtonClickedListener");
        }
    }
}