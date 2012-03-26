package generator.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import difflib.*;

public class PatchDiff {

        // Helper method for get the file content
        private static List<String> fileToLines(String filename) {
                List<String> lines = new LinkedList<String>();
                String line = "";
                try {
                        BufferedReader in = new BufferedReader(new FileReader(filename));
                        while ((line = in.readLine()) != null) {
                                lines.add(line);
                        }
                } catch (IOException e) {
                        e.printStackTrace();
                }
                return lines;
        }

        public static String generarPatch(String arg1, String arg2) {
                List<String> original = fileToLines(arg1);
                List<String> revised  = fileToLines(arg2);
                String ret = "";

                Patch patch = DiffUtils.diff(original, revised);
                List<String> unidiff = DiffUtils.generateUnifiedDiff(arg1, arg2, original, patch, 0);
            	for (String string : unidiff)
        			ret += string+"\n";
				return (ret+="\n");
        }
}