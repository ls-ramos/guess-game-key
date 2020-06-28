package com.longynuss.guessgame;

/**
 * Created by lucas on 04/07/2017.
 */

class userGuess {

    private String guessUser;
    private int image1;
    private int image2;
    private int image3;
    private int image4;

    userGuess(String userGuess, int image1, int image2, int image3, int image4) {
        this.guessUser = userGuess;
        this.image1 = image1;
        this.image2 = image2;
        this.image3 = image3;
        this.image4 = image4;
    }


    String getUserGuess() {
        return guessUser;
    }


    int getImage1() {

        return image1;
    }

    int getImage2() {
        return image2;
    }

    int getImage3() {
        return image3;
    }

    int getImage4() {
        return image4;
    }

}
