public class TestSpriteSheetsExist {
    public static void main(String[] args) {
        boolean ok = true;
        try {
            javafx.application.Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {}

        for (NPCJob.JobType jt : NPCJob.JobType.values()) {
            String name = "assets/sprites/harvest_" + jt.name().toLowerCase() + ".png";
            java.io.File f = new java.io.File(name);
            if (!f.exists()) {
                System.out.println("Missing sheet: " + name);
                ok = false;
                continue;
            }
            try {
                javafx.scene.image.Image img = new javafx.scene.image.Image(f.toURI().toString());
                int w = (int)Math.round(img.getWidth());
                int h = (int)Math.round(img.getHeight());
                if (w <= 0 || h <= 0) {
                    System.out.println("Invalid dimensions for " + name);
                    ok = false;
                    continue;
                }
                if (w % h != 0) {
                    System.out.println("Sheet " + name + " width not a multiple of height (frames) -> " + w + "x" + h);
                    ok = false;
                } else {
                    System.out.println("OK: " + name + " (" + (w/h) + " frames)");
                }
            } catch (Exception e) {
                System.out.println("Error loading sheet " + name + ": " + e);
                ok = false;
            }
        }
        if (!ok) System.exit(2);
    }
}