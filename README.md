> **Warning**  
> Virekuvain is a **study project.** Some features are missing or require re-compilation, and no pre-built binaries are available.

# Virekuvain

https://user-images.githubusercontent.com/52505120/217366057-82572f45-6807-4ed9-b78f-3030a8078670.mp4

Virekuvain is a Java program for visualising audio from input devices.

## Features

- Support for over 60 audio formats
- Audio device selection
- Customisable colours
- Dark theme
- Support for mono, stereo, and multi-channel audio
- Waveform and frequency spectrum modes

## Installation

Install Java 17+ and Maven 3.X.X, then clone this repository and run `mvn javafx:run` to start the application.

> **Note**
> Not sure whether you have these installed already?  
>
> Run `java -version; mvn --version` in the terminal to check!

## Building binaries

At the moment, you can't. Eventually, you'll be able to use `mvn javafx:jlink`, but that's broken because the
dependency we're using to obtain the frequency spectrum (JTransforms) is outdated.

## Screenshots & Videos

![Ring of Fire by Johnny Cash](https://user-images.githubusercontent.com/52505120/217363664-92a44992-5061-44a7-93f5-ba31d70c5b08.png)

![How You Like That by BLACKPINK](https://user-images.githubusercontent.com/52505120/217363940-c957fc7e-34be-4807-8fb0-48c859b5188b.png)
