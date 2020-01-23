# openHAB 2 Flic Button Binding 
[![Actively Maintained](https://maintained.tech/badge.svg)](https://maintained.tech/)
[![Build Status](https://travis-ci.org/pfink/openhab2-flicbutton.svg?branch=master)](https://travis-ci.org/pfink/openhab2-flicbutton) [![codebeat badge](https://codebeat.co/badges/c5ff5257-96fe-4414-ab57-240fde1dc9e9)](https://codebeat.co/projects/github-com-pfink-openhab2-flicbutton)

openHAB 2 binding for [fliclib-linux-hci](https://github.com/50ButtonsEach/fliclib-linux-hci) using the java clientlib by Shortcut Labs. When you use this binding, please share your expierences [here](https://community.openhab.org/t/how-to-integrate-flic-buttons/4468/12) and create issues at this repository for feature requests and bug reports.

## Supported Things

* flicd-bridge - The bridge representing a running instance of [fliclib-linux-hci (flicd)](https://github.com/50ButtonsEach/fliclib-linux-hci) on the server .
* button - The Flic button (supports Flic 1 buttons as well as Flic 2 buttons)

## Discovery

* There is no automatic discovery for flicd-bridges available.
* After flicd-bridge is (manually) configured, buttons will be automatically discovered as soon as they're addded with [simpleclient](https://github.com/50ButtonsEach/fliclib-linux-hci) via background discovery. If they're already attached to the flicd-bridge before configuring this binding, they can be discovered by triggering an active scan.

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

* **rawbutton**: [System Trigger Channel](https://www.openhab.org/docs/developer/bindings/thing-xml.html#system-trigger-channel-types) of type `rawbutton`.
* **button**: [System Trigger Channel](https://www.openhab.org/docs/developer/bindings/thing-xml.html#system-trigger-channel-types) of type `button`.
* **battery-level**: [System State Channel](https://www.openhab.org/docs/developer/bindings/thing-xml.html#system-state-channel-types) of type `battery-level`.

## Getting Started

1. Setup and run flicd as described in [fliclib-linux-hci](https://github.com/50ButtonsEach/fliclib-linux-hci). Please consider that you need a seperate Bluetooth adapter. Shared usage with other Bluetooth services (e.g. Bluez) is not possible.
1. Connect your buttons to flicd using the simpleclient as described in [fliclib-linux-hci](https://github.com/50ButtonsEach/fliclib-linux-hci). Flicd has to run in background the whole time, simpleclient can be killed after you successfully tested the button connection.
1. Add a flicd-bridge via PaperUI or Textual Configuration. Please consider that flicd does only accept connections from localhost by default, to enable remote connections from openHAB you have to use the `--server-addr` parameter as described in [fliclib-linux-hci](https://github.com/50ButtonsEach/fliclib-linux-hci).
1. When the bridge is online, buttons newly added via simpleclient will automatically get discovered via background Discovery. To discover buttons that were set up before the Binding setup, please run an active scan.
1. [Profiles](https://www.openhab.org/docs/configuration/items.html#profiles) are the recommended way to use this binding. But it's also possible to setup [Rules](https://www.openhab.org/docs/configuration/rules-dsl.html), e.g. like this:
    ```
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