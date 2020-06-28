package com.longynuss.guessgame;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Objects;

public class MultiplayerGame extends Fragment implements View.OnClickListener {

    private OnMultiplayerScreenInteractionListener mListener;

    private Activity activity;

    private EditText dataToSend;
    private TextView messageToPlayer;
    private boolean sentNumberAlready, dataReceivedAlready;

    private Player me;

    private View viewGlobal;

    public String wonMessage;

    private ArrayList<userGuess> guesses;
    private ArrayAdapter adapter;

    //buttons of the help bar, THE POSITION OF THE BUTTON CORRESPOND TO THE NUMBER THAT THE BUTTON CARRY
    //ex: btn_HelpBar[0] is the button that carry the number 0 on it and so on...
    private Button[] btn_HelpBar = new Button[10];

    //count the time that a button of the help bar was touched
    //(ex: btnnTouches[0]=2, means the button 0 of the help bar was touches 2 times)
    //MAX NUMBER OF TOUCHES IS 2, if any position get greater than 2 will be set back to 0 immediately
    private int[] btn_nTouches = new int[10];

    private View view;

    public MultiplayerGame() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getActivity() instanceof OnMultiplayerScreenInteractionListener) {
            mListener = (OnMultiplayerScreenInteractionListener) getActivity();
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnMultiplayerScreenInteractionListener");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        activity = Objects.requireNonNull(getActivity());

        // Inflate the layout for this fragment
        if(view==null){
            view =inflater.inflate(R.layout.fragment_multiplayer_game, container, false);
        }

        view.findViewById(R.id.buttonGuess_Id).setVisibility(View.GONE);
        view.findViewById(R.id.buttonSendNumber).setVisibility(View.VISIBLE);


        view.findViewById(R.id.buttonGuess_Id).setOnClickListener(this);
        view.findViewById(R.id.buttonSendNumber).setOnClickListener(this);
        view.findViewById(R.id.backToMenuFromMulti).setOnClickListener(this);

        dataToSend =  view.findViewById(R.id.inputNum_Id);
        dataToSend.setHint(R.string.multi_PasswordHint);
        messageToPlayer = view.findViewById(R.id.messageToPlayer_Id);
        messageToPlayer.setText(view.getResources().getString(R.string.text_typeYourNumber));

        viewGlobal = view;

        sentNumberAlready=false;
        dataReceivedAlready =false;

        me = new Player();

        wonMessage = "won";

        guesses = new ArrayList<>();
        ListView list = view.findViewById(R.id.ListOfGuesses_Multi);

        adapter = new ListAdapter(getActivity(),guesses);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                userGuess title = (userGuess)parent.getItemAtPosition(position);

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getResources().getString(R.string.Oexplaining) +"\n"+
                        getResources().getString(R.string.Xexplaining));
                builder.setMessage(getResources().getString(R.string.helpGuesses1) +" "+
                        me.findO(title.getUserGuess())+" "+
                        getResources().getString(R.string.helpGuesses2) +" "+
                        me.findX(title.getUserGuess()) +" "+
                        getResources().getString(R.string.helpGuesses3));


                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // You don't have to do anything here if you just want it dismissed when clicked
                    }
                }).create().show();

            }
        });
        //Find the helpBar buttons by id and set to the respective position of the btn_HelpBar
        btn_HelpBar[0]= view.findViewById(R.id.buttonHelp0_Id);
        btn_HelpBar[1]= view.findViewById(R.id.buttonHelp1_Id);
        btn_HelpBar[2]= view.findViewById(R.id.buttonHelp2_Id);
        btn_HelpBar[3]= view.findViewById(R.id.buttonHelp3_Id);
        btn_HelpBar[4]= view.findViewById(R.id.buttonHelp4_Id);
        btn_HelpBar[5]= view.findViewById(R.id.buttonHelp5_Id);
        btn_HelpBar[6]= view.findViewById(R.id.buttonHelp6_Id);
        btn_HelpBar[7]= view.findViewById(R.id.buttonHelp7_Id);
        btn_HelpBar[8]= view.findViewById(R.id.buttonHelp8_Id);
        btn_HelpBar[9]= view.findViewById(R.id.buttonHelp9_Id);

        for(int i=0;i<=9;i++){
            btn_HelpBar[i].setOnClickListener(this);

            btn_HelpBar[i].setBackgroundColor(ContextCompat.getColor(activity, R.color.buttonHelpBar_Black));
            btn_HelpBar[i].setTextColor(ContextCompat.getColor(activity, R.color.text_colorBlack));
        }

        view.findViewById(R.id.keyboard_button0Multi).setOnClickListener(this);
        view.findViewById(R.id.keyboard_button1Multi).setOnClickListener(this);
        view.findViewById(R.id.keyboard_button2Multi).setOnClickListener(this);
        view.findViewById(R.id.keyboard_button3Multi).setOnClickListener(this);
        view.findViewById(R.id.keyboard_button4Multi).setOnClickListener(this);
        view.findViewById(R.id.keyboard_button5Multi).setOnClickListener(this);
        view.findViewById(R.id.keyboard_button6Multi).setOnClickListener(this);
        view.findViewById(R.id.keyboard_button7Multi).setOnClickListener(this);
        view.findViewById(R.id.keyboard_button8Multi).setOnClickListener(this);
        view.findViewById(R.id.keyboard_button9Multi).setOnClickListener(this);
        view.findViewById(R.id.keyboard_button_backSpaceMulti).setOnClickListener(this);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mListener = null;
        me = null;
        sentNumberAlready=false;
        dataReceivedAlready =false;
    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.buttonSendNumber){
            String myNumber = dataToSend.getText().toString();
            if (authenticateNumber(myNumber)) {
                me.setMyNum(myNumber);
                mListener.onSendNumber(myNumber);
                dataToSend.setText("");
            }else{
                //reset input
                dataToSend.setText("");

                Toast toast = Toast.makeText(getActivity(),R.string.invalidInput, Toast.LENGTH_SHORT);
                toast.show();
            }

        }else if(v.getId()==R.id.buttonGuess_Id){
            if (dataToSend.length()>0) {
                //take input
                guessClicked(dataToSend.getText().toString());
            }else{
                Toast toast = Toast.makeText(getActivity(),R.string.invalidInput, Toast.LENGTH_SHORT);
                toast.show();
            }

            //reset input
            dataToSend.setText("");
        }else if (v.getId() == R.id.backToMenuFromMulti) {
            mListener.onBackToMenuRequestedDuringGameplay();
        }

        //region Keyboard
        if(v.getId() == R.id.keyboard_button0Multi) {
            dataToSend.setText(dataToSend.getText().toString() + 0);
        }
        if(v.getId() == R.id.keyboard_button1Multi) {
            dataToSend.setText(dataToSend.getText().toString() + 1);
        }
        if(v.getId() == R.id.keyboard_button2Multi) {
            dataToSend.setText(dataToSend.getText().toString() + 2);
        }
        if(v.getId() == R.id.keyboard_button3Multi) {
            dataToSend.setText(dataToSend.getText().toString() + 3);
        }
        if(v.getId() == R.id.keyboard_button4Multi) {
            dataToSend.setText(dataToSend.getText().toString() + 4);
        }
        if(v.getId() == R.id.keyboard_button5Multi) {
            dataToSend.setText(dataToSend.getText().toString() + 5);
        }
        if(v.getId() == R.id.keyboard_button6Multi) {
            dataToSend.setText(dataToSend.getText().toString() + 6);
        }
        if(v.getId() == R.id.keyboard_button7Multi) {
            dataToSend.setText(dataToSend.getText().toString() + 7);
        }
        if(v.getId() == R.id.keyboard_button8Multi) {
            dataToSend.setText(dataToSend.getText().toString() + 8);
        }
        if(v.getId() == R.id.keyboard_button9Multi) {
            dataToSend.setText(dataToSend.getText().toString() + 9);
        }
        if(v.getId() == R.id.keyboard_button_backSpaceMulti) {
            if (dataToSend.length()>0) {
                String text = dataToSend.getText().toString();
                text = text.substring(0,text.length()-1);
                dataToSend.setText(text);
            }
        }
        //endregion

        //region HelpBar
        else if(v.getId() == R.id.buttonHelp0_Id ||
                v.getId() == R.id.buttonHelp1_Id||
                v.getId() == R.id.buttonHelp2_Id||
                v.getId() == R.id.buttonHelp3_Id||
                v.getId() == R.id.buttonHelp4_Id||
                v.getId() == R.id.buttonHelp5_Id||
                v.getId() == R.id.buttonHelp6_Id||
                v.getId() == R.id.buttonHelp7_Id||
                v.getId() == R.id.buttonHelp8_Id||
                v.getId() == R.id.buttonHelp9_Id){
            //Variable that tells which button of the help bar was pressed (0,1,2,...,9)
            int nButton=0;

            //Conditions that fill nButton with the number that the pressed button carry (the position of the button in btn_HelpBar array)
            if(v.getId() == R.id.buttonHelp1_Id) {
                nButton=1;
            }
            else if(v.getId() == R.id.buttonHelp2_Id) {
                nButton=2;
            }
            else if(v.getId() == R.id.buttonHelp3_Id) {
                nButton=3;
            }
            else if(v.getId() == R.id.buttonHelp4_Id) {
                nButton=4;
            }
            else if(v.getId() == R.id.buttonHelp5_Id) {
                nButton=5;
            }
            else if(v.getId() == R.id.buttonHelp6_Id) {
                nButton=6;
            }
            else if(v.getId() == R.id.buttonHelp7_Id) {
                nButton=7;
            }
            else if(v.getId() == R.id.buttonHelp8_Id) {
                nButton=8;
            }
            else if(v.getId() == R.id.buttonHelp9_Id) {
                nButton=9;
            }

            btn_nTouches[nButton]++;

            if(btn_nTouches[nButton]>2) {
                btn_nTouches[nButton]=0;
            }

            if(btn_nTouches[nButton]==0) {
                btn_HelpBar[nButton].setBackgroundColor(ContextCompat.getColor(activity, R.color.buttonHelpBar_Black));
                btn_HelpBar[nButton].setTextColor(ContextCompat.getColor(activity, R.color.text_colorBlack));
            }
            else if(btn_nTouches[nButton]==1) {
                btn_HelpBar[nButton].setBackgroundColor(ContextCompat.getColor(activity, R.color.buttonHelpBar_Green));
                btn_HelpBar[nButton].setTextColor(ContextCompat.getColor(activity, R.color.text_colorGreen));

            }
            else if(btn_nTouches[nButton]==2) {
                btn_HelpBar[nButton].setBackgroundColor(ContextCompat.getColor(activity, R.color.buttonHelpBar_Red));
                btn_HelpBar[nButton].setTextColor(ContextCompat.getColor(activity, R.color.text_colorRed));
            }
        }
        //endregion
    }

    public void DataReceived(String message){
        //the first time take the number
        //TODO: show my password in the won screen, after click send set the myPassword
        if(!dataReceivedAlready){
            //get the opponent number
            me.setOpponentNum(message);
            dataReceivedAlready =true;
        }

        //if already sent the number to opponent start the game
        if(sentNumberAlready){
            //start game - make guess button visible and send data gone
            viewGlobal.findViewById(R.id.buttonGuess_Id).setVisibility(View.VISIBLE);
            viewGlobal.findViewById(R.id.buttonSendNumber).setVisibility(View.GONE);

            //tell the player what to do
            messageToPlayer.setText(viewGlobal.getResources().getString(R.string.text_gameStartedMessage));

            dataToSend.setHint(activity.getString(R.string.multi_AttemptdHint));

            //game start
            mListener.gameMultiplayerStarted();

            //clear the input EditText
            dataToSend.setText("");
        }
    }

    public void guessClicked(String input_Attempt){
        if (authenticateNumber(input_Attempt)){
            me.addN_Attempts();
            int number_X = me.findX(input_Attempt);
            int number_O = me.findO(input_Attempt);

            guesses = show(input_Attempt , number_O,number_X);
            adapter.notifyDataSetChanged();

            if(number_O==4){
                mListener.onSendNumber(wonMessage);

                //end the game you win
                mListener.onMultiplayerGameFinish(true,me.getN_Attempt(),
                        me.getOpponentNum(),me.getMyNum());

            }
        }else {
            //clear the input EditText
            dataToSend.setText("");

            Toast toast = Toast.makeText(getActivity(),R.string.invalidInput, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public boolean authenticateNumber(String attempt){
        boolean authentication = true;

        char[] fragmentedAttempt = attempt.toCharArray();

        //check if number has 4 digits
        if(dataToSend.length()==4){

            //check if the number has repeated digits
            for(int i=0;i<4;i++)
            {
                for(int j=0;j<4;j++)
                {
                    if(fragmentedAttempt[i]==fragmentedAttempt[j] && i != j)
                    {
                        authentication = false;
                    }
                }
            }
        }else {
            authentication = false;
        }

        return  authentication;
    }

    public void messageSentArrived(){
        sentNumberAlready = true;

        messageToPlayer.setText(viewGlobal.getResources().getString(R.string.text_NumberArrived));

        if(sentNumberAlready && dataReceivedAlready){
            //start game - make guess button visible and send data gone
            viewGlobal.findViewById(R.id.buttonGuess_Id).setVisibility(View.VISIBLE);
            viewGlobal.findViewById(R.id.buttonSendNumber).setVisibility(View.GONE);

            //tell the player what to do
            messageToPlayer.setText(viewGlobal.getResources().getString(R.string.text_gameStartedMessage));

            dataToSend.setHint(activity.getString(R.string.multi_AttemptdHint));

            //game start
            mListener.gameMultiplayerStarted();

            //clear the input EditText
            dataToSend.setText("");
        }
    }

    public void opponentUnfortunatelyWon(){
        //end the game you lost
        mListener.onMultiplayerGameFinish(false,me.getN_Attempt(),
                me.getOpponentNum(),me.getMyNum());
    }

    private ArrayList<userGuess> show(String numGuessFullString, int numO, int numX) {
        int simbol1,simbol2,simbol3,simbol4;
        int[] vet = new int[4];
        int i,j,k;


        for(i=0;i<numO;i++) {
            vet[i] = R.drawable.circulo;
        }

        for (j = i; j <i+ numX; j++) {
            vet[j] = R.drawable.orangex;
        }

        if(numX+numO <4)
        {
            for(k=j;k<vet.length;k++)
            {
                vet[k]=0;
            }
        }
        simbol1=vet[0];
        simbol2=vet[1];
        simbol3=vet[2];
        simbol4=vet[3];

        userGuess g = new userGuess(numGuessFullString, simbol1, simbol2, simbol3, simbol4);
        guesses.add(g);
        return guesses;
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
    public interface OnMultiplayerScreenInteractionListener {
        void onSendNumber(String numberToSend);
        void onMultiplayerGameFinish(boolean won, int n_Attempts, String opponentNum , String myNum);
        void gameMultiplayerStarted();
        void onBackToMenuRequestedDuringGameplay();
    }
}
