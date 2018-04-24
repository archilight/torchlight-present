package com.coreoz.ppt;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xslf.usermodel.XSLFTextShape;

import com.coreoz.ppt.PptTemplates.ImageToReplaceText;
import com.torchlight.poiwiki.PptWikiParser;

import lombok.AllArgsConstructor;

class PptParser {

	static Optional<PptVariable> parse(String text) {
		if(text.startsWith("${") && text.endsWith("}")) {
			int indexStartParameter = text.indexOf(':');
			if(indexStartParameter < 0) {
				return Optional.of(PptVariable.of(text.substring(2, text.length() - 1), null));
			}
			return Optional.of(PptVariable.of(
				text.substring(2, indexStartParameter),
				text.substring(indexStartParameter + 1, text.length() - 1)
			));
		}
		return Optional.empty();
	}

	static void replaceTextVariable(XSLFTextParagraph paragraph, List<ImageToReplaceText> imagesToReplaceText, PptMapper mapper) {
		int indexOfStartVariable = -1;
		
		List<XSLFTextRun> textPartsVariable = null;
		StringBuilder variableName = null;
		StringBuilder variableArgument = null;
		State currentState = State.INITIAL;

		for(XSLFTextRun textPart : paragraph.getTextRuns()) {
			char[] textPartRaw = textPart.getRawText().toCharArray();
			int indexOfChar = 0;

			if(currentState.inVariable) {
				textPartsVariable.add(textPart);
			}

			for(char c : textPartRaw) {
				State nextState = process(currentState, c);

				switch (nextState) {
				case INITIAL:
					if(currentState != State.INITIAL) {
						indexOfStartVariable = -1;
						textPartsVariable = null;
						variableName = null;
						variableArgument = null;
					}

					break;
				case MAY_BE_VARIABLE:
					indexOfStartVariable = indexOfChar;
					textPartsVariable = new ArrayList<>();
					textPartsVariable.add(textPart);

					break;
				case START_VARIABLE:
					variableName = new StringBuilder();

					break;
				case VARIABLE:
					variableName.append(c);

					break;
				case START_ARGUMENT:
					variableArgument = new StringBuilder();

					break;
				case ARGUMENT:
					variableArgument.append(c);

					break;
				case END_VARIABLE:
					
					// JP mods	-------------------------------------------------------------------------
					
					/*
					Optional<String> placeHolderText = mapper.textMapping(variableName.toString(),
							variableArgument == null ? null : variableArgument.toString());
					if (placeHolderText.isPresent()) {
						// process the text element
						System.out.println("****present");
					}
					*/
					
					Optional<PptImageMapper> placeHolderImage = mapper.imageMapping(variableName.toString());
					if (placeHolderImage.isPresent()) {
						// process the text element
						XSLFTextShape toReplace = paragraph.getParentShape();
						//imagesToReplaceText.add(ImageToReplace.of(toReplace, placeHolderImage));
						
						placeHolderImage.ifPresent(image -> imagesToReplaceText.add(ImageToReplaceText.of(toReplace, image) ));
											
						//if(shouldHide(imageVariable, mapper)) {
							//System.out.println("\t\tshould hide is true ");
							//return true;
						//}

					}
						
					// process text everything else
					indexOfChar = replaceVariable(
						indexOfStartVariable,
						indexOfChar,
						mapper.textMapping(
							variableName.toString(),
							variableArgument == null ? null : variableArgument.toString()
						),
						textPartsVariable
					);
					
					break;
				}

				indexOfChar++;
				currentState = nextState;
			}
		}
		
		// mod to use Wiki text
		PptWikiParser parser = new PptWikiParser();  // create Wiki parser
		List<String> contents = new ArrayList<>();
		
		// empty out the previous text runs and add to string array
		for(XSLFTextRun textPart : paragraph.getTextRuns()) {
			contents.add(textPart.getRawText());
			textPart.setText("");
		}
		
		// parse the content for wiki markup and add as new text runs	
		for(String elem : contents) {
			parser.parse(elem);			
			paragraph = parser.format(paragraph);
		}	
		
	}

	/**
	 *
	 * @param indexOfStartVariable The index of the first char of the variable in the first TextRun
	 * @param indexOfEndVariable The index of the last char of the variable in the last TextRun
	 * @param replacedText The value to replace the variable
	 * @param textParts The text parts in which the variable name should be replaced by its value
	 * @return The index of the character in the last text part to continue to search for variable
	 */
	private static int replaceVariable(int indexOfStartVariable, int indexOfEndVariable,
			Optional<String> replacedText, List<XSLFTextRun> textParts) {
		
		
		
		if(!replacedText.isPresent()) {
			return indexOfEndVariable;
		}

		for (int i = 0; i < textParts.size(); i++) {
			XSLFTextRun textPart = textParts.get(i);
			
			if(i == 0) {
				String partContent = textPart.getRawText();
					
				StringBuilder textPartReplaced = new StringBuilder(partContent.substring(0, indexOfStartVariable));
				textPartReplaced.append(replacedText.get());
				if(textParts.size() == 1) {
					textPartReplaced.append(partContent.substring(indexOfEndVariable + 1));
				}
				
				
	    						
				//System.out.println("["+textPart.getRawText()+"]->["+textPartReplaced.toString()+"]");
				textPart.setText(textPartReplaced.toString());
				
				if(textParts.size() == 1) {
					return replacedText.get().length() - 1;
				}
			} else if(i < (textParts.size() - 1)) {
				textPart.setText("");
				//System.out.println("["+textPart.getRawText()+"]->[]");
			} else {
				
				//System.out.println("["+textPart.getRawText()+"]->["+textPart.getRawText().substring(indexOfEndVariable + 1)+"]");
				textPart.setText(textPart.getRawText().substring(indexOfEndVariable + 1));
				return -1;
			}
		}

		throw new RuntimeException("Parsing issue, please report at https://github.com/archilight/torchlight-present/issues");
	}

	private static State process(State before, char c) {
		switch (before) {
		case END_VARIABLE:
		case INITIAL:
			if(c == '$') {
				return State.MAY_BE_VARIABLE;
			}
			break;
		case MAY_BE_VARIABLE:
			if(c == '{') {
				return State.START_VARIABLE;
			}
			break;
		case START_VARIABLE:
			if(c != '{') {
				return State.VARIABLE;
			}
			break;
		case VARIABLE:
			if(c == '}') {
				return State.END_VARIABLE;
			}
			if(c == ':') {
				return State.START_ARGUMENT;
			}
			return State.VARIABLE;
		case START_ARGUMENT:
		case ARGUMENT:
			if(c == '}') {
				return State.END_VARIABLE;
			}
			return State.ARGUMENT;
		}

		return State.INITIAL;
	}

	@AllArgsConstructor
	private static enum State {
		INITIAL(false),
		MAY_BE_VARIABLE(true),
		START_VARIABLE(true),
		VARIABLE(true),
		START_ARGUMENT(true),
		ARGUMENT(true),
		END_VARIABLE(false)
		;

		private boolean inVariable;
	}

}
