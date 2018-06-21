package rts.games;

import static ai.socket.SocketAI.LANGUAGE_JSON;
import static ai.socket.SocketAI.LANGUAGE_XML;

import ai.core.AI;
import ai.socket.SocketAI;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import rts.GameSettings;
import util.XMLWriter;

/** Run a microRTS server for a client AI that connects via a socket. */
public class ServerGame extends Game {

  private final int DEFAULT_TIME_BUDGET = 100;
  private final int DEFAULT_ITERATIONS_BUDGET = 0;

  private Socket socket;

  /**
   * Build a game server.
   *
   * @param gameSettings The game settings to use.
   */
  public ServerGame(GameSettings gameSettings) {
    super(gameSettings);
  }

  @Override
  void initialize() throws Exception {
    ServerSocket serverSocket = new ServerSocket(gameSettings.getServerPort());
    socket = serverSocket.accept();
    super.initialize();
  }

  @Override
  AI buildAi(Class clazz)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException,
          InstantiationException {
    if (clazz.equals(SocketAI.class)) {
      return SocketAI.createFromExistingSocket(
          DEFAULT_TIME_BUDGET,
          DEFAULT_ITERATIONS_BUDGET,
          unitTypeTable,
          gameSettings.getSerializationType(),
          socket);
    } else {
      return super.buildAi(clazz);
    }
  }

  @Override
  void printGameResults() throws Exception {
    super.printGameResults();

    PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
    if (gameSettings.getSerializationType() == LANGUAGE_XML) {
      XMLWriter xmlWriter = new XMLWriter(writer, " ");
      gameState.toxml(xmlWriter);
      xmlWriter.getWriter().append("\n").flush();
    } else if (gameSettings.getSerializationType() == LANGUAGE_JSON) {
      gameState.toJSON(writer);
      writer.append("\n").flush();
    } else {
      throw new IllegalArgumentException(
          String.format("Communication language %s not supported", gameSettings.getSerializationType())
      );
    }
  }
}
