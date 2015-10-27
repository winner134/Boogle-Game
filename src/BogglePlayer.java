import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;
import java.io.*;
import java.lang.*;

/**
   This program implements code that allows the user to play a game of
    boggle, the famous fun fabulous board game.  However to display
    the results of the BogglePlayer class and game, Boggle GUI must be
    implemented as a graphical interface and to interpret the results
    of BoglePlayer.  The results are stored in useful data structures,
    such to make for easy retrieval and implementation.  The game
    works for any sized board and for any minimum word length.  The
    computer then gets all valid words from the board and displays
    them, thus thoroughly trouncing the human opponent.  The program
    checks the input the user puts in, and checks the board and all
    the words in enable1.txt (the dictionary) and gives the user
    points for that word.  In this fashion a Boggle Game is played.

 *
 */



public class BogglePlayer {
    
    
    /**
     * Builds a data structure for use as a dictionary.  Given a Set of
     * words to use in a lexicon for the game, this method will
     * extract the words, sort them, and put them into a suitable
     * data structure such that they can be quickly searched.
     * @param wordlist the collection of words to use in the lexicon
     * (potentially unordered)
     * @return void
     * @see    BoggleGUI
     */
    
    //Constants - only used if call doesn't give values for these things
    private static final int MIN_WORD_DEFAULT = 4;
    private static final int ROWS_DEFAULT = 4;
    private static final int COLS_DEFAULT = 4;
    private static final int TILES_DEFAULT = (ROWS_DEFAULT*COLS_DEFAULT);
    
  
    //vars
    private Vector<String> lexicon; //Stores lexicon
    private String board[][];  //Stores board
    
    private int rows, cols;   //Size of board
    private int tiles; //Total number of tile on board
    private int minWordLength; //Minimum size for a valid word
    private Vector<String> wordsOnBoard;
    private char[] wordStart = new char[500];
    private String[] startMatch = new String[200000];
    private int startMatchCounter = 0;
    private int graph[][];
    
    //Constructor for default BogglePlayer with default values
    public BogglePlayer() {
	minWordLength = MIN_WORD_DEFAULT;
	rows = ROWS_DEFAULT;
	cols = COLS_DEFAULT;
	tiles = TILES_DEFAULT;
	
	lexicon = new Vector<String>();
	board = new String[rows][cols];
	graph = new int[rows][cols];
    }
  
  
    /*Constructor for when user or BoggleGUI wants to state minimum
     * word-size and board size.
     */
    public BogglePlayer(int minLength, int r, int c)  {
	minWordLength = minLength;
	rows = r;
	cols = c;
	tiles = (rows*cols);
	
	lexicon = new Vector<String>();
	board = new String[rows][cols];
	graph = new int[rows][cols];
    }
    
  
    /* Method to get lexicon from file; lexicon is sorted and only has
     * valid length words.
     * @param wordList   A list of legal words in Boggle
     * @see BoggleGUI
     */
  public void buildLexicon(Set wordList) {
      Iterator i;
      String newWord;
      i = wordList.iterator();
      
      //read in each word to newWord until there are no words left
      while(i.hasNext() == true) { 
	  newWord = (String)i.next();
	  
	  //only add word to lexicon if it is long enough
	  if (newWord.length() >= minWordLength)
	      lexicon.addElement(newWord);
      }   
      
      quickSort(lexicon, 0, lexicon.size() - 1);
  }
    
  
    /**
     * Method to create the board given an array of letters.  This
     * method is passed an array of strings.  Each element of the
     * array corresponds to one cube in the boggle board.  This method
     * converts the array into a suitable data structure to enable
     * efficient searching.
     * @param letterArray   the letters that make up the board
     * @return void
     * @see    BoggleGUI
     */
    public void setBoard(String[] letterArray) {
	for(int i = 0; i < rows; i++)
	    for(int j = 0; j < cols; j++)
		board[i][j] = letterArray[i*rows +j];
    }
    
  
    /**
     * Method to retrieve all legal words on the board.  This method
     * returns all of the words in the board that are in the lexicon
     * and are at least the minimum length size.
     * @param minimumWordLength  the minimum size of a legal word
     * @return a Vector of strings, each representing a word on the board
     * @see BoggleGUI
     */
    public Vector<String> getAllValidWords(int minimumWordLength) {
    	
    	wordsOnBoard = new Vector<String>();
    	
    	char firstLetter; 
    	
    	for (int i = 0; i < rows; i++) {
    		
    		for (int j = 0 ; j < cols; j++) {
    			
    			for (int n = 0; n < startMatchCounter; n++)
    				startMatch[n] = null;
    			
    			startMatchCounter = 0;
    			//startMatch = null;
    			
    			firstLetter = board[i][j].toLowerCase().charAt(0);
    			
    			for (int a = 0; a < lexicon.size(); a++) {
    	    		
    	    		if (lexicon.get(a).charAt(0) == firstLetter) {
    	    			
    	    			startMatch[startMatchCounter] = lexicon.get(a);
    	    			//System.out.println(startMatch[startMatchCounter]);
    	    			startMatchCounter++;
    	    		}
    	    	}
    			
    			//System.out.println(startMatchCounter);

    			wordStart = null;
    			wordStart = new char[500];
    			
    			//wordStart = board[i][j];
    			getWordsOnBoard(i, j, 0);
    		}
    	}
    	
    	//System.out.println("I AM OUT");
    	//System.out.println(wordsOnBoard.size());
    	
    	return wordsOnBoard;
    }
    
