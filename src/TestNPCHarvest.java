public class TestNPCHarvest {
    public static void main(String[] args) {
        Town village = new Town(10, 10, "Harveston", VillageType.AGRICULTURAL, GrainType.WHEAT);
        NPC npc = NPC.createRandom(NPC.NPCType.PEASANT, village);
        npc.setManager(new NPCManager());

        double siteX = village.getGridX() + 5;
        double siteY = village.getGridY() + 5;
        NPCJob job = new NPCJob(NPCJob.JobType.GATHER_CROPS, siteX, siteY, village);
        npc.assignJob(job);

        double t = 0;
        boolean printedReturning = false;
        while (!job.isComplete() && t < 60) {
            npc.update(0.5); // half-second updates
            job.update(0.5);
            if (!printedReturning && job.getState() == NPCJob.JobState.RETURNING) {
                printedReturning = true;
                // Allow NPC one more update cycle to process picking up items
                npc.update(0.5);
                System.out.println("Job just moved to RETURNING: resources gathered = " + job.getResourcesGathered());
                System.out.println("NPC inventory used slots (mid-return): " + npc.getInventory().getUsedSlots());
                System.out.println("NPC carryingResources (mid-return): " + npc.getCarryingResources());
            }
            t += 0.5;
        }

        System.out.println("Job complete: resources gathered = " + job.getResourcesGathered());
        System.out.println("NPC inventory used slots: " + npc.getInventory().getUsedSlots());
        System.out.println("NPC carried resource type: " + npc.getCarryingResourceType());
        System.out.println("NPC carryingResources: " + npc.getCarryingResources());
    }
}