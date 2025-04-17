
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class lab5 {

    public static void clearScreen() {
        try {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    public static int checkInput(String inputChar) {
        if (inputChar.length() == 0) {
            return -1;
        }

        if (inputChar.equals("pass")) {
            return 0;
        }

        boolean hasCharacter = false;
        for (int i = 0; i < inputChar.length(); i++) {
            if (Character.isLetter(inputChar.charAt(i))) {
                hasCharacter = true;
                break;
            } else if (Character.isDigit(inputChar.charAt(i))) {
            } else {
                return -1;
            }
        }

        if (hasCharacter) {
            if (inputChar.length() != 2) {
                return -1;
            }

            if (inputChar.charAt(0) < 'A' || inputChar.charAt(0) > 'H' || inputChar.charAt(1) < '1'
                    || inputChar.charAt(1) > '8') {
                return -1;
            }

            return 0;
        } else {
            int index = Integer.parseInt(inputChar);
            if (index > currentGames.size() || index <= 0) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    public static List<Game> currentGames = new ArrayList<>();
    public static int activeGameIndex = 0;
    public static String[] playerNames = { "Zhangsan", "Lisi" };

    public static void main(String[] args) {
        currentGames.add(new PeaceGame(currentGames.size(), playerNames));
        currentGames.add(new ReversiGame(currentGames.size(), playerNames));
        currentGames.add(new GomokuGame(currentGames.size(), playerNames));

        Scanner input = new Scanner(System.in);
        while (true) {
            Game currentGame = currentGames.get(activeGameIndex);
            String currentPlayerName = playerNames[currentGame.currentPlayer];
            clearScreen();

            List<String> lines = currentGame.render();
            for (int i = 0; i < Math.max(3, currentGames.size() + 1); i++) {
                if (i + 3 >= lines.size()) {
                    lines.add(String.format("%-17s", ""));
                }

                if (i == 0) {
                    lines.set(i + 3, lines.get(i + 3) + String.format("%-20s", "Game List"));
                } else if (i > 3) {
                    lines.set(i + 3, lines.get(i + 3) + String.format("%-29s", ""));
                }

                if (i > 0 && i <= currentGames.size()) {
                    lines.set(i + 3, lines.get(i + 3)
                            + String.format("%-29s",
                                    String.format("%d.  %s", i, currentGames.get(i - 1).getModeName())));
                }
            }

            for (String str : lines) {
                System.out.println(str);
            }

            if (currentGame.ended) {
                currentGame.onEnd();
                System.out.println(String.format(
                        "请输入游戏编号(1-%d) / 新游戏类型(peace/reversi/gomoku) / 退出程序(quit): ", currentGames.size()));
            } else {
                if (!currentGame.currentRoundCanPlace) {
                    if (currentGame.countPlacable((currentGame.currentPlayer + 1) % 2) > 0) {
                        System.out.println(String.format("玩家[%s]无棋可以下 请执行pass ",
                                currentPlayerName));
                        System.out.println(String.format(
                                "请玩家[%s]输入落子位置(A1) / 游戏编号(1-%d) / 新游戏类型(peace/reversi/gomoku) / 放弃行棋(pass) / 退出程序(quit): ",
                                currentPlayerName, currentGames.size()));
                    } else {
                        currentGame.onEnd();
                        if (currentGames.size() <= 1) {
                            break;
                        } else {
                            System.out.println("游戏结束，请按回车继续");
                            input.nextLine();
                            continue;
                        }
                    }
                } else {
                    System.out.println(
                            String.format("请玩家[%s]输入落子位置(A1) / 游戏编号(1-%d) / 新游戏类型(peace/reversi/gomoku) / 退出程序(quit): ",
                                    currentPlayerName, currentGames.size()));
                }
            }

            String inputString = input.nextLine();
            if (inputString.equals("quit")) {
                break;
            } else if (inputString.equals("peace")) {
                currentGames.add(new PeaceGame(currentGames.size(), playerNames));
                continue;
            } else if (inputString.equals("reversi")) {
                currentGames.add(new ReversiGame(currentGames.size(), playerNames));
                continue;
            } else if (inputString.equals("gomoku")) {
                currentGames.add(new GomokuGame(currentGames.size(), playerNames));
                continue;
            }

            // 先检测输入是否合法，是否需要切换棋盘
            inputString = inputString.toUpperCase();
            int result = checkInput(inputString);
            if (result == -1) {
                System.out.println("输入错误，请按回车继续");
                input.nextLine();
                continue;
            }
            char[] inputChar = inputString.toCharArray();
            if (result == 1) {
                // 切换棋盘
                activeGameIndex = Integer.parseInt(inputString) - 1;
            } else if (result == 0) {
                if (currentGame.ended) {
                    System.out.println("输入错误，请按回车继续");
                    input.nextLine();
                    continue;
                }

                if (!currentGame.currentRoundCanPlace) {
                    if (inputString.equals("PASS")) {
                        currentGame.currentPlayer = (currentGame.currentPlayer + 1) % 2;
                        continue;
                    } else {
                        System.out.println("必须跳过行棋，请按任意键继续");
                        input.nextLine();
                        continue;
                    }
                }

                // 落子，需要检查当前位置是否存在棋子
                int y = (int) (inputChar[0] - 'A');
                int x = (int) (inputChar[1] - '1');
                if (!currentGame.canPlaceChess(x, y, currentGame.currentPlayer)) {
                    System.out.println("当前位置不允许摆放棋子，请按回车继续");
                    input.nextLine();
                } else {
                    currentGame.placeChessNoCheck(x, y);
                    currentGame.step();
                }
            }
        }

        input.close();
    }
}

abstract class Game {

    static final char[] boardElements = { '·', '○', '●' };

    int[][] board = new int[8][8];

    public int currentPlayer;
    public int numRounds;
    public int gameId;
    public String[] playerNames;
    public boolean ended = false, currentRoundCanPlace = true;

    public List<String> render() {
        List<String> lines = new ArrayList<>();

        lines.add("  A B C D E F G H");
        for (int i = 0; i < 8; i++) {
            StringBuilder builder = new StringBuilder();

            builder.append((i + 1) + " ");
            for (int j = 0; j < 8; j++) {
                if (j < 7) {
                    builder.append(boardElements[board[i][j]] + " ");
                } else {
                    builder.append(boardElements[board[i][j]]);
                }
            }

            lines.add(builder.toString());
        }

        return lines;
    }

    public void onEnd() {

    }

    public abstract String getModeName();

    public Game() {
        currentPlayer = 0;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = 0;
            }
        }
        board[3][3] = 1;
        board[4][4] = 1;
        board[4][3] = 2;
        board[3][4] = 2;
    }

    public boolean canPlaceChess(int x, int y, int player) {
        return board[x][y] == 0;
    }

    public int countPlacable(int playerId) {
        int numPlacablePositions = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] == 0 && canPlaceChess(i, j, playerId)) {
                    numPlacablePositions++;
                }
            }
        }

        return numPlacablePositions;
    }

    public void placeChess(int x, int y) throws Exception {
        if (!canPlaceChess(x, y, currentPlayer)) {
            throw new Exception("Cannot place piece on occupied square");
        }
        placeChessNoCheck(x, y);
    }

    public void placeChessNoCheck(int x, int y) {
        board[x][y] = (currentPlayer == 0) ? 1 : 2;
    }

    public void step() {
        currentPlayer = (currentPlayer + 1) % 2;
        numRounds++;

        currentRoundCanPlace = countPlacable(currentPlayer) > 0;
        if (!currentRoundCanPlace && countPlacable((currentPlayer + 1) % 2) == 0) {
            ended = true;
        }
    }
}

class PeaceGame extends Game {

    public PeaceGame(int gameId, String[] playerNames) {
        super();
        this.gameId = gameId;
        this.playerNames = playerNames;
    }

    @Override
    public void onEnd() {
        System.out.println("游戏结束");
    }

    @Override
    public String getModeName() {
        return "peace";
    }

    @Override
    public List<String> render() {
        List<String> lines = super.render();
        lines.set(3, lines.get(3)
                + String.format("%-4s%-25s", "", "Game " + (gameId + 1)));
        for (int i = 4; i <= 5; i++) {
            char displayChar = ' ';
            if (currentPlayer == i - 4) {
                displayChar = boardElements[i - 3];
            }
            lines.set(i, lines.get(i)
                    + String.format("%-4s%-20s%-5c", "", String.format("Player%d [%s]", i - 3, playerNames[i - 4]),
                            displayChar));
        }

        lines.set(6, lines.get(6) + String.format("%-29s", ""));

        return lines;
    }
}

class ReversiGame extends Game {
    record Coord(int x, int y) {
    };

    final static Coord[] directions = { new Coord(-1, 0), new Coord(1, 0), new Coord(0, -1), new Coord(0, 1),
            new Coord(-1, -1), new Coord(1, 1), new Coord(1, -1), new Coord(-1, 1) };
    public int[] playerScores = new int[2];

    public ReversiGame(int gameId, String[] playerNames) {
        super();
        this.gameId = gameId;
        this.playerNames = playerNames;
        playerScores[0] = playerScores[1] = 0;
    }

    @Override
    public void onEnd() {
        int winner = playerScores[0] > playerScores[1] ? 0 : 1;
        System.out.println(String.format("玩家[%s]得分：", playerNames[0]) + playerScores[0]);
        System.out.println(String.format("玩家[%s]得分：", playerNames[1]) + playerScores[1]);
        System.out.println(String.format("游戏结束，[%s]获胜！", playerNames[winner]));
    }

    @Override
    public String getModeName() {
        return "reversi";
    }

    @Override
    public List<String> render() {
        List<String> lines = new ArrayList<>();
        playerScores[0] = playerScores[1] = 0;

        lines.add("  A B C D E F G H");
        for (int i = 0; i < 8; i++) {
            StringBuilder builder = new StringBuilder();

            builder.append((i + 1) + " ");
            for (int j = 0; j < 8; j++) {
                char displayChar = boardElements[board[i][j]];
                if (board[i][j] > 0) {
                    playerScores[board[i][j] - 1]++;
                }

                if (board[i][j] == 0 && canPlaceChess(i, j, currentPlayer)) {
                    displayChar = '+';
                }

                builder.append(displayChar);
                if (j < 7) {
                    builder.append(" ");
                }
            }

            lines.add(builder.toString());
        }

        lines.set(3, lines.get(3)
                + String.format("%-4s%-25s", "", "Game " + (gameId + 1)));
        for (int i = 4; i <= 5; i++) {
            char displayChar = ' ';
            if (currentPlayer == i - 4) {
                displayChar = boardElements[i - 3];
            }
            lines.set(i, lines.get(i)
                    + String.format("%-4s%-20s%c %-3d", "", String.format("Player%d [%s]", i - 3, playerNames[i - 4]),
                            displayChar, playerScores[i - 4]));
        }

        lines.set(6, lines.get(6) + String.format("%-29s", ""));

        return lines;
    }

    private boolean findReversiblePieces(int x, int y, int playerId, List<Coord> reversiblePieces) {
        List<Coord> reversedPieces = new ArrayList<>();
        int numReversiblePieces = 0;

        for (Coord direction : directions) {
            boolean canReverse = false;
            int newX = x + direction.x;
            int newY = y + direction.y;
            while (true) {
                if (newX >= 0 && newY >= 0 && newX < 8 && newY < 8 && board[newX][newY] != 0) {
                    if (board[newX][newY] == playerId + 1) {
                        canReverse = true;
                        break;
                    } else {
                        reversedPieces.add(new Coord(newX, newY));
                    }
                } else {
                    break;
                }

                newX += direction.x;
                newY += direction.y;
            }

            if (canReverse) {
                numReversiblePieces += reversedPieces.size();
                reversiblePieces.addAll(reversedPieces);
            }

            reversedPieces.clear();
        }

        return numReversiblePieces > 0;
    }

    @Override
    public void placeChessNoCheck(int x, int y) {
        board[x][y] = (currentPlayer == 0) ? 1 : 2;

        List<Coord> reversedPieces = new ArrayList<>();
        findReversiblePieces(x, y, currentPlayer, reversedPieces);

        for (Coord piece : reversedPieces) {
            board[piece.x][piece.y] = currentPlayer + 1;
        }
    }

    @Override
    public boolean canPlaceChess(int x, int y, int playerId) {
        if (!super.canPlaceChess(x, y, playerId)) {
            return false;
        }

        return findReversiblePieces(x, y, playerId, new ArrayList<>());
    }
}

class GomokuGame extends Game {
    record Coord(int x, int y) {
    };

    final static Coord[] directions = { new Coord(-1, 0), new Coord(1, 0), new Coord(0, -1), new Coord(0, 1),
            new Coord(-1, -1), new Coord(1, 1), new Coord(1, -1), new Coord(-1, 1) };

    public GomokuGame(int gameId, String[] playerNames) {
        currentPlayer = 0;
        this.gameId = gameId;
        this.playerNames = playerNames;
        this.numRounds = 1;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                board[i][j] = 0;
            }
        }
    }

    public int winner = -1;

    @Override
    public void onEnd() {
        if(winner<0){
            System.out.println("游戏结束，棋盘已满，平局！");
        }else{
            System.out.println(String.format("游戏结束，[%s]获胜！", playerNames[winner]));
        }
    }

    @Override
    public List<String> render() {
        List<String> lines = super.render();

        lines.set(3, lines.get(3)
                + String.format("%-4s%-25s", "", "Game " + (gameId + 1)));
        for (int i = 4; i <= 5; i++) {
            char displayChar = ' ';
            if (currentPlayer == i - 4) {
                displayChar = boardElements[i - 3];
            }
            lines.set(i, lines.get(i)
                    + String.format("%-4s%-20s%c %-3s", "", String.format("Player%d [%s]", i - 3, playerNames[i - 4]),
                            displayChar, ""));
        }
        lines.set(6, lines.get(6)
                + String.format("%-4s%-25s", "", "Current round: " + numRounds));

        return lines;
    }

    @Override
    public String getModeName() {
        return "gomoku";
    }

    private boolean traceDirections(int x, int y) {
        int pieceColor = board[x][y];

        if (pieceColor == 0) {
            return false;
        }

        for (Coord direction : directions) {
            int pieceCount = 1;
            int newX = x, newY = y;
            while (true) {
                newX += direction.x;
                newY += direction.y;
                if (newX >= 0 && newY >= 0 && newX < 8 && newY < 8 && board[newX][newY] == pieceColor) {
                    pieceCount++;
                } else {
                    break;
                }
            }

            if (pieceCount >= 5) {
                winner = pieceColor - 1;
                return true;
            }
        }

        return false;
    }

    private boolean checkWin() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (traceDirections(i, j)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void step() {
        if (checkWin() || countPlacable(currentPlayer) == 0) {
            ended = true;
            return;
        }

        currentPlayer = (currentPlayer + 1) % 2;
        numRounds++;
    }
}
