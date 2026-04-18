package gr1mly4memes.parallix.launcher.install;

public class ProgressBar {
    private final int total;
    private int current;
    private final String label;
    private final int barWidth;

    public ProgressBar(int total, String label) {
        this.total = total;
        this.current = 0;
        this.label = label;
        this.barWidth = 40;
    }

    public synchronized void update() {
        current++;
        if (current % Math.max(1, total / 20) == 0 || current == total) {
            print();
        }
    }

    public synchronized void finish() {
        current = total;
        print();
        System.out.println();
    }

    private void print() {
        double progress = (double) current / total;
        int filled = (int) (progress * barWidth);
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < barWidth; i++) {
            if (i < filled) {
                bar.append("=");
            } else if (i == filled) {
                bar.append(">");
            } else {
                bar.append(" ");
            }
        }
        bar.append("] ");
        bar.append(String.format("%.0f%%", progress * 100));
        bar.append(" (");
        bar.append(current);
        bar.append("/");
        bar.append(total);
        bar.append(") ");
        bar.append(label);
        
        System.out.print("\r" + bar);
        System.out.flush();
    }
}
