package logic;

/**
 * Auf diesem Thread läuft die update-Methode der Game_Impl.
 */

public class ThreadUpdate extends Thread {
    private Game_Impl game;
    private final Object synchronizer = new Object();

    public ThreadUpdate(Game_Impl game){
        this.game = game;
    }

    @Override
    public void run(){
        synchronized (synchronizer) {
            while (game.update()) {
            }
        }
    }

    public Object getSynchronizer(){
        return synchronizer;
    }


    public void waitForInput(){
        synchronized(synchronizer){
            try {
                synchronizer.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
