package com.oopsappgroup.praktika.Fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.oopsappgroup.praktika.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class WelcomeFragment extends Fragment {

    ImageView johny;

    public WelcomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup rootView = (ViewGroup)inflater.inflate(
                R.layout.fragment_welcome,container,
                false);

        johny = (ImageView)rootView.findViewById(R.id.ivJohnyCatsville);

        Glide.with(getActivity()).load(R.drawable.johny_catsville).into(johny);

        return rootView;
    }

}
