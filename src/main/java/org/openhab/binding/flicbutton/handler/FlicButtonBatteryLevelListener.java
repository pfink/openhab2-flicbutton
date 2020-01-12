package org.openhab.binding.flicbutton.handler;

import io.flic.fliclib.javaclient.BatteryStatusListener;
import io.flic.fliclib.javaclient.Bdaddr;
import org.eclipse.jdt.annotation.NonNull;

import java.io.IOException;

public class FlicButtonBatteryLevelListener extends BatteryStatusListener.Callbacks {

    private final FlicButtonHandler thingHandler;

    FlicButtonBatteryLevelListener(@NonNull FlicButtonHandler thingHandler) {
        this.thingHandler = thingHandler;
    }

    @Override
    public void onBatteryStatus(Bdaddr bdaddr, int i, long l) throws IOException {
        thingHandler.updateBatteryChannel(i);
    }
}
