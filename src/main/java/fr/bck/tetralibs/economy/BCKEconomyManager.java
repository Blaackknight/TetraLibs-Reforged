package fr.bck.tetralibs.economy;

import fr.bck.tetralibs.core.BCKCore;
import fr.bck.tetralibs.core.BCKLog;
import fr.bck.tetralibs.core.DataWrapper;
import fr.bck.tetralibs.data.BCKUserdata;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;
import java.util.concurrent.*;

public class BCKEconomyManager {
    public static final class Utils {
        // ta couleur
        public static final String color = "§d";

        // récupère la classe qui déclare Utils
        private static final Class<?> PARENT = Utils.class.getDeclaringClass();

        // full = couleur + nom simple de la classe parente
        public static final String full = color + PARENT.getSimpleName();
    }

    private static final String MONEY_KEY = "economy.money";
    private static final String BANK_KEY = "economy.bank";

    // Executor pour planifier les sauvegardes différées
    private static final ScheduledExecutorService SAVE_SCHEDULER = Executors.newSingleThreadScheduledExecutor();
    private static final ConcurrentHashMap<UUID, ScheduledFuture<?>> PENDING_SAVES = new ConcurrentHashMap<>();
    private static final long SAVE_DEBOUNCE_MS = 500; // temps de debounce en ms

    // Accumulateurs de delta par joueur pour regrouper les modifications rapides
    private static final ConcurrentHashMap<UUID, Double> PENDING_MONEY_DELTA = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Double> PENDING_BANK_DELTA = new ConcurrentHashMap<>();

    // ------------------------------------------------------------------
    // === Helpers internes ===
    // ------------------------------------------------------------------

    /**
     * Récupère le DataWrapper pour la clé spécifiée et le joueur.
     */
    private static DataWrapper getWrapper(Player player, String key) {
        return (DataWrapper) BCKUserdata.data(key, player.level(), player);
    }

    /**
     * Lit la valeur stockée ou l'initialise à zéro si elle est absente.
     */
    private static double readOrInit(Player player, String key) {
        DataWrapper dw = getWrapper(player, key);
        if (dw.isNull()) {
            BCKUserdata.data(key, "set", 0.0, player.level(), player);
            return 0.0;
        }
        return dw.getDouble();
    }

    /**
     * Écrit immédiatement la valeur en mémoire (cache) pour la clé.
     */
    private static void write(Player player, String key, double amount) {
        BCKUserdata.data(key, "set", amount, player.level(), player);
    }

    /**
     * Planifie une sauvegarde différée des soldes du joueur.
     */
    private static void scheduleSave(Player player) {
        UUID id = player.getUUID();
        // annule l'écriture précédente si en cours
        ScheduledFuture<?> prev = PENDING_SAVES.get(id);
        if (prev != null && !prev.isDone()) {
            prev.cancel(false);
        }
        // planifie la nouvelle écriture
        ScheduledFuture<?> future = SAVE_SCHEDULER.schedule(() -> {
            // applique et persiste les deltas accumulés
            flushPending(player);
            PENDING_SAVES.remove(id);
        }, SAVE_DEBOUNCE_MS, TimeUnit.MILLISECONDS);
        PENDING_SAVES.put(id, future);
    }

    /**
     * Applique les deltas d'argent et de banque accumulés, puis persiste.
     */
    private static void flushPending(Player player) {
        UUID id = player.getUUID();
        // Money
        Double moneyDelta = PENDING_MONEY_DELTA.remove(id);
        if (moneyDelta != null && moneyDelta != 0.0) {
            double base = readOrInit(player, MONEY_KEY);
            write(player, MONEY_KEY, base + moneyDelta);
        }
        // Bank
        Double bankDelta = PENDING_BANK_DELTA.remove(id);
        if (bankDelta != null && bankDelta != 0.0) {
            double base = readOrInit(player, BANK_KEY);
            write(player, BANK_KEY, base + bankDelta);
        }
    }

    // ------------------------------------------------------------------
    // === Chargement / Sauvegarde des soldes ===
    // ------------------------------------------------------------------

    /**
     * Charge les soldes depuis la base de données. Initialise à zéro si nécessaire.
     */
    public static void loadBalances(Player player, boolean callback) {
        double money = readOrInit(player, MONEY_KEY);
        double bank = readOrInit(player, BANK_KEY);
        if (callback) {
            BCKLog.info(BCKCore.TITLES_COLORS.title(BCKEconomyManager.class), "§7" + player.getStringUUID() + " §ebalances §nloaded§r: §2pocket=§a" + money + " §5bank=§d" + bank);
        }
    }

    /**
     * Variante de loadBalances sans callback.
     */
    public static void loadBalances(Player player) {
        loadBalances(player, false);
    }

    /**
     * Sauvegarde immédiate des soldes dans la base de données.
     * (utilisée par flushPending et saveBalances)
     */
    public static void saveBalances(Player player, boolean callback) {
        double money = readOrInit(player, MONEY_KEY);
        double bank = readOrInit(player, BANK_KEY);
        write(player, MONEY_KEY, money);
        write(player, BANK_KEY, bank);
        if (callback) {
            BCKLog.info(BCKCore.TITLES_COLORS.title(BCKEconomyManager.class), "§7" + player.getStringUUID() + " §ebalances §nsaved§r: §2pocket=§a" + money + " §5bank=§d" + bank);
        }
    }

