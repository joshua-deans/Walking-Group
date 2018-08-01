package ca.cmpt276.walkinggroupindigo.walkinggroup.dataobjects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom class that your group can change the format of in (almost) any way you like
 * to encode the rewards that this user has earned.
 *
 * This class gets serialized/deserialized as part of a User object. Server stores it as
 * a JSON string, so it has no direct knowledge of what it contains.
 * (Rewards may not be used during first project iteration or two)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EarnedRewards {
    private String title = "";
    private List<String> listOfTitlesOwned = new ArrayList<>();
    private List<String> listOfThemesOwned = new ArrayList<>();
    private String selectedTheme = "Default";

    // Needed for JSON deserialization
    public EarnedRewards() {
        listOfThemesOwned.add("Default");
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getListOfTitlesOwned() {
        return listOfTitlesOwned;
    }

    public void addListOfTitlesOwned(String titleToAdd) {
        this.listOfTitlesOwned.add(titleToAdd);
    }

    public String getSelectedTheme() {
        return selectedTheme;
    }

    public void setSelectedTheme(String selectedTheme) {
        this.selectedTheme = selectedTheme;
    }

    public List<String> getListOfThemesOwned() {
        return listOfThemesOwned;
    }

    public void addListOfThemesOwned(String themeToAdd) {
        this.listOfThemesOwned.add(themeToAdd);
    }
}