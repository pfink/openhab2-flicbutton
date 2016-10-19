# OpenHab 2 Flic Button Binding ** UNDER DEVELOPMENT **

OpenHab2 binding for [fliclib-linux-hci](https://github.com/50ButtonsEach/fliclib-linux-hci) using the java clientlib by Shortcut Labs.

## Current Status and ToDo's

- [x] Flic Button Auto Discovery (buttons have to be scanned and verified by other clients first, e.g. by simpleclient)
- [x] Implement and test flicbutton-pressed-channel (channel that exposes the raw button state, pressed (ON) or unpressed (OFF) to a OpenHab Switch)
- [ ] Handle removal of Flic Buttons
- [ ] Add initial status check on FlicButtonHandler (buttons which are not auto discovered will currently not go online until the first status change happens)
- [ ] Clarify licensing (see also 50ButtonsEach/fliclib-linux-hci#35)
- [ ] Test and document some use cases for this binding
- [ ] Clarify and document deployment to already running OpenHab2 instances
- [ ] More channels? Click, DoubleClick, Hold...
- [ ] Integrate button scan and connection process to this binding so that simpleclient is not needed anymore (will probably not be done by me, but could be interesting stuff to contribute)
- [ ] Unit Tests?
