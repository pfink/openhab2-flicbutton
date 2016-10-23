# OpenHab 2 Flic Button Binding ** Pre-Alpha **

OpenHab2 binding for [fliclib-linux-hci](https://github.com/50ButtonsEach/fliclib-linux-hci) using the java clientlib by Shortcut Labs. **Important note: You're currently on the master / development branch. If you want to test the latest release (0.1 Pre-Alpha), [please switch to the 0.1 tag](https://github.com/pfink/openhab2-flicbutton/tree/0.1) to see the proper version of the documentation.**

## Current Status and ToDo's

- [x] Flic Button Auto Discovery (buttons have to be scanned and verified by other clients first, e.g. by simpleclient)
- [x] Implement and test flicbutton-pressed-channel (channel that exposes the raw button state, pressed (ON) or unpressed (OFF) to a OpenHab Switch)
- [ ] Handle removal of Flic Buttons
- [ ] Handle temporary unavailibility of flicd (research how's the right way to handle such stuff in OpenHab2)
- [ ] Add initial status check on FlicButtonHandler (buttons which are not auto discovered will currently not go online until the first status change happens)
- [ ] Clarify licensing (see also 50ButtonsEach/fliclib-linux-hci#35)
- [ ] Test and document some use cases for this binding (+ use openhab docs template)
- [ ] Clarify and document deployment to already running OpenHab2 instances
- [ ] More channels? Click, DoubleClick, Hold...
- [ ] Integrate button scan and connection process to this binding so that simpleclient is not needed anymore (will probably not be done by me, but could be interesting stuff to contribute)
- [ ] Unit Tests?

## Tested use case that should work in 0.5 / Alpha

I strongly advice against using this within a production system right now. Currently, only basic capabilities are implemented and much stuff is not tested yet (like proper removal of buttons etc.). When you test this binding, please share your expierences [here](https://community.openhab.org/t/how-to-integrate-flic-buttons/4468/12) and leave issues for stuff not listed in the ToDo's.

1. Setup and run flicd as described in [fliclib-linux-hci](https://github.com/50ButtonsEach/fliclib-linux-hci)
1. Connect your buttons to flicd using the simpleclient as described in [fliclib-linux-hci](https://github.com/50ButtonsEach/fliclib-linux-hci). Flicd has to run in background the whole time, simpleclient can be killed after you successfully tested the button connection
1. Drop the .jar file of this plugin into the `addons` directory from OpenHab 2
1. Stop OpenHab 2
1. Add one or more bridges (one for each flicd service you're running - typically just a single one) to your *.things file:

	```
	Bridge flicbutton:flicdaemon-bridge:mybridge [ hostname="<YOUR_HOSTNAME>" ]
	```

	Please consider that flicd does only accept connections from localhost by default.
1. Start OpenHab 2
1. Check if the bridge got up correctly within the Things menue of PaperUI.
1. Buttons should get discovered automatically and can be added as read-only Switches via Paper UI
1. Now go to the PaperUI control page, press and hold the button -> the Switch should get "ON" as long as you hold the button
1. Trigger something using Rules, e.g. toggle a light on/off each button click (here: on each button release, to be more correct):
	```
	rule 'FlicLightOn'

	when
		Item mybutton changed from ON to OFF
	then
		if (Light_Bedroom.state != ON)
	        Light_Bedroom.sendCommand(ON)
	    else
	        Light_Bedroom.sendCommand(OFF)
	end
	```
1. Or use rules to react to `SINGLE_PRESSED`, `DOUBLE_PRESSED` or `LONG_PRESSED` events:
    ```
    rule "My Flic Short Pressed Rule"
    when
        Channel "flicbutton:flicbutton-thing:1:80-e4-da-71-12-34:button-trigger" triggered SHORT_PRESSED
    then
        logInfo("Flic", "Flic 'short pressed' triggered")
    end
    
    rule "My Flic Rule"
    when
        Channel "flicbutton:flicbutton-thing:1:80-e4-da-71-12-34:button-trigger" triggered
    then
        logInfo("Flic", "Flic pressed: " + receivedEvent.event)
    end
    ```
     
## License

The code within this repository is released under Eclipse Publice License 1.0. Nevertheless, the released .jar contains the (compiled) java clientlib for flicd by Shortcut Labs (which was excluded from this repositories source files). For this java clientlib, Shortcut Labs made 2 statements regarding licensing [here](https://github.com/50ButtonsEach/fliclib-linux-hci/issues/35):

1. `Basically, you may use examples and libraries in any way you wish as long as the purpose is to interact with our Flic buttons.`
1. `Yes you can redistribute them in a .deb, or any other way you like.`
