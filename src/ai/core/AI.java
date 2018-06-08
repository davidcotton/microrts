/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.core;

import java.util.List;
import rts.GameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;

/**
 *
 * @author santi
 */
public abstract class AI {
    public abstract void reset();
    
    // indicates to the AI that the new game will be played with a new UTT (in case the AI needs to do anything with it)
    public void reset(UnitTypeTable utt)  
    {
        reset();
    }
    
    
    public abstract PlayerAction getAction(int player, GameState gs) throws Exception;
    
    
    @Override
    public abstract AI clone();   // this function is NOT supposed to do an exact clone with all the internal state, etc.
                                  // just a copy of the AI with the same configuration.
        
    public abstract List<ParameterSpecification> getParameters();

    
    // This method can be used to report any meaningful statistics once the game is over 
    // (for example, average nodes explored per move, etc.)
    public String statisticsString() {
        return null;
    }
    
    
    public void printStats() {
        String stats = statisticsString();
        if (stats!=null) System.out.println(stats);        
    }
    
    
    @Override
    public String toString() {
        return this.getClass().getSimpleName();
    }  


    // This function could be called before starting a game (depending on the tournament
    // configuration it will be called or not). If it is called, this gives the AIs an 
    // opportunity to see the initial game state before the game start and do any kind of
    // analysis.
    // - If "milliseconds" is > 0, then this is a time bound that the AI should respect.
    // - Even if the game is partially observable, the game state received by this function
    //   might be fully observable.
    public void preGameAnalysis(GameState gs, long milliseconds) throws Exception
    {
    }

    
    // If bots want to write anything to disc after the pre-game analysis, this function has an additional argument
    // Specifying a folder, where AIs can write the result of the analysis. Then this can be loaded before starting 
    // subsequent games.
    public void preGameAnalysis(GameState gs, long milliseconds, String readWriteFolder) throws Exception
    {
        preGameAnalysis(gs, milliseconds);
    }
    
    
    // Notifies the AI that the game is over, and reports who was the winner
    public void gameOver(GameState gameState) throws Exception
    {
    }
} 
