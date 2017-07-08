import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import java.awt.Color;

public class GraphicsRunner {

	public static void main(String[] args) {
		/*
		try{
			UIManager.setLookAndFeel("javax.swing.plaf.synth");
		}
		catch(Exception e){
			e.printStackTrace();
		}
		*/

		UIManager.put("ScrollBar.knob", Color.BLACK);
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
