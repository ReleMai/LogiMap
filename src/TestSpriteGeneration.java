public class TestSpriteGeneration {
    public static void main(String[] args) {
        SpriteManager sm = SpriteManager.getInstance();
        for (NPCJob.JobType jt : NPCJob.JobType.values()) {
            try {
                java.awt.Toolkit.getDefaultToolkit(); // noop but keeps headless tolerant
            } catch (Throwable ignored) {}
            var frames = sm.getHarvestFrames(jt);
            System.out.println(jt.name() + " frames: " + (frames != null ? frames.length : 0));
            if (frames == null || frames.length == 0) {
                System.out.println("ERROR: No frames for " + jt);
            }
        }
    }
}