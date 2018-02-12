/*
 * Copyright 2014. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.urlmonitor.config;

@SuppressWarnings("unused")
public class MatchPattern {

    public enum PatternType {
        SUBSTRING,
        CASE_INSENSITIVE_SUBSTRING,
        REGEX,
        WORD;

        public static PatternType fromString(String type) {
            if (type.equalsIgnoreCase("substring"))
                return SUBSTRING;
            else if (type.equalsIgnoreCase("caseInsensitiveSubstring"))
                return CASE_INSENSITIVE_SUBSTRING;
            else if (type.equalsIgnoreCase("regex"))
                return REGEX;
            else if (type.equalsIgnoreCase("word"))
                return WORD;
            else
                throw new IllegalArgumentException("Unknown pattern type: " + type);
        }
    }

    private String type = "substring";
    private String pattern;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) { this.pattern = pattern; }
}
