package announcer;

public enum DialogueCategory {
    ALL_PACKS("All Packs (Ultimate Hinglish Chaos)"),
    DESI_MEME("Desi Viral Memes"),
    INSTAGRAM_GENZ("Instagram Reels & Gen-Z Slang"),
    BOLLYWOOD("Bollywood Classics & Comedy"),
    GENERAL_ROAST("Toxic Pilot Roasts & Sarcasm"),
    VICTORY_PRAISE("Exaggerated Victory & Praise"),
    RANDOM_INTERRUPT("Bizarre Space Interruptions");

    private final String displayName;

    DialogueCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static DialogueCategory fromString(String str) {
        if (str == null) return ALL_PACKS;
        try {
            return DialogueCategory.valueOf(str.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            for (DialogueCategory cat : values()) {
                if (cat.name().equalsIgnoreCase(str) || cat.displayName.equalsIgnoreCase(str)) {
                    return cat;
                }
            }
            return ALL_PACKS;
        }
    }
}

