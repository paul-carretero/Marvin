package eventManager;

import aiPlanner.Main;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.sensor.EV3TouchSensor;

/**
 * Fournit une primitive pour la detection de la pression du capteur de pression.
 */
public class PressionSensor extends EV3TouchSensor
{
    /**
     * Instancie le capteur de pression
     */
    public PressionSensor(){
        super(LocalEV3.get().getPort(Main.TOUCH_SENSOR));
    }

    /**
     * Vérifie la pression du capteur de pression (par exemple si l'on a detecté un palet)
     * @return vrai si le capteur de pression est enfoncé, faux sinon
     */
    public boolean isPressed(){
        float[] sample = new float[1];
        fetchSample(sample, 0);
        return sample[0] != 0;
    }
}
