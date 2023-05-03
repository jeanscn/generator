package org.mybatis.generator.api.dom.java.render;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.mybatis.generator.api.dom.OutputUtilities;

public class BodyLineRenderer {

    public List<String> render(List<String> bodyLines) {
        List<String> lines = new ArrayList<>();
        int indentLevel = 1;
        StringBuilder sb = new StringBuilder();

        ListIterator<String> listIter = bodyLines.listIterator();
        while (listIter.hasNext()) {
            sb.setLength(0);
            String line = listIter.next();
            if (line.startsWith("}")) { //$NON-NLS-1$
                indentLevel--;
            }

            OutputUtilities.javaIndent(sb, indentLevel);
            sb.append(line);
            lines.add(sb.toString());

            if (isCodeBlockStartExceptSwitchStatement(line) || line.endsWith(":")) { //$NON-NLS-1$
                indentLevel++;
            }

            if (line.startsWith("break")) { //$NON-NLS-1$
                // if the next line is '}', then don't outdent
                if (listIter.hasNext()) {
                    String nextLine = listIter.next();
                    if (nextLine.startsWith("}")) { //$NON-NLS-1$
                        indentLevel++;
                    }

                    // set back to the previous element
                    listIter.previous();
                }
                indentLevel--;
            }
        }

        return lines;
    }

    private boolean isCodeBlockStartExceptSwitchStatement(String line) {
        return line.endsWith("{") && !line.startsWith("switch"); //$NON-NLS-1$ //$NON-NLS-2$
    }
}
