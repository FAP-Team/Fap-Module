package es.fap.simpleled.ui.contentassist;

import org.eclipse.xtext.ui.editor.contentassist.FQNPrefixMatcher;

public class MyLastSegmentFinder extends FQNPrefixMatcher.DefaultLastSegmentFinder{
	
	public String getLastSegment(String fqn, char delimiter) {
		return fqn;
	}
	
}
