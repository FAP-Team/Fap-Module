package generator.utils;

import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

public class Beautifier {
        
        public static String formatear(String code) throws MalformedTreeException, BadLocationException{
        	
        	// take default Eclipse formatting options
    		Map options = DefaultCodeFormatterConstants.getEclipseDefaultSettings();
    		
    		// initialize the compiler settings to be able to format 1.5 code
    		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
    		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
    		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
    		
    		// change the option to wrap each enum constant on a new line
    		options.put(
    			DefaultCodeFormatterConstants.FORMATTER_ALIGNMENT_FOR_ENUM_CONSTANTS,
    			DefaultCodeFormatterConstants.createAlignmentValue(
    				true,
    				DefaultCodeFormatterConstants.WRAP_ONE_PER_LINE,
    				DefaultCodeFormatterConstants.INDENT_ON_COLUMN
    			)
    		);
    		IDocument doc = new Document(code);
        	CodeFormatter form = ToolFactory.createCodeFormatter(options);
        	TextEdit edit = form.format(CodeFormatter.K_COMPILATION_UNIT, doc.get(), 0, doc.get().length(), 0, null);
        	if (edit != null)
        		edit.apply(doc);
        	
       		return doc.get();
        } 
}