    /**
     * Variante de saveBalances sans callback.
     */
    public static void saveBalances(Player player) {
        saveBalances(player, false);
    }

    // ------------------------------------------------------------------
    // === Argent en poche avec accumulation ===
    // ------------------------------------------------------------------

    /**
     * Récupère le solde courant d'argent en poche.
     */
    public static double getMoney(Player player) {
        return readOrInit(player, MONEY_KEY);
    }

    /**
     * Définit directement le montant d'argent en poche et planifie la sauvegarde.
     */
    public static void setMoney(Player player, double amount, boolean callback) {
        // écriture directe sans accumulation
        write(player, MONEY_KEY, amount);
        if (callback) {
            BCKLog.info(BCKCore.TITLES_COLORS.title(BCKEconomyManager.class), player.getStringUUID() + " money set to " + amount);
        }
        scheduleSave(player);
    }

    /**
     * Ajoute une somme au solde en poche (accumulée) et planifie la sauvegarde.
     */
    public static void addMoney(Player player, double amount, boolean callback) {
        UUID id = player.getUUID();
        PENDING_MONEY_DELTA.merge(id, amount, Double::sum);
        if (callback) {
            BCKLog.info(BCKCore.TITLES_COLORS.title(BCKEconomyManager.class), player.getStringUUID() + " money queued add " + amount);
        }
        scheduleSave(player);
    }

    /**
     * Retire une somme du solde en poche (accumulée) et planifie la sauvegarde.
     */
    public static void removeMoney(Player player, double amount, boolean callback) {
        UUID id = player.getUUID();
        PENDING_MONEY_DELTA.merge(id, -amount, Double::sum);
        if (callback) {
            BCKLog.info(BCKCore.TITLES_COLORS.title(BCKEconomyManager.class), player.getStringUUID() + " money queued remove " + amount);
        }
        scheduleSave(player);
    }

    /**
     * Vérifie si le solde actuel (persisté + débuts accumulés) est suffisant.
     */
    public static boolean hasMoney(Player player, double amount) {
        double base = readOrInit(player, MONEY_KEY);
        double delta = PENDING_MONEY_DELTA.getOrDefault(player.getUUID(), 0.0);
        return (base + delta) >= amount;
    }

    // ------------------------------------------------------------------
    // === Argent en banque avec accumulation ===
    // ------------------------------------------------------------------

    /**
     * Récupère le solde courant du compte banque.
     */
    public static double getBank(Player player) {
        return readOrInit(player, BANK_KEY);
    }

    /**
     * Définit directement le solde bancaire et planifie la sauvegarde.
     */
    public static void setBank(Player player, double amount, boolean callback) {
        write(player, BANK_KEY, amount);
        if (callback) {
            BCKLog.info(BCKCore.TITLES_COLORS.title(BCKEconomyManager.class), player.getStringUUID() + " bank set to " + amount);
        }
        scheduleSave(player);
    }

    /**
     * Ajoute une somme au solde bancaire (accumulée) et planifie la sauvegarde.
     */
    public static void addBank(Player player, double amount, boolean callback) {
        UUID id = player.getUUID();
        PENDING_BANK_DELTA.merge(id, amount, Double::sum);
        if (callback) {
            BCKLog.info(BCKCore.TITLES_COLORS.title(BCKEconomyManager.class), player.getStringUUID() + " bank queued add " + amount);
        }
        scheduleSave(player);
    }

    /**
     * Retire une somme du solde bancaire (accumulée) et planifie la sauvegarde.
     */
    public static void removeBank(Player player, double amount, boolean callback) {
        UUID id = player.getUUID();
        PENDING_BANK_DELTA.merge(id, -amount, Double::sum);
        if (callback) {
            BCKLog.info(BCKCore.TITLES_COLORS.title(BCKEconomyManager.class), player.getStringUUID() + " bank queued remove " + amount);
        }
        scheduleSave(player);
    }

    /**
     * Vérifie si le solde bancaire actuel (persisté + delta) est suffisant.
     */
    public static boolean hasBank(Player player, double amount) {
        double base = readOrInit(player, BANK_KEY);
        double delta = PENDING_BANK_DELTA.getOrDefault(player.getUUID(), 0.0);
        return (base + delta) >= amount;
    }

    // ------------------------------------------------------------------
    // === Transfert entre comptes ===
    // ------------------------------------------------------------------

    /**
     * Transfère de l'argent du compte bancaire de sender vers receiver.
     * Les modifications sont accumulées avant la persistance.
     */
    public static boolean transferBank(Player sender, Player receiver, double amount, boolean callback) {
        if (!hasBank(sender, amount)) {
            return false;
        }
        // accumulateur négatif pour sender, positif pour receiver
        UUID sId = sender.getUUID();
        UUID rId = receiver.getUUID();
        PENDING_BANK_DELTA.merge(sId, -amount, Double::sum);
        PENDING_BANK_DELTA.merge(rId, amount, Double::sum);
        if (callback) {
            BCKLog.info(BCKCore.TITLES_COLORS.title(BCKEconomyManager.class), sender.getStringUUID() + " bank queued transfer " + amount + " to " + receiver.getStringUUID());
        }
        scheduleSave(sender);
        scheduleSave(receiver);
        return true;
    }

    /**
     * Variante de transferBank sans callback.
     */
    public static boolean transferBank(Player sender, Player receiver, double amount) {
        return transferBank(sender, receiver, amount, false);
    }
}
