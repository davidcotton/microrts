package rts;

import static rts.GameSettings.DEFAULT_AI;
import static rts.GameSettings.DEFAULT_CONFLICT_POLICY;
import static rts.GameSettings.DEFAULT_IS_PARTIALLY_OBSERVABLE;
import static rts.GameSettings.DEFAULT_MAX_CYCLES;
import static rts.GameSettings.DEFAULT_RENDER;
import static rts.GameSettings.DEFAULT_SERIALIZATION_TYPE;
import static rts.GameSettings.DEFAULT_SERVER_ADDRESS;
import static rts.GameSettings.DEFAULT_SERVER_PORT;
import static rts.GameSettings.DEFAULT_UTT_VERSION;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import rts.gamemodes.ServerGame;
import rts.gamemodes.StandAloneGame;

/**
 * Run a microRTS game from the command line.
 * Allows for games to be configured via command line arguments.
 */
public class CommandLine {

  static final String ARG_LAUNCH_MODE = "mode";
  static final String ARG_MAP = "map";
  static final String ARG_RENDER = "render";
  static final String ARG_SERVER_ADDRESS = "address";
  static final String ARG_SERVER_PORT = "port";
  static final String ARG_SERIALIZATION_TYPE = "serialization";
  static final String ARG_MAX_CYCLES = "maxcycles";
  static final String ARG_IS_PARTIALLY_OBSERVABLE = "po";
  static final String ARG_UTT_VERSION = "utt";
  static final String ARG_CONFLICT_POLICY = "cp";
  static final String ARG_PLAYER_1 = "ai1";
  static final String ARG_PLAYER_2 = "ai2";

  private final GameSettings gameSettings;

  public CommandLine(GameSettings gameSettings) {
    this.gameSettings = gameSettings;
  }

  public static void main(String[] args) {
    ArgumentParser parser = ArgumentParsers.newFor("MicroRTS").build();
    parser
        .addArgument(formatArgName(ARG_LAUNCH_MODE))
        .choices("standalone", "gui", "server", "client")
        .setDefault("standalone");
    parser.addArgument(formatArgName(ARG_MAP));
    parser.addArgument(formatArgName(ARG_RENDER)).type(Boolean.class).setDefault(DEFAULT_RENDER);
    parser.addArgument(formatArgName(ARG_SERVER_ADDRESS)).setDefault(DEFAULT_SERVER_ADDRESS);
    parser
        .addArgument(formatArgName(ARG_SERVER_PORT))
        .type(Integer.class)
        .setDefault(DEFAULT_SERVER_PORT);
    parser
        .addArgument(formatArgName(ARG_SERIALIZATION_TYPE))
        .type(Integer.class)
        .choices(1, 2)
        .setDefault(DEFAULT_SERIALIZATION_TYPE);
    parser.addArgument(formatArgName(ARG_MAX_CYCLES)).type(Integer.class).setDefault(DEFAULT_MAX_CYCLES);
    parser
        .addArgument(formatArgName(ARG_IS_PARTIALLY_OBSERVABLE))
        .type(Boolean.class)
        .setDefault(DEFAULT_IS_PARTIALLY_OBSERVABLE);
    parser
        .addArgument(formatArgName(ARG_UTT_VERSION))
        .type(Integer.class)
        .choices(1, 2, 3)
        .setDefault(DEFAULT_UTT_VERSION);
    parser
        .addArgument(formatArgName(ARG_CONFLICT_POLICY))
        .type(Integer.class)
        .choices(1, 2, 3)
        .setDefault(DEFAULT_CONFLICT_POLICY);
    parser.addArgument(formatArgName(ARG_PLAYER_1)).setDefault(DEFAULT_AI.toString());
    parser.addArgument(formatArgName(ARG_PLAYER_2)).setDefault(DEFAULT_AI.toString());

    Namespace namespace = null;
    try {
      namespace = parser.parseArgs(args);
    } catch (ArgumentParserException exception) {
      parser.handleError(exception);
      System.exit(1);
    }

    final GameSettings gameSettings = GameSettings.loadFromArgs(namespace);
    System.out.println(gameSettings);
    final CommandLine commandLine = new CommandLine(gameSettings);
    commandLine.run();

    System.exit(0);
  }

  public void run() {
    switch (gameSettings.getLaunchMode()) {
      case SERVER:
        new ServerGame(gameSettings).run();
        break;
      case CLIENT:
        System.out.println("Not implemented.");
        break;
      case GUI:
        System.out.println("Not implemented.");
        break;
      case STANDALONE:
        // fall through
      default:
        new StandAloneGame(gameSettings).run();
    }
  }

  private static String formatArgName(String argName) {
    return "--" + argName;
  }
}
