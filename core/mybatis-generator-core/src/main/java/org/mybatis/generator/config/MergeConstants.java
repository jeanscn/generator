package org.mybatis.generator.config;

import java.util.Arrays;

/**
 * This class holds constants useful in the XML and Java merging operations.
 *
 * @author Jeff Butler
 *
 */
public class MergeConstants {

    /**
     * Utility class - no instances.
     *
     */
    private MergeConstants() {
    }

    private static final String[] OLD_XML_ELEMENT_PREFIXES = {
            "ibatorgenerated_", "abatorgenerated_" }; //$NON-NLS-1$ //$NON-NLS-2$

    public static final String NEW_ELEMENT_TAG = "@mbg.generated"; //$NON-NLS-1$
    private static final String[] OLD_ELEMENT_TAGS = {
            "@ibatorgenerated", //$NON-NLS-1$
            "@abatorgenerated", //$NON-NLS-1$
            "@mbggenerated", //$NON-NLS-1$
            NEW_ELEMENT_TAG };

    public static String[] getOldElementTags() {
        return OLD_ELEMENT_TAGS;
    }

    public static boolean idStartsWithPrefix(String id) {
        return Arrays.stream(OLD_XML_ELEMENT_PREFIXES)
                .anyMatch(id::startsWith);
    }

    public static boolean commentContainsTag(String comment) {
        return Arrays.stream(OLD_ELEMENT_TAGS)
                .anyMatch(comment::contains);
    }
}
