package com.michaelbarany.perka;

public class ApplicationForm {
    String first_name;
    String last_name;
    String email;
    String position_id = "ANDROID";
    String explanation = "https://bitbucket.org/mbarany/android-perka";
    String source;
    String resume;

    /**
     * resume gets validated later
     */
    public boolean validates() {
        return !hasNulls()
            && first_name.length() > 0
            && last_name.length() > 0
            && email.length() > 0
            && source.length() > 0;
    }

    private boolean hasNulls() {
        return null == first_name
            || null == last_name
            || null == email
            || null == source;
    }
}
