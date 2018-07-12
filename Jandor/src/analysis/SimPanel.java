package analysis;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;

import run.Jandor;
import ui.ProgressBar;
import ui.pwidget.PPanel;

public class SimPanel extends PPanel {

	private LandSimulation sim;
	private JButton startButton;
	private ProgressBar progressPanel;
	private PPanel infoPanel;
	
	private int deckId;
	
	public SimPanel(int deckId) {
		this.deckId = deckId;
		init();
	}
	
	private void init() {
		startButton = new JButton("Recommend Lands");
		startButton.setActionCommand("start");

		progressPanel = new ProgressBar(startButton) {

			@Override
			public void run(ProgressTask task) {
				sim = new LandSimulation(deckId);
				sim.run(task);
			}

			@Override
			public void finished(ProgressTask task) {
				SimPanel.this.finished();
			}
			
		};

		infoPanel = new PPanel();
		
		PPanel panel = new PPanel();
		panel.c.insets(0, 0, 0, 5);
		panel.addc(startButton);
		panel.c.gridx++;
		panel.c.insets(0, 5);
		panel.addc(progressPanel);

		addc(panel);
		c.gridy++;
		c.insets(10, 0, 10);
		addc(infoPanel);
		
		// Add a place holder
		infoPanel.addc(Box.createVerticalStrut(75));
	}
	
	public void finished() {
		if(sim == null) {
			return;
		}
		SimResultList genericResults = sim.getGenericLandResults();
		SimResultList basicResults = sim.getBasicLandResults();
		SimResultList dualResults = sim.getDualLandResults();
		
		infoPanel.removeAll();
		infoPanel.c.reset();

		if(dualResults == null) {
			StringBuilder sb = new StringBuilder();
			sb.append("<html>");
			SimResult bestResult = genericResults.getBestResult();
			for(String land : bestResult.getCountsByLand().keySet()) {
				sb.append(bestResult.getCountsByLand().get(land) + " lands<br>");
			}
			sb.append("</html>");
			JLabel resultLabel = new JLabel(sb.toString());
			infoPanel.addc(resultLabel);
		} else {
			StringBuilder sa = new StringBuilder();
			StringBuilder sb = new StringBuilder();
			sa.append("<html>");
			sb.append("<html>");
			SimResult bestResult = basicResults.getBestResult();
			for(String land : bestResult.getCountsByLand().keySet()) {
				String landText = land;
				sa.append(bestResult.getCountsByLand().get(land) + " " + landText + "<br>");
			}
			bestResult = dualResults.getBestResult();
			for(String land : bestResult.getCountsByLand().keySet()) {
				String landText = land.equals(LandSimulation.ALL_COLORED_LAND) ? "Dual Lands" : land;
				sb.append(bestResult.getCountsByLand().get(land) + " " + landText + "<br>");
			}
			sa.append("</html>");
			sb.append("</html>");
			JLabel resultLabel = new JLabel(sa.toString());
			infoPanel.addc(resultLabel);
			infoPanel.c.gridx++;
			infoPanel.c.insets(0, 10, 0, 10);
			infoPanel.addc(new JLabel("or"));
			infoPanel.c.insets();
			infoPanel.c.gridx++;
			resultLabel = new JLabel(sb.toString());
			infoPanel.addc(resultLabel);
		}

		infoPanel.revalidate();
		infoPanel.repaint();
	}
	
	/**
	* Create the GUI and show it. As with all GUI code, this must run
	* on the event-dispatching thread.
	*/
	private static void createAndShowGUI() {
		//Create and set up the window.
		JFrame frame = new JFrame("ProgressBarDemo");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
		//Create and set up the content pane.
		JComponent newContentPane = new SimPanel(43741);
		newContentPane.setOpaque(true); //content panes must be opaque
		frame.setContentPane(newContentPane);
	
		//Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		
		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Jandor.init();
				createAndShowGUI();
			}
		});
	}
	
}
