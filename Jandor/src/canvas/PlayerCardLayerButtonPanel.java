package canvas;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ButtonGroup;

import ui.pwidget.JUtil;
import ui.pwidget.PButton;
import ui.pwidget.PPanel;
import ui.pwidget.PRadio;

public class PlayerCardLayerButtonPanel extends AbstractCardLayerButtonPanel {
	
	private ButtonGroup opponentRadioGroup;
	private Map<String, PRadio> opponentRadiosByGUID = new HashMap<String, PRadio>();
	
	public PlayerCardLayerButtonPanel(CardLayer layer) {
		super(layer);
	}
	
	@Override
	protected void init() {
		PButton untap = new PButton("Untap");
		untap.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				layer.untap();
			}
			
		});
		
		PButton draw = new PButton("Draw");
		draw.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				layer.draw();
			}
			
		});
		
		PButton shuffle = new PButton("Shuffle");
		shuffle.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				layer.shuffle();
			}
			
		});
		
		PButton discardRandom = new PButton("Discard Random");
		discardRandom.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				layer.discardRandom();
			}
			
		});

		PButton newGame = new PButton("New Game");
		newGame.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(JUtil.showConfirmYesNoCancelDialog(layer.getCanvas(), "Start New Game", "Are you sure you want to start a new game?")) {
					layer.reset();
				}
			}
			
		});
		
		PButton invite = new PButton("+ Invite");
		invite.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				layer.getMenuBar().actionViewFriends();
			}
			
		});
		
		int topMargin = 5;
		int buttonMargin = 10;
		
		PPanel metaPanel = new PPanel();
		metaPanel.c.insets(topMargin);
		metaPanel.addc(newGame);
		metaPanel.c.insets(topMargin, buttonMargin);
		metaPanel.c.gridx++;
		metaPanel.addc(invite);
		
		PPanel mainPanel = new PPanel();
		mainPanel.c.insets(topMargin);
		mainPanel.addc(untap);
		mainPanel.c.insets(topMargin, buttonMargin);
		mainPanel.c.gridx++;
		mainPanel.addc(draw);
		mainPanel.c.gridx++;
		mainPanel.addc(shuffle);
		mainPanel.c.gridx++;
		mainPanel.addc(discardRandom);
		
		PPanel subPanel = new PPanel();
		subPanel.c.insets(topMargin, 0);
		subPanel.addc(shuffle);
		subPanel.c.insets(topMargin, buttonMargin);
		subPanel.c.gridx++;
		subPanel.addc(discardRandom);
		
		c.weaken();
		add(metaPanel, c);
		c.gridx++;
		fill();
		c.gridx++;
		add(mainPanel, c);
		c.gridx++;
		fill();
		c.gridx++;
		add(subPanel, c);
	}
	
}
