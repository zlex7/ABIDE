import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;
import javax.swing.*;


public class StreamProcessor{

  BufferedReader reader;
  public StreamProcessor(InputStream stream){
    reader = new BufferedReader(new InputStreamReader(stream));
  }

  public void run(){

    JTextArea jTextArea = new JTextArea();
    jTextArea.setWrapStyleWord(true);
    jTextArea.setLineWrap(true);

    JScrollPane jScrollPane = new JScrollPane(jTextArea,
            JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    JDialog dialog = new JDialog();
    JPanel panel = new JPanel();

    panel.add(jScrollPane);
    dialog.add(panel);

    dialog.setVisible(true);

    System.out.println("created dialog");

    String next;
    try{
      while((next = reader.readLine()) != null){
        System.out.println(next);
      }
    } catch(IOException e){
      e.printStackTrace();
    }

  }
}
