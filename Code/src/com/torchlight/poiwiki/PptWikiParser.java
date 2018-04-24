package com.torchlight.poiwiki;
/*
 * This program and the accompanying materials
 * are made available under the terms of the License
 * which accompanies this distribution in the file LICENSE.txt
 */

import java.util.LinkedList;

import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;




/**
 * Class to take a text String with Wiki Mark up and parse this into array of tokens.  
 * Class then provides function to format list into TextRuns
 * 
 * @author Jeff Parker
 * 
 * See Licence.
 *
 */
public class PptWikiParser {
	
	// Structure to hold parsed tokens
	public class Token
	{
	    public final String sequence;
	    public final MarkupNodeType type;
	    
	    public Token(MarkupNodeType type, String sequence)
	    {
	      super();
	      this.type = type;
	      this.sequence = sequence;
	    }
	    
	}
	
	private LinkedList<Token> tokens;
	
	private void addToken(MarkupNodeType type, String sequence)
	{
		if (sequence.length() > 0 ) {
			tokens.add(new Token(type,sequence));
		}
		else if (type != MarkupNodeType.text) {
			tokens.add(new Token(type,sequence));
		}
	}
	
	public PptWikiParser() { // initialise parser
	    tokens = new LinkedList<Token>();
	}
	
	
	public String getTypeName(Token token)
	{
		switch (token.type) {
		case strong : 	return "strong"; 
		case emphasis : return "emphasis";
		case delete : 	return "delete";
		case text: 		return "text";
		case strike:	return "strike";
		case bullet:	return "bullet";
		case endofline: return "endofline";
		default : 		return "none";
		}
	}
	
	public MarkupNodeType getType(final String deliminator)
	{
		switch (deliminator) {
		case "*" : return MarkupNodeType.strong;
		case "_" : return MarkupNodeType.emphasis;
		case "~" : return MarkupNodeType.delete;
		default :  return MarkupNodeType.text;
		}
	}
	
	public Boolean is_deliminator(final String deliminator)
	{
		switch (deliminator) {
		case "*" : 
		case "_" : 
		case "~" : return true;
		default :  return false;
		}
	}
	
	public Boolean is_bullet(final String c)
	{
		if (c!= null) {
			switch (c) {
			case "*" : return true;
			default :  return false;
			}
		}
		else
			return false;
	}
	 
	public Boolean is_whitespace(final String c)
	{
		if (c!= null) {
			switch (c) {
			case " " : 
			case "\t": 
			case "\r":
			case "":
			case "\n" : return true;
			default :  return false;
			}
		}
		else
			return true;
	}
	  
	/**
	 * Parse string for deliminators and output as tokens.  
	 * Each deliminator must have whitespace before or after
	 * @param str
	 */
	public void parse(String str)
	{
	    tokens.clear();
		String[] lines = str.split("\n");
		for (int j=0; j < lines.length; j++) {
			
			input =lines[j];
			currentIndex = 0;
			String cToken = "";
			String lastChar = "";
			String c = nextChar();
			
//			System.out.println(">>"+input+ "<<");
			
			if (is_bullet(c)) {
				// first character contains * then its a bullet
				addToken(MarkupNodeType.bullet,"");
				c = nextChar(); // skip the deliminator
			}
			
			while (c != null ) {
				if (is_deliminator(c) && is_whitespace(lastChar)) {
					addToken(MarkupNodeType.text,cToken);
					cToken = "";
					parse_quote(c);
				}
				else
					cToken = cToken + c;
				lastChar = c;
				c = nextChar();
			}
			addToken(MarkupNodeType.text,cToken);
			if (lines.length > 1)
				// add new line
				tokens.add(new Token(MarkupNodeType.endofline,""));
			
		}
		
	}

	private int currentIndex = 0;
	private String input;
	
	/**
	 * Return next Character in input string
	 * @return String
	 */
	private  String nextChar()
	{
		String c;
		if (currentIndex > input.length()-1 ) {
			return null;
		}
		else {
			c = input.substring(currentIndex, currentIndex+1);
			currentIndex = currentIndex + 1;
			return c;
		}
		
	}
	
	/**
	 * Return next character without advancing
	 * @return String
	 */
	private  String peekChar()
	{
		String c;
		if (currentIndex > input.length()-1 ) {
			return null;
		}
		else {
			c = input.substring(currentIndex, currentIndex+1);
			return c;
		}
		
	}
	
	/**
	 * Find matching quote for supplied deliminator in string
	 * @param delim
	 */
	void parse_quote(String delim) {
		String c = nextChar();
		String myToken = "";
		while (c != null) {
			if ( c.equals(delim)) {
				// found matching deliminator output as token and return
				if ( is_whitespace( peekChar() ) ){
					addToken(getType(delim),myToken);
					return;
				}
			}
			myToken = myToken + c;
			c = nextChar();
		}
		
		// reached end of text, so output text as text token
		addToken(MarkupNodeType.text,delim+myToken);
		
	}  
	
	/**
	 * Return token structure
	 * 
	 * @return LikedList<token>
	 */
	public LinkedList<Token> getTokens()
	{
	    return tokens;
	}
	
	/**
	 * Formats TextPart according to Token list and adds to paragraph
	 * @param paragraph
	 */	
	public XSLFTextParagraph format(XSLFTextParagraph paragraph) {
		
		XSLFTextRun thisTextPart = null;
		
		for (Token token : tokens) {
			// set Text part
			thisTextPart = paragraph.addNewTextRun();
			thisTextPart.setText(token.sequence);
			
			// do formatting based on token type
			switch (token.type) {
			case strong : 	 
				thisTextPart.setBold(true);
				break;
			case emphasis : 
				thisTextPart.setItalic(true);
				break;
			case delete : 	
			case strike:
				thisTextPart.setStrikethrough(true);
				break;
			case text: 		
				// leave text as is
				break;
			case bullet:	
				paragraph.setIndentLevel(1);
				break;
			case endofline:
				paragraph = paragraph.getParentShape().addNewTextParagraph();
			default : 		break;
			}
			//System.out.println("[" + getTypeName(token) + "][" + token.sequence+"]");
			
		}
		
		return paragraph;
	}
	
	
}
