<p align="center">
  <img src="src/main/resources/filmrole.png?raw=true" alt="APM Screenshot"/>
</p>

### APM-Creator - The Storyboard Editor for AutoPodMobile (APM)

This program allows the creation of storyboards for the Datset creation and injection framework AutoPodMobile (APM).
A storyboard defines a sequence of actions by several actors, which is then executed step by step with APM.

<p align="center">
  <img src="APMCreator_Screenshot.jpg?raw=true" alt="APM Screenshot"/>
</p>

### Prerequisites

In the latest version, the APMCreator is bundled with a Java Runtime Environment (JRE) and all required libraries.

# Installation
## macOS
### Installation via .dmg File

1. Download the latest version of APM Creator in .dmg format from the Release page.
2. Open the .dmg file and drag the application into the "Applications" folder.
3. You can now launch APM Creator from the Applications folder.

## Important node: 
If you try to open an app by an unknown developer and you see a warning dialog on your Mac.
A dialog is displayed saying that the app is damaged. In fact, the app is simply not signed 
with a developer certificate. For this reason, Gatekeeper refuses to execute. 
The first method will allow a single program to run, without having to disable Gatekeeper. 
Open a terminal and run the following command:

```bash
sudo  xattr -dr com.apple.quarantine /Applications/APM Creator.app
```
The app should then start without any further complaints. 

### Installation via Homebrew

1. Open the Terminal.
2. Install APM Creator with the following command:

```bash
brew install --cask bocian67/APM Creator/APM Creator
```

3. After installation, APM Creator can be launched directly from the Applications folder.

## Windows
### Installation via .exe File

1. Download the latest version of APM Creator in .exe format from the Release page.
2. Run the .exe file and follow the installation instructions.
3. After installation, APM Creator can be opened from the Start menu.


## Linux
### Installation via .deb File
1. Download the latest version of APM Creator in .deb format from the Release page.
2. Open a terminal and navigate to the directory where the .deb file is saved.
3. Install APM Creator with the following command:
```bash
sudo apt install ./APM Creator.deb
```

4. After installation, APM Creator can be launched from the application menu.
