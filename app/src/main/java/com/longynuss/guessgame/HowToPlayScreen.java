package com.longynuss.guessgame;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.Objects;

public class HowToPlayScreen extends Fragment implements View.OnClickListener {

    private OnFragmentInteractionListener mListener;
    private ConstraintLayout layout1,layout2,layout3,layout4,layout5,layout6;
    private Button btnNext;

    private boolean changeButton;
    private int controlScreens;


    private View view;
    public HowToPlayScreen() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) getActivity();
        } else {
            throw new RuntimeException(Objects.requireNonNull(getActivity()).toString()
                    + " must implement OnFragmentInteractionListener");
        }

        controlScreens=0;
        changeButton=false;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(view==null){
            view = inflater.inflate(R.layout.fragment_how_to_play_screen, container, false);
        }

        btnNext =  view.findViewById(R.id.btn_nextTeaching);

        view.findViewById(R.id.btn_nextTeaching).setOnClickListener(this);
        view.findViewById(R.id.btn_backTeaching).setOnClickListener(this);
        view.findViewById(R.id.btn_backMenuTeaching).setOnClickListener(this);
        view.findViewById(R.id.btn_privacyPolicy).setOnClickListener(this);

        layout1 = view.findViewById(R.id.layout1);
        layout2 = view.findViewById(R.id.layout2);
        layout3 = view.findViewById(R.id.layout3);
        layout4 = view.findViewById(R.id.layout4);
        layout5 = view.findViewById(R.id.layout5);
        layout6 = view.findViewById(R.id.layout6);

        btnNext.setText(R.string.btnNext_NextText);

        layout1.setVisibility(View.VISIBLE);
        layout2.setVisibility(View.GONE);
        layout3.setVisibility(View.GONE);
        layout4.setVisibility(View.GONE);
        layout5.setVisibility(View.GONE);
        layout6.setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mListener = null;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_privacyPolicy){
            mListener.onprivacyPolicyResquested();
        }else if (view.getId() == R.id.btn_nextTeaching ) {
            if(controlScreens<5){
                controlScreens++;
            }else{
                mListener.endHowToPlay();
            }
        }else if(view.getId() == R.id.btn_backTeaching && controlScreens>0){
            controlScreens--;
        }else if(view.getId() ==R.id.btn_backMenuTeaching){
            mListener.endHowToPlay();
        }

        //change screens
        layout1.setVisibility(View.GONE);
        layout1.setVisibility(View.GONE);
        layout2.setVisibility(View.GONE);
        layout3.setVisibility(View.GONE);
        layout4.setVisibility(View.GONE);
        layout5.setVisibility(View.GONE);
        layout6.setVisibility(View.GONE);

        if(controlScreens==0){
            layout1.setVisibility(View.VISIBLE);
        }else if(controlScreens==1){
            layout2.setVisibility(View.VISIBLE);
        }else if(controlScreens==2){
            layout3.setVisibility(View.VISIBLE);
        }else if(controlScreens==3){
            layout4.setVisibility(View.VISIBLE);
        }else if(controlScreens==4){
            layout5.setVisibility(View.VISIBLE);
            if(changeButton){
                btnNext.setText(Objects.requireNonNull(getActivity()).getString(R.string.btnNext_NextText));

                changeButton=false;
            }
        }else if(controlScreens==5){
            layout6.setVisibility(View.VISIBLE);

            btnNext.setText(Objects.requireNonNull(getActivity()).getString(R.string.btnNext_MenuText));
            changeButton=true;
        }
    }

    public interface OnFragmentInteractionListener {
        void endHowToPlay();
        void onprivacyPolicyResquested();
    }
}
