package com.dev.cyclo.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.dev.cyclo.IButton;
import com.dev.cyclo.R;
import org.jetbrains.annotations.NotNull;

/**
 * Button for the type mode of the map
 */
public class ButtonTypeMap extends Fragment implements View.OnClickListener{

    private IButton.OnButtonClickedListener mCallback;

    public ButtonTypeMap() {
        // Required empty public constructor
    }



    @Override
    public void onCreate(@NonNull Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragment = inflater.inflate(R.layout.fragment_button_typemap, container, false);
        LinearLayout changeTypeMap = fragment.findViewById(R.id.typemap);//set bouton quit pour une future interaction
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
        this.mCallback.onButtonMapTypeClicked(v);
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