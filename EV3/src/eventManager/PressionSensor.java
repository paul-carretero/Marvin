package eventManager;

import aiPlanner.Main;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.sensor.EV3TouchSensor;

public class PressionSensor extends EV3TouchSensor
{
    public PressionSensor(){
        super(LocalEV3.get().getPort(Main.TOUCH_SENSOR));
    }

    public boolean isPressed(){
        float[] sample = new float[1];
        fetchSample(sample, 0);
        return sample[0] != 0;
    }
}
