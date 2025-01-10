## Yet Another Wordle Clone

This project is an attempt to learn Compose and other recent Android technologies, as well as to explore coding with an AI assistant.

- The ux mimics the original/NYT version of the game
- Uses words from assets/words.txt
- The game is permanently set to hard mode
- Does not remember previously played words or any other game state between restarts

The architecture is slightly complicated for the task, but this serves as an exercise. It consists of:

- Data Layer with a repository
- Domain Layer with a reducer for business logic
- UI Layer with a ViewModel that exposes the state through a Flow 
