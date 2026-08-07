import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;

/**
 * Exercises the GLJPanel native-window teardown path that crashes CED on macOS.
 */
public final class JoglLifecycleProbe {

    private JoglLifecycleProbe() {
    }

    public static void main(String[] args) {
        System.out.println("java.version=" + System.getProperty("java.version"));
        System.out.println("main.thread=" + Thread.currentThread().getName());

        EventQueue.invokeLater(() -> {
            System.out.println("create.thread=" + Thread.currentThread().getName());

            GLProfile profile = GLProfile.get(GLProfile.GL2);
            GLJPanel panel = new GLJPanel(new GLCapabilities(profile));
            JFrame frame = new JFrame("JOGL lifecycle probe");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(panel, BorderLayout.CENTER);
            frame.setSize(480, 320);
            frame.setLocationByPlatform(true);
            frame.setVisible(true);

            Timer timer = new Timer(1500, event -> {
                ((Timer) event.getSource()).stop();
                System.out.println("dispose.thread=" + Thread.currentThread().getName());
                frame.dispose();

                new Timer(1000, exitEvent -> {
                    ((Timer) exitEvent.getSource()).stop();
                    System.out.println("probe.completed="
                            + SwingUtilities.isEventDispatchThread());
                    System.exit(0);
                }).start();
            });
            timer.setRepeats(false);
            timer.start();
        });
    }
}
