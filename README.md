<p align="center">
  <img src="resources/filmrole.png?raw=true" alt="APM Screenshot"/>
</p>

### APM-Creator - The Storyboard Editor for AutoPodMobile (APM)

This program allows the creation of storyboards for the dataset creation and injection framework, AutoPodMobile (APM).
A storyboard defines a sequence of actions by several actors, which is then executed step by step with APM.

<p align="center">
  <img src="APMCreator_Screenshot.png?raw=true" alt="APM Screenshot"/>
</p>

### AI Support
The latest version of the tool supports the creation of storyboards using artificial intelligence via an LLM agent. 
An LLM, or Large Language Model, is a type of artificial intelligence system designed to understand and generate human language.
An LLM is essentially a very large neural network that has been trained on massive amounts of text data from the internet,
books, articles, and other sources. Through this training, it learns patterns in language—how words relate to each other, 
grammar rules, facts about the world, and even reasoning patterns.

<p align="center">
  <img src="APMCreator_AI.png?raw=true" alt="LLM Agent with prompt" width="600"/>
</p>

### Prerequisites

In the latest version, the APMCreator is bundled with a Java Runtime Environment (JRE) and all required libraries.

Important note: An LLM model file is required to use the AI function.

Currently, the .gguf format is supported.

The main repository for open-source models is huggingface.co. You can reach the repository with the following link:
https://huggingface.co/datasets

Popular open-source models to try:

- Llama 3.2 (Meta) - various sizes from 1B to 90B parameters
- Mistral and Mixtral - efficient and capable
- Phi-3 (Microsoft) - smaller but surprisingly capable
- Gemma (Google) - various sizes available

Hardware considerations:

- Smaller models (1-7B parameters) can run on most modern laptops
- Medium models (13-30B) need good GPUs or Apple Silicon Macs
- Larger models (70B+) require significant GPU memory (24GB+ VRAM)

A good entry-level model, for example, is Qwen2.5-Coder is the latest series of Code-Specific Qwen large language models (formerly known as CodeQwen):
https://huggingface.co/Qwen/Qwen2.5-Coder-7B-Instruct


Ensure that the path to the model file is set correctly in the settings dialogue.

<p align="center">
  <img src="APMCreator_Settings.png?raw=true" alt="LLM Agent with prompt" width="400"/>
</p>

# Technical Background

An overview article highlighting the technical background of AutoPodMobile and APCreator can be retrieved from

Dirk Pawlaszczyk, Philipp Engler, Ronny Bodach, Christian Hummert, Margaux Michel, Ralf Zimmermann,
AI-driven dataset creation in mobile forensics using LLM-based storyboards,
Forensic Science International: Digital Investigation,
Volume 55, 2025, 302002, ISSN 2666-2817,
https://doi.org/10.1016/j.fsidi.2025.302002.

# Installation
## macOS
### Installation via .dmg File

1. Download the latest version of APM Creator in .dmg format from the Release page.
2. Open the .dmg file and drag the application into the "Applications" folder.
3. You can now launch APM Creator from the Applications folder.

## Important node: 
If you try to open an app by an unknown developer, and you see a warning dialogue on your Mac.
A dialogue is displayed saying that the app is damaged. In fact, the app is simply not signed 
with a developer certificate. For this reason, Gatekeeper refuses to execute. 
The first method will allow a single program to run without having to disable Gatekeeper. 
Open a terminal and run the following command:

```bash
sudo  xattr -dr com.apple.quarantine /Applications/APMCreator.app
```
The app should then start without any further complaints. 

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

