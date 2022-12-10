# Tic Tac Toe
<div align="center">
  <a href="https://github-readme-tech-stack.vercel.app/api/cards?theme=github_dark&line1=java,java,0;firebase,firebase,0;androidstudio,anddroidstudio,0,title=This%20Project%27s%20Tech%20Stack">
    <img  src="https://github-readme-tech-stack.vercel.app/api/cards?theme=github_dark&line1=java,java,0;firebase,firebase,0;androidstudio,androidstudio,0&title=This%20Project%27s%20Tech%20Stack">
  </a>
</div>


# Description 
This is the code for the Tic Tac Toe multiplayer game android app. A user can play with the app logic as a single player or with another user. Any user is created using authentication by email and password. For each user, there's a count of wins and losses and available open games. 

It uses Android Navigation Component, with a single activity and three fragments:

- The DashboardFragment is the home screen. If a user is not logged in, it will navigate to the LoginFragment. 

- The floating button in the dashboard creates a dialog that asks which type of game to create and passes that information to the GameFragment (using SafeArgs).

- Appropriate listeners and game play logic are added in GameFragment

- Pressing the back button in the GameFragment opens a dialog that confirms if the user wants to forfeit the game. 

- A "log out" action bar menu is shown on both the dashboard and the game fragments. Clicking it should log the user out and show the LoginFragment. This click is handled in the MainActivity.

## Known Bugs
- For player 2, there's no warning for waiting for their turn when it's not their turn
- The app crashes when player who creates a two-player game forfeits the game and when the other user clicks back
- On pressing back for forfeit does not update count of losses.

## Screenshots
<img width="252" alt="WhatsApp Image 2022-12-08 at 20 29 55 (1)" src="https://user-images.githubusercontent.com/54110949/206480229-b7aadcd6-1704-4a47-be12-d223c00518fd.jpeg"> <img width="252" alt="WhatsApp Image 2022-12-09 at 17 26 48" src="https://user-images.githubusercontent.com/54110949/206698162-c99f8f5e-227b-4ff4-822e-16085bdd41c8.jpeg"> <img width="252" alt="WhatsApp Image 2022-12-08 at 20 29 55 (3)" src="https://user-images.githubusercontent.com/54110949/206480239-5cd17e6c-04cd-437c-8c0e-9d8a23cb1f9d.jpeg"> <img width="252" alt="WhatsApp Image 2022-12-08 at 20 29 55" src="https://user-images.githubusercontent.com/54110949/206480241-aef79886-d669-4ee6-9205-9c2cae33993f.jpeg"> <img width="252" alt="WhatsApp Image 2022-12-08 at 20 29 56 (1)" src="https://user-images.githubusercontent.com/54110949/206480243-352d7de8-f7a2-4d0c-8e1d-459152e9ff93.jpeg"> <img width="252" alt="WhatsApp Image 2022-12-08 at 20 29 56" src="https://user-images.githubusercontent.com/54110949/206480245-037f5077-2422-4b49-be89-bf7fc9986680.jpeg"> <img width="252" alt="WhatsApp Image 2022-12-08 at 20 29 57 (1)" src="https://user-images.githubusercontent.com/54110949/206480249-300a5ee4-fffc-4384-a1a2-5596f0366e05.jpeg"> <img width="252" alt="WhatsApp Image 2022-12-08 at 20 29 57" src="https://user-images.githubusercontent.com/54110949/206480254-f321b4ea-5cd5-49ca-8ba5-be945202908b.jpeg"> <img width="252" alt="Screenshot 2022-12-08 at 8 27 35 PM" src="https://user-images.githubusercontent.com/54110949/206480282-49ab872e-84f8-41fd-a9ef-7c9527ccfe82.png"> <img width="238" alt="Screenshot 2022-12-08 at 8 28 10 PM" src="https://user-images.githubusercontent.com/54110949/206480291-c08b18d8-2753-47d1-8560-d7ee460dc9ab.png"> <img width="302" alt="Screenshot 2022-12-08 at 8 28 19 PM" src="https://user-images.githubusercontent.com/54110949/206480292-ccab06cc-c531-4a34-8239-745b667b64a4.png"> <img width="259" alt="Screenshot 2022-12-08 at 8 30 00 PM" src="https://user-images.githubusercontent.com/54110949/206480296-a66826f6-676a-4367-99d8-3b424318f960.png"> <img width="244" alt="Screenshot 2022-12-09 at 5 27 43 PM" src="https://user-images.githubusercontent.com/54110949/206697642-2b73ccbe-094c-458b-8996-306c7d68967f.png"> <img width="244" alt="Screenshot 2022-12-08 at 8 30 27 PM" src="https://user-images.githubusercontent.com/54110949/206480311-4edf5fab-3c8c-42b7-baae-e403ccdd5379.png">


