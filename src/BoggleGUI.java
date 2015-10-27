import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;
import java.awt.event.*;
import java.io.*;

/**  A GUI for the game of Boggle.
 * 
 * BoggleGUI defines and implements methods to do these things:
 * 
 * 1.  Draw the boggle board
 * 2.  Manage the computer and human players' word lists
 * 3.  Update the scoreboard
 *
 * BoggleGUI requires class BogglePlayer, to define these instance methods:
 * <PRE>
 * public void buildLexicon(Set wordList)
 * public void setBoard(String[] letterArray)
 * public Vector getAllValidWords(int minimumWordLength)
 * public boolean isInLexicon(String wordToCheck)
 * public Vector isOnBoard(String wordToCheck)
 * public String[] getCustomBoard()
 * </PRE>
 
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.  See
 * http://www.gnu.org/copyleft/gpl.html
 */
public class BoggleGUI extends JFrame {

  public static String WORDLISTFILENAME = "enable1.txt";  // default
  public static  int DICEROWS = 4;  // default 4
  public static  int DICECOLS = 4;  // default 4
  public static  int MINIMUMWORDLENGTH = 4; // default
  
  
  private BogglePlayer computerPlayer;
  private BoggleBoard theBoard;
  private ScoreArea humanArea, computerArea;
  private WordEntryField wordEntryField;
  private ComputerTurnButton computerTurnButton;
        
  public BoggleGUI()  {
    super("Welcome to CS 210!");
                
    // create a BogglePlayer computer player to use
    computerPlayer = new BogglePlayer(MINIMUMWORDLENGTH, DICEROWS, DICECOLS);
    // Read word list from file and initialize computerPlayer's Lexicon
    initLexicon();
    // Intialize graphics panels
    initPanels();
    // Establish menu bar options and listeners for them
    setUpMenuBar();
    // WindowClosing listener:  for JDK 1.2 compatibility
    addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {
		System.exit(0);
	    }});
    // Trust contained components to set their sizes; top level frame packs
    pack();
  }

  /**
   * Get ready for a new game, given a letter list specification of
   * the Boggle board.
   * @param letterList specification of the board
   * @return void
   */
  public void newGame(String[] letterList)  {
    // Tell theBoard about the board layout
    theBoard.setBoard(letterList);
    // Tell computerPlayer about the board layout
    computerPlayer.setBoard(letterList);
    // Prepare score areas
    humanArea.setReady();
    computerArea.setReady();
    // Clear board highlighting
    theBoard.unHighlightAllDice();
    // Allow edits to text area and set focus
    wordEntryField.setReady();
    // redraw the whole thing so it looks as nice as can be
    
    computerTurnButton.setReady();
    
    repaint();
    
   // Vector<String> words = computerPlayer.getAllValidWords(MINIMUMWORDLENGTH);
    
    
  }
        
  /**
   * Consider a word entered by the human player.
   * @param wordToCheck word entered by human
   * @return Nothing, but highlight the letters when on board
   */
  public void checkAndAddWordHuman(String wordToCheck)   {

    // clear any board highlighting
    theBoard.unHighlightAllDice();

    // if empty word, the computer gets to take its turn
    if(wordToCheck.length() == 0)      {
      wordEntryField.setUnready();
      computerPlay();
      return;
    }

    // check that word is:
    // (1) at least minimum length, (2) on the board, (3) in the lexicon,
    // (4) not already entered by the player
    if(wordToCheck.length() <  MINIMUMWORDLENGTH)      {
      chideUser(wordToCheck, "Less Than " + MINIMUMWORDLENGTH + " Letters");
      return;
    }

    Vector letterLocations;
    
    // letterLocations = theBoard.getLocationsForWord(wordToCheck);
    // String wordOnBoard = theBoard.getWordFromLocations(letterLocations);
    //    if(!wordToCheck.equalsIgnoreCase(wordOnBoard)) { // double check

    // we just trust the computer player to check this
    letterLocations = computerPlayer.isOnBoard(wordToCheck);
    if(letterLocations == null) { 
      chideUser(wordToCheck, "Not On Board");
      return;
    }

    if(!computerPlayer.isInLexicon(wordToCheck))          {
      chideUser(wordToCheck, "Not In Lexicon");
      return;
    }

    if(humanArea.containsWord(wordToCheck)) {
      chideUser(wordToCheck, "Duplicate Word");
      return;
    }

    // OK, this word passed our rigorous suite of tests.  Add it
    humanArea.addWord(wordToCheck);
    // Highlight locations on board
    theBoard.highlightDice(letterLocations); 
    wordEntryField.clear();      //clear the wordEntryField text
  }

  /**
      * Print a message that the human player did something wrong.
   * @param attemptedWord  The word the human entered
   *        complaint      Description of what went wrong
   * @return void
   */

  public void chideUser(String attemptedWord, String complaint) {
    JOptionPane.showMessageDialog(this,
                                  attemptedWord + ": " + complaint + "!!!",
                                  complaint,
                                  JOptionPane.ERROR_MESSAGE);
    wordEntryField.clear();      //clear the wordEntryField text
  }

  /**
   * Let the computer player take its turn.
   * @param none
   * @return nothing, but highlights words computer got and computes its score
   */
  public void computerPlay() {
    computerArea.setName("Thinking!");
    computerArea.paintImmediately(computerArea.getVisibleRect());
    Vector<String> allWords = computerPlayer.getAllValidWords(MINIMUMWORDLENGTH);
    Iterator allWordsIterator = allWords.iterator();
    computerArea.setName("Computer");
    while(allWordsIterator.hasNext())     {
      String newWord = (String) allWordsIterator.next();
      Vector wordPath = computerPlayer.isOnBoard(newWord);
      // Add word to appropriate score area & highlight appropriate dice
      computerArea.addWord(newWord.toLowerCase());
      theBoard.highlightDice(wordPath);
      // pause for a tenth of a second
      //try {Thread.sleep(100); }   catch (Exception e) {}
    }
    theBoard.unHighlightAllDice(); // leave board unhighlighted when done
    
   //computerPlayer.debug();  //Show debug info
  } 


  /**
   * Read word list from file with name WORDLISTFILENAME, and pass a Set
   * containing those words to the computer player to intialize its lexicon.
   * @param none
   * @return void
   */
  private void initLexicon()  { 
    boolean EOF = false;
    HashSet<String> wordsFromFile = new HashSet<String>();
    System.err.print("Reading word list from file " + WORDLISTFILENAME + "...");
    try      {
      // Open File
      BufferedReader input = new BufferedReader( new FileReader(WORDLISTFILENAME));
      String nextWord;
      while((nextWord = input.readLine()) != null)
        // there is a contract that these are all lowercase but
        // we'll just assume it here for speed
        wordsFromFile.add(nextWord);

      //Close the file
      input.close();
    } catch (FileNotFoundException fnfex )      {
      JOptionPane.showMessageDialog(null, 
                                    "Unable to find word list file " + WORDLISTFILENAME, "Error",
                                    JOptionPane.ERROR_MESSAGE);
    }
    catch( IOException e)       {
      JOptionPane.showMessageDialog(null, 
                                    "Error Opening File", "Error",
                                    JOptionPane.ERROR_MESSAGE);
    }
    System.err.println(" Read " + wordsFromFile.size() + " words.");
    System.err.print("Building player's lexicon... ");
    computerPlayer.buildLexicon(wordsFromFile);
    System.err.println("done.");
  }

  /**
      * Initialize graphic display.
   * @param none
   * @return void
   */

  private void initPanels()  {
    Container contentPane = getContentPane();
    humanArea = new ScoreArea("Me");
    computerArea = new ScoreArea("Computer");
    theBoard = new BoggleBoard(DICEROWS,DICECOLS,computerPlayer);
    wordEntryField = new WordEntryField();
    computerTurnButton = new ComputerTurnButton();
    contentPane.add(wordEntryField,BorderLayout.NORTH);
    contentPane.add(humanArea, BorderLayout.WEST);
    contentPane.add(theBoard, BorderLayout.CENTER);
    contentPane.add(computerArea, BorderLayout.EAST);
    //JPanel buttonPanel = new JPanel();
    //buttonPanel.add(computerTurnButton);
    //buttonPanel.setLayout(new FlowLayout());
    //contentPane.setLayout(new FlowLayout());
    contentPane.add(computerTurnButton, BorderLayout.SOUTH);
  }
                

  /**
      * Initialize menus for controlling game.
   * @param none
   * @return void
   */
  private void setUpMenuBar()  {
    //Set Up Menu Bar
    JMenuBar menu = new JMenuBar();
                
    // Game Menu
    JMenu gameMenu = new JMenu("Game");
    gameMenu.setMnemonic('G');
    menu.add(gameMenu);

    JMenuItem newRandom = new JMenuItem("New Random");
    gameMenu.add(newRandom);
    newRandom.setMnemonic('N');
    newRandom.addActionListener(new ActionListener() {
      public void actionPerformed( ActionEvent e) {
        newGame(theBoard.getRandomBoard());
      }});

    JMenuItem customGame = new JMenuItem("New Custom");
    gameMenu.add(customGame);
    customGame.setMnemonic('C');
    customGame.addActionListener(new ActionListener() {
      public void actionPerformed( ActionEvent e) {
        newGame(computerPlayer.getCustomBoard());
      }});

    JMenuItem quitGame = new JMenuItem("Quit");
    gameMenu.add(quitGame);
    quitGame.setMnemonic('Q');
    quitGame.addActionListener( new ActionListener() {
      public void actionPerformed( ActionEvent e) {
        System.exit(0);
      }});

    // Help menu
    JMenu helpMenu = new JMenu("Help");
    menu.add(helpMenu);
    helpMenu.setMnemonic('H');
                
    JMenuItem aboutGame = new JMenuItem("About...");
    helpMenu.add(aboutGame);
    aboutGame.setMnemonic('A');
    aboutGame.addActionListener(new ActionListener() {
      public void actionPerformed( ActionEvent e)  {
        JOptionPane.showMessageDialog(BoggleGUI.this,
                                      "It's ABOUT time you got to work", 
                                      "About Game",
                                      JOptionPane.PLAIN_MESSAGE);
      }});
    setJMenuBar(menu);
  }

  /**
   * A class defining the visual appearance of a Boggle board, and
   * defining some related methods.
   */
  static class BoggleBoard extends JPanel {
    public static final Color BACKGROUNDCOLOR = new Color(255, 219, 13);

    private DiePanel theDice[][];
    private Vector<Die> diceBag;  // used for drawing a random game
    private int rows, cols;
    private BogglePlayer theComputer;

    BoggleBoard(int rows, int cols, BogglePlayer theComputer ) {
      this.rows = rows; this.cols = cols;
      this.theComputer = theComputer;

      // create some dice to use when getting a random game
      initDiceBag();

      // create a JPanel with rowsXcols GridLayout to hold the DiePanels
      JPanel innerPanel = new JPanel();
      innerPanel.setLayout(new GridLayout(rows,cols,1,1));
      innerPanel.setBackground(BACKGROUNDCOLOR);
        
      // Create Individual DiePanels, and add them
      theDice = new DiePanel[rows][cols];
      for (int row = 0; row < rows; row++)      {
        for (int col = 0; col < cols; col++)      {
          theDice[row][col] = new DiePanel();

          innerPanel.add(theDice[row][col]);
        }
      }
      innerPanel.setBorder(BorderFactory.createMatteBorder(10,10,10,10,      
                                                           BACKGROUNDCOLOR));
      this.add(innerPanel);
    }

    /**
        * fill the diceBag with the 16 "official" Boggle dice for the 4X4 game
     * @param none
     * @return void
     */
    private void initDiceBag() {
      diceBag = new Vector<Die>(16);
      diceBag.add(new Die("A", "O", "B", "B", "O", "J"));
      diceBag.add(new Die("W", "H", "G", "E", "E", "N"));
      diceBag.add(new Die("N", "R", "N", "Z", "H", "L"));
      diceBag.add(new Die("N", "A", "E", "A", "G", "E"));
      diceBag.add(new Die("D", "I", "Y", "S", "T", "T"));
      diceBag.add(new Die("I", "E", "S", "T", "S", "O"));
      diceBag.add(new Die("A", "O", "T", "T", "W", "O"));
      diceBag.add(new Die("H", "Qu", "U", "M", "N", "I"));
      diceBag.add(new Die("R", "Y", "T", "L", "T", "E"));
      diceBag.add(new Die("P", "O", "H", "C", "S", "A"));
      diceBag.add(new Die("L", "R", "E", "V", "Y", "D"));
      diceBag.add(new Die("E", "X", "L", "D", "I", "R"));
      diceBag.add(new Die("I", "E", "N", "S", "U", "E"));
      diceBag.add(new Die("S", "F", "F", "K", "A", "P"));
      diceBag.add(new Die("I", "O", "T", "M", "U", "C"));
      diceBag.add(new Die("E", "H", "W", "V", "T", "R"));
    }

    /**
     * Return an array of Strings showing the sequence of faces on
     * a randomly generated board.
     * @param none
     * @return array of strings representing the letters on the board
     */
    public String[] getRandomBoard()  {
      // Get random faces from randomy selected dice to fill the letterList
      int diceBagSize = diceBag.size();
      int boardSize = rows * cols;
      String[] letterList = new String[(rows * cols)];
      // we try to handle any size board with a fixed diceBag
      for (int i = 0; i<boardSize; i++) {
        if(i % diceBagSize == 0 )  Collections.shuffle(diceBag);
        Die d = (Die) diceBag.elementAt(i % diceBagSize);
        letterList[i] =   d.getRandomFace();
      }
      // set the corresponding DiePanels, and return the result
      return letterList;
    }

    /**
     * Non-randomly reset the board according to 
     * an array of Strings showing the sequence of faces on
     * the new board.  Return the array.
     * @param letterList an array of strings to use for the board
     * @return void
     */
    public void setBoard(String[] letterList)  {
      if(letterList == null ||
         letterList.length != rows*cols) {
        throw new IllegalArgumentException("setBoard(): String array length is not " + rows*cols);
      }
                
      //Set the DiePanels with given letters
      for (int row = 0; row < rows; row++)  {
        for (int col = 0; col < cols; col++)  {
          theDice[row][col].setFace(letterList[(row*cols) + col]);
        }
      }
    }

    /**
     * Find the locations of the letters for a legal word.  We
     * cheat and let the computer player do this for us.
     * @param word the word to fnd on the board
     * @return Vector of locations, one for each letter
     * @see BogglePlayer
     */
    public Vector getLocationsForWord(String word) {
      // we use the computer player for this
      return theComputer.isOnBoard(word);
    }

    /**
       * Given a list of locations, recreate the word at the locations.
     * @param locations Vector containing the letter placements
     * @return String of letters
     */
    public String getWordFromLocations(Vector locations) {
      int loc;
      int row,col;
      if (locations==null) return null;
      String result = "";
      for(int i = 0; i < locations.size(); i++)              {
        loc = ((Integer)locations.get(i)).intValue();
        row = loc / rows;
        col = loc % cols;
        result += theDice[row][col].getFace();
      }
      return result;
    }

    /**
        * Highlight the dice corresponding to the chosen word.
     * @param locations for each die
     * @return void
     */

    public void highlightDice(Vector locations) {
      if(locations == null) return;

      Integer loc;
      int row, col;

      unHighlightAllDice();
      for(int i = 0; i < locations.size(); i++) {
    	  
    	  highlightDie((Integer) locations.get(i));
      }
      this.paintImmediately(this.getVisibleRect());
    }

    /**
     * Highlight the specified die, given row and column.
     * @param row
     *        col
     * @return void
     */
    public void highlightDie(int row, int column) {
      theDice[row][column].highlight();
    }

    /**
     * Highlight the specified die, given Integer offset
     * from upper-left-hand corner, using left-to-right,
     * top-to-bottom ordering of dice.
     * @param indx the die's location
     * @return void
     */
    public void highlightDie(Integer indx) {
      int i = indx.intValue();
      int row = i / theDice[0].length;
      int col = i % theDice[0].length;
      highlightDie(row, col);
    }

    /**
     * Unhighlight all dice.
     */
    public void unHighlightAllDice()  {
      for (int row = 0; row < theDice.length; row++)   {
        for (int col = 0; col < theDice[row].length; col++)  {
          theDice[row][col].unHighlight();
        }
      }
      this.paintImmediately(this.getVisibleRect());
    }


    /**
        Helper class used to represent dice.
     */
    static class Die {
      private String[] sides;
      private int currentSideUp;
      public Random randomizer = new Random();

      public Die(String side1, String side2, String side3,
                 String side4, String side5, String side6)    {
        sides = new String[] {side1,side2,side3,side4,side5,side6};
        randomize();
      }

      /**
          * Given the state of the die, retrieves the letter on top.
       * @return String the letter
       */
      public String getLetter()    {
        return sides[currentSideUp];
      }

      /**
          * Picks a side of the die at random and returns the letter.
       * @return String the letter chosen.
       */
      public String getRandomFace()    {
        randomize();
        return sides[currentSideUp];
      }

      /**
          * Picks a number between 0 and 5.
       */
      private void randomize()    {
        currentSideUp = randomizer.nextInt(6);
      }
    }
                


    /** For displaying one Die on the board
        */
    static class DiePanel extends JPanel {
      private String face;
      private boolean isHighlighted;
      private JLabel faceLabel;

      private final Color DIECOLOR = new Color(230, 230, 230);
      private final Color FACECOLOR = new Color(3, 51, 217);
        
      private final Font FACEFONT = new Font("SansSerif", Font.PLAIN, 24);
      private final int DIESIZE = 50;

      public DiePanel()  {
        face = new String(""); 
        faceLabel = new JLabel("", SwingConstants.CENTER);
        setLayout(new BorderLayout());
        add(faceLabel, BorderLayout.CENTER);
        setBackground(BoggleBoard.BACKGROUNDCOLOR);
        setSize(DIESIZE, DIESIZE);
      }

      public Dimension getPreferredSize()  {
        return (new Dimension (DIESIZE+1, DIESIZE+1));
      }

      public Dimension getMinimumSize()  {
        return (getPreferredSize());
      }

      public void setFace( String chars )  {
        face = chars;
      }

      public String getFace() {
        return face;
      }

      /**
       * Draw one die including the letter centered in the middle of the die.
       * If highlight is true, we 
       * reverse the background and letter colors to highlight the die.
       */
      public void paintComponent(Graphics g)  {
        super.paintComponent(g);
                
        int centeredXOffset, centeredYOffset;
        // Draw the blank die
        g.setColor( (isHighlighted) ? FACECOLOR : DIECOLOR);
        g.fillRoundRect( 0, 0, DIESIZE, DIESIZE, DIESIZE/2, DIESIZE/2);
                
        // Outline the die with black
        g.setColor(Color.black);
        g.drawRoundRect( 0, 0, DIESIZE, DIESIZE, 
                         DIESIZE/2, DIESIZE/2);
        Graphics faceGraphics = faceLabel.getGraphics();
        faceGraphics.setColor( isHighlighted ? DIECOLOR : FACECOLOR);   
        Color myColor =  isHighlighted ? DIECOLOR : FACECOLOR;  
        faceLabel.setForeground(myColor);
        faceLabel.setFont(FACEFONT);
        faceLabel.setText(face);
      }

      public void unHighlight()  {
        isHighlighted = false;
      }

      public void highlight()  {
        isHighlighted = true;
      }
    }

  } // class BoggeBoard

  /** Maintains name, score, and word list information for one player
      */
  class ScoreArea extends JPanel {
    private final int WORDAREALINES = 16;
    private String playerName;
    private int playerScore, dimensionX, dimensionY;
    private HashSet<String> wordList;
        
    private final Font ScoreFont = new Font("SansSerif", Font.PLAIN, 18);
    private final Font WordFont = new Font("Geneva", Font.PLAIN, 9);
    private final Font LabelFont = new Font("Helvitica", Font.PLAIN, 9);

    private final Color WordColor = new Color(3, 128, 77);
    private final Color LabelColor = new Color(3, 115, 64);

    private JPanel topPanel, wordPanel, namePanel, scorePanel;
    private JTextArea wordArea;
    private JLabel nameText, scoreText;
        
    public ScoreArea(String player) {
      playerName = new String(player);
      playerScore = 0;
      wordList = new HashSet<String>();
                
      // Set-Up Top of Score Area
      namePanel = new JPanel();
      nameText = new JLabel(player);
      nameText.setFont(ScoreFont);
      namePanel.setLayout( new BorderLayout() );
      namePanel.add(nameText, BorderLayout.CENTER);

      scorePanel = new JPanel();
      scoreText = new JLabel("  0");
      scoreText.setFont(ScoreFont);
      scorePanel.setLayout( new BorderLayout() );
      scorePanel.add(scoreText, BorderLayout.CENTER);

      topPanel = new JPanel();
      topPanel.setLayout( new BorderLayout());
      topPanel.add(namePanel, BorderLayout.WEST);
      topPanel.add(scorePanel, BorderLayout.EAST);
                
      // Create bordering for top panel
      Border raisedBevel, loweredBevel, compound;
                
      raisedBevel = BorderFactory.createRaisedBevelBorder();
      loweredBevel = BorderFactory.createLoweredBevelBorder(); 
      compound = BorderFactory.createCompoundBorder(raisedBevel,
                                                    loweredBevel);
      topPanel.setBorder(compound);
                
      // Set-Up area to display word list
      wordPanel = new JPanel();
      Border etched = BorderFactory.createEtchedBorder();
      TitledBorder etchedTitle =
        BorderFactory.createTitledBorder(etched, "Word List");
      etchedTitle.setTitleJustification(TitledBorder.RIGHT);
      wordPanel.setBorder(etchedTitle);
      wordArea = new JTextArea(WORDAREALINES,
                               // 2/3 of max word len is a good # of columns
                               BoggleGUI.DICEROWS*BoggleGUI.DICECOLS*2/3);
      wordArea.addMouseListener(new MouseAdapter() {
        public void mouseClicked(MouseEvent e) {
          // if double-click, highlight the selection on the board
          if(e.getClickCount() == 2) {
            String word = wordArea.getSelectedText().trim();
            theBoard.highlightDice(theBoard.getLocationsForWord(word));
          }
        }});
      wordArea.setEditable(false); // for now
      wordPanel.add(new JScrollPane(wordArea,
                                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

      setLayout(new BorderLayout());
      add(topPanel, BorderLayout.NORTH);
      add(wordPanel, BorderLayout.CENTER);
    }

    public void setReady()  {
      resetScore();  // zero out score
      wordList.clear();  // remove words from HashSet
      // remove words from TextArea
      wordArea.setEditable(true);
      wordArea.selectAll();
      wordArea.cut();
      wordArea.setEditable(false);
      paintImmediately(getVisibleRect());
    }

    public void setName(String newName)  {
      playerName = newName;
      nameText.setText(playerName);
      repaint();
    }

    public boolean containsWord(String word) {
      return wordList.contains(word);
    }

    public void addWord(String word)  {
      if (containsWord(word)) return;
      wordList.add(word);
      wordArea.append(" " +word+"\n");
      addPoints(pointsForWord(word));
      wordArea.paintImmediately(wordArea.getVisibleRect());
    }

    /**
     * Define how many points a player gets for a given word.
     */
    public int pointsForWord(String word) {
      return word.length() - BoggleGUI.MINIMUMWORDLENGTH + 1;
    }

    public void addPoints(int points)  {
      playerScore += points;
      scoreText.setText(playerScore+"");
      scoreText.paintImmediately(scoreText.getVisibleRect());
    }

    public void resetScore() {
      playerScore = 0;
      scoreText.setText(playerScore+"");
      scoreText.paintImmediately(scoreText.getVisibleRect());
    }

  } // class ScoreArea

  class WordEntryField extends JPanel {
    private JTextField textField;
    
    public WordEntryField() {
      //Set up for human player's Text Entry Field
      textField = new JTextField(30);
      //Add listener to text entry field
      textField.addActionListener( new ActionListener() {
        public void actionPerformed( ActionEvent e) {
          checkAndAddWordHuman(textField.getText());
        }});
      this.add(new JLabel("Enter word: "));
      this.add(textField);
      setUnready();
    }

    public void clear() {
      textField.setText("");
    }

    public void setUnready() {
      clear();
      textField.setEditable(false);
      paintImmediately(getVisibleRect());
      // attempt to give up focus to top-level frame
      BoggleGUI.this.requestFocus();
    }
    public void setReady() {
      textField.setEditable(true);
      textField.requestFocus();
    }
  } // class WordEntryField
  
  class ComputerTurnButton extends JPanel {
	  
	  private JButton changeTurnButton;
	  
	  public ComputerTurnButton() {
		  
		  changeTurnButton = new JButton("Give Turn to Computer");
		  changeTurnButton.setVisible(true);
		  setReady();
		  //Add listener to text entry field
	      changeTurnButton.addActionListener( new ActionListener() {
	        public void actionPerformed( ActionEvent e) {
	          
	        	computerPlay();
	        	
	        }});
		  
	      this.add(changeTurnButton);
	  }
	  
	  public void setReady() {
		  
		  changeTurnButton.setEnabled(true);
		  changeTurnButton.requestFocus();
	  }
	  
	  public void setUnready() {
		  
		  changeTurnButton.setEnabled(false);
	  }
  }

  /**
   * The entry point for the BoggleGUI application.
   * Usage:
   * java BoggleGUI [ wordfile [ rows [ columns [ minwordlength ]]]]
   */
  public static void main(String args[] )  {
    if(args.length > 0) WORDLISTFILENAME = args[0];
    if(args.length > 1) DICEROWS = Integer.parseInt(args[1]);
    if(args.length > 2) DICECOLS = Integer.parseInt(args[2]);
    if(args.length > 3) MINIMUMWORDLENGTH = Integer.parseInt(args[3]);

    System.err.println("Starting " + DICEROWS + "x" + DICECOLS + " game," +
         " words from " + WORDLISTFILENAME +
         ", min word length " + MINIMUMWORDLENGTH + ".");
    (new BoggleGUI()).setVisible(true);
  }

}
