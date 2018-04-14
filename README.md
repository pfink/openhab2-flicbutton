# openHAB 2 Flic Button Binding 
[![Actively Maintained](https://maintained.tech/badge.svg)](https://maintained.tech/)
[![Build Status](https://travis-ci.org/pfink/openhab2-flicbutton.svg?branch=master)](https://travis-ci.org/pfink/openhab2-flicbutton) [![codebeat badge](https://codebeat.co/badges/c5ff5257-96fe-4414-ab57-240fde1dc9e9)](https://codebeat.co/projects/github-com-pfink-openhab2-flicbutton)

openHAB 2 binding for [fliclib-linux-hci](https://github.com/50ButtonsEach/fliclib-linux-hci) using the java clientlib by Shortcut Labs. When you use this binding, please share your expierences [here](https://community.openhab.org/t/how-to-integrate-flic-buttons/4468/12) and leave issues for stuff not listed in the ToDo's.

## Supported Things

* flicd-bridge - The bridge representing a running instance of the  server (flicd).
* button - The flic button

## Discovery

* There is no automatic discovery for flicd-bridges available.
* After flicd-bridge is (manually) configured, buttons will be automatically discovered as soon as they're addded with [simpleclient](https://github.com/50ButtonsEach/fliclib-linux-hci). If they're already attached to the flicd-bridge before configuring this binding, they'll be automatically discovered as soon as you click the button.

## Thing Configuration

### flicd-bridge

The bridge should be added to a *.things file. Example:

```
Bridge flicbutton:flicd-bridge:mybridge
```

The default host is localhost:5551 (this should be sufficient if flicd is running with default settings on the same server as openHAB). If your flicd service is running somewhere else, specify it like this:

```
Bridge flicbutton:flicd-bridge:mybridge [ hostname="<YOUR_HOSTNAME>",  port="<YOUR_PORT>"]
```

If flicd is running on a remote host, please do not forget to start it with the parameter `-s 0.0.0.0`, otherwise it won't be accessible for openHAB (more details on [fliclib-linux-hci](https://github.com/50ButtonsEach/fliclib-linux-hci)).

### button

There are no configuration parameters for buttons available and normally no textual configuration is necessary as buttons are autodiscovered as soon as the bridge is configured. If you want to use textual configuration anyway, you can do it like this:

```
Bridge flicbutton:flicd-bridge:mybridge [ hostname="<YOUR_HOSTNAME>",  port="<YOUR_PORT>"] {
    Thing button <MAC_ADDRESS> "<YOUR_LABEL>"
    Thing button <MAC_ADDRESS> "<YOUR_LABEL>"
    ...
}
```

You can lookup the MAC addresses of your buttons within the inbox of Paper UI. You're free to choose any label you like for your button.

## Channels

* **rawbutton**: Uses [system channel](https://github.com/eclipse/smarthome/blob/master/bundles/core/org.eclipse.smarthome.core.thing/src/main/java/org/eclipse/smarthome/core/thing/DefaultSystemChannelTypeProvider.java) SYSTEM_RAWBUTTON. Triggers raw [button events](https://github.com/eclipse/smarthome/blob/master/bundles/core/org.eclipse.smarthome.core.thing/src/main/java/org/eclipse/smarthome/core/thing/CommonTriggerEvents.java): PRESSED / RELEASED
* **button**: Uses [system channel](https://github.com/eclipse/smarthome/blob/master/bundles/core/org.eclipse.smarthome.core.thing/src/main/java/org/eclipse/smarthome/core/thing/DefaultSystemChannelTypeProvider.java) SYSTEM_BUTTON. Triggers common [button events](https://github.com/eclipse/smarthome/blob/master/bundles/core/org.eclipse.smarthome.core.thing/src/main/java/org/eclipse/smarthome/core/thing/CommonTriggerEvents.java): SHORT_PRESSED / DOUBLE_PRESSED / LONG_PRESSED
* **pressed-switch**: Switch that exposes the button's current state (ON -> pressed; OFF -> not pressed)


## Full example

1. Setup and run flicd as described in [fliclib-linux-hci](https://github.com/50ButtonsEach/fliclib-linux-hci)
1. Connect your buttons to flicd using the simpleclient as described in [fliclib-linux-hci](https://github.com/50ButtonsEach/fliclib-linux-hci). Flicd has to run in background the whole time, simpleclient can be killed after you successfully tested the button connection
1. Drop the .jar file of this plugin into the `addons` directory from openHAB 2
1. Stop openHAB 2
1. Add one or more bridges (one for each flicd service you're running - typically just a single one) to your *.things file:

	```
	Bridge flicbutton:flicd-bridge:mybridge [ hostname="<YOUR_HOSTNAME>" ]
	```

    Please consider that flicd does only accept connections from localhost by default.
1. Start openHAB 2
1. Check if the bridge got up correctly within the Things menue of PaperUI.
1. Buttons should get discovered automatically and can be added as read-only Switches via Paper UI
1. Now go to the PaperUI control page, press and hold the button -> the Switch should get "ON" as long as you hold the button
1. Now you can either bind items to the pressed-switch channel and trigger something on state changes or directly react to the events triggered by the button and rawbutton channel. Here some examples:
    ```
    rule 'Button rule using an item bound to the pressed-switch channel'

    when
        Item mybutton changed from ON to OFF
    then
        if (Light_Bedroom.state != ON)
            Light_Bedroom.sendCommand(ON)
        else
            Light_Bedroom.sendCommand(OFF)
    end

    rule "Button rule using the button channel"

    when
        Channel "flicbutton:button:1:80-e4-da-71-12-34:button" triggered SHORT_PRESSED
    then
        logInfo("Flic", "Flic 'short pressed' triggered")
    end

    rule "Button rule directly using the rawbutton channel"

    when
        Channel "flicbutton:button:1:80-e4-da-71-12-34:rawbutton" triggered
    then
        logInfo("Flic", "Flic pressed: " + receivedEvent.event)
    end
    ```

## Update FlicButton Binding to the newest version

1. Delete the old version's .jar file from the addons directory
1. Download the newest release and put the new .jar file to the addons directory
1. Restart openHAB

## License

The source code within this repository is released under Eclipse Publice License 1.0. Please be aware that the binary (.jar) releases may also contain software by other vendors which is not EPL-licensed.
