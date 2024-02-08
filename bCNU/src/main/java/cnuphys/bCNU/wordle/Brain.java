package cnuphys.bCNU.wordle;

import java.util.ArrayList;

public class Brain {
	private static volatile Brain _instance;


	//letters that haven't been used yet
	private ArrayList<Character> _usedLetters = new ArrayList<>();

	//which word guess are we on? [0..5]
	private int _currentWordIndex = 0;

	//THE current word!
	private String _currentWord;

	//the database
	private static Data _data = Data.getInstance();

	private Brain() {
	}

	/**
	 * Get the singleton instance
	 *
	 * @return the singleton instance
	 */
	public static Brain getInstance() {
		if (_instance == null) {
			_instance = new Brain();
		}
		return _instance;
	}
	
	/**
	 * Get the current word index.
	 *
	 * @return the current word index
	 */
	public int getCurrentWordIndex() {
		return _currentWordIndex;
	}

	/**
	 * Get the current word.
	 *
	 * @return the current word
	 */
	public String getCurrentWord() {
		return _currentWord;
	}

	/**
	 * Set the current word. For testing, not cheating!
	 *
	 * @param word the current word
	 */
	public void setCurrentWord(String word) {
		_currentWord = word;
	}

	/**
	 * Reset everything for a new game
	 */
	public void newGame() {
		Wordle.getInstance().setMessage("New game!");
		_currentWord = (new String(_data.randomWord())).toUpperCase();

		System.err.println("Current word: " + _currentWord);
		_currentWordIndex = 0;

		_usedLetters.clear();
		Keyboard.getInstance().reset();
	}


	/**
	 * Process a character entry
	 *
	 * @param c the character
	 */
	public void processCharacterEntry(char c) {
		if (c == Keyboard.ENTER) {
            processEnter();
         }
		else if (c == Keyboard.BACKSPACE) {
            processDelete();
         }
        else {
            processLetter(c);
        }

		refresh();
	}

	//process a letter
	private void processLetter(char c) {
        System.out.println("Letter " + c);
        LetterGrid.getInstance().processLetter(_currentWordIndex, c);
        refresh();
    }

	//process the enter key
	private void processEnter() {
		System.out.println("Enter");

		char[] word = LetterGrid.getInstance().getWord(_currentWordIndex);
		String guess = new String(word);

		//handle too short
		if (guess.length() < 5) {
			Wordle.getInstance().setMessage("Guess too short!");
			LetterGrid.getInstance().badGuess(_currentWordIndex);
			return;
		}
		
		//handle not in dictionary
		if (!_data.goodWord(guess)) {
			Wordle.getInstance().setMessage("Not a word!");
			LetterGrid.getInstance().badGuess(_currentWordIndex);
			return;
		}

		//right or wrong, this word is done
		LetterGrid.getInstance().setCompleted(_currentWordIndex, true);

		boolean result = submitGuess(guess);
		
		if (result) {
			System.out.println("Correct!");
			gameWon();
		} else {
			Wordle.getInstance().setMessage("Nope.\nYou have " + (5 - _currentWordIndex) + " tries left.");
			LetterGrid.getInstance().badGuess(_currentWordIndex);

			//are we done?
			if (_currentWordIndex == 5) {
				gameLost();
			}
			_currentWordIndex++;
		}
		refresh();
	}

	//the game was won
	private void gameWon() {
		switch (_currentWordIndex) {
		case 0:
			Wordle.getInstance().setMessage("You won in one try! \nDid you cheat?");
			break;
		case 1:
			Wordle.getInstance().setMessage("You won! 2nd try! \nThat's pretty amazing");
			break;
		case 2:
			Wordle.getInstance().setMessage("You won! 3rd try! \nYou're good!");
			break;
		case 3:
                Wordle.getInstance().setMessage("You won! 4th try! \nNot too shabby!");
                break;
			case 4:
				Wordle.getInstance().setMessage("You won! 5th try! \nMeh.");
				break;
		default:
			Wordle.getInstance().setMessage("You won on the 6th try. \nColor me unimpressed");

		}
	}

	//the game was lost
	private void gameLost() {
		Wordle.getInstance().setMessage("Better luck next time!\n" + "The word was " + _currentWord);
	}

	//process the enter delete key
	private void processDelete() {
		System.out.println("Delete");
		LetterGrid.getInstance().delete(_currentWordIndex);
		refresh();
	}

	/**
	 * Refresh the display
	 */
	public void refresh() {
		_usedLetters.clear();
		LetterGrid.getInstance().fillUsedLetters(_usedLetters);
		LetterGrid.getInstance().refresh();
		Keyboard.getInstance().refresh();
	}

	/**
	 * Submit a guess
	 *
	 * @param chars the characters of the guess
	 * @return <code>true</code> if the guess is correct
	 */
	private boolean submitGuess(String guess) {
		System.out.println("Guess: " + guess);
		return _currentWord.equals(guess);
	}

	/**
	 * Has a char been used?
	 * @param c the char
	 * @return <code>true</code> if the char has been used
     */
	public boolean unused(char c) {
		return !_usedLetters.contains(c);
	}
}