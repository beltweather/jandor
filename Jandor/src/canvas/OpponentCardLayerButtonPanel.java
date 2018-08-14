package canvas;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;

import ui.pwidget.PLabel;
import ui.pwidget.PPanel;
import ui.pwidget.PRadio;
import util.FriendUtil;
import util.UserUtil;

public class OpponentCardLayerButtonPanel extends AbstractCardLayerButtonPanel {
	
	private ButtonGroup opponentRadioGroup;
	private Map<String, PRadio> opponentRadiosByGUID;
	public OpponentCardLayerButtonPanel(CardLayer layer) {
		super(layer);
	}
	
	@Override
	protected void init() {
		String previousGUID = getOpponentGUID();
		
		PPanel buttonPanel = new PPanel();

		List<String> guids = new ArrayList<String>(FriendUtil.getConnectedUserGUIDS());
		Collections.sort(guids, new Comparator<String>() {

			@Override
			public int compare(String guidA, String guidB) {
				return UserUtil.getUserByGUID(guidA).getFirstName().compareTo(
					   UserUtil.getUserByGUID(guidB).getFirstName());
			}
			
		});
		PRadio radioNone = new PRadio("None");
		radioNone.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				layer.opponentLayer = null;
				layer.repaint();
			}
			
		});
		
		opponentRadiosByGUID = new HashMap<String, PRadio>();
		opponentRadioGroup = new ButtonGroup();
		opponentRadioGroup.add(radioNone);
		
		buttonPanel.c.insets = new Insets(0,5,0,0);
		buttonPanel.addc(new PLabel("Opponent:"));
		buttonPanel.c.gridx++;
		buttonPanel.addc(radioNone);
		buttonPanel.c.gridx++;
		for(final String guid : guids) {
			final PRadio radio = new PRadio(UserUtil.getUserByGUID(guid).getFirstName());
			radio.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					layer.opponentLayer = FriendUtil.getFriendLayer(guid);
					layer.repaint();
				}
				
			});
			
			opponentRadioGroup.add(radio);
			buttonPanel.addc(radio);
			buttonPanel.c.gridx++;
			opponentRadiosByGUID.put(guid, radio);
		
			if(previousGUID == null || (previousGUID != null && guid.equals(previousGUID))) {
				previousGUID = guid;
				radio.setSelected(true);
			}
		}
		radioNone.setSelected(previousGUID == null);
		
		c.strengthen();
		fill();
		c.gridx++;
		c.weaken();
		add(buttonPanel, c);
		
		String guid = getOpponentGUID();
		if(guid != null) {
			layer.opponentLayer = FriendUtil.getFriendLayer(guid);
			layer.repaint();
		}
	}
	
	public String getOpponentGUID() {
		if(opponentRadiosByGUID == null) {
			return null;
		}
		for(String guid : opponentRadiosByGUID.keySet()) {
			if(opponentRadiosByGUID.get(guid).isSelected()) {
				return guid;
			}
		}
		return null;
	}
	
	public void setOpponentGUID(String guid) {
		if(opponentRadiosByGUID.containsKey(guid)) {
			opponentRadiosByGUID.get(guid).setSelected(true);
		}
	}
	
}
