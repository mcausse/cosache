package io.homs.gitman.controller;

public class Utils {

    public String truncateBranchNameIfTooLong(String branchName) {
        return truncateBranchNameIfTooLong(branchName, 40);
    }

    public String truncateBranchNameIfTooLong(String branchName, int maxLenght) {
        if (branchName.length() > maxLenght) {
            if (branchName.startsWith("feature/")) {
                branchName = branchName.replaceAll("feature/", "f/");
            }
        }
        if (branchName.length() > maxLenght) {
            if (branchName.endsWith("-deployable")) {
                branchName = branchName.replaceAll("-deployable", "-d");
            }
        }
        return truncate(branchName, maxLenght);
    }

    public String truncate(String text, int maxLength) {
        if (text.length() > maxLength) {
            return text.substring(0, maxLength) + "[...]";
        }
        return text;
    }
}
