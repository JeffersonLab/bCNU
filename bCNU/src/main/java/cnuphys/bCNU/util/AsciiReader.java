package cnuphys.bCNU.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * Simplifies the reading of an ascii file. Assumes comments begin with a "!".
 * This should be extended, and the processLine method overridden.
 *
 * @author heddle
 *
 */
public abstract class AsciiReader {

	// the number of noncomment lines processed
	private int nonCommentLineCount;

	/**
	 * Constructor
	 *
	 * @param file the ascii file to be processed
	 * @throws FileNotFoundException
	 */
	public AsciiReader(File file) throws FileNotFoundException {
		FileReader fileReader = new FileReader(file);
		final BufferedReader bufferedReader = new BufferedReader(fileReader);

		boolean reading = true;
		while (reading) {
			String s = AsciiReadSupport.nextNonComment(bufferedReader);
			if (s != null) {
				nonCommentLineCount++;
				processLine(s);
			} else {
				reading = false;
			}
		}
		done();
	}

	/**
	 * Process one non comment line from the file.
	 *
	 * @param line the line to be processed.
	 */
	protected abstract void processLine(String line);

	/**
	 * @return the nonCommentLineCount
	 */
	public int getNonCommentLineCount() {
		return nonCommentLineCount;
	}

	/** Done reading */
	public abstract void done();

}
