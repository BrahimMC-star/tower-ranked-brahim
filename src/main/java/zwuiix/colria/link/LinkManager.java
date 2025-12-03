package zwuiix.colria.link;

import lombok.Getter;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.util.LinkedHashMap;

@Getter
public class LinkManager {
    @Getter
    private static final LinkManager instance = new LinkManager();

    private final LinkedHashMap<String, LinkInfo> waitingLinks = new LinkedHashMap<>();

    public LinkManager() {}

    public boolean exists(String code) {
        return waitingLinks.containsKey(code);
    }

    public boolean is(String discordId) {
        for (var entry : waitingLinks.entrySet()) {
            if(entry.getValue().discordId().equals(discordId)) {
                return true;
            }
        }
        return false;
    }

    public LinkInfo fromCode(String code) {
        return waitingLinks.get(code);
    }

    public String getCode(String discordId) {
        for (var entry : waitingLinks.entrySet()) {
            if(entry.getValue().discordId().equals(discordId)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void add(String code, LinkInfo info) {
        waitingLinks.put(code, info);
    }

    public void remove(String code) {
        waitingLinks.remove(code);
    }

    public String generateCode(String discordId) {
        String code = String.valueOf((int)(Math.random() * 900000) + 100000);
        while(waitingLinks.containsKey(code)) {
            code = String.valueOf((int)(Math.random() * 900000) + 100000);
        }
        return code;
    }

    public record LinkInfo(String discordId, InteractionHook hook) { }
}
