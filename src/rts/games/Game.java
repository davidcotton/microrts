package rts.games;

import static gui.PhysicalGameStatePanel.COLORSCHEME_BLACK;

import ai.core.AI;
import ai.core.AIWithComputationBudget;
import ai.core.ContinuingAI;
import ai.core.InterruptibleAI;
import ai.core.PseudoContinuingAI;
import gui.PhysicalGameStatePanel;
import java.io.PrintWriter;
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

  final GameSettings gameSettings;
  final UnitTypeTable unitTypeTable;
  GameState gameState;
  PhysicalGameState pgs;
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
      pgs = PhysicalGameState.load(gameSettings.getMapLocation(), unitTypeTable);
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
   * Run a series of games.
   */
  public final void run() {
    try {
      initialize();
      while (true) {
        beforeGame();
        gameLoop();
        afterGame();
      }
    } catch (StopGameException e) {
      // do nothing
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Initialize a series of games.
   */
  void initialize() throws Exception {
    try {
      Class[] aiClasses = {gameSettings.getAI1(), gameSettings.getAI2()};
      for (Class clazz : aiClasses) {
        players.add(buildAi(clazz));
      }
    } catch (Exception e) {
      throw new IllegalArgumentException(
          String.format("Unable to create AI due to '%s'", e.getMessage()));
    }
  }

  /**
   * Initialize a single game.
   */
  void beforeGame() throws Exception {
    pgs = PhysicalGameState.load(gameSettings.getMapLocation(), unitTypeTable);
    gameState = new GameState(pgs, unitTypeTable);
    if (gameSettings.isRender()) {
      if (window != null) {
        // get rid of old window
        window.dispose();
      }
      window = PhysicalGameStatePanel.newVisualizer(gameState, 640, 640, gameSettings.isPartiallyObservable(), COLORSCHEME_BLACK);
    }
    players.forEach(AI::reset);
  }

  /**
   * The main game loop.
   */
  void gameLoop() throws Exception {
    boolean gameOver;
    do {
      takeAction();
      gameOver = gameState.cycle();
      if (gameSettings.isRender()) {
        window.repaint();
      }
      try {
        if (gameSettings.isRender()) {
          Thread.sleep(1); // give time to the window to repaint
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    } while (!gameOver && gameState.getTime() < gameSettings.getMaxCycles());

    for (AI player : players) {
      player.gameOver(gameState);
    }
  }

  /**
   * Every player takes one action.
   */
  void takeAction() throws Exception {
    PlayerAction[] playerActions = new PlayerAction[players.size()];
    // generate actions
    for (int id = 0; id < players.size(); id++) {
      GameState gs = gameSettings.isPartiallyObservable()
          ? new PartiallyObservableGameState(gameState, id)
          : gameState;
      AI player = players.get(id);
      try {
        playerActions[id] = player.getAction(id, gs);
      } catch (Exception e) {
        // do nothing (invalid action)
      }
    }
    // conduct actions
    for (int id = 0; id < players.size(); id++) {
      if (playerActions[id] != null) {
        gameState.issueSafe(playerActions[id]);
      }
    }
  }

  /**
   * Carry out any clean up needed after a game.
   */
  void afterGame() throws Exception {
    // printGameResults();
  }

  /**
   * Build an AI player.
   *
   * @param clazz The class to build.
   * @return An AI player.
   */
  AI buildAi(Class clazz) throws Exception {
    AI bot = (AI) clazz.getConstructor(UnitTypeTable.class).newInstance(unitTypeTable);
    if (bot instanceof AIWithComputationBudget) {
      if (bot instanceof InterruptibleAI) {
        return new ContinuingAI(bot);
      } else {
        return new PseudoContinuingAI((AIWithComputationBudget) bot);
      }
    }

    return bot;
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
