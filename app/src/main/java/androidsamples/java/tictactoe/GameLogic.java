package androidsamples.java.tictactoe;

import java.util.List;

public class GameLogic {
    private static final String TAG = "GameLogic";

    public boolean singlePlayer;
    public boolean open;
    public List<String> tictactoe;
    public String status;
    public String gameUUID;
    public String player1;
    public String player2;
    public String winner;
    public Integer turn;
    public Integer setTurn;

    GameLogic(String gameUUID,boolean singlePlayer){
        this.gameUUID = gameUUID;
        this.singlePlayer = singlePlayer;
    }

    GameLogic(Game game) {
        open = game.getOpen();
        singlePlayer = game.getSinglePlayer();
        winner = game.getWinner();
        status = game.getStatus();
        gameUUID = game.getUUID();
        player1 = game.getPlayer1();
        player2 = game.getPlayer2();
        turn = game.getTurn();
        tictactoe = game.getTictactoe();
    }

    public boolean play(Integer index,Integer player){
        String symbol = player == 1 ? "X" : "O";

        if(tictactoe.get(index).equals("")){
            tictactoe.set(index, symbol);
            return true;
        }
        return false;
    }

    public int checkResult(){
        String winChar = "";
        boolean isFilled = true;
        for(int i = 0; i <9; i++){
            isFilled = !tictactoe.get(i).equals("");
            if (!isFilled) break;
        }

        if  (tictactoe.get(0).equals(tictactoe.get(1)) && tictactoe.get(1).equals(tictactoe.get(2)) && !tictactoe.get(0).isEmpty()) winChar = tictactoe.get(0);
        else if (tictactoe.get(3).equals(tictactoe.get(4)) && tictactoe.get(4).equals(tictactoe.get(5)) && !tictactoe.get(3).isEmpty()) winChar = tictactoe.get(3);
        else if (tictactoe.get(6).equals(tictactoe.get(7)) && tictactoe.get(7).equals(tictactoe.get(8)) && !tictactoe.get(6).isEmpty()) winChar = tictactoe.get(6);
        else if (tictactoe.get(0).equals(tictactoe.get(3)) && tictactoe.get(3).equals(tictactoe.get(6)) && !tictactoe.get(0).isEmpty()) winChar = tictactoe.get(0);
        else if (tictactoe.get(4).equals(tictactoe.get(1)) && tictactoe.get(1).equals(tictactoe.get(7)) && !tictactoe.get(1).isEmpty()) winChar = tictactoe.get(1);
        else if (tictactoe.get(2).equals(tictactoe.get(5)) && tictactoe.get(5).equals(tictactoe.get(8)) && !tictactoe.get(2).isEmpty()) winChar = tictactoe.get(2);
        else if (tictactoe.get(0).equals(tictactoe.get(4)) && tictactoe.get(4).equals(tictactoe.get(8)) && !tictactoe.get(0).isEmpty()) winChar = tictactoe.get(0);
        else if (tictactoe.get(6).equals(tictactoe.get(4)) && tictactoe.get(4).equals(tictactoe.get(2)) && !tictactoe.get(2).isEmpty()) winChar = tictactoe.get(2);
        else if(isFilled) return 3;
        else return 0;

        return (winChar.equals("X")) ? 1 : 2;
    }

    public void getComputerMove(){
        for(int i=0;i<9;i++){
            if(tictactoe.get(i).equals("")){
                tictactoe.set(i,"O");
                return;
            }
        }
    }
}
