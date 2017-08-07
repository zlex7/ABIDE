import javax.swing.JPanel;
import javax.swing.JLabel;

import java.awt.BorderLayout;

public class TitlePanel extends JPanel{
		

	private JLabel label;

	public TitlePanel(){

		super(new BorderLayout());

	}

	public void setLabel(JLabel label){

		this.label = label;
	}

	public JLabel getLabel(){

		return label;

	}


}