public class TestNPCTradeFlow {
    public static void main(String[] args) {
        // Setup towns
        Town village = new Town(10, 10, "Harveston", VillageType.AGRICULTURAL, GrainType.WHEAT);
        Town city = new Town(40, 40, "Metropolis", true);

        // Setup economy and NPC manager
        EconomySystem es = new EconomySystem(42);
        es.registerTown(city);
        es.registerTown(village);

        NPCManager nm = new NPCManager();
        nm.setEconomySystem(es);
        nm.populateAllTowns(java.util.Arrays.asList(village, city));

        // Create an NPC from the village and give it a job to gather crops and sell at the city
        NPC npc = NPC.createRandom(NPC.NPCType.PEASANT, village);
        npc.setManager(nm);

        double siteX = village.getGridX() + 5;
        double siteY = village.getGridY() + 5;
        NPCJob job = new NPCJob(NPCJob.JobType.GATHER_CROPS, siteX, siteY, city);
        npc.assignJob(job);

        // Record starting gold in village
        SettlementPopulation villagePop = nm.getPopulationForTown(village);
        int startGold = villagePop.getGold();
        System.out.println("Start gold in " + village.getName() + " = " + startGold);

        // Run the NPC until it finishes round-trip (gather -> sell at city -> return and deposit gold)
        double t = 0;
        while ((npc.hasJob() || npc.getCarryingGold() > 0) && t < 300) {
            npc.update(0.5);
            t += 0.5;
        }

        // Ensure any incomes processed to gold for immediate balance
        // (deposit uses addGold directly so this is mostly a safeguard)
        villagePop.processEndOfDay();

        int endGold = villagePop.getGold();
        System.out.println("End gold in " + village.getName() + " = " + endGold);
        System.out.println("Delta gold = " + (endGold - startGold));

        if (endGold > startGold) {
            System.out.println("PASS: Gold returned to village vault after trade.");
        } else {
            System.out.println("FAIL: No gold returned to village vault.");
        }
    }
}