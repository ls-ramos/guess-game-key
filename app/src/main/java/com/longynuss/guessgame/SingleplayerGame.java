package com.longynuss.guessgame;

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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

import static java.lang.Integer.parseInt;

public class SingleplayerGame extends Fragment implements View.OnClickListener {

    private OnSingleplayerScreenInteractionListener mListener;

    private Player me;

    private ArrayList<userGuess> guesses;
    private ArrayAdapter adapter;

    private EditText inputNum;

    //buttons of the help bar, THE POSITION OF THE BUTTON CORRESPOND TO THE NUMBER THAT THE BUTTON CARRY
    //ex: btn_HelpBar[0] is the button that carry the number 0 on it and so on...
    private Button[] btn_HelpBar = new Button[10];

    //count the time that a button of the help bar was touched
    //(ex: btn_Touches[0]=2, means the button 0 of the help bar was touches 2 times)
    //MAX NUMBER OF TOUCHES IS 2, if any position get greater than 2 will be set back to 0 immediately
    private int[] btn_nTouches = new int[10];
    private View view;
    public SingleplayerGame() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() instanceof OnSingleplayerScreenInteractionListener) {
            mListener = (OnSingleplayerScreenInteractionListener) getActivity();
        } else {
            throw new RuntimeException(Objects.requireNonNull(getActivity()).toString()
                    + " must implement OnSingleplayerScreenInteractionListener");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(view==null){
            view = inflater.inflate(R.layout.fragment_singleplayer_game, container, false);
        }

        view.findViewById(R.id.buttonGuessSingle_Id).setOnClickListener(this);
        inputNum = view.findViewById(R.id.inputNumSingle_Id);

        me = new Player();

        guesses = new ArrayList<>();
        ListView list = view.findViewById(R.id.ListOfGuesses);
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

        adapter = new ListAdapter(getActivity(),guesses);
        list.setAdapter(adapter);

        view.findViewById(R.id.backToMenuFromSingle).setOnClickListener(this);

        //Find the helpBar buttons by id and set to the respective position of the btn_HelpBar
        btn_HelpBar[0]= view.findViewById(R.id.buttonHelpSingle0_Id);
        btn_HelpBar[1]= view.findViewById(R.id.buttonHelpSingle1_Id);
        btn_HelpBar[2]= view.findViewById(R.id.buttonHelpSingle2_Id);
        btn_HelpBar[3]= view.findViewById(R.id.buttonHelpSingle3_Id);
        btn_HelpBar[4]= view.findViewById(R.id.buttonHelpSingle4_Id);
        btn_HelpBar[5]= view.findViewById(R.id.buttonHelpSingle5_Id);
        btn_HelpBar[6]= view.findViewById(R.id.buttonHelpSingle6_Id);
        btn_HelpBar[7]= view.findViewById(R.id.buttonHelpSingle7_Id);
        btn_HelpBar[8]= view.findViewById(R.id.buttonHelpSingle8_Id);
        btn_HelpBar[9]= view.findViewById(R.id.buttonHelpSingle9_Id);

        view.findViewById(R.id.keyboard_button0).setOnClickListener(this);
        view.findViewById(R.id.keyboard_button1).setOnClickListener(this);
        view.findViewById(R.id.keyboard_button2).setOnClickListener(this);
        view.findViewById(R.id.keyboard_button3).setOnClickListener(this);
        view.findViewById(R.id.keyboard_button4).setOnClickListener(this);
        view.findViewById(R.id.keyboard_button5).setOnClickListener(this);
        view.findViewById(R.id.keyboard_button6).setOnClickListener(this);
        view.findViewById(R.id.keyboard_button7).setOnClickListener(this);
        view.findViewById(R.id.keyboard_button8).setOnClickListener(this);
        view.findViewById(R.id.keyboard_button9).setOnClickListener(this);
        view.findViewById(R.id.keyboard_button_backSpace).setOnClickListener(this);

        //make the random number
        me.setOpponentNum(MakeRandom());

        //notify the player
        Toast toast = Toast.makeText(getActivity(),getResources().getString(R.string.startSingleMessage), Toast.LENGTH_SHORT);
        toast.show();

        //clean the input field
        inputNum.setText("");

        btn_HelpBar[0].setOnClickListener(this);

        btn_HelpBar[0].setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.buttonHelpBar_Black));
        btn_HelpBar[0].setTextColor(ContextCompat.getColor(getActivity(), R.color.text_colorBlack));
        btn_HelpBar[1].setOnClickListener(this);

        btn_HelpBar[1].setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.buttonHelpBar_Black));
        btn_HelpBar[1].setTextColor(ContextCompat.getColor(getActivity(), R.color.text_colorBlack));
        btn_HelpBar[2].setOnClickListener(this);

        btn_HelpBar[2].setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.buttonHelpBar_Black));
        btn_HelpBar[2].setTextColor(ContextCompat.getColor(getActivity(), R.color.text_colorBlack));
        btn_HelpBar[3].setOnClickListener(this);

        btn_HelpBar[3].setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.buttonHelpBar_Black));
        btn_HelpBar[3].setTextColor(ContextCompat.getColor(getActivity(), R.color.text_colorBlack));
        btn_HelpBar[4].setOnClickListener(this);

        btn_HelpBar[4].setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.buttonHelpBar_Black));
        btn_HelpBar[4].setTextColor(ContextCompat.getColor(getActivity(), R.color.text_colorBlack));
        btn_HelpBar[5].setOnClickListener(this);

        btn_HelpBar[5].setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.buttonHelpBar_Black));
        btn_HelpBar[5].setTextColor(ContextCompat.getColor(getActivity(), R.color.text_colorBlack));
        btn_HelpBar[6].setOnClickListener(this);

        btn_HelpBar[6].setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.buttonHelpBar_Black));
        btn_HelpBar[6].setTextColor(ContextCompat.getColor(getActivity(), R.color.text_colorBlack));
        btn_HelpBar[7].setOnClickListener(this);

        btn_HelpBar[7].setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.buttonHelpBar_Black));
        btn_HelpBar[7].setTextColor(ContextCompat.getColor(getActivity(), R.color.text_colorBlack));
        btn_HelpBar[8].setOnClickListener(this);

        btn_HelpBar[8].setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.buttonHelpBar_Black));
        btn_HelpBar[8].setTextColor(ContextCompat.getColor(getActivity(), R.color.text_colorBlack));
        btn_HelpBar[9].setOnClickListener(this);

        btn_HelpBar[9].setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.buttonHelpBar_Black));
        btn_HelpBar[9].setTextColor(ContextCompat.getColor(getActivity(), R.color.text_colorBlack));

        return view;
    }



    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.buttonGuessSingle_Id){
            if (inputNum.length()>0) {
                guessClicked(inputNum.getText().toString());
            }else{
                //clean the input field
                inputNum.setText("");

                Toast toast = Toast.makeText(getActivity(),R.string.invalidInput, Toast.LENGTH_SHORT);
                toast.show();
            }
        }else if (v.getId() == R.id.backToMenuFromSingle) {
            mListener.onBackToMenuRequestedDuringGameplay();
        }

        //region Keyboard
        if(v.getId() == R.id.keyboard_button0) {
            inputNum.setText(inputNum.getText().toString() + 0);
        }
        if(v.getId() == R.id.keyboard_button1) {
            inputNum.setText(inputNum.getText().toString() + 1);
        }
        if(v.getId() == R.id.keyboard_button2) {
            inputNum.setText(inputNum.getText().toString() + 2);
        }
        if(v.getId() == R.id.keyboard_button3) {
            inputNum.setText(inputNum.getText().toString() + 3);
        }
        if(v.getId() == R.id.keyboard_button4) {
            inputNum.setText(inputNum.getText().toString() + 4);
        }
        if(v.getId() == R.id.keyboard_button5) {
            inputNum.setText(inputNum.getText().toString() + 5);
        }
        if(v.getId() == R.id.keyboard_button6) {
            inputNum.setText(inputNum.getText().toString() + 6);
        }
        if(v.getId() == R.id.keyboard_button7) {
            inputNum.setText(inputNum.getText().toString() + 7);
        }
        if(v.getId() == R.id.keyboard_button8) {
            inputNum.setText(inputNum.getText().toString() + 8);
        }
        if(v.getId() == R.id.keyboard_button9) {
            inputNum.setText(inputNum.getText().toString() + 9);
        }
        if(v.getId() == R.id.keyboard_button_backSpace) {
            if (inputNum.length()>0) {
                String text = inputNum.getText().toString();
                text = text.substring(0,text.length()-1);
                inputNum.setText(text);
            }
        }
        //endregion

        //region HelpBar
        else if(v.getId() == R.id.buttonHelpSingle0_Id ||
                v.getId() == R.id.buttonHelpSingle1_Id||
                v.getId() == R.id.buttonHelpSingle2_Id||
                v.getId() == R.id.buttonHelpSingle3_Id||
                v.getId() == R.id.buttonHelpSingle4_Id||
                v.getId() == R.id.buttonHelpSingle5_Id||
                v.getId() == R.id.buttonHelpSingle6_Id||
                v.getId() == R.id.buttonHelpSingle7_Id||
                v.getId() == R.id.buttonHelpSingle8_Id||
                v.getId() == R.id.buttonHelpSingle9_Id)
        {
            //Variable that tells which button of the help bar was pressed (0,1,2,...,9)
            int nButton=0;

            //Conditions that fill nButton with the number that the pressed button carry (the position of the button in btn_HelpBar array)
            if(v.getId() == R.id.buttonHelpSingle1_Id) {
                nButton=1;
            }
            else if(v.getId() == R.id.buttonHelpSingle2_Id) {
                nButton=2;
            }
            else if(v.getId() == R.id.buttonHelpSingle3_Id) {
                nButton=3;
            }
            else if(v.getId() == R.id.buttonHelpSingle4_Id) {
                nButton=4;
            }
            else if(v.getId() == R.id.buttonHelpSingle5_Id) {
                nButton=5;
            }
            else if(v.getId() == R.id.buttonHelpSingle6_Id) {
                nButton=6;
            }
            else if(v.getId() == R.id.buttonHelpSingle7_Id) {
                nButton=7;
            }
            else if(v.getId() == R.id.buttonHelpSingle8_Id) {
                nButton=8;
            }
            else if(v.getId() == R.id.buttonHelpSingle9_Id) {
                nButton=9;
            }

            btn_nTouches[nButton]++;

            if(btn_nTouches[nButton]>2) {
                btn_nTouches[nButton]=0;
            }

            if(btn_nTouches[nButton]==0) {
                btn_HelpBar[nButton].setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.buttonHelpBar_Black));
                btn_HelpBar[nButton].setTextColor(ContextCompat.getColor(getActivity(), R.color.text_colorBlack));
            }
            else if(btn_nTouches[nButton]==1) {
                btn_HelpBar[nButton].setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.buttonHelpBar_Green));
                btn_HelpBar[nButton].setTextColor(ContextCompat.getColor(getActivity(), R.color.text_colorGreen));

            }
            else if(btn_nTouches[nButton]==2) {
                btn_HelpBar[nButton].setBackgroundColor(ContextCompat.getColor(Objects.requireNonNull(getActivity()), R.color.buttonHelpBar_Red));
                btn_HelpBar[nButton].setTextColor(ContextCompat.getColor(getActivity(), R.color.text_colorRed));
            }
        }
        //endregion
    }

    public void guessClicked(String input_Attempt){

        if (authenticateNumber(input_Attempt)){
            me.addN_Attempts();
            int number_X = me.findX(input_Attempt);
            int number_O = me.findO(input_Attempt);

            guesses = show(input_Attempt , number_O,number_X);
            adapter.notifyDataSetChanged();

            if(number_O==4){
                //end the game you win
                mListener.onSinglePlayerGameFinish(true, me.getN_Attempt(), me.getOpponentNum());
            }

            //clean the input field
            inputNum.setText("");
        }else {
            //clean the input field
            inputNum.setText("");

            Toast toast = Toast.makeText(getActivity(),R.string.invalidInput, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public boolean authenticateNumber(String attempt){
        boolean authentication = true;

        int num = parseInt(attempt);
        int[] fragmentedNum = new int[4];

        //check if number has 4 digits
        if(inputNum.length()==4){
            //break the number in 4 digits one in which space of the array
            for(int x=3;x>=0;x--){
                fragmentedNum[x] = num%10;
                num = num /10;
            }

            //check if the number has repeated digits
            for(int i=0;i<4;i++)
            {
                for(int j=0;j<4;j++)
                {
                    if(fragmentedNum[i]==fragmentedNum[j] && i != j)
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

    private String MakeRandom() {
        int number,error=0;
        boolean testNumber;

        Random makeRandomNumber = new Random();

        int[] randomNumber = new int[4];
        
        for(int i=0;i<randomNumber.length;i++){
        	randomNumber[i]=-1;
        }
		
        for(int i=0;i<4;i++) {
            number = makeRandomNumber.nextInt(10);
            testNumber = true;

            //have the number been used already ?
            //put the number in test mode
            while(testNumber) {
                //check if the generated number exits in the final number
                //if so point the error
                for(int j=0;j<4;j++) {
                    if(randomNumber[j]==number) {
                        error++;
                    }
                }

                //if there is an error in the generated number generates another one
                //and keep in test mode
                if(error>=1) {
                    testNumber=true;
                    error=0;

                    number = makeRandomNumber.nextInt(10);
                }

                //if no error occurred then go out of the test mode
                else {
                    testNumber=false;
                    error=0;
                }
            }

            //add the number that was tested to the final number não
            randomNumber[i] = number;
        }
        StringBuilder strNum = new StringBuilder();

        for (int num : randomNumber)
        {
            strNum.append(num);
        }

        return strNum.toString();
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
    public interface OnSingleplayerScreenInteractionListener {

        void onSinglePlayerGameFinish(boolean won, int n_Attempts, String opponentNum);
        void onBackToMenuRequestedDuringGameplay();
    }
}
