package com.longynuss.guessgame;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesStatusCodes;
import com.google.android.gms.games.leaderboard.LeaderboardVariant;
import com.google.android.gms.games.leaderboard.Leaderboards;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.OnInvitationReceivedListener;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMultiplayer;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateListener;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateListener;

import com.google.example.games.basegameutils.BaseGameUtils;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        MenuScreen.OnFragmentInteractionListener,
        MultiplayerGame.OnMultiplayerScreenInteractionListener,
        SingleplayerGame.OnSingleplayerScreenInteractionListener,
        WonLostScreen.onWonLostInteractionListener,
        HowToPlayScreen.OnFragmentInteractionListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        RoomUpdateListener,
        RoomStatusUpdateListener, RealTimeMessageReceivedListener,
        OnInvitationReceivedListener,
        RealTimeMultiplayer.ReliableMessageSentCallback

{
    private SharedPreferences mPrefs;
    private InterstitialAd mInterstitialAd;
    private TextView message;

    private boolean isOnMultiplayer;
    private boolean opponentIsThere;

    //regionSound Control
    //button and the boolean that says if the sound is on or off
    private Button btn_sound;
    private boolean isSound_On;//Sound by default start ON - set in onCreate

    //Media Player that controls which Song will play and when play and stop
    private MediaPlayer mediaPlayer;
    //endregion

    //region Screens
    MenuScreen menuScreen;
    MultiplayerGame multiGameScreen;
    SingleplayerGame singleGameScreen;
    WonLostScreen wonLostScreen;
    HowToPlayScreen howToPlayScreen;
    //endregion

    //regionGoogleServerVariables
    private GoogleApiClient mGoogleApiClient;
    private Long mPoints;
    private static int RC_SIGN_IN = 9001;
    private boolean mResolvingConnectionFailure = false;
    private boolean mAutoStartSignInFlow = true;
    private boolean mSignInClicked = false;

    boolean mExplicitSignOut = false;
    boolean mInSignInFlow = false; // set to true when you're in the middle of the
    // sign in flow, to know you should not attempt
    // to connect in onStart()

    // arbitrary request code for the waiting room UI.
    // This can be any integer that's unique in your Activity.
    final static int RC_WAITING_ROOM = 10002;

    // are we already playing?
    boolean mPlaying = false;

    // at least 2 players required for our game
    final static int MIN_PLAYERS = 2;

    // request code for the "select players" UI
    // can be any number as long as it's unique
    final static int RC_SELECT_PLAYERS = 10000;

    // request code (can be any number, as long as it's unique)
    final static int RC_INVITATION_INBOX = 10001;

    // request code (can be any number, as long as it's unique)
    final static int REQUEST_ACHIEVEMENTS = 10008;

    // request code (can be any number, as long as it's unique)
    private static final int REQUEST_LEADERBOARD = 1009;

    private String mRoomId = null, mMyId=null;
    private ArrayList<Participant> players;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the Google Api Client with access to the Play Games services
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API)
                .addScope(Games.SCOPE_GAMES)
                .build();

        if(!isGooglePlayServicesAvailable(this)){
            Toast.makeText(this,R.string.googlePlayServicesError,Toast.LENGTH_LONG).show();
        }

        menuScreen = new MenuScreen();
        multiGameScreen = new MultiplayerGame();
        singleGameScreen = new SingleplayerGame();
        wonLostScreen = new WonLostScreen();
        howToPlayScreen = new HowToPlayScreen();

        mPrefs = getSharedPreferences("label", 0);

        isOnMultiplayer = false;

        //assign the sound button
        btn_sound =  findViewById(R.id.buttonSound_Id);
        //Create the media Player and tell the path of the sound that will play
        mediaPlayer = MediaPlayer.create(MainActivity.this,R.raw.guess_song);
        //Set the media Player to loop the song
        mediaPlayer.setLooping(true);

        //plays the song


        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this,"ca-app-pub-3178146623136354~5194557299");

        // Create the InterstitialAd and set the adUnitId.
        mInterstitialAd = new InterstitialAd(this);

        // Defined in res/values/ids.xml
        mInterstitialAd.setAdUnitId(getString(R.string.ad_unit_id));
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        mInterstitialAd.loadAd(adRequest);

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Request a new ad if one isn't already loaded, hide the button, and kick off the timer.
                if (!mInterstitialAd.isLoading() && !mInterstitialAd.isLoaded()) {
                    AdRequest adRequest = new AdRequest.Builder()
                            .build();
                    mInterstitialAd.loadAd(adRequest);
                }            }
        });
        if (savedInstanceState == null) {
            //Display menu screen
            getSupportFragmentManager().beginTransaction().add(R.id.Screens_Layout, menuScreen).commit();
        }

        if (!mediaPlayer.isPlaying()) {
            btn_sound.setBackgroundResource(R.drawable.sound_on);
            isSound_On = true;
            mediaPlayer.start();
        }
    }

    private boolean isGooglePlayServicesAvailable(Activity activity) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if(status != ConnectionResult.SUCCESS) {
            if(googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404).show();
            }
            return false;
        }
        return true;
    }

    private void showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and restart the game.
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Toast toast = Toast.makeText(MainActivity.this,"Ad not loaded", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(isSound_On){
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isSound_On){
            mediaPlayer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer.release();
    }

    //regionMenuScreenControl
    @Override
    public void onSignInClicked() {
        mSignInClicked = true;
        mGoogleApiClient.connect();
    }

    @Override
    public void onSinglePlayerClicked() {
        //Go to SinglePlayer screen
        getSupportFragmentManager().beginTransaction().replace(R.id.Screens_Layout, singleGameScreen).commit();
    }

    @Override
    public void onMultiPlayerClicked() {
        if (mGoogleApiClient.isConnected()) {
            //set GONE to menu buttons
            findViewById(R.id.buttonSingle_Id).setVisibility(View.GONE);
            findViewById(R.id.buttonHowPlay_Id).setVisibility(View.GONE);
            findViewById(R.id.buttonMulti_Id).setVisibility(View.GONE);
            findViewById(R.id.sing_out_button).setVisibility(View.GONE);

            //set VISIBLE to multiplayer screen
            findViewById(R.id.quick_match_button).setVisibility(View.VISIBLE);
            findViewById(R.id.invite_friends_button).setVisibility(View.VISIBLE);
            findViewById(R.id.invitations_button).setVisibility(View.VISIBLE);
            findViewById(R.id.quick_match_button).setVisibility(View.VISIBLE);
            findViewById(R.id.backMenu_fromMultiplayer).setVisibility(View.VISIBLE);

            isOnMultiplayer = true;
        }else{
            showSimpleMessageDialog(getResources().getString(R.string.notConnectedError),
                    getResources().getString(R.string.okMessage));
        }
    }

    @Override
    public void onHowToPlayClicked() {
        //Go to HowToPlay screen
        getSupportFragmentManager().beginTransaction().replace(R.id.Screens_Layout, howToPlayScreen).commit();
    }

    @Override
    public void onQuickMatchClicked() {
        if (mGoogleApiClient.isConnected()) {
            Toast toast = Toast.makeText(MainActivity.this,getResources().getString(R.string.creatingRoom) , Toast.LENGTH_SHORT);
            toast.show();
            //start the AUTO-MATCH game
            startQuickGame();
        }else{
            showSimpleMessageDialog(getResources().getString(R.string.notConnectedError),
                    getResources().getString(R.string.okMessage));
        }
    }

    @Override
    public void onInviteFriendsClicked() {
        if (mGoogleApiClient.isConnected()) {
            // launch the player selection screen
            // minimum and maximum: 1 other player;
            Intent intent = Games.RealTimeMultiplayer.getSelectOpponentsIntent(mGoogleApiClient, 1, 1);
            startActivityForResult(intent, RC_SELECT_PLAYERS);
        }else{
            showSimpleMessageDialog(getResources().getString(R.string.notConnectedError),
                    getResources().getString(R.string.okMessage));
        }
    }

    @Override
    public void onInvitationsClicked() {
        if (mGoogleApiClient.isConnected()) {
            // launch the intent to show the invitation inbox screen
            Intent intent = Games.Invitations.getInvitationInboxIntent(mGoogleApiClient);
            startActivityForResult(intent, RC_INVITATION_INBOX);
        }else{
            showSimpleMessageDialog(getResources().getString(R.string.notConnectedError),
                    getResources().getString(R.string.okMessage));
        }
    }

    @Override
    public void onBackToMenuClicked() {
        //set GONE to menu buttons
        findViewById(R.id.buttonSingle_Id).setVisibility(View.VISIBLE);
        findViewById(R.id.buttonHowPlay_Id).setVisibility(View.VISIBLE);
        findViewById(R.id.buttonMulti_Id).setVisibility(View.VISIBLE);
        findViewById(R.id.sing_out_button).setVisibility(View.VISIBLE);

        //set VISIBLE to multiplayer screen
        findViewById(R.id.quick_match_button).setVisibility(View.GONE);
        findViewById(R.id.invite_friends_button).setVisibility(View.GONE);
        findViewById(R.id.invitations_button).setVisibility(View.GONE);
        findViewById(R.id.quick_match_button).setVisibility(View.GONE);
        findViewById(R.id.backMenu_fromMultiplayer).setVisibility(View.GONE);

        isOnMultiplayer = false;
    }

    @Override
    public void onSignOutClicked() {
        mSignInClicked = false;
        mGoogleApiClient.disconnect();

        message.setText(getResources().getString(R.string.isDisconnectedText));

        Toast toast = Toast.makeText(MainActivity.this,getResources().getString(R.string.disconnectedMessage) , Toast.LENGTH_LONG);
        toast.show();

        findViewById(R.id.btn_signIn).setVisibility(View.VISIBLE);
        findViewById(R.id.buttonMulti_Id).setVisibility(View.GONE);
        findViewById(R.id.sing_out_button).setVisibility(View.GONE);
        findViewById(R.id.buttonRecord_Id).setVisibility(View.GONE);
        findViewById(R.id.leaderboard_Button_Id).setVisibility(View.GONE);
    }

    @Override
    public void onMenuScreenCreated() {
        message = (TextView) findViewById(R.id.connectionMessagerToPlayer);
        if(mGoogleApiClient.isConnected()){

            message.setText(getResources().getString(R.string.isConnectedText));
            if(isOnMultiplayer){
                findViewById(R.id.btn_signIn).setVisibility(View.GONE);
                findViewById(R.id.quick_match_button).setVisibility(View.VISIBLE);
                findViewById(R.id.invite_friends_button).setVisibility(View.VISIBLE);
                findViewById(R.id.invitations_button).setVisibility(View.VISIBLE);
                findViewById(R.id.leaderboard_Button_Id).setVisibility(View.VISIBLE);
            }else{
                findViewById(R.id.btn_signIn).setVisibility(View.GONE);
                findViewById(R.id.buttonMulti_Id).setVisibility(View.VISIBLE);
                findViewById(R.id.sing_out_button).setVisibility(View.VISIBLE);
                findViewById(R.id.buttonRecord_Id).setVisibility(View.VISIBLE);
                findViewById(R.id.leaderboard_Button_Id).setVisibility(View.VISIBLE);
            }

        }
    }

    @Override
    public void onAchievementScreenRequested() {
        if (mGoogleApiClient.isConnected()) {
            startActivityForResult(Games.Achievements.getAchievementsIntent(mGoogleApiClient),
                    REQUEST_ACHIEVEMENTS);
        }else{
            showSimpleMessageDialog(getResources().getString(R.string.notConnectedError),
                    getResources().getString(R.string.okMessage));
        }
    }

    @Override
    public void onLeaderboardScreenRequested() {
        if (mGoogleApiClient.isConnected()) {
            startActivityForResult(Games.Leaderboards.getLeaderboardIntent(mGoogleApiClient,
                    getResources().getString(R.string.leaderboard_ranking)), REQUEST_LEADERBOARD);
        }else{
            showSimpleMessageDialog(getResources().getString(R.string.notConnectedError),
                    getResources().getString(R.string.okMessage));
        }
    }

    //On muteSound button clicked (DEFINED ON XML OF THE CURRENT ACTIVITY)
    public void onMuteSoundClicked(View v) {
        //Change the state of the sound
        if(isSound_On)
        {
            //if the sound is on turn it off
            btn_sound.setBackgroundResource(R.drawable.sound_off);
            isSound_On = !isSound_On;
            mediaPlayer.pause();
        }else {
            //if the sound is off turn it on
            btn_sound.setBackgroundResource(R.drawable.sound_on);
            isSound_On = !isSound_On;
            mediaPlayer.start();
        }

    }
    //endregion

    //regionGoogleServerMethods
    @Override
    protected void onStart() {
        super.onStart();
        if (!mInSignInFlow && !mExplicitSignOut && !"false".equals(mPrefs.getString("autoConnect","null"))) {
            // auto sign in
            mGoogleApiClient.connect();
        }


        if ("null".equals(mPrefs.getString("autoConnect","null"))) {
            AlertDialog.Builder builder;
            builder = new AlertDialog.Builder(MainActivity.this);

            builder.setTitle(getResources().getString(R.string.autoConectingTitle))
                    .setMessage(getResources().getString(R.string.autoConectingText))
                    .setPositiveButton(getResources().getString(R.string.yesText), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // continue with delete
                            mPrefs.edit().putString("autoConnect","false").apply();
                            showLearnMessage();
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.noText), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                            mPrefs.edit().putString("autoConnect","true").apply();
                            showLearnMessage();
                        }
                    }).show();
        }
    }

    private void showLearnMessage(){
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle(getResources().getString(R.string.learnToPlayTitle))
                .setMessage(getResources().getString(R.string.learnMessageText))
                .setPositiveButton(getResources().getString(R.string.yesText), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //go to to tutorial: how to play screen
                        getSupportFragmentManager().beginTransaction().replace(R.id.Screens_Layout, howToPlayScreen).commit();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.noText), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //tutorial denied
                    }
                }).show();
    }

    private RoomConfig.Builder makeBasicRoomConfigBuilder() {
        return RoomConfig.builder(this)
                .setMessageReceivedListener(this)
                .setRoomStatusUpdateListener(this);
    }

    private void startQuickGame() {

        // auto-match criteria to invite one random automatch opponent.
        // You can also specify more opponents (up to 3).
        Bundle am = RoomConfig.createAutoMatchCriteria(1, 1, 0);

        // build the room config:
        RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
        roomConfigBuilder.setAutoMatchCriteria(am);
        RoomConfig roomConfig = roomConfigBuilder.build();

        // create room:
        Games.RealTimeMultiplayer.create(mGoogleApiClient, roomConfig);

        // prevent screen from sleeping during handshake
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //go to the game screen:
        //THIS PART IS MADE IN THE FUNCTION onPeersConnected
    }

    // returns whether there are enough players to start the game
    boolean shouldStartGame(Room room) {
        int connectedPlayers = 0;
        for (Participant p : room.getParticipants()) {
            if (p.isConnectedToRoom()) ++connectedPlayers;
        }
        return connectedPlayers >= MIN_PLAYERS;
    }

    // Returns whether the room is in a state where the game should be canceled.
    boolean shouldCancelGame() {
        if(!players.isEmpty()){
            for (Participant p : players) {
                if (p.isConnectedToRoom()) {
                    //at least one participant is not connected to the room
                    return true;
                }
            }
        }
        return false;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == RC_SIGN_IN) {
            mSignInClicked = false;
            mResolvingConnectionFailure = false;
            if (resultCode == RESULT_OK) {
                mGoogleApiClient.connect();
            } else {
                // Bring up an error dialog to alert the user that sign-in
                // failed. The R.string.signin_failure should reference an error
                // string in your strings.xml file that tells the user they
                // could not be signed in, such as "Unable to sign in."
                BaseGameUtils.showActivityResultError(this,
                        requestCode, resultCode, R.string.signin_failure);
            }
        }

        if (requestCode == RC_SELECT_PLAYERS) {
            if (resultCode != Activity.RESULT_OK) {
                // user canceled
                return;
            }

            // get the invitee list
            final ArrayList<String> invitees =
                    intent.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);

            // get auto-match criteria
            Bundle autoMatchCriteria;
            int minAutoMatchPlayers =
                    intent.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
            int maxAutoMatchPlayers =
                    intent.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);

            if (minAutoMatchPlayers > 0) {
                autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                        minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            } else {
                autoMatchCriteria = null;
            }

            // create the room and specify a variant if appropriate
            RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
            roomConfigBuilder.addPlayersToInvite(invitees);
            if (autoMatchCriteria != null) {
                roomConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
            }
            RoomConfig roomConfig = roomConfigBuilder.build();
            Games.RealTimeMultiplayer.create(mGoogleApiClient, roomConfig);

            // prevent screen from sleeping during handshake
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        if (requestCode == RC_INVITATION_INBOX) {
            if (resultCode != Activity.RESULT_OK) {
                // canceled
                return;
            }

            // get the selected invitation
            Bundle extras = intent.getExtras();
            Invitation invitation =
                    extras.getParcelable(Multiplayer.EXTRA_INVITATION);

            // accept it!
            RoomConfig roomConfig = null;
            if (invitation != null) {
                roomConfig = makeBasicRoomConfigBuilder()
                        .setInvitationIdToAccept(invitation.getInvitationId())
                        .build();
            }
            Games.RealTimeMultiplayer.join(mGoogleApiClient, roomConfig);

            // prevent screen from sleeping during handshake
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            // go to game screen
        }

        if (requestCode == RC_WAITING_ROOM) {
            if (resultCode == Activity.RESULT_OK) {
                //go to multiplayer game fragment
                getSupportFragmentManager().beginTransaction().replace(R.id.Screens_Layout, multiGameScreen).commit();
            }
            else if (resultCode == Activity.RESULT_CANCELED) {
                // Waiting room was dismissed with the back button. The meaning of this
                // action is up to the game. You may choose to leave the room and cancel the
                // match, or do something else like minimize the waiting room and
                // continue to connect in the background.
                Toast toast = Toast.makeText(MainActivity.this,getResources().getString(R.string.waitingRoomCanceled) , Toast.LENGTH_SHORT);
                toast.show();
                // in this example, we take the simple approach and just leave the room:
                Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, mRoomId);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
            else if (resultCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                // player wants to leave the room.
                Toast toast = Toast.makeText(MainActivity.this,getResources().getString(R.string.waitingRoomCanceled) , Toast.LENGTH_SHORT);
                toast.show();
                Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, mRoomId);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {


        message.setText(getResources().getString(R.string.isConnectedText));

        Toast toast = Toast.makeText(MainActivity.this,getResources().getString(R.string.connectedMessage) , Toast.LENGTH_LONG);
        toast.show();

        // The player is signed in. Hide the sign-in button and allow the
        // player to proceed.

        // show sign-out button and the multiplayer, hide the sign-in button
        if(!isOnMultiplayer){
            findViewById(R.id.btn_signIn).setVisibility(View.GONE);
            findViewById(R.id.buttonMulti_Id).setVisibility(View.VISIBLE);
            findViewById(R.id.buttonSingle_Id).setVisibility(View.VISIBLE);
            findViewById(R.id.buttonHowPlay_Id).setVisibility(View.VISIBLE);
            findViewById(R.id.sing_out_button).setVisibility(View.VISIBLE);
            findViewById(R.id.buttonRecord_Id).setVisibility(View.VISIBLE);
            findViewById(R.id.leaderboard_Button_Id).setVisibility(View.VISIBLE);
        }

        Games.Invitations.registerInvitationListener(mGoogleApiClient, this);

        //Handle the invitation accepted
        if (bundle != null) {
            Invitation inv =
                    bundle.getParcelable(Multiplayer.EXTRA_INVITATION);

            if (inv != null) {
                // accept invitation
                RoomConfig.Builder roomConfigBuilder = makeBasicRoomConfigBuilder();
                roomConfigBuilder.setInvitationIdToAccept(inv.getInvitationId());
                Games.RealTimeMultiplayer.join(mGoogleApiClient, roomConfigBuilder.build());

                // prevent screen from sleeping during handshake
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                // go to game screen
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (mResolvingConnectionFailure) {
            // already resolving
            return;
        }

        // if the sign-in button was clicked or if auto sign-in is enabled,
        // launch the sign-in flow
        if (mSignInClicked || mAutoStartSignInFlow) {
            mAutoStartSignInFlow = false;
            mSignInClicked = false;
            mResolvingConnectionFailure = true;

            // Attempt to resolve the connection failure using BaseGameUtils.
            // The R.string.signin_other_error value should reference a generic
            // error string in your strings.xml file, such as "There was
            // an issue with sign-in, please try again later."
            if (!BaseGameUtils.resolveConnectionFailure(this,
                    mGoogleApiClient, connectionResult,
                    RC_SIGN_IN, R.string.signin_other_error)) {
                mResolvingConnectionFailure = false;
            }
        }

        // Put code here to display the sign-in button
    }

    @Override
    public void onInvitationReceived(Invitation invitation) {
        // show in-game popup to let user know of pending invitation
        Toast toast = Toast.makeText(MainActivity.this, getResources().getString(R.string.invitationArrivedText), Toast.LENGTH_LONG);
        toast.show();
    }

    @Override
    public void onInvitationRemoved(String s) {

    }

    @Override
    public void onRealTimeMessageReceived(RealTimeMessage realTimeMessage) {
        byte[] messageByte = realTimeMessage.getMessageData();
        String message = new String(messageByte, Charset.forName("UTF-8"));

        if(!message.isEmpty()){
            if (message.equals(multiGameScreen.wonMessage)){
                //opponent won
                multiGameScreen.opponentUnfortunatelyWon();
            }else{
                //it's the number of the opponent
                //send to fragment the received message from opponent
                multiGameScreen.DataReceived(message);
            }
        } else{
                Log.e("Erro Recieved","Number is empty");
            }
    }

    @Override
    public void onRealTimeMessageSent(int statusCode, int tokenId, String recipientParticipantId) {
        if(statusCode == GamesStatusCodes.STATUS_OK){
            //tell fragment that was okay and now can start the game
            multiGameScreen.messageSentArrived();
        }else if(statusCode == GamesStatusCodes.STATUS_REAL_TIME_MESSAGE_SEND_FAILED){
            //error now the program will automatic resend the message
            Log.w("error to send message","Resending automatically");
        }else if(statusCode == GamesStatusCodes.STATUS_REAL_TIME_ROOM_NOT_JOINED){
            Log.w("error to send message","Resending automatically");
        } else {
            Toast toast = Toast.makeText(MainActivity.this,getResources().getString(R.string.errorTosendNumber), Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public void onRoomConnecting(Room room) {
        Toast toast = Toast.makeText(MainActivity.this,getResources().getString(R.string.connectingRoom) , Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onRoomAutoMatching(Room room) {

    }

    @Override
    public void onPeerInvitedToRoom(Room room, List<String> list) {

    }

    @Override
    public void onPeerDeclined(Room room, List<String> list) {
        // peer declined invitation -- see if game should be canceled
        if (!mPlaying && shouldCancelGame()) {
            Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, room.getRoomId());
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onPeerJoined(Room room, List<String> list) {

    }

    @Override
    public void onPeerLeft(Room room, List<String> list) {

    }

    @Override
    public void onConnectedToRoom(Room room) {

    }

    @Override
    public void onDisconnectedFromRoom(Room room) {

    }

    @Override
    public void onPeersConnected(Room room, List<String> list) {
        if (!mPlaying && shouldStartGame(room)) {
            opponentIsThere=true;
            players = room.getParticipants();
            mMyId = Games.Players.getCurrentPlayerId(mGoogleApiClient);
        }
    }

    @Override
    public void onPeersDisconnected(Room room, List<String> list) {
        if (mPlaying) {
            // do game-specific handling of this -- remove player's avatar
            // from the screen, etc. If not enough players are left for
            // the game to go on, end the game and leave the room.
            opponentIsThere=false;

            Toast toast3 = Toast.makeText(MainActivity.this,getResources().getString(R.string.opponentLeaftText), Toast.LENGTH_SHORT);
            toast3.show();
        } else if (shouldCancelGame()) {
            // cancel the game
            leaveRoom();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
    public void onP2PConnected(String s) {

    }

    @Override
    public void onP2PDisconnected(String s) {
    }

    @Override
    public void onRoomCreated(int statusCode, Room room) {

        if (statusCode == GamesStatusCodes.STATUS_CLIENT_RECONNECT_REQUIRED) {
            // display error
            Toast toast = Toast.makeText(MainActivity.this,getResources().getString(R.string.reconnectError) , Toast.LENGTH_LONG);
            toast.show();
            mGoogleApiClient.reconnect();
            return;
        }

        if (statusCode != GamesStatusCodes.STATUS_OK) {
            // display error
            Toast toast = Toast.makeText(MainActivity.this,getResources().getString(R.string.errorRoom) , Toast.LENGTH_LONG);
            toast.show();
            return;
        }

        Toast toast = Toast.makeText(MainActivity.this,getResources().getString(R.string.createdRoom) , Toast.LENGTH_SHORT);
        toast.show();
        //getRoomId
        mRoomId = room.getRoomId();

        // get waiting room intent
        Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(mGoogleApiClient, room, Integer.MAX_VALUE);
        startActivityForResult(i, RC_WAITING_ROOM);
    }

    @Override
    public void onJoinedRoom(int statusCode, Room room) {
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            // display error
            return;
        }

        //getRoomId
        mRoomId = room.getRoomId();

        // get waiting room intent
        Intent i = Games.RealTimeMultiplayer.getWaitingRoomIntent(mGoogleApiClient, room, Integer.MAX_VALUE);
        startActivityForResult(i, RC_WAITING_ROOM);
    }

    @Override
    public void onLeftRoom(int i, String s) {
        //back to menu after leaving the room
        Toast toast = Toast.makeText(MainActivity.this,"Left Room", Toast.LENGTH_SHORT);
        toast.show();

        mRoomId = null;
        getSupportFragmentManager().beginTransaction().replace(R.id.Screens_Layout, menuScreen).commit();

        //Game Finish now leave the room if you are in any room
        showInterstitial();
    }

    @Override
    public void onRoomConnected(int statusCode, Room room) {
        if (statusCode != GamesStatusCodes.STATUS_OK) {
            // let screen go to sleep
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            // show error message, return to main screen.
        }

        //get RoomId
        mRoomId = room.getRoomId();
    }
    //endregion

    //regionMultiplyer Game Control
    @Override
    public void onSendNumber(String numberToSend) {
        //send  number to opponent
        Toast toast = Toast.makeText(MainActivity.this,getResources().getString(R.string.sendingText), Toast.LENGTH_SHORT);
        toast.show();

        if(opponentIsThere){
            for (Participant p : players) {
                if (!p.getParticipantId().equals(mMyId)) {
                    Games.RealTimeMultiplayer.sendReliableMessage(mGoogleApiClient, this, numberToSend.getBytes(Charset.forName("UTF-8")),
                            mRoomId, p.getParticipantId());
                }
            }
        }
    }

    @Override
    public void onMultiplayerGameFinish(boolean won, int n_Attempts, String opponentNum,String myNum) {

        //set the bundle to send to the fragment if the player won or lost and the n_Attempts
        Bundle bundle = new Bundle();
        String wonString = String.valueOf(won);
        bundle.putString("won/lost", wonString);
        bundle.putString("typeGame", "multi");
        bundle.putString("opponentNum", opponentNum);
        bundle.putString("myNum",myNum);
        bundle.putInt("attempts", n_Attempts);
        wonLostScreen.setArguments(bundle);

        getSupportFragmentManager().beginTransaction().replace(R.id.Screens_Layout, wonLostScreen).commit();
    }

    @Override
    public void gameMultiplayerStarted() {
        mPlaying = true;
    }

    @Override
    public void onBackToMenuRequestedDuringGameplay() {
        //get text from xml Strings(get the text based on the language of the phone)
        String titleDialog = getResources().getString(R.string.text_TitleBackDialog);
        String messageDialog = getResources().getString(R.string.text_MessagebackDialog);
        String negativeDialog = getResources().getString(R.string.text_Negative);
        String positiveDialog = getResources().getString(R.string.text_Positive);

        //create the warning dialog with the respective texts
        CreateMenuDialog(titleDialog,messageDialog,negativeDialog,positiveDialog);
    }

    @Override
    public void onBackToMenuRequested(){

        if(mRoomId!=null){
            leaveRoom();
        }else{
            //SinglePlayer requested back to menu
            getSupportFragmentManager().beginTransaction().replace(R.id.Screens_Layout, menuScreen).commit();

            //Game Finish now leave the room if you are in any room
            showInterstitial();

        }

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void checkAchievements(int n_attempts, String typeGame) {
        int score =0;

        if (mGoogleApiClient.isConnected() && typeGame.equals("single")) {
            //single player achievements
            if(n_attempts<=50 && n_attempts>20){
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_i_never_give_up_));
                score= 10;
            }else if(n_attempts<=20 && n_attempts>10){
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_i_am_getting_better));
                score= 30;
            }else if(n_attempts<=10 && n_attempts>5){
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_logical_brain));
                score= 60;
            }else if(n_attempts<=5 && n_attempts>3){
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_im_not_kidding_here));
                score= 80;
            }else if(n_attempts<=3 && n_attempts>1){
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_yes_i_just_did_that_));
                score= 120;
            }else if(n_attempts==1){
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_what_a_lucky_son_of_a___));
                score= 200;
            }

            loadScoreOfLeaderBoard(score);

            Toast toast = Toast.makeText(MainActivity.this,getResources().getString(R.string.rightArchievements), Toast.LENGTH_SHORT);
            toast.show();
        }else if(mGoogleApiClient.isConnected() && typeGame.equals("multi")){
            //multi player achievements
            if(n_attempts<=50 && n_attempts>20){
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_searching_the_luck));
                score= 10;
            }else if(n_attempts<=20 && n_attempts>10){
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_starting_to_learn));
                score= 30;
            }else if(n_attempts<=10 && n_attempts>5){
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_logic_always_wins));
                score= 60;
            }else if(n_attempts<=5 && n_attempts>3){
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_no_chance_for_the_opponent));
                score= 80;
            }else if(n_attempts<=3 && n_attempts>1){
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_what_just_happened_));
                score= 120;
            }else if(n_attempts==1){
                Games.Achievements.unlock(mGoogleApiClient, getResources().getString(R.string.achievement_oh_come_on_your_friend_told_you_));
                score= 200;
            }

            loadScoreOfLeaderBoard(score);
            Toast toast = Toast.makeText(MainActivity.this,getResources().getString(R.string.rightArchievements), Toast.LENGTH_SHORT);
            toast.show();
        }else{
            //display error
            Toast toast = Toast.makeText(MainActivity.this,getResources().getString(R.string.errorArchievements), Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private void loadScoreOfLeaderBoard(final int scoreToAdd) {
        Games.Leaderboards.loadCurrentPlayerLeaderboardScore(mGoogleApiClient,
                getResources().getString(R.string.leaderboard_ranking),
                LeaderboardVariant.TIME_SPAN_ALL_TIME,
                LeaderboardVariant.COLLECTION_PUBLIC)
                .setResultCallback(new ResultCallback<Leaderboards.LoadPlayerScoreResult>() {
                    @Override
                    public void onResult(final @NonNull Leaderboards.LoadPlayerScoreResult scoreResult) {
                        if (isScoreResultValid(scoreResult,scoreToAdd)) {
                            // here you can get the score like this
                            mPoints = scoreResult.getScore().getRawScore();
                            //update the points on the leaderboard
                            //if the point of the play is less than the MAX
                            if(mPoints+scoreToAdd<99999){
                                Games.Leaderboards.submitScore(mGoogleApiClient,
                                        getResources().getString(R.string.leaderboard_ranking),
                                        mPoints+scoreToAdd);
                            }else{
                                //When the point is more than MAX then submit MAX(99999)
                                Games.Leaderboards.submitScore(mGoogleApiClient,
                                        getResources().getString(R.string.leaderboard_ranking),
                                        99999);
                            }

                        }
                    }
                });
    }

    private boolean isScoreResultValid(final Leaderboards.LoadPlayerScoreResult scoreResult,int scoreToAdd) {
        if (scoreResult.getScore()==null){
            //update the points on the leaderboard
            Games.Leaderboards.submitScore(mGoogleApiClient,
                    getResources().getString(R.string.leaderboard_ranking),
                    scoreToAdd);
        }

        return GamesStatusCodes.STATUS_OK == scoreResult.getStatus().getStatusCode() && scoreResult.getScore() != null;
    }
    /*This Method Create the dialog to warn the person that:
   go back to menu before end the game will lead to lose all the progress of the current game*/
    private void CreateMenuDialog(String title, String message, String textNegative, String textPositive)
    {
        AlertDialog.Builder exitDialog = new AlertDialog.Builder(MainActivity.this);

        exitDialog.setTitle(title);

        exitDialog.setMessage(message);

        //set negative button text and action
        exitDialog.setNegativeButton(textNegative,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Nothing to do for now
                    }
                });

        //set the positive button text and action
        exitDialog.setPositiveButton(textPositive,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Game Finish now leave the room if you are in any room
                        if(mRoomId!=null){
                            leaveRoom();
                        }else{
                            //SinglePlayer requested back to menu
                            getSupportFragmentManager().beginTransaction().replace(R.id.Screens_Layout, menuScreen).commit();
                        }
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    }
                });

        exitDialog.create();
        exitDialog.show();
    }

    public void leaveRoom(){
        //Multiplayer requested back to menu
        Games.RealTimeMultiplayer.leave(mGoogleApiClient, this, mRoomId);
        mPlaying = false;
        mRoomId = null;
    }
    //endregion

    //regionSingleplayer Game Control
    @Override
    public void onSinglePlayerGameFinish(boolean won , int n_Attempts, String opponentNum) {
        //set the bundle to send to the fragment if the player won or lost and the n_Attempts
        Bundle bundle = new Bundle();
        String wonString = String.valueOf(won);
        bundle.putString("won/lost", wonString);
        bundle.putString("typeGame", "single");
        bundle.putString("opponentNum",opponentNum);
        bundle.putString("myNum","");
        bundle.putInt("attempts", n_Attempts);
        wonLostScreen.setArguments(bundle);

        getSupportFragmentManager().beginTransaction().replace(R.id.Screens_Layout, wonLostScreen).commit();
    }

    //endregion

    private void showSimpleMessageDialog(String message, String buttonText){
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, buttonText,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    @Override
    public void endHowToPlay() {
        //backToMenu
        getSupportFragmentManager().beginTransaction().replace(R.id.Screens_Layout, menuScreen).commit();
    }

    @Override
    public void onprivacyPolicyResquested() {
        startActivity(new Intent(Intent.ACTION_VIEW,
        Uri.parse("https://docs.google.com/document/d/1GD6rMUHQcOlI42tFk6dUGiRx27LXw2kSw1dKlyA-amE/edit?usp=sharing")));
    }
}
