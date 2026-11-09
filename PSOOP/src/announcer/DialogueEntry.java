package announcer;

import java.util.Collections;
import java.util.List;

public class DialogueEntry {
    private final String id;
    private final DialogueCategory category;
    private final List<AnnouncerEvent> triggers;
    private final String text;
    private final boolean highPriority;

    public DialogueEntry(String id, DialogueCategory category, List<AnnouncerEvent> triggers, String text, boolean highPriority) {
        this.id = id != null ? id : "unknown";
        this.category = category != null ? category : DialogueCategory.GENERAL_ROAST;
        this.triggers = triggers != null ? triggers : Collections.emptyList();
        this.text = text != null ? text : "";
        this.highPriority = highPriority;
    }

    public String getId() {
        return id;
    }

    public DialogueCategory getCategory() {
        return category;
    }

    public List<AnnouncerEvent> getTriggers() {
        return triggers;
    }

    public String getText() {
        return text;
    }

    public boolean isHighPriority() {
        return highPriority;
    }

    public boolean matchesEvent(AnnouncerEvent event) {
        if (triggers == null || event == null) return false;
        return triggers.contains(event);
    }
}
