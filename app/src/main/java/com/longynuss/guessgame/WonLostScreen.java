package com.longynuss.guessgame;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class WonLostScreen extends Fragment implements View.OnClickListener{

    private onWonLostInteractionListener mListener;

    private View view;
    public WonLostScreen() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getActivity() instanceof onWonLostInteractionListener) {
            mListener = (onWonLostInteractionListener) getActivity();
        } else {
            if(getActivity()!=null){
                throw new RuntimeException(getActivity().toString()
                        + " must implement OnMultiplayerScreenInteractionListener");
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(view==null){
            view = inflater.inflate(R.layout.fragment_won_lost_screen, container, false);
        }

        //declaring variable
        String resultOfMatch = null;
        int nAttemptsFinal = 0;
        String typeGame = null;
        String opponentNum = null;
        String myPassword = null;

        if(getArguments()!=null){
            resultOfMatch = getArguments().getString("won/lost");
            nAttemptsFinal = getArguments().getInt("attempts");
            typeGame = getArguments().getString("typeGame");
            opponentNum = getArguments().getString("opponentNum");
            myPassword = getArguments().getString("myNum");
        }


        TextView resultMessage = view.findViewById(R.id.won_lost_message);
        TextView resultNumbers = view.findViewById(R.id.numbersResult);

        if (resultOfMatch != null && getActivity()!=null) {
            resultMessage.setText((resultOfMatch.equals("true")) ?
                    getActivity().getString(R.string.wonMessage_text) :
                    getActivity().getString(R.string.lostMessage_text));

            resultNumbers.setText(getActivity().getString(R.string.opponentNumberText) + opponentNum + "\n"+
                    getActivity().getString(R.string.myNumberText) + myPassword);
        }




        if (resultOfMatch!=null && resultOfMatch.equals("true")) {
            mListener.checkAchievements(nAttemptsFinal,typeGame);

            TextView nAttemptsMessage = view.findViewById(R.id.number_of_attempts);
            nAttemptsMessage.setText(Integer.toString(nAttemptsFinal)+" "+
                    getResources().getString(R.string.attemptsText));
        }

        view.findViewById(R.id.endGame).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.endGame){
            mListener.onBackToMenuRequested();
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
    public interface onWonLostInteractionListener {
        void onBackToMenuRequested();
        void checkAchievements(int n_attempts, String typeGame);
    }
}
