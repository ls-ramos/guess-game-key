package com.longynuss.guessgame;

import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Objects;

public class MenuScreen extends Fragment implements View.OnClickListener{

    private OnFragmentInteractionListener mListener;
    private View view;
    public MenuScreen() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getActivity() instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) getActivity();
        } else {
            throw new RuntimeException(Objects.requireNonNull(getActivity()).toString()
                    + " must implement OnMultiplayerScreenInteractionListener");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        if(view==null){
            view = inflater.inflate(R.layout.fragment_menu_screen, container, false);
        }

        view.findViewById(R.id.btn_signIn).setOnClickListener(this);
        view.findViewById(R.id.buttonSingle_Id).setOnClickListener(this);
        view.findViewById(R.id.buttonMulti_Id).setOnClickListener(this);
        view.findViewById(R.id.buttonHowPlay_Id).setOnClickListener(this);

        view.findViewById(R.id.quick_match_button).setOnClickListener(this);
        view.findViewById(R.id.invite_friends_button).setOnClickListener(this);
        view.findViewById(R.id.invitations_button).setOnClickListener(this);
        view.findViewById(R.id.backMenu_fromMultiplayer).setOnClickListener(this);

        view.findViewById(R.id.sing_out_button).setOnClickListener(this);
        view.findViewById(R.id.buttonRecord_Id).setOnClickListener(this);
        view.findViewById(R.id.leaderboard_Button_Id).setOnClickListener(this);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mListener.onMenuScreenCreated();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mListener = null;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_signIn) {
            mListener.onSignInClicked();
        }else if (view.getId() == R.id.buttonSingle_Id){
            mListener.onSinglePlayerClicked();
        }else if (view.getId() == R.id.buttonHowPlay_Id) {
            mListener.onHowToPlayClicked();
        }else if (view.getId() == R.id.buttonMulti_Id) {
            mListener.onMultiPlayerClicked();
        }else if (view.getId() == R.id.quick_match_button) {
            mListener.onQuickMatchClicked();
        }else if (view.getId() == R.id.invite_friends_button) {
            mListener.onInviteFriendsClicked();
        }else if (view.getId() == R.id.invitations_button) {
            mListener.onInvitationsClicked();
        }else if (view.getId() == R.id.backMenu_fromMultiplayer) {
            mListener.onBackToMenuClicked();
        }else if (view.getId() == R.id.sing_out_button) {
            mListener.onSignOutClicked();
        }else if (view.getId() == R.id.buttonRecord_Id) {
            mListener.onAchievementScreenRequested();
        }else if (view.getId() == R.id.leaderboard_Button_Id) {
            mListener.onLeaderboardScreenRequested();
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onSignInClicked();
        void onSinglePlayerClicked();
        void onMultiPlayerClicked();
        void onHowToPlayClicked();
        void onQuickMatchClicked();
        void onInviteFriendsClicked();
        void onInvitationsClicked();
        void onBackToMenuClicked();
        void onSignOutClicked();
        void onMenuScreenCreated();
        void onAchievementScreenRequested();
        void onLeaderboardScreenRequested();
    }
}
