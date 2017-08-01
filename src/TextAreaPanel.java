import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Stack;

import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.Utilities;
import javax.swing.text.DefaultStyledDocument;
import java.awt.Insets;
import javax.swing.text.DefaultCaret;
import java.lang.Thread;
import java.lang.InterruptedException;
import java.lang.Character;

//This is the main text area of the editor, where all of the programming happens
public class TextAreaPanel extends JPanel {

	//the underlying text component
	JTextPane textArea = new JTextPane();

	DocumentListener dlistener;

	LineNumberList lineNumbers;

	//the file that is represented by this TextAreaPanel instance
	File file;

	//whether the file was just created (specifically by handleNew())
	boolean isNew = false;

	boolean changed = true;
	//stores the keywords based on what is passed to it from the editor class
	HashMap<String, String> keywords = new HashMap<String, String>();

	//the style of TextAreaPanel
	//most of it is inherited from Editor, but there are some specifics to this class
	StyleContext styleContext;
	SimpleAttributeSet keywordAttributes;
	SimpleAttributeSet normalAttributes;

	private Color blackTheme = new Color(38,38,38);
	private Color greyTheme = new Color(128, 129, 135);
	private Color whiteTheme = new Color(239,237,230);

	private DefaultHighlighter highlighter = new DefaultHighlighter();
	private DefaultHighlightPainter highlightPainter;

	private String keywordRegex = "[a-zA-Z]+|\".*\"|//[^\r\n]*|/\\*[\\s\\S]*?(\\*/|\\Z)|[-+!.*/=]";

	private boolean replaceQuotesRecursive = false;

	private boolean currentUndo = false;

	private Stack<String[]> pastChanges = new Stack<String[]>();

	private Stack<String[]> futureChanges = new Stack<String[]>();

	Document d;


	public TextAreaPanel(StyleContext styleContext) {

		highlightPainter = new DefaultHighlightPainter(greyTheme);

		setLayout(new BorderLayout());

		d = textArea.getDocument();

		textArea.setHighlighter(highlighter);

		add(textArea);

		this.styleContext = styleContext;

		Action beepAction = textArea.getActionMap().get(DefaultEditorKit.beepAction);

		beepAction.setEnabled(false);

		textArea.setParagraphAttributes(styleContext.getStyle("standard"), true);

		normalAttributes = new SimpleAttributeSet();

		normalAttributes.addAttributes(textArea.getInputAttributes());

		setForeground(whiteTheme);

		DefaultCaret caret = (DefaultCaret)textArea.getCaret();

		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

		textArea.setCaretColor(whiteTheme);
		// keywordAttributes.addAttributes(styleContext.getStyle("keywords"));

	}

	public TextAreaPanel(HashMap<String, HashSet<String>> keywords, StyleContext styleContext) {

		this(styleContext);

		setKeywords(keywords);
	}

	public TextAreaPanel(HashMap<String, HashSet<String>> keywords, StyleContext styleContext, File file) {

		this(styleContext);

		setKeywords(keywords);

		this.file = file;
	}

	public void setText(String input) {

		textArea.setText(input);

	}

