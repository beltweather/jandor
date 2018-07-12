package ui.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.JLabel;

import ui.pwidget.G;
import ui.pwidget.PPanel;
import ui.pwidget.PScrollPane;
import util.SystemUtil;
import util.VersionUtil;

public class AboutView extends JandorView {

	private PPanel p;
	
	public AboutView() {
		super("Jandor - About", true);
		rebuild();
	}

	@Override
	public void handleClosed() {
		
	}

	@Override
	protected void rebuild() {
		removeAll();
		p = new PPanel();
		
		addHeader("Jandor");
		addLine("Software Version", VersionUtil.VERSION);
		addLine("Data Version", VersionUtil.MTG_JSON_VERSION + " (mtgjson.com)");
		addLine("Created by", "Jon Harter");
		addLine("Date", "4/30/2016");
		c.strengthen();
		add(p, c);
	}
	
	private void addHeader(String header) {
		JLabel headerLabel = new JLabel("<html><h1>" + header + "</h1></html>");
		headerLabel.setForeground(Color.WHITE);
		p.c.gridwidth = 2;
		p.c.anchor = G.CENTER;
		p.add(headerLabel, p.c);
		p.c.gridy++;
		p.c.gridwidth = 1;
	}
	
	private void addLine(String name, String description) {
		JLabel nameLabel = new JLabel("<html><b>" + name + "</b></html>");
		nameLabel.setForeground(Color.WHITE);
		JLabel descriptionLabel = new JLabel("<html><div>" + clean(description) + "</div></html>");
		descriptionLabel.setForeground(Color.WHITE);
		p.c.anchor = G.EAST;
		p.c.insets = new Insets(0, 0, 10, 30);
		p.add(nameLabel, p.c);
		p.c.insets = new Insets(0, 0, 10, 0);
		p.c.anchor = G.WEST;
		p.c.gridx++;
		p.add(descriptionLabel, p.c);
		p.c.gridx--;
		p.c.gridy++;
	}
	
	private String clean(String s) {
		if(SystemUtil.isMac()) {
			return s;
		}
		
		s = s.replace("Right", "Cmd+left");
		s = s.replace("right", "cmd+left");
		s = s.replace("Middle", "Shift+left");
		s = s.replace("middle", "shift+left");
		
		return s;
	}

	@Override
	public void reset() {
		
	}
	
}
