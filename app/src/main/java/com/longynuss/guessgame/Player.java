package com.longynuss.guessgame;

/**
 * Created by lucas on 29/06/2017.
 */

class Player {
    private static String my_Num, opponent_Num;
    private static int n_Attempts;

     Player(){
        my_Num = "";
        opponent_Num = "";
        n_Attempts = 0;
    }

     int findX(String attempt) {
        int numX=0;

        char[] fragmentedNum = opponent_Num.toCharArray();

        char[] fragmentedAttempt = attempt.toCharArray();

        for (int i = 0; i < opponent_Num.length(); i++) {
            for (int j = 0; j < opponent_Num.length(); j++) {
                if (fragmentedAttempt[i] == fragmentedNum[j] && i != j) {
                    numX++;
                }
            }
        }


        return  numX;
    }

     int findO(String attempt) {
        int numO=0;

        char[] fragmentedNum = opponent_Num.toCharArray();

        char[] fragmentedAttempt = attempt.toCharArray();

        for (int i = 0; i < opponent_Num.length(); i++) {
            if (fragmentedAttempt[i] == fragmentedNum[i]) {
                numO++;
            }
        }

        return  numO;
    }

     void setMyNum(String x){
        my_Num = x;
    }

     String getMyNum(){
        return my_Num;
    }

     void setOpponentNum(String x){
        opponent_Num = x;
    }

     String getOpponentNum(){
        return opponent_Num;
    }

     void addN_Attempts(){
        n_Attempts++;
    }

     int getN_Attempt(){
        return n_Attempts;
    }


}