	public String getText() {

		try {
			return textArea.getDocument().getText(0, textArea.getDocument().getLength());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public JTextPane getTextArea() {

		return textArea;

	}

	public void setFile(File f) {

		file = f;

	}

	public File getFile() {

		return file;
	}

	public String getFileName() {

		return file.getName();

	}

	public String getFilePath() {

		return file.getAbsolutePath();
	}

	public void setDocListener(DocumentListener dlistener){

		this.dlistener = dlistener;
	}

	public DocumentListener getDocListener(){

		return dlistener;
	}

	public void setLineNumbers(LineNumberList lineNumbers){

		this.lineNumbers = lineNumbers;
	}

	public LineNumberList getLineNumbers(){

		return lineNumbers;
	}

	//sets the keywords with a new HashMap<>
	public void setKeywords(HashMap<String, HashSet<String>> keywords) {

		for(String key: keywords.keySet()){

			for(String word:keywords.get(key)){
				this.keywords.put(word,key);
			}
		}

		try {
			updateKeywords();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}


	public void setIsNew(boolean b) {

		isNew = b;
	}

	public boolean getIsNew() {

		return isNew;
	}

	public void setIsChanged(boolean b) {

		changed = b;
	}
	public boolean getIsChanged() {

		return changed;
	}

	public void highlight(String sentence){

		try{
			String text = d.getText(0,d.getLength());

			highlighter.removeAllHighlights();

			int length = sentence.length();

			int index=0;

			while((index=text.indexOf(sentence,index)) > -1){

				highlighter.addHighlight(index,index+length,highlightPainter);
				index+=length;

			}

		}catch(BadLocationException e){

			e.printStackTrace();
		}
	}

	public void undo() throws BadLocationException{

		System.out.println("running undo");

		if(!pastChanges.isEmpty()){

			//Document d = textArea.getDocument();

			String[] lastChange = pastChanges.pop();

			futureChanges.push(lastChange);

			String changeType = lastChange[3];

			String deletedText = lastChange[2];
			String insertedText = lastChange[1];

			int offset = Integer.parseInt(lastChange[0]);

			//long start = System.currentTimeMillis();

			if(changeType.equals("rep")){

				currentUndo=true;
				this.d.remove(offset,insertedText.length());
				this.d.insertString(offset,deletedText,normalAttributes);


			}

			else{
				currentUndo=true;
				this.d.insertString(offset,deletedText,normalAttributes);
			}

			///long stop = System.currentTimeMillis();

			//System.out.println("undo took : " + (stop-start) + " milliseconds.");

		}

	}

	public void redo() throws BadLocationException{

		System.out.println("running undo");

		if(!futureChanges.isEmpty()){

			//Document d = textArea.getDocument();

			String[] lastChange = futureChanges.pop();

			pastChanges.push(lastChange);

			String changeType = lastChange[3];

			String deletedText = lastChange[2];
			String insertedText = lastChange[1];

			int offset = Integer.parseInt(lastChange[0]);

			//long start = System.currentTimeMillis();

			if(changeType.equals("rep")){

				this.d.remove(offset,deletedText.length());
				this.d.insertString(offset,insertedText,normalAttributes);


			}

			else{
				this.d.remove(offset,deletedText.length());
			}

			//long stop = System.currentTimeMillis();

			//System.out.println("undo took : " + (stop-start) + " milliseconds.");

		}
	}

	//returns either the style associated with a word or null (not a keyword)
	public String getKeywordStyle(String word) {

		int length = word.length();
		if(length>0){

			/*

				this is a test comment
			*/

			char first = word.charAt(0);

			if(first=='"'){

				return "strings";
			}

			else if(first=='.' || first=='!' || first=='-' || first=='+' || first=='*'  || first=='='){

				return "periods";
			}

			else if(first=='/'){

				if(length>1){
					char second = word.charAt(1);
					if(second=='/' || second=='*'){
						return "comments";
					}
				}

				else{

					return "periods";
				}

			}

			else if(Character.isUpperCase(first)){

				return "classes";
			}

			char last = word.charAt(word.length()-1);

			if(last=='('){

				return "periods";
			}
		}


		return keywords.get(word);
	}

	//recolors keywords
	//call this after updating keywords
	/*
		major problem with this method. When I try to optimize it by temporarily replacing the set document,
		it breaks because of setCaretPosition in remove(). Because the temp set document on the textpane is empty,
		when I try to set the caret position it doesn't work.
	*/

	public void updateKeywords() throws BadLocationException {

		double startTime = System.currentTimeMillis();

		StyledDocument d = textArea.getStyledDocument();

		String text = null;
		try {
			text = d.getText(0, d.getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}

		if(d.getLength()>1){
			StyledDocument temp = new DefaultStyledDocument();

			//temp.insertString(0,text,normalAttributes);

			textArea.setStyledDocument(temp);

			//temp.insertString(0,text,normalAttributes);


			Matcher m;
			if (!text.isEmpty()) {
				int i = 0;
				if (Character.isAlphabetic(text.charAt(i))) {
					i++;
					while (i < text.length() && Character.isAlphabetic(text.charAt(i))) {
						i++;
					}
					String word = text.substring(0, i);
					System.out.println("word: " + word);
					String style = getKeywordStyle(word);
					if (style != null) {
						currentUndo = true;
						d.remove(0, i);
						d.insertString(0, word, styleContext.getStyle(style));
					}
				}
				//regex pattern to find words within the text
				Pattern p = Pattern.compile(keywordRegex);

				m = p.matcher(text);

				System.out.println("finding keywords: ");

				//finding words and coloring them if they are keywords
				while (m.find()) {

					String word = m.group();

					//System.out.println(keywords);
					System.out.println(word);

					//returns the style associated with the keyword or null if the word is not a keyword
					String style = getKeywordStyle(word);
					if (style != null) {
						int start = m.start();
						System.out.println("start: " + start);
						currentUndo = true;
						d.remove(start, word.length());
						d.insertString(start, word, styleContext.getStyle(style));
					}

				}

				//this is checking the end of the text for keywords, because the regex pattern will not find it if there isn't whitespace before and after a word
				if (i < text.length()-1) {
					i = text.length() - 1;
					if (Character.isAlphabetic(text.charAt(i))) {
						i--;
						while (Character.isAlphabetic(text.charAt(i))) {
							i--;
						}
						i++;
						String word = text.substring(i, text.length());
						System.out.println("Word at end of string: " + word);
						String style = getKeywordStyle(word);
						if (style != null) {
							//removes and then inserts the string with the correct style
							currentUndo = true;
							d.remove(i, word.length());
							d.insertString(i, word, styleContext.getStyle(style));
						}

						else{
							currentUndo = true;
							d.remove(i,word.length());
							d.insertString(i, word, styleContext.getStyle("standard"));
						}

					}
				}

				textArea.setStyledDocument(d);
			}
		}

			double stopTime = System.currentTimeMillis();

			System.out.println("time took updating: " + (stopTime-startTime));

	}

	//sets the listener for updating keywords as the user types
	public void setKeywordListener() {

		AbstractDocument d = (AbstractDocument) textArea.getDocument();

		//creates document filter to check keywords on every insertion of text
		d.setDocumentFilter(new DocumentFilter() {

			@Override
			public void insertString(FilterBypass fb, int offset, String text, AttributeSet attributeSet)
					throws BadLocationException {

			int length = text.length();

			if(currentUndo){

				d.replace(offset,0,text,attributeSet);
			}

			else{
				/*
				//regex pattern to find words within inserted text
				Pattern p = Pattern.compile(keywordRegex);

				//indexes of where keywords start and end
				ArrayList<Integer> indexes = new ArrayList<Integer>();

				if (!text.isEmpty()) {
					int i = 0;
					//checking the beginning of the text for keywords, because they're not matched by the regex expression
					if (Character.isAlphabetic(text.charAt(i))) {
						i++;
						while (i<text.length() && Character.isAlphabetic(text.charAt(i))) {
							i++;
						}
						String word = text.substring(0, i);
						System.out.println("word: " + word);

						String style = getKeywordStyle(word);
						if (style != null) {
							indexes.add(0);
							indexes.add(i);
						}
					}

					Matcher m = p.matcher(text);

					System.out.println("finding keywords in insertString: ");

					//actual finder of keywords within the text. Finds keywords and then stores the start and end indexes in the indexes ArrayList<>
					while (m.find()) {

						String word = m.group();

						System.out.println(word);

						String style = getKeywordStyle(word);

						if (style != null) {
							int start = m.start();
							System.out.println("start: " + start);
							indexes.add(start);
							indexes.add(m.end());
						}

					}

					if (!indexes.isEmpty()) {
						super.insertString(fb, offset, text.substring(0, indexes.get(0)), attributeSet);
						for (i = 0; i < indexes.size(); i += 2) {

							int first = indexes.get(i);
							int second = indexes.get(i + 1);
							String temp = text.substring(first, second);
							super.insertString(fb, offset + first, temp, styleContext.getStyle(getKeywordStyle(temp)));

							if(i+2<indexes.size()){
								super.insertString(fb, offset + second, text.substring(second, indexes.get(i + 2)),
										attributeSet);
								}

						}

					}

					else {

						super.insertString(fb, offset, text, attributeSet);

					}

					}

				textArea.setCaretPosition(offset+length);*/

					super.insertString(fb,offset,text,attributeSet);
				}

			}

			@Override
			public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {



				String deletedText = d.getText(offset,length);

				if(!currentUndo){
					String[] changedAttributes = new String[4];
					changedAttributes[0] = Integer.toString(offset);
					changedAttributes[1] = Integer.toString(length);
					changedAttributes[2] = deletedText;
					changedAttributes[3] = "rem";

					pastChanges.push(changedAttributes);
				}
				else{

					currentUndo = false;
				}

				boolean areQuotes=false;

				if(deletedText.contains("\"")){

					areQuotes=true;

					DefaultStyledDocument temp = new DefaultStyledDocument();

					textArea.setDocument(temp);
				}

				super.remove(fb, offset, length);

				System.out.println("running remove with offset: " + offset + " and length: " + length);

				int startFirst = -1;
				int endFirst = -1;

				int startSecond = -1;
				int endSecond = -1;

				int temp = offset - 1;
				System.out.println("temp: " + temp);

				if (temp>-1 && Character.isAlphabetic(d.getText(temp, 1).charAt(0))) {
					startFirst = Utilities.getWordStart(textArea, temp);
					endFirst = Utilities.getWordEnd(textArea, temp);
				}

				temp = offset;

				if (offset < d.getLength() - 1) {
					if (Character.isAlphabetic(d.getText(temp, 1).charAt(0))) {
						startSecond = Utilities.getWordStart(textArea, temp);
						endSecond = Utilities.getWordEnd(textArea, temp);
					}
				}
				/*
				 * int tempOffset = offset-1;
				 *
				 * StringBuilder sb = new StringBuilder(d.getText(tempOffset,
				 * 1));
				 *
				 * tempOffset--;
				 *
				 * while(Character.isAlphabetic(sb.charAt(0))){ sb.insert(0,
				 * d.getText(tempOffset, 1)); tempOffset--; }
				 *
				 * tempOffset=offset;
				 *
				 * while(tempOffset<=d.getLength()-1 &&
				 * Character.isAlphabetic(sb.charAt(sb.length()-1))){
				 * sb.append(d.getText(tempOffset, 1)); }
				 */

				String style;

				if (startFirst != -1) {
					String firstWord = d.getText(startFirst, endFirst - startFirst);
					System.out.println("first word: " + firstWord);
					style = getKeywordStyle(firstWord);
					if (style != null) {

						super.remove(fb, startFirst, endFirst - startFirst);
						super.insertString(fb, startFirst, firstWord, styleContext.getStyle(style));
					}

					else {
						super.remove(fb, startFirst, endFirst - startFirst);
						super.insertString(fb, startFirst, firstWord, styleContext.getStyle("standard"));
					}
				}
				if (startSecond != -1) {
					String secondWord = d.getText(startSecond, endSecond - startSecond);
					style = getKeywordStyle(secondWord);
					System.out.println("second word: " + secondWord);
					if (style != null) {
						super.remove(fb, startSecond, endSecond - startSecond);
						super.insertString(fb, startSecond, secondWord, styleContext.getStyle(style));
					} else {
						super.remove(fb, startSecond, endSecond - startSecond);
						super.insertString(fb, startSecond, secondWord, styleContext.getStyle("standard"));
					}

				}

				if(areQuotes)	{



              System.out.println("text contains string. searching through file for quotes");


              String allText = d.getText(0,d.getLength());
              char quote2 = '"';

              int count = 1;
              int index = allText.indexOf(quote2);
              int lastIndex = -1;
              System.out.println("offset: " + offset);
              System.out.println("offset + length: " + (offset+length));
              while(index>0){

              lastIndex=index;
              index=allText.indexOf(quote2,index+1);
             	count+=1;

             	System.out.println("index: " + index);

             	if(index>=offset){

             		if(count%2==0){
                            System.out.println("replacing quotes with yellow 1");
                           //System.out.println("inserted text: " + d.getText(lastIndex,index-lastIndex+1));
                           System.out.println("index: " + index);
                           System.out.println("last index: " + lastIndex);
                           if(count<6){
                           	System.out.println("making yellow : \"" + d.getText(lastIndex,index-lastIndex+1) + "\"");
                           }
                           super.replace(fb,lastIndex,index-lastIndex+1,d.getText(lastIndex,index-lastIndex+1),styleContext.getStyle("strings"));

                      		}


                     else{
                     	//System.out.println("making not yellow: " + d.getText(lastIndex+1,index-lastIndex));

                     	replaceQuotesRecursive = true;
                     	System.out.println("recursively replacing text : \"" + d.getText(lastIndex+1,index-lastIndex-1)+"\"" );
                     	d.replace(lastIndex+1,index-lastIndex-1,d.getText(lastIndex+1,index-lastIndex-1),normalAttributes);
                     }
                  }

                  }


                  textArea.setDocument(d);
            	}



				System.out.println("setting caret to position: " + offset);
				if(offset<=textArea.getDocument().getLength()){
					System.out.println("setting caret to position: " + offset);
					textArea.setCaretPosition(offset);
				}

			}

		@Override
		public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attributeSet)
					throws BadLocationException {



				long dlength = d.getLength();

				String deletedText = d.getText(offset,length);



				boolean areQuotes = false;

				if(!replaceQuotesRecursive){

					if(!currentUndo){
						String[] changedAttributes = new String[4];

						changedAttributes[0] = Integer.toString(offset);
						changedAttributes[1] = text;
						changedAttributes[2] = deletedText;
						changedAttributes[3] = "rep";

						pastChanges.push(changedAttributes);
					}

					else{
						currentUndo=false;
					}

					if(deletedText.contains("\"")||text.contains("\"")||text.length()>15){

						areQuotes=true;

						DefaultStyledDocument temp = new DefaultStyledDocument();

						textArea.setDocument(temp);
					}
				}

				else{

					replaceQuotesRecursive=false;
				}

				int originalOff = offset;
				int originalLength = text.length();

				if (attributeSet.getAttributeCount() > 0) {
					attributeSet = normalAttributes;
				}

				Pattern p = Pattern.compile("^[a-zA-Z]");
				int tempOffset = offset;

				if (tempOffset > 0) {
					String before = d.getText(offset - 1, 1);
					System.out.println("before: " + before);
					if (Character.isAlphabetic(before.charAt(0))) {
						text = before + text;
						offset--;
						tempOffset--;
						length++;
					}
				}
				System.out.println("hello");

				if(offset+length<d.getLength()-1){
					String after = d.getText(offset + length, 1);
					System.out.println("after: " + after);
					if (Character.isAlphabetic(after.charAt(0))) {
						text += after;
						length++;
					}
				}

				System.out.println("hello2");

					if (tempOffset > 0) {

							tempOffset--;

							StringBuilder sb = new StringBuilder(d.getText(tempOffset, 1));

							tempOffset -= 1;

							char sbFirst = sb.charAt(0);

							while (tempOffset>=0 && (Character.isAlphabetic(sbFirst) || sbFirst=='/')) {


								sb.insert(0, d.getText(tempOffset, 1));

								tempOffset -= 1;

								sbFirst = sb.charAt(0);

								if(sbFirst == '/'){

									while (offset + length <= dlength - 1
									&& sbFirst!='\n' ){

										sb.insert(0, d.getText(tempOffset, 1));

										tempOffset -= 1;

										sbFirst = sb.charAt(0);
									}
								}
							}

							sb.deleteCharAt(0);

							offset -= sb.length();

							length += sb.length();

							text = sb.toString() + text;
					}

					System.out.println("hello3");

					if (offset + length < dlength - 1) {

							StringBuilder sb = new StringBuilder(d.getText(offset + length, 1));

							char fbLast = sb.charAt(sb.length()-1);

							while (offset + length <= dlength - 1
									&& Character.isAlphabetic(fbLast) ){

								length += 1;
								sb.append(d.getText(offset + length, 1));
								fbLast=sb.charAt(sb.length()-1);

								if(fbLast == '/'){

									while (offset + length <= dlength - 1
									&& fbLast!='\n' ){

										length+=1;
										sb.append(d.getText(offset+length,1));
										fbLast=sb.charAt(sb.length()-1);
									}
								}
							}

							sb.deleteCharAt(sb.length() - 1);

							// sb.deleteCharAt(sb.lastIndexOf("\n"));
							System.out.println("sb: " + sb.toString());
							text += sb.toString();

					}


					System.out.println("text");
				System.out.println("text: " + text);

				super.remove(fb, offset, length);

				ArrayList<Integer> indexes = new ArrayList<Integer>();

				if (!text.isEmpty()) {

					int i = 0;
					/*
					if (Character.isAlphabetic(text.charAt(i))) {
						i++;
						while (i < text.length() && Character.isAlphabetic(text.charAt(i))) {
							i++;
						}
						String word = text.substring(0, i);
						System.out.println("first word: " + word);
						if (getKeywordStyle(word) != null) {
							indexes.add(0);
							indexes.add(i);
						}
					}

					*/
					p = Pattern.compile(keywordRegex);
					Matcher m = p.matcher(text);

					System.out.println("created matcher on text: " + text);

					System.out.println("finding keywords in replace: ");

					while (m.find()) {

						String word = m.group();

					//	System.out.println(keywords);
						System.out.println("word: " + word);

						if (getKeywordStyle(word) != null) {
							System.out.println("keyword");
							int start = m.start();
							System.out.println("start: " + start);
							indexes.add(start);
							indexes.add(m.end());
							System.out.println(start);
							System.out.println(m.end());
						}

					}

					int j = text.length() - 1;

					/*
					if (i < j && Character.isAlphabetic(text.charAt(j))) {
						j--;

						while (j > 0 && Character.isAlphabetic(text.charAt(j))) {
							j--;
						}

						String word = text.substring(j + 1);
						System.out.println("last word: " + word);
						if (getKeywordStyle(word) != null) {
							indexes.add(j + 1);
							indexes.add(text.length());
						}
					}
					*/

					if (!indexes.isEmpty()) {
						System.out.println("there are keywords");
						super.insertString(fb, offset, text.substring(0, indexes.get(0)), attributeSet);
						for (i = 0; i < indexes.size(); i += 2) {

							int first = indexes.get(i);
							int second = indexes.get(i + 1);

							String temp = text.substring(first, second);

							super.insertString(fb, offset + first, temp, styleContext.getStyle(getKeywordStyle(temp)));

							if (i + 2 < indexes.size()) {
								try{
								super.insertString(fb, offset + second, text.substring(second, indexes.get(i + 2)),
										attributeSet);
								}
								catch(Exception e){


									e.printStackTrace();

									System.out.println("indexes: " + indexes);
									System.out.println("i: " + i);
									System.out.println("leftover text: " + text.substring(second));
									System.out.println(second);
									System.out.println(indexes.get(i+2));
								}
							}
						}

						super.insertString(fb, offset + indexes.get(indexes.size() - 1),
								text.substring(indexes.get(indexes.size() - 1)), attributeSet);

					}

					else {

						super.insertString(fb, offset, text, attributeSet);

					}

			if(areQuotes){

              System.out.println("text contains string. searching through file for quotes");


              String allText = d.getText(0,d.getLength());
              char quote2 = '"';

              int count = 1;
              int index = allText.indexOf(quote2);
              int lastIndex = -1;
              System.out.println("offset: " + offset);
              System.out.println("offset + length: " + (offset+length));
              while(index>0){

              lastIndex=index;
              index=allText.indexOf(quote2,index+1);
             	count+=1;

             	System.out.println("index: " + index);

             	if(index>=offset){

             		if(count%2==0){
                            System.out.println("replacing quotes with yellow 1");
                           //System.out.println("inserted text: " + d.getText(lastIndex,index-lastIndex+1));
                           System.out.println("index: " + index);
                           System.out.println("last index: " + lastIndex);
                           if(count<6){
                           	System.out.println("making yellow : \"" + d.getText(lastIndex,index-lastIndex+1) + "\"");
                           }
                           super.replace(fb,lastIndex,index-lastIndex+1,d.getText(lastIndex,index-lastIndex+1),styleContext.getStyle("strings"));

                      		}


                     else{
                     	//System.out.println("making not yellow: " + d.getText(lastIndex+1,index-lastIndex));

                     	replaceQuotesRecursive = true;
                     	System.out.println("recursively replacing text : \"" + d.getText(lastIndex+1,index-lastIndex-1)+"\"" );
                     	d.replace(lastIndex+1,index-lastIndex-1,d.getText(lastIndex+1,index-lastIndex-1),normalAttributes);
                     }
                  }

                  }

            }
					//System.out.println("Thread is sleeping");
					/*try{
						Thread.sleep(1000);
					}
					catch(InterruptedException e){
						e.printStackTrace();
					}*/
					textArea.setDocument(d);
					System.out.println("Setting caret position to " + (originalOff + originalLength));
					textArea.setCaretPosition(originalOff + originalLength);
					System.out.println("end of update");
				}

			}

		});

	}

	public void paintComponent(Graphics g) {

		super.paintComponent(g);

	}

}
