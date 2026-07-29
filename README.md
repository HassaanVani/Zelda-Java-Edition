# Zelda-Java-Edition

## Project description

Java recreation/interpretation of a classic Zelda-style action-adventure game.

## Architecture

`src/` contains game state, rendering, combat, rooms, enemies, inventory, audio, and save systems; `data/`, `sprites/`, and `sounds/` hold content; Ant configuration builds the desktop application.

## Technology

Java • Swing/AWT • Ant

## Run locally

`ant run`

## Repository guide

The implementation is organized so that entry points remain thin and domain-specific logic stays in the modules named above. Configuration, assets, and deployment files are kept separate from application code. Review the source tree before changing behavior, and keep secrets in local environment files rather than committing them.