    public void getWordsOnBoard(int x, int y, int depth) {
    	
    	int i, j;
    	
    	if (x < 0 || y < 0 || x >= rows || y >= cols)
    		return;
    	
    	if (graph[x][y] == 1)
    		return;
    	
    	graph[x][y] = 1;
    	
    	wordStart[depth] = board[x][y].charAt(0);
    	depth++;
    	//System.out.println(wordStart.toString());
    	
    	String word = new String();
    	//System.out.println(wordStart.length);
    	
    	for (int f = 0; f <= depth-1; f++) {
    		
    		if (f == 0)
    			word = Character.toString(wordStart[f]);
    		
    		else
    			word = word.concat(Character.toString(wordStart[f]));
    	}
    	
    //	System.out.println(word);
    	
    	if (depth >= minWordLength) {
    		
    		if (isInLexicon(word.toLowerCase()))
    			wordsOnBoard.add(word);
    		
    	}
    	
    	if (depth <= 10) {
    		
	    	for (j = -1; j <= 1; j++) {
	    		
	    		for (i = -1; i<= 1; i++) {
	    			
	    			
	    				getWordsOnBoard(x+i, y+j, depth);
	    		}
	    			
	    	}
	    	
    	}
    	
    	graph[x][y] = 0;
    	
    }
    	
    
    /**
     * This method checks if a word is in the lexicon specified by buildLexicon.
     * @param wordToCheck the word to be checked
     * @return true when the word is in the lexicon, false otherwise
     * @see BoggleGUI
     */
    public boolean isInLexicon(String wordToCheck) {
	boolean result;
	result = BinarySearch(lexicon, 0, lexicon.size() - 1, wordToCheck);  
	return result;
    }
  
  
    /**
     * Method to check whether or not a word in on the board.  This
     * method checks if the given word can be found on the board using
     * a legal connected path (i.e. consecutive letters are adjacent,
     * no cube on the board is used twice)
     * @param wordToCheck the word to be checked
     * @return a Vector of the locations of the letters.  If not found, returns null
     * @see BoggleGUI
    */
  public Vector<Integer> isOnBoard(String wordToCheck) {
	  
	  Vector<Integer> locations = new Vector<Integer>();
	  int letterMatchCount = 0;
	  char firstLetter = wordToCheck.toLowerCase().charAt(0);
	  boolean continueSearch = true;
	  int infiniteLoopCounter = 10;
	  int counter = 0;
	  int x1,y1;
	  int i1 = 0;
	  int j1 = 0;
	  
	  for (int x = 0; x < rows; x++) {
		  
		  for (int y = 0; y < cols; y++) {
			  
			  
			  if (board[x][y].toLowerCase().charAt(0) == firstLetter) {
					  
					  locations.addElement((cols * x) + y);
					  letterMatchCount++;
			  }
			  
			  else
				  continue;
			  
			  
			  x1 = x;
			  y1 = y;
			  
			  continueSearch = true;
			  
			  while (continueSearch) {
				  
				  for (int i = -1; i <= 1; i++) {
						  
					  for (int j = -1; j <= 1; j++) {
						  
						  try {
							  
						 
							  if (board[x + i][y + j].toLowerCase().charAt(0) == wordToCheck.toLowerCase().charAt(letterMatchCount)) {
									  
									  
								  locations.addElement(cols * (x + i) + y + j);
								  letterMatchCount++;
								  x = x + i;
								  y = y + j;
								  continueSearch = true;
								  i1 = i;
								  j1 = j;
								  i = 2;
								  j = 2;
							   }
							  
							  else { 
								  
								  if (i == 1 && j == 1) {
									  
									  if (i1 == 1 && j1 == 1)
										  continueSearch = false;
									  
									  else {
										  
										  if (counter <= infiniteLoopCounter) {
											
											counter++;
											x = x + (i1 * -1);
										  	y = y + (j1 * -1);
										  	i = i1;
										  	j = j1;
										  	letterMatchCount--;
										  	locations.removeElementAt(locations.size() - 1);
										  
										  }
									  }
								  }
								  
							  }
						  }
						  
						  catch (Exception e) {
							  
							  continueSearch = false;
						  }
							  
							  
						  }
					  }
				  
				  }
				  
			 if (letterMatchCount == wordToCheck.length()) {
				
				 return locations;
				 
			 }
				 
		     letterMatchCount = 0;
		     locations.clear();
		     x = x1;
		     y = y1;
		  
			  
			 }
		  
	  }
	  
	  
      return null;
  }
    
    
    /**
     * This method creates a fixed board.
     * It is mainly useful in debugging such
     * that specific problems can be addressed easily (e.g. "Qu" boards).
     * @param none
     * @return a string array representing the board
     * @see BoggleGUI
     */
    public String[] getCustomBoard(){
	String[] customboard={"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P"};      
	return customboard;
    }
    
    
    /**
     * This method sorts a vector of strings using Quicksort.
     * @param data  the Vector to be sorted
     *        low   the leftmost boundary of the Vector to be sorted
     *        high  the rightmost boundary of the Vector to be sorted
     * @return void
     */
    public void quickSort(Vector<String> dat, int low, int high) {
        // Base case
        if (low >= high) return;
        // partition the Vector into two halves
        int pivot = partition(dat, low, high);
        // recursive calls to sort the two parts
        quickSort(dat, low, pivot - 1);
        quickSort(dat, pivot + 1, high);
    } 


