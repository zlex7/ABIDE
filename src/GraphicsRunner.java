import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class GraphicsRunner {

	public static void main(String[] args) {
		/*
		try{
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		}
		catch(Exception e){
			e.printStackTrace();
		}
		*/
		Editor editor = new Editor();
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				try{
					editor.runGraphics();
				}
				catch(IOException e){
					e.printStackTrace();
					JOptionPane.showMessageDialog(editor.frame, "An error occurred");
				}
			}
		});

	}

}
