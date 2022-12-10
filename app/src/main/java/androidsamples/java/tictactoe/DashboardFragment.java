package androidsamples.java.tictactoe;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

  private static final String TAG = "DashboardFragment";
  private NavController mNavController;
  private FirebaseAuth mAuth;
  private FirebaseDatabase mDatabase;
  private DatabaseReference mRef;
  private OpenGamesAdapter adapter;
  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public DashboardFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.d(TAG, "onCreate");

    setHasOptionsMenu(true); // Needed to display the action menu for this fragment
    mAuth = FirebaseAuth.getInstance();
  }

  @Override
  public View onCreateView(LayoutInflater inflater,
                           ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_dashboard, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    mNavController = Navigation.findNavController(view);

    // TODO if a user is not logged in, go to LoginFragment
    FirebaseUser currentUser = mAuth.getCurrentUser();
    if (currentUser == null) {
      NavDirections loginAction = DashboardFragmentDirections.actionNeedAuth();
      mNavController.navigate(loginAction);
      return;
    }
    Log.d(TAG, "currentUser: " + currentUser);
    TextView username = view.findViewById(R.id.txt_username);
    username.setText("Welcome, " + currentUser.getEmail());

    mDatabase = FirebaseDatabase.getInstance();
    mRef = mDatabase.getReference();

    readAndUpdate(view, currentUser);

    RecyclerView recycleView = view.findViewById(R.id.list);
    recycleView.setLayoutManager(new LinearLayoutManager(getActivity()));
    adapter = new OpenGamesAdapter(view,requireContext());
    recycleView.setAdapter(adapter);
    getOpenMatches();
    // Show a dialog when the user clicks the "new game" button
    view.findViewById(R.id.fab_new_game).setOnClickListener(v -> {
      // A listener for the positive and negative buttons of the dialog
      DialogInterface.OnClickListener listener = (dialog, which) -> {
        String gameType = "No type";
        String gameId = "";
        if (which == DialogInterface.BUTTON_POSITIVE) {
          gameType = getString(R.string.two_player);
        } else if (which == DialogInterface.BUTTON_NEGATIVE) {
          gameType = getString(R.string.one_player);
        }
        Log.d(TAG, "New Game: " + gameType);

        // Passing the game type as a parameter to the action
        // extract it in GameFragment in a type safe way
        NavDirections action = DashboardFragmentDirections.actionGame(gameType,gameId);
        mNavController.navigate(action);
      };

      // create the dialog
      AlertDialog dialog = new AlertDialog.Builder(requireActivity())
              .setCancelable(false)
              .setTitle(R.string.new_game)
              .setMessage(R.string.new_game_dialog_message)
              .setPositiveButton(R.string.two_player, listener)
              .setNegativeButton(R.string.one_player, listener)
              .setNeutralButton(R.string.cancel, (d, which) -> d.dismiss())
              .create();
      dialog.show();
    });
  }

  private void readAndUpdate(@NonNull View view, FirebaseUser currentUser) {
    mRef.child("users").child(currentUser.getUid()).addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
        Player player = dataSnapshot.getValue(Player.class);
        assert player != null;
        updateUI(player, view);
      }

      @Override
      public void onCancelled(DatabaseError databaseError) {
        Log.w(TAG, "onCancelled", databaseError.toException());
      }
    });
  }

  private void getOpenMatches(){
    List<Game> openGames = new ArrayList<>();
    mRef.child("games").addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(@NonNull DataSnapshot snapshot) {
        Log.d(TAG, "onDataChange");
        openGames.clear();
        for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
          // TODO: handle the post
          Game game = dataSnapshot.getValue(Game.class);
          assert game != null;
//          Log.d(TAG,"gameId" + game.getUUID());
          if(game.getOpen()) openGames.add(game);
        }
        adapter.setOpen(openGames);
      }

      @Override
      public void onCancelled(@NonNull DatabaseError error) {
        Log.w(TAG, "onCancelled" + error.getMessage());
      }
    });
  }

  private void updateUI(Player player,@NonNull View v){
    TextView score = v.findViewById(R.id.txt_score);
    score.setText("Wins: " + player.getWins() + ", Losses: " + player.getLosses());
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_logout, menu);
    // this action menu is handled in MainActivity
  }
}