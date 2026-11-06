package database;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.SwingUtilities;

/**
 * Thread-safe singleton session manager for the AIRSTRIKE application.
 * Manages active player identity, access tokens, and broadcasts state changes
 * to subscribed UI components (e.g., MenuPanel, MainWindow).
 */
public class SessionManager {

    public interface SessionListener {
        void onSessionChanged(UserSession oldSession, UserSession newSession);
    }

    private static volatile SessionManager instance;

    private final AtomicReference<UserSession> activeSession;
    private final List<SessionListener> listeners;

    private SessionManager() {
        this.activeSession = new AtomicReference<>(UserSession.guest());
        this.listeners = new CopyOnWriteArrayList<>();
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager();
                }
            }
        }
        return instance;
    }

    public UserSession getCurrentSession() {
        return activeSession.get();
    }

    public boolean isLoggedIn() {
        UserSession session = activeSession.get();
        if (session == null || !session.isAuthenticated()) {
            return false;
        }
        if (session.isExpired()) {
            clearSession();
            return false;
        }
        return true;
    }

    /**
     * Inspects active session expiration. If expired, automatically clears session,
     * informs listeners, and returns true.
     */
    public boolean checkAndHandleExpiredSession() {
        UserSession session = activeSession.get();
        if (session != null && session.isAuthenticated() && session.isExpired()) {
            clearSession();
            return true;
        }
        return false;
    }

    public String getCurrentUsername() {
        return activeSession.get().getUsername();
    }

    public String getCurrentUserId() {
        return activeSession.get().getUserId();
    }

    public void setSession(UserSession newSession) {
        if (newSession == null) {
            newSession = UserSession.guest();
        }
        UserSession oldSession = activeSession.getAndSet(newSession);
        notifyListeners(oldSession, newSession);
    }

    public void clearSession() {
        setSession(UserSession.guest());
    }

    public void addListener(SessionListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(SessionListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
    }

    private void notifyListeners(UserSession oldSession, UserSession newSession) {
        Runnable notifier = () -> {
            for (SessionListener listener : listeners) {
                try {
                    listener.onSessionChanged(oldSession, newSession);
                } catch (Exception e) {
                    System.err.println("[SessionManager] Error notifying listener: " + e.getMessage());
                }
            }
        };

        if (SwingUtilities.isEventDispatchThread()) {
            notifier.run();
        } else {
            SwingUtilities.invokeLater(notifier);
        }
    }
}