    /**
     * Quicksort helper method that partitions a Vector into two
     *  halves based on a "pivot."  All the elements less than the
     *  pivot are placed in the left half, all the rest are in the
     *  right half.
     * @param data  The Vector being sorted
     *       left  The leftmost boundary
     *       right The rightmost boundary
     * @return the location of the pivot in the Vector
     */
    public int partition(Vector<String> data, int left, int right){
        // left and right represent the boundaries of elements we
        // haven't partitioned Our goal is to get to all of them
        // moving partitioned elements to either side as necessary.
        while (true) {
            // move right "pointer" toward left
            while (left < right && 
		   ((String)data.elementAt(left)).compareTo(((String)data.elementAt(right))) < 0) {
                right--;
            }
	    
            if (left < right) swap(data,left++,right);
            else return left;
            // move left pointer toward right
            while (left < right && ((String)data.elementAt(left)).compareTo(((String)data.elementAt(right))) < 0){
                left++;
            } 
            if (left < right) swap(data,left,right--);
            else return right;
        }
    }
    

    
    /**
     * This method swaps two elements in a Vector (regardless of their type).
     * @param data The vector
     *        i    The position of one element
     *        j    The position of the other element
     * @return void
     */
    public void swap(Vector<String> data, int i, int j) {
        String temp;
        temp = (String)data.elementAt(i);
        data.setElementAt(data.elementAt(j), i);
        data.setElementAt(temp, j);
    }
    

    
    /**
     * This method performs a recursive binary search on a Vector.  
     * It returns true if the search item is in the Vector and false otherwise.
     * @param s The Vector to be searched
     *        front The leftmost boundary of the Vector that we're still interested in
     *        back  The rightmost boundary
     *        item  The thing we're searching for
     * @return true when the item is in the vector, false otherwise
    */
    public boolean BinarySearch(Vector<String> s, int front, int back, String item){
        // Check the middle spot
        int middle = (front + back) / 2;
        // base cases
        if(((String)s.elementAt(middle)).compareTo(item)==0)
            return true;
        if(front >= back)
            return false;
        // More searching necessary
        if(((String)s.elementAt(middle)).compareTo(item) > 0)
            return BinarySearch(s, front, middle-1, item);
        return BinarySearch(s, middle + 1, back, item);
    }
    
    public Vector<String> getWords() {
    	
    	return wordsOnBoard;
    }

}
