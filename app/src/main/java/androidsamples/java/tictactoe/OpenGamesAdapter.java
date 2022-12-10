package androidsamples.java.tictactoe;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

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

public class OpenGamesAdapter extends RecyclerView.Adapter<OpenGamesAdapter.ViewHolder> {
  private static final String TAG = "OpenGamesAdapter";
  private List<Game> openGames;
  private NavController mNavController;
  private DatabaseReference mRef;
  private String openGamePlayerEmail;
  private Context mContext;
  private String matchUUID;

  public OpenGamesAdapter(View view,Context context) {
    // FIXME if needed
    mNavController = Navigation.findNavController(view);
    mRef = FirebaseDatabase.getInstance().getReference("users");
    openGamePlayerEmail = "";
    matchUUID = "";
    mContext = context;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.fragment_item, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
    // TODO bind the item at the given position to the holder
    if(openGames!=null){
      Game game = openGames.get(position);
      matchUUID = game.getUUID();
      holder.mIdView.setText("Game ID - " + game.getUUID());
      mRef.child(game.getPlayer1()).addListenerForSingleValueEvent(
              new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                  Log.d(TAG, "onDataChange ");
                  Player player = snapshot.getValue(Player.class);
                  assert player != null;
                  Log.d(TAG, "getting user email to display in view holder - " + player.getEmail());
                  openGamePlayerEmail = player.getEmail();
                  holder.mContentView.setText(openGamePlayerEmail);
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                  Log.w(TAG, "onCancelled", error.toException());
                }
              }
      );

    }
  }

  @Override
  public int getItemCount() {
    return (openGames == null) ? 0 : openGames.size();
  }

  public void setOpen(List<Game> openGames){
    this.openGames = openGames;
    Log.d(TAG, "openGames: " + openGames.size());
    notifyDataSetChanged();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {
    public final View mView;
    public final TextView mIdView;
    public final TextView mContentView;

    public ViewHolder(View view) {
      super(view);
      mView = view;
      mIdView = view.findViewById(R.id.txt_gameID);
      mContentView = view.findViewById(R.id.txt_playerName);

      mView.setOnClickListener(this::launchGameFragment);
    }

    private void launchGameFragment(View v) {
      Log.d(TAG, "launching the game fragment");
      FirebaseUser currentUser = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser());
      if(Objects.equals(currentUser.getEmail(), openGamePlayerEmail)){
        Toast.makeText(mContext, "Only Another Player can join the game", Toast.LENGTH_SHORT).show();
        return;
      }
      NavDirections gameAction = DashboardFragmentDirections.actionGame("Two-Player",matchUUID);
      mNavController.navigate(gameAction);
    }

    @NonNull
    @Override
    public String toString() {
      return super.toString() + " '" + mContentView.getText() + "'";
    }
  }
}