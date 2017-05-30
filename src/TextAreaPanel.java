import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.io.File;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DocumentFilter;
import javax.swing.text.AttributeSet;
import javax.swing.text.Utilities;


public class TextAreaPanel extends JPanel{

	JTextPane textArea = new JTextPane();
	File file;
	boolean isNew = false;
	HashSet<String> keywords = new HashSet<String>();
	StyleContext styleContext = new StyleContext();

	public TextAreaPanel() {

		setLayout(new BorderLayout());
		add(textArea);
		
		Style style = styleContext.addStyle("keywords",null);
		
		StyleConstants.setForeground(style, Color.BLUE);
		

	}

	public TextAreaPanel(HashSet<String> keywords, Style keywordStyle) {

		this();

		this.keywords = keywords;
	}
	
	public TextAreaPanel(HashSet<String> keywords, Style keywordStyle, File file) {

		this();

		this.keywords = keywords;
		
		this.file=file;
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
	
	public File getFile(){
		
		return file;
	}
	
	public String getFileName(){
		
		return file.getName();
		
	}
	
	public String getFilePath(){
		
		return file.getAbsolutePath();
	}

	
	public void setKeywords(HashSet<String> keywords){
		
		this.keywords.addAll(keywords);
		
	}
	
	public void setIsNew(boolean b){
		
		isNew = b;
	}
	
	public boolean getIsNew(){
		
		return isNew;
	}

	public void updateKeywords() throws BadLocationException {

		StyledDocument d = textArea.getStyledDocument();

		Pattern p = Pattern.compile("(?<=\\W)[a-z]+(?=\\W)");

		Matcher m;
		String text = null;
		try {
			text = d.getText(0, d.getLength());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		if (!text.isEmpty()) {
			int i = 0;
			if (Character.isAlphabetic(text.charAt(i))) {
				i++;
				while (Character.isAlphabetic(text.charAt(i))) {
					i++;
				}
				String word = text.substring(0, i);
				System.out.println("word: " + word);
				if (keywords.contains(word)) {
					d.remove(0, i);
					d.insertString(0, word, styleContext.getStyle("keywords"));
				}
			}

			m = p.matcher(text);

			System.out.println("finding keywords: ");

			while (m.find()) {

				String word = m.group();

				System.out.println(keywords);
				System.out.println(word);

				if (keywords.contains(word)) {
					int start = m.start();
					System.out.println("start: " + start);
					d.remove(start, word.length());
					d.insertString(start, word, styleContext.getStyle("keywords"));
				}
				
				
			}
			
			textArea.setStyledDocument(d);
		}

	}



	public void setKeywordListener(){

		
		AbstractDocument d = (AbstractDocument)textArea.getDocument();

		d.setDocumentFilter(new DocumentFilter() {
			
		    @Override
		    public void insertString(FilterBypass fb, int offset, String text, AttributeSet attributeSet) throws BadLocationException {
			
			Pattern p = Pattern.compile("(?<=\\W)[a-z]+(?=\\W)");
			ArrayList<Integer> indexes = new ArrayList<Integer>();

			if (!text.isEmpty()) {
			int i = 0;
			if (Character.isAlphabetic(text.charAt(i))) {
				i++;
				while (Character.isAlphabetic(text.charAt(i))) {
					i++;
				}
				String word = text.substring(0, i);
				System.out.println("word: " + word);
				if (keywords.contains(word)) {
					indexes.add(0);
					indexes.add(i);
				}
			}

			Matcher m = p.matcher(text);

			System.out.println("finding keywords in insertString: ");

			while (m.find()) {

				String word = m.group();

				System.out.println(keywords);
				System.out.println(word);

				if (keywords.contains(word)) {
					int start = m.start();
					System.out.println("start: " + start);
					indexes.add(start);
					indexes.add(m.end());
				}
				
				
			}
			
			if(!indexes.isEmpty()){
				super.insertString(fb,offset,text.substring(0,indexes.get(0)),attributeSet);
				for(i=0;i<indexes.size();i+=2){

					int first = indexes.get(i);
					int second = indexes.get(i+1);

						super.insertString(fb,offset+first,text.substring(first,second),styleContext.getStyle("keywords"));
						
						super.insertString(fb,offset+second,text.substring(second,indexes.get(i+2)),attributeSet);
							
					}
			
		    	}

			else{

				super.insertString(fb,offset,text,attributeSet);

			}

			}



		    }	

		    @Override
		    public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {

			super.remove(fb, offset, length);

			int startIndex = Utilities.getWordStart(textArea,offset);
			int endIndex=  Utilities.getWordEnd(textArea,offset);
			
			String word = d.getText(startIndex,endIndex);

			if(keywords.contains(word)){
				
				d.remove(startIndex,endIndex-startIndex);
				d.insertString(startIndex,word,styleContext.getStyle("keywords"));
			}

		    }

		    @Override
		    public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attributeSet) throws BadLocationException {
			

			super.remove(fb,offset,length);

			ArrayList<Integer> indexes = new ArrayList<Integer>();

			if (!text.isEmpty()) {
			int i = 0;
			if (Character.isAlphabetic(text.charAt(i))) {
				i++;
				while (Character.isAlphabetic(text.charAt(i))) {
					i++;
				}
				String word = text.substring(0, i);
				System.out.println("word: " + word);
				if (keywords.contains(word)) {
					indexes.add(0);
					indexes.add(i);
				}
			}
			
			Pattern p = Pattern.compile("(?<=\\W)[a-z]+(?=\\W)");
			Matcher m = p.matcher(text);

			System.out.println("finding keywords in replace: ");

			while (m.find()) {

				String word = m.group();

				System.out.println(keywords);
				System.out.println(word);

				if (keywords.contains(word)) {
					int start = m.start();
					System.out.println("start: " + start);
					indexes.add(start);
					indexes.add(m.end());
				}
				
				
			}
			
			if(!indexes.isEmpty()){
				super.insertString(fb,offset,text.substring(0,indexes.get(0)),attributeSet);
				for(i=0;i<indexes.size();i+=2){

					int first = indexes.get(i);
					int second = indexes.get(i+1);

						super.insertString(fb,offset+first,text.substring(first,second),styleContext.getStyle("keywords"));
						
						super.insertString(fb,offset+second,text.substring(second,indexes.get(i+2)),attributeSet);
							
					}
			
		    	}

			else{

				super.insertString(fb,offset,text,attributeSet);

			}

			}

    		    }
			    
			    
		});
	}


	public void paintComponent(Graphics g) {

		super.paintComponent(g);

	}

}
