public class TestNPCAssertions {
    public static void main(String[] args) {
        Town harveston = new Town(10, 10, "Harveston", VillageType.AGRICULTURAL, GrainType.WHEAT);
        NPC npc = NPC.createRandom(NPC.NPCType.PEASANT, harveston);
        NPCManager nm = new NPCManager();
        npc.setManager(nm);

        double siteX = harveston.getGridX() + 5;
        double siteY = harveston.getGridY() + 5;
        NPCJob job = new NPCJob(NPCJob.JobType.GATHER_CROPS, siteX, siteY, harveston);
        npc.assignJob(job);

        double t = 0;
        boolean sawReturning = false;
        while (!job.isComplete() && t < 120) {
            npc.update(0.5);
            job.update(0.5);
            if (job.getState() == NPCJob.JobState.RETURNING && !sawReturning) {
                // allow NPC to process pickup
                npc.update(0.5);
                sawReturning = true;
                if (npc.getInventory().getUsedSlots() <= 0 || npc.getCarryingResources() <= 0) {
                    throw new RuntimeException("NPC did not pick up resources during RETURNING");
                }
            }
            t += 0.5;
        }

        // Complete: NPC should have delivered (manager handles delivery) at end of job
        // Verify inventory empty after delivery
        if (!npc.getInventory().isEmpty()) {
            throw new RuntimeException("NPC inventory not empty after delivery");
        }

        System.out.println("NPC assertions passed");
    }
}