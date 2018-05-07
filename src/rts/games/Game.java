package rts.games;

import static gui.PhysicalGameStatePanel.COLORSCHEME_BLACK;

import ai.core.AI;
import gui.PhysicalGameStatePanel;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import rts.GameSettings;
import rts.GameState;
import rts.PartiallyObservableGameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;

/**
 * Abstract game base class.
 * Contains the common game functionality that is used by different game types.
 */
public abstract class Game {

  private final int GAME_UPDATE_PERIOD = 20;

  final GameSettings gameSettings;
  final GameState gameState;
  final PhysicalGameState pgs;
  final UnitTypeTable unitTypeTable;
  final List<AI> players;
  JFrame window = null;

  /**
   * Create a new game.
   *
   * @param gameSettings The game settings to use.
   */
  public Game(GameSettings gameSettings) {
    this.gameSettings = gameSettings;
    this.unitTypeTable =
        new UnitTypeTable(gameSettings.getUTTVersion(), gameSettings.getConflictPolicy());
    try {
      this.pgs = PhysicalGameState.load(gameSettings.getMapLocation(), unitTypeTable);
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("Error loading game state");
    }
    this.gameState = new GameState(pgs, unitTypeTable);
    this.players = new ArrayList<>();
  }

  /**
   * Run a single game.
   */
  public final void run() {
    try {
      beforeGame();
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }

    try{
      gameLoop();
    } catch (Exception e) {
      e.printStackTrace();
      return;
    }

    try {
      afterGame();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Initialize a game.
   */
  void beforeGame() throws Exception {
    try {
      Class[] aiClasses = {gameSettings.getAI1(), gameSettings.getAI2()};
      for (Class clazz : aiClasses) {
        players.add(buildAi(clazz));
      }
    } catch (Exception e) {
      throw new IllegalArgumentException(
          String.format("Unable to create AI due to '%s'", e.getMessage()));
    }

    players.forEach(AI::reset);

    if (gameSettings.isRender()) {
      window = PhysicalGameStatePanel.newVisualizer(gameState, 640, 640, gameSettings.isPartiallyObservable(), COLORSCHEME_BLACK);
    }
  }

  /**
   * The main game loop.
   */
  void gameLoop() throws Exception {
    // run game loop
    boolean gameOver = false;
    long nextTimeToUpdate = System.currentTimeMillis() + gameUpdatePeriod();
    do {
      if (System.currentTimeMillis() >= nextTimeToUpdate) {
        takeAction();
        gameOver = gameState.cycle();
        if (gameSettings.isRender()) {
          window.repaint();
        }
        nextTimeToUpdate += GAME_UPDATE_PERIOD;
      } else {
        Thread.sleep(1);
      }
    } while (!gameOver && gameState.getTime() < gameSettings.getMaxCycles());

    for (AI player : players) {
      player.gameOver(gameState.winner());
    }
  }

  /**
   * Every player takes one action.
   */
  void takeAction() throws Exception {
    for (int id = 0; id < players.size(); id++) {
      GameState gs =
          gameSettings.isPartiallyObservable()
              ? new PartiallyObservableGameState(gameState, id)
              : gameState;
      AI player = players.get(id);
      try {
        PlayerAction playerAction = player.getAction(id, gs);
        gameState.issueSafe(playerAction);
      } catch (Exception e) {
        // do nothing (invalid action)
      }
    }
  }

  /**
   * Carry out any clean up needed after a game.
   */
  void afterGame() throws Exception {
    printGameResults();
  }

  /**
   * Build an AI player.
   *
   * @param clazz The class to build.
   * @return An AI player.
   */
  AI buildAi(Class clazz) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
    return (AI) clazz.getConstructor(UnitTypeTable.class).newInstance(unitTypeTable);
  }

  int gameUpdatePeriod() {
    return GAME_UPDATE_PERIOD;
  }

  /**
   * Print the game state.
   */
  void printGameResults() throws Exception {
    PrintWriter writer = new PrintWriter(System.out);
    gameState.toJSON(writer);
    writer.flush();
    writer.close();
  }
}