# How I completed Tasks

- To make the signin/register user page as the initial fragment, I changed the start destination to `loginFragment` in `nav_graph.xml` :  
```
    app:startDestination="@id/loginFragment">
```
- Firebase Authentication is used for registering and signing in a user. In `LoginFragment.java` : 
In `onCreate()`, a FirebaseAuth instance is created:
```
    mAuth = FirebaseAuth.getInstance();
```
- On clicking the register/sign in button the `createUserWithEmailAndPassword` and `signInWithEmailAndPassword()` are used for signup and signin respectively. 

- On other fragments, the user can log out by clicking the options menu. In `MainActivity.java` following code logs out user
```
    FirebaseAuth.getInstance().signOut();
    NavController mNavController = Navigation.findNavController(this, R.id.nav_host_fragment);
    mNavController.navigate(R.id.loginFragment);
```


- In `LoginFragment.java`, a new user is added to database on sign up:
```
    // add player
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference userRef = database.getReference("users");
    assert currentUser != null;
    Player player = new Player(currentUser.getUid(),0,0, currentUser.getEmail());
    userRef.child(currentUser.getUid()).setValue(player);
```
- There's a `Game` class that stores various parameters like, the game uuid, the current board, if it's singleplayer, etc ->
```
    private String uuid;
    private boolean open;
    private boolean singlePlayer;
    private String player1, player2;
    private Integer turn;
    private String winner;
    private String status;
    private List<String> tictactoe;
```    
- `GameLogic.java` implements the logic for checking who has won, i.e. it checks all the 8 possible combinations of winning situations, and if not won, it is a tie. 
- The user starts first and by default their move is "X"
- In `GameFragment.java`, the player plays against the computer i.e. the app logic, where the computer sets the next available button on tictactoe as "O".
- On winning, the number of wins and losses are updated in the database the dashboard for the user. 

- Game uuid is also stored in the database, and it is fetched through safe args : 
```
    gameUUID = args.getGameID();
```
- Then it is checked if it's empty, that is the user who creates it will be player1 and vice-versa
```
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
```    
```
  private Game createGame() {
    boolean open = !singlePlayer;
    String player1 = currentUser.getUid();
    String player2 = singlePlayer ? getString(R.string.computer) : getString(R.string.waiting);
    String status = singlePlayer ? getString(R.string.status_started) : getString(R.string.status_waiting);
    String winner = getString(R.string.undecided);
    Integer turn = 1;
    return new Game(open, singlePlayer, player1,player2,status,winner,turn);
  }
```
- Once the other user has joined, a listener was needed to check for when a user joined an ongoing game, and to check when the data was being changed on database. 
```
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
```
- For checking who has won, I've checked all the possible combinations in which players can win
- The database has 2 paths, one for game and other for users
- The game model stores gameID, isOpen, player1ID, player2ID, isSinglePlayer, status, board's status, whose turn, and who won
- The user model stores userID, wins, losses and their email (for displaying "Welcome, <email>")

# How to run/host app

Install the app via running the app directly through Android Studio. Authentication and database hosted through Firebase. 

# Testing and Accessibility
## Testing

- Manually tested by different users. 
- For monkey stress testing : Ran the monkey tool successfully for 10000 iterations using the command `adb -s a470ca26 shell monkey -p androidsamples.java.tictactoe -v 10000`


<img width="500" alt="Screenshot 2022-12-07 at 11 11 13 PM" src="https://user-images.githubusercontent.com/54110949/206254104-2f041f92-3aca-488f-a356-0d289b7b66b4.png">


## Talkback 
It was fairly smooth to navigate througout the application, where the assitant talks everything on the screen aloud, like the wins, losses, whether to create a single-player or multi-player game. 

## Accessibilty
It gives suggestions like `Multiple Descriptions`, `Text contrast` for "OK", "ONE-PLAYER" in dialog box, `Image contrast` for + button, 
![WhatsApp Image 2022-12-08 at 21 13 33](https://user-images.githubusercontent.com/54110949/206491382-52795baf-5fd6-4c39-a79b-1a9b75cf25a4.jpeg) ![WhatsApp Image 2022-12-08 at 21 13 35](https://user-images.githubusercontent.com/54110949/206491394-3d797bdb-c73d-4f7e-a37e-56c8b3cb2879.jpeg) ![WhatsApp Image 2022-12-08 at 21 13 30](https://user-images.githubusercontent.com/54110949/206491365-f430cbf6-73a6-491f-a62b-aa6ffc719515.jpeg)

# Hours taken

Took around 48 hours to complete this project. 


