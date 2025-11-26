package de.kleinanzeigen.app.searchprofile;

public class DuplicateSearchProfileException extends RuntimeException {

    public DuplicateSearchProfileException(String name) {
        super("search profile with name already exists: " + name);
    }
}
