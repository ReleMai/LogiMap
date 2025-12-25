public class TestNPCDelivery {
    public static void main(String[] args) {
        EconomySystem es = new EconomySystem(42);
        Town wheat = new Town(0,0,"Wheatville", VillageType.AGRICULTURAL, GrainType.WHEAT);
        es.registerTown(wheat);

        // Sell attempt: should be blocked (town produces this resource)
        ResourceItem grain = new ResourceItem(GrainType.WHEAT, ResourceItem.Quality.COMMON, 10);
        int gold = es.sellToTown(wheat, grain, 5);
        System.out.println("Attempted sell to " + wheat.getName() + " returned gold: " + gold);
        System.out.println("Supply after sell: " + es.getSupplyLevel(wheat, "grain_wheat"));

        // NPC delivery path
        NPCManager nm = new NPCManager();
        nm.setEconomySystem(es);
        NPC npc = NPC.createRandom(NPC.NPCType.PEASANT, wheat);
        nm.handleNPCDelivery(npc, "crops", 8, wheat);

        System.out.println("Supply after NPC delivery: " + es.getSupplyLevel(wheat, "grain_wheat"));

        // --- Quick check for action progress / time multiplier behavior ---
        GameSettings.getInstance().setGameSpeed(1.0);
        GameTime gt = new GameTime();
        ActionProgress ap = new ActionProgress(gt);
        ap.startAction("TestGather", 30, () -> System.out.println("Action done"), null);
        System.out.println("GameTime multiplier during action: " + gt.getTimeMultiplier());
        // Simulate player cycling speed while harvesting
        System.out.println("Player cycles speed presets while action is active...");
        GameSettings.getInstance().cycleGameSpeed();
        gt.setTimeMultiplier(GameSettings.getInstance().getGameSpeed());
        System.out.println("GameTime multiplier after player change: " + gt.getTimeMultiplier());
        // After action completes, reset should not override player's choice
        ap.cancel(); // simulate completion to trigger reset
        System.out.println("GameTime multiplier after action reset: " + gt.getTimeMultiplier());
    }
}