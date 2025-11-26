package de.kleinanzeigen.app.searchprofile;

public class SearchProfileNotFoundException extends RuntimeException {

    public SearchProfileNotFoundException(Long id) {
        super("search profile not found: " + id);
    }
}
