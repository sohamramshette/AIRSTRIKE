package announcer;

/**
 * Singleton configuration managing pilot preferences for the AIRSTRIKE Hinglish Announcer.
 */
public class AnnouncerSettings {
    private static final AnnouncerSettings INSTANCE = new AnnouncerSettings();

    private boolean enabled = true;
    private DialogueCategory activeCategory = DialogueCategory.ALL_PACKS;
    private CommentaryFrequency frequency = CommentaryFrequency.NORMAL;
    private boolean voiceEnabled = false;
    private float volume = 0.8f;

    private AnnouncerSettings() {}

    public static AnnouncerSettings getInstance() {
        return INSTANCE;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public DialogueCategory getActiveCategory() {
        return activeCategory;
    }

    public void setActiveCategory(DialogueCategory activeCategory) {
        this.activeCategory = activeCategory != null ? activeCategory : DialogueCategory.ALL_PACKS;
    }

    public CommentaryFrequency getFrequency() {
        return frequency;
    }

    public void setFrequency(CommentaryFrequency frequency) {
        this.frequency = frequency != null ? frequency : CommentaryFrequency.NORMAL;
    }

    public boolean isVoiceEnabled() {
        return voiceEnabled;
    }

    public void setVoiceEnabled(boolean voiceEnabled) {
        this.voiceEnabled = voiceEnabled;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));
    }
}
