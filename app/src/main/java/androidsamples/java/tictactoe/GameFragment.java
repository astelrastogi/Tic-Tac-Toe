package androidsamples.java.tictactoe;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GameFragment extends Fragment {
  private static final String TAG = "GameFragment";
  private static final int GRID_SIZE = 9;
  private final Button[] mButtons = new Button[GRID_SIZE];
  private NavController mNavController;
  private FirebaseUser currentUser;
  private FirebaseDatabase mDatabase;
  private boolean singlePlayer;
  private String gameUUID;
  DatabaseReference gameRef,userRef;
  private GameLogic mLogic;
  private Integer playerWins,playerLosses;
  private TextView gamePlayer1,gamePlayer2;
  private Integer activeColour,inactiveColour;
  private boolean back;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true); // Needed to display the action menu for this fragment
    mDatabase = FirebaseDatabase.getInstance();
    currentUser = FirebaseAuth.getInstance().getCurrentUser();
    gameRef = mDatabase.getReference("games");
    userRef = mDatabase.getReference("users");
    back =  false;

    // setting active player blue
    TypedValue typedValue1 = new TypedValue();
    requireActivity().getTheme().resolveAttribute(R.attr.colorPrimary, typedValue1, true);
    activeColour = ContextCompat.getColor(requireContext(), typedValue1.resourceId);

    TypedValue typedValue2 = new TypedValue();
    requireActivity().getTheme().resolveAttribute(R.attr.colorOnBackground, typedValue2, true);
    inactiveColour = ContextCompat.getColor(requireContext(), typedValue2.resourceId);

    // Extract the arguments (Game Type and Game ID) passed with the action in a type-safe way
    GameFragmentArgs args = GameFragmentArgs.fromBundle(getArguments());

    // Game Type
    Log.d(TAG, "New game type = " + args.getGameType());
    if(args.getGameType().equals("One-Player")){
      singlePlayer = true;
    } else if(args.getGameType().equals("Two-Player")){
      singlePlayer = false;
    }
    if(singlePlayer) {
      Log.d(TAG, "This is a single player game");
    }
    else Log.d(TAG, "This is a multi player game");

    // Game ID
    gameUUID = args.getGameID();

    if(gameUUID.isEmpty()) {
      Game game = createGame();
      mLogic = new GameLogic(game);
      mLogic.setTurn = 1;
      gameRef.child(game.getUUID()).setValue(game);
      Log.d(TAG, "Player1: " + currentUser.getEmail() + " has created a " + args.getGameType() + " game : " + gameUUID);
    } else {
      mLogic = new GameLogic(gameUUID, singlePlayer);
      mLogic.setTurn = 2;
      setTwoPlayerMatch(gameUUID);
      Log.d(TAG, "Player2: " + currentUser.getEmail() + " has joined game : " + gameUUID);
      Toast.makeText(requireContext(),"Joined game as Player 2", Toast.LENGTH_SHORT).show();
    }

    // Handle the back press by adding a confirmation dialog
    OnBackPressedCallback callback = new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {
        Log.d(TAG, "Back pressed");

        // TODO show dialog only when the game is still in progress
        AlertDialog dialog = new AlertDialog.Builder(requireActivity())
                .setTitle(R.string.confirm)
                .setMessage(R.string.forfeit_game_dialog_message)
                .setPositiveButton(R.string.yes, (d, which) -> {
                  // TODO update loss count
                  //endGameAsQuit();
                  mNavController.popBackStack();
                })
                .setNegativeButton(R.string.cancel, (d, which) -> d.dismiss())
                .create();
        dialog.show();
      }
    };
    requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
  }

  @NonNull
  private Game createGame() {
    boolean open = !singlePlayer;
    String player1 = currentUser.getUid();
    String player2 = singlePlayer ? getString(R.string.computer) : getString(R.string.waiting);
    String status = singlePlayer ? getString(R.string.status_started) : getString(R.string.status_waiting);
    String winner = getString(R.string.undecided);
    Integer turn = 1;
    return new Game(open, singlePlayer, player1,player2,status,winner,turn);
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_game, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mNavController = Navigation.findNavController(view);

    mButtons[0] = view.findViewById(R.id.button0);
    mButtons[1] = view.findViewById(R.id.button1);
    mButtons[2] = view.findViewById(R.id.button2);

    mButtons[3] = view.findViewById(R.id.button3);
    mButtons[4] = view.findViewById(R.id.button4);
    mButtons[5] = view.findViewById(R.id.button5);

    mButtons[6] = view.findViewById(R.id.button6);
    mButtons[7] = view.findViewById(R.id.button7);
    mButtons[8] = view.findViewById(R.id.button8);

    gamePlayer1 = view.findViewById(R.id.player1);
    gamePlayer2 = view.findViewById(R.id.player2);

    for (int i = 0; i < mButtons.length; i++) {
      int finalI = i;
      mButtons[i].setOnClickListener(v -> {
        Log.d(TAG, "Button " + finalI + " clicked");
        // TODO implement listeners
        startMove(finalI);
      });
    }

    if(!singlePlayer && mLogic.setTurn == 1){
      notifyPlayer1ThatPlayer2Joined();
    }

    readDBWhilePlayingGame();

    readDBforGameStatus();
  }

  private void setTwoPlayerMatch(String gameUUID){
    Log.d(TAG, "setTwoPlayerMatch is called");
    Log.d(TAG, "gameUUID is " + gameUUID);

    updateFieldsOnPlayerJoin(currentUser.getUid());
    gameRef.child(gameUUID).child("status").setValue(mLogic.status);
    gameRef.child(gameUUID).child("open").setValue(false);
    gameRef.child(gameUUID).child("player2").setValue(mLogic.player2);
    gameRef.child(gameUUID).addListenerForSingleValueEvent(
            new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange in setTwoPlayerMatch");
                Game localGame = snapshot.getValue(Game.class);
                assert localGame != null;
                mLogic.player1 = localGame.getPlayer1();
                mLogic.turn = localGame.getTurn();
                mLogic.tictactoe = localGame.getTictactoe();
                mLogic.winner = localGame.getWinner();
              }
              @Override
              public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "onCancelled", error.toException());
              }
            }
    );
  }

  private void notifyPlayer1ThatPlayer2Joined() {
    gameRef.child(mLogic.gameUUID).child("player2").addValueEventListener(new ValueEventListener() {

      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        if(snapshot.getValue() == null) return;
        updateFieldsOnPlayerJoin(snapshot.getValue().toString());
        updateUI();
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {
        Log.w(TAG, "onCancelled", error.toException());
      }
    });
  }

  private void readDBWhilePlayingGame(){
    gameRef.child(mLogic.gameUUID).addValueEventListener(new ValueEventListener() {

      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        Game localGame = (Game) snapshot.getValue(Game.class);
        if(localGame == null) return;
        mLogic.tictactoe = localGame.getTictactoe();
        mLogic.turn = localGame.getTurn();
        updateUI();
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {
        Log.w(TAG, "onCancelled", error.toException());
      }
    });
  }

  private void readDBforGameStatus() {
    gameRef.child(mLogic.gameUUID).addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        Game localGame = (Game) snapshot.getValue(Game.class);
        if(localGame == null) return;
        mLogic.winner = localGame.getWinner();
        mLogic.status = localGame.getStatus();

        if(mLogic.status.equals(getString(R.string.status_finished))){
          String messageToDisplay = "";
          String titleToDisplay = "";
          int result = 0;
          if(mLogic.winner.equals(getString(R.string.draw))){
            messageToDisplay = "It's a draw!";
            titleToDisplay = "Draw";
            result = 3;
          } else if(mLogic.winner.equals("Player1")){
            result = 1;
            if(mLogic.setTurn == 1){
              messageToDisplay = "Congratulations!";
              titleToDisplay = "You've Won!";
            }
            else{
              messageToDisplay = "Sorry!";
              titleToDisplay = "Ono :(";
            }
          } else {
            result = 2;
            if(mLogic.setTurn == 2){
              messageToDisplay = "Congratulations!";
              titleToDisplay = "You've Won!";
            }
            else {
              messageToDisplay = "Sorry!";
              titleToDisplay = "Ono :(";
            }
          }

          addSingleEventListener(currentUser.getUid(), result);
          showDialogAndClose(messageToDisplay, titleToDisplay);
        }
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {
        Log.w(TAG, "onCancelled", error.toException());
      }
    });
  }

  private void updateFieldsOnPlayerJoin(String player2Id) {
    mLogic.status = getString(R.string.status_started);
    mLogic.open = false;
    mLogic.player2 = player2Id;
  }

  private void startMove(Integer index){
    if(mLogic.status.equals(getString(R.string.status_waiting))){
      Toast.makeText(requireContext(), "Waiting for other player to join..", Toast.LENGTH_SHORT).show();
      return;
    }
    if(!Objects.equals(mLogic.turn, mLogic.setTurn)) {
      Toast.makeText(requireContext(), "Please wait for your turn", Toast.LENGTH_SHORT).show();
      return;
    }
    if(mLogic.play(index,mLogic.setTurn)) {
      mLogic.turn = ((mLogic.turn) % 2) + 1;
      gameRef.child(mLogic.gameUUID).child("tictactoe").setValue(mLogic.tictactoe);
      gameRef.child(mLogic.gameUUID).child("turn").setValue(mLogic.turn);
      updateUI();

      if(mLogic.checkResult() == 1) {
        Log.d(TAG, "Player 1 won");
        updateDbEntryOnGameEnd("Player1");
      }

      if(mLogic.checkResult() == 3) {
        Log.d(TAG, "Draw");
        updateDbEntryOnGameEnd(getString(R.string.draw));
      }

      if(singlePlayer && mLogic.checkResult() == 0) {
        mLogic.getComputerMove();
        mLogic.turn = ((mLogic.turn) % 2) + 1;
        gameRef.child(mLogic.gameUUID).child("tictactoe").setValue(mLogic.tictactoe);
        gameRef.child(mLogic.gameUUID).child("turn").setValue(mLogic.turn);
        updateUI();
        if(mLogic.checkResult() == 2) {
          Log.d(TAG, "Computer Won");
          updateDbEntryOnGameEnd(getString(R.string.computer));
        }
      } else if(!singlePlayer && mLogic.checkResult() == 2) {
        Log.d(TAG, "Player 2 won");
        updateDbEntryOnGameEnd("Player2");
      }
    } else {
      Toast.makeText(requireContext(), "Please select a different cell", Toast.LENGTH_SHORT).show();
      return;
    }

  }

  private void updateDbEntryOnGameEnd(String winner) {
    mLogic.status = getString(R.string.status_finished);
    mLogic.winner = winner;
    Game newObj = new Game(mLogic.open, mLogic.singlePlayer, mLogic.player1, mLogic.player2, mLogic.status, mLogic.winner, mLogic.turn,mLogic.tictactoe);
    gameRef.child(mLogic.gameUUID).setValue(newObj);
  }

  private void updateDbEntryOnGameQuit(String winner) {
    mLogic.status = getString(R.string.status_finished);
    mLogic.winner = winner;
    Game newObj = new Game(mLogic.open, mLogic.singlePlayer, mLogic.player1, mLogic.player2, mLogic.status, mLogic.winner, mLogic.turn,mLogic.tictactoe);
    gameRef.child(mLogic.gameUUID).setValue(newObj);
  }

  private void updateUI(){
    for(int i=0 ;i < GRID_SIZE; i++){
      mButtons[i].setText(mLogic.tictactoe.get(i));
    }
    gamePlayer1.setText("Player 1");

    String player2display = "";
    if(mLogic.player2.equals(getActivity().getString(R.string.computer)) || mLogic.player2.equals(getActivity().getString(R.string.waiting))){
      player2display = mLogic.player2;
    }else player2display = "Player 2";

    gamePlayer2.setText(player2display);

    if(mLogic.turn == 1){
      gamePlayer1.setTextColor(activeColour);
      gamePlayer2.setTextColor(inactiveColour);
    }else  {
      gamePlayer1.setTextColor(inactiveColour);
      gamePlayer2.setTextColor(activeColour);
    }
  }

  private void showDialogAndClose(String messageToDisplay, String titleToDisplay) {
    AlertDialog dialog = new AlertDialog.Builder(requireActivity())
            .setCancelable(false)
            .setTitle(titleToDisplay)
            .setMessage(messageToDisplay)
            .setPositiveButton("OK", (d, which) -> {
              // TODO update loss count
              if(!back) mNavController.navigate(R.id.dashboardFragment);
            })
            .create();
    dialog.show();
  }

  private void updateWinsAndLosses(String playerId, int result) {
    if(result == mLogic.setTurn) {
      userRef.child(playerId).child("wins").setValue(playerWins + 1);
    } else if(result != 3) {
      userRef.child(playerId).child("losses").setValue(playerLosses + 1);
    }
  }

  private void addSingleEventListener(String playerId,int result) {
    Log.d(TAG, "addSingleEventListener is called");
    Log.d(TAG , "playerId: " + playerId);
    userRef.child(playerId).addListenerForSingleValueEvent(
            new ValueEventListener() {
              @Override
              public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "onDataChange ");
                Player player = snapshot.getValue(Player.class);
                assert player != null;
                Log.d(TAG, "getting wins and losses for " + player.toString());
                playerWins = player.getWins();
                playerLosses = player.getLosses();
                updateWinsAndLosses(playerId, result);
              }
              @Override
              public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "onCancelled", error.toException());
              }
            }
    );
  }

//  private void endGameAsQuit(){
//    back = true;
//    if(mLogic.status.equals(getString(R.string.status_waiting))){
//      updateDbEntryOnGameEnd("Draw");
//    }else if(mLogic.setTurn == 1){
//      updateDbEntryOnGameEnd("Player2");
//    }else updateDbEntryOnGameEnd("Player1");
//  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_logout, menu);
    // this action menu is handled in MainActivity
  }
}