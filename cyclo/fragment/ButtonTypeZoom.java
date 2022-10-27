package com.dev.cyclo.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.dev.cyclo.IButton;
import com.dev.cyclo.R;
import org.jetbrains.annotations.NotNull;

/**
 * Button for the zoom type on the map
 */
public class ButtonTypeZoom extends Fragment implements View.OnClickListener{

    private IButton.OnButtonClickedListener mCallback;

    public ButtonTypeZoom() {
        // Required empty public constructor
    }



    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragment = inflater.inflate(R.layout.fragment_button_typezoom, container, false);
        LinearLayout changeTypeMap = fragment.findViewById(R.id.typezoom);//set bouton quit pour une future interaction
        changeTypeMap.setOnClickListener(this);
        return fragment;
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        this.createCallbackToParentActivity();
    }

    @Override
    public void onClick(@NonNull View v) {
        // 5 - Spread the click to the parent activity
        this.mCallback.onButtonZoomTypeClicked(v);
    }

    private void createCallbackToParentActivity(){
        try {
            //Parent activity will automatically subscribe to callback
            mCallback = (IButton.OnButtonClickedListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(e + " must implement OnButtonClickedListener");
        }
    }
}