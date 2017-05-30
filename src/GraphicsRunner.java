import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class GraphicsRunner {

	public static void main(String[] args) {

		Editor editor = new Editor();
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				try{
					editor.runGraphics();
				}
				catch(IOException e){
					JOptionPane.showMessageDialog(editor.frame, "An error occurred");
				}
			}
		});

	}

}
