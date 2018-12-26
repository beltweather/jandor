package deck;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;

import canvas.CardRenderer;
import canvas.IRenderable;
import canvas.IRenderer;
import json.JSONArray;
import json.JSONException;
import json.JSONObject;
import util.CardUtil;
import util.ImageUtil;
import util.ManaUtil;
import util.MtgJsonUtil;

public class Card extends CardRenderer implements IRenderable<Card> {

	private static final long serialVersionUID = 1L;

	private static Map<String, String> setsByCardName = new HashMap<String, String>();

	private String name;
	private String set;
	private String tooltipText = null;
	private transient JLabel tooltipLabel = null;
	private boolean transformed = false;
	private Card transformCard = null;
	private boolean commander = false;

	public Card() {
		this(null);
	}

	public Card(String name) {
		this.setName(name);
		setObject(this);
	}

	@Override
	public IRenderer<Card> getRenderer() {
		return this;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public int hashCode() {
		return getName() == null ? -1 : getName().hashCode();
	}

	public Card copyRenderable() {
		Card c = new Card(getName());
		c.getRenderer().setLocation(getRenderer().getLocation());
		c.getRenderer().setFaceUp(getRenderer().isFaceUp());
		c.setCommander(commander);
		c.setSet(set);
		return c;
	}

	public JSONObject getCardInfo() {
		return CardUtil.getCardInfo(this);
	}

	public boolean exists() {
		return CardUtil.exists(this);
	}

	public String getCardString(String field) {
		try {
			if(!getCardInfo().has(field)) {
				return null;
			}
			return String.valueOf(getCardInfo().get(field));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public int getCardInt(String field) {
		try {
			if(!getCardInfo().has(field)) {
				return -1;
			}
			return getCardInfo().getInt(field);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public double getCardDouble(String field) {
		try {
			if(!getCardInfo().has(field)) {
				return -1;
			}
			return getCardInfo().getDouble(field);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public JSONArray getCardArray(String field) {
		try {
			if(!getCardInfo().has(field)) {
				return null;
			}
			return getCardInfo().getJSONArray(field);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}


	public JSONArray getSets() {
		return CardUtil.getCardSets(this);
	}

	public List<String> getSetsAsList() {
		JSONArray jsonArray = getSets();
		List<String> list = new ArrayList<String>();
		try {
			for(int i = 0; i < jsonArray.length(); i++) {
				list.add(jsonArray.getString(i));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return list;
	}


	public JSONObject getSetCardInfo() {
		return getSetCardInfo(null);
	}

	public JSONObject getSetCardInfo(String cardSet) {
		JSONObject setInfo = null;
		if(cardSet != null) {
			set = cardSet;
		}
		if(set == null && setsByCardName.containsKey(getName())) {
			set = setsByCardName.get(getName());
		}

		if(set == null) {
			setInfo = CardUtil.getSetCardInfo(this, isLand());
			if(setInfo != null) {
				try {
					set = setInfo.getString("set");
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			if(!isLand()) {
				setsByCardName.put(getName(), set);
			}
		} else {
			setInfo = CardUtil.getSetCardInfo(this, set);
		}
		return setInfo;
	}

	public String getSetString(String field) {
		return getSetString(null, field);
	}

	public String getSetString(String cardSet, String field) {
		try {
			if(getSetCardInfo(cardSet) == null || !getSetCardInfo(cardSet).has(field)) {
				return null;
			}
			return getSetCardInfo(cardSet).getString(field);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public int getSetInt(String field) {
		return getSetInt(null, field);
	}

	public int getSetInt(String cardSet, String field) {
		try {
			if(getSetCardInfo(cardSet) == null || !getSetCardInfo(cardSet).has(field)) {
				return -1;
			}
			return getSetCardInfo(cardSet).getInt(field);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return -1;
	}

	public JSONArray getSetArray(String field) {
		return getSetArray(null, field);
	}

	public JSONArray getSetArray(String cardSet, String field) {
		try {
			if(!getSetCardInfo(cardSet).has(field)) {
				return null;
			}
			return getSetCardInfo(cardSet).getJSONArray(field);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	public int getMultiverseId() {
		return getSetInt(MtgJsonUtil.multiverseId);
	}

	public boolean isLand() {
		JSONArray types = getTypes();
		if(types == null) {
			return false;
		}
		try {
			for(int i = 0; i < types.length(); i++) {
				if(types.getString(i).equals("Land")) {
					return true;
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean isBasicLand() {
		return isLand() && CardUtil.isBasicLandName(getName());
	}

	public JSONArray getTypes() {
		return getCardArray("types");
	}

	public String getType() {
		return getCardString("type");
	}

	public String getPower() {
		return getCardString("power");
	}

	public String getToughness() {
		return getCardString("toughness");
	}

	public String getLoyalty() {
		return getCardString("loyalty");
	}

	public String getText() {
		return getCardString("text");
	}

	public String getToolTipText() {
		return getToolTipText(200);
	}

	public String getToolTipText(int width) {
		return getToolTipText(width, true);
	}

	public String getToolTipText(int width, boolean insertImages) {
		return getToolTipText(width, insertImages, true);
	}

	public String getToolTipText(int width, boolean insertImages, boolean includeTransform) {
		/*if(tooltipText != null) {
			return tooltipText;
		}*/

		if(getCardInfo() == null) {
			return null;
		}

		StringBuilder sb = new StringBuilder();

		sb.append("<b>" + getName() + "</b>");
		String text = getText();
		List<String> manaCost = getManaCost();
		String type = getType();
		String power = getPower();
		String toughness = getToughness();
		String set = getSetName();
		String loyalty = getLoyalty();

		if(manaCost != null) {
			sb.append(" ");
			if(insertImages) {
				for(String mana : manaCost) {
					sb.append("<span>" + ManaUtil.getSmallHtml(mana) + "</span>");
				}
			} else {
				for(String mana : manaCost) {
					sb.append("<span>" + "{" + mana + "}" + "</span>");
				}
			}
		}

		if(type != null) {
			sb.append("<br><span>" + type + "</span>");
		}

		if(power != null) {
			sb.append("<span> (" + power + "/" + toughness + ")</span>");
		}

		if(loyalty != null) {
			sb.append("<span> (" + loyalty + ")</span>");
		}

		if(set != null) {
			sb.append("<br><span><i>" + set + " - " + getRarity() + "</i></span>");
		}

		if(text != null) {
			if(insertImages) {
				text = CardUtil.toHtml(text); //ManaUtil.insertManaSymbols(text);
			}
			sb.append("<hr>" + text);

			if(includeTransform && canTransform()) {
				Card transform = getTransformCard();
				String tt = transform.getToolTipText(width, insertImages, false);
				tt = tt.replace("<html>", "").replace("</html>", "");
				sb.append("<br><br>" + tt);
			} else if(!includeTransform) {
				return sb.toString();
			}

			if(width == -1) {
				tooltipText = "<html><div>" + sb.toString() + "</div></html>";
				return tooltipText;
			}
			tooltipText = "<html><div \"width=" + width + "px\">" + sb.toString() + "</div></html>";
			return tooltipText;
		}

		tooltipText = "<html>" + sb.toString() + "</html>";
		return tooltipText;
	}

	public String getSearchableString() {
		if(getCardInfo() == null) {
			return "";
		}

		StringBuilder sb = new StringBuilder();

		sb.append(getName());
		String text = getText();
		List<String> manaCost = getManaCost();
		String type = getType();
		String power = getPower();
		String toughness = getToughness();
		String set = getSetName();
		String loyalty = getLoyalty();
		String rarity = getRarity();

		if(manaCost != null) {
			sb.append(" ");
			for(String mana : manaCost) {
				sb.append("{" + mana + "}");
			}
		}

		if(type != null) {
			sb.append(" " + type);
		}

		if(power != null) {
			sb.append(" " + power + "/" + toughness);
		}

		if(loyalty != null) {
			sb.append(" " + loyalty);
		}

		if(set != null) {
			sb.append(" " + set);
		}

		if(text != null) {
			sb.append(" " + text);
		}

		if(rarity != null) {
			sb.append(" " + rarity);
		}

		return sb.toString().toLowerCase();
	}

	public List<String> getManaCost() {
		String manaCost = getCardString("manaCost");
		return manaCost == null ? null : ManaUtil.jsonToGatherer(manaCost);
	}

	public JSONArray getColorIdentity() {
		return getCardArray("colorIdentity");
	}

	public int getConvertedManaCost() {
		return (int) Math.floor(getCardDouble(MtgJsonUtil.cmc));
	}

	public String getSetName() {
		if(set == null) {
			getSetCardInfo();
		}
		return CardUtil.getSetName(set);
	}

	public String getSet() {
		return set;
	}

	public void setSet(String set) {
		this.set = set;
	}

	public JLabel getToolTipLabel() {
		if(tooltipLabel == null) {
			tooltipLabel = new JLabel(getToolTipText(500));
		}
		return tooltipLabel;
	}

	public boolean isCommon() {
		return isCommon(null);
	}

	public boolean isCommon(String set) {
		String rarity = getRarity(set);
		return rarity == null || rarity.equals("Common");
	}

	public boolean isUncommon() {
		return isUncommon(null);
	}

	public boolean isUncommon(String set) {
		String rarity = getRarity(set);
		return rarity == null || rarity.equals("Uncommon");
	}

	public boolean isRare() {
		return isRare(null);
	}

	public boolean isRare(String set) {
		String rarity = getRarity(set);
		return rarity == null || rarity.equals("Rare");
	}

	public boolean isMythic() {
		return isMythic(null);
	}

	public boolean isMythic(String set) {
		String rarity = getRarity(set);
		return rarity == null || rarity.equals("Mythic Rare");
	}

	public String getRarity() {
		return getRarity(null);
	}

	public String getRarity(String set) {
		String rarity = getSetString("rarity");
		return rarity == null ? "" : rarity;
	}

	public void setCommander(boolean commander) {
		this.commander = commander;
	}

	public boolean isCommander() {
		return commander;
	}

	public void setTransformed(boolean transformed) {
		this.transformed = transformed;
	}

	public boolean isTransformed() {
		return transformed;
	}

	public boolean canTransform() {
		String layout = getCardString("layout");
		return layout != null && layout.equals("double-faced");
	}

	public String getTransformName() {
		JSONArray names = getCardArray("names");
		if(names == null) {
			return name;
		}
		try {
			for(int i = 0; i < names.length(); i++) {
					if(!names.getString(i).equals(name)) {
						return names.getString(i);
					}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return name;
	}

	public boolean isFirstTransform() {
		JSONArray names = getCardArray("names");
		if(names == null || names.length() <= 1) {
			return true;
		}
		try {
			return name.equalsIgnoreCase(names.getString(0));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return true;
	}

	public Card getTransformCard() {
		if(transformCard == null) {
			transformCard = new Card(getTransformName());
			transformCard.setCommander(commander);
		}
		return transformCard;
	}

	@Override
	public void toggleFaceUp() {
		if(!canTransform()) {
			super.toggleFaceUp();
			return;
		}

		if(!isFaceUp()) {
			setTransformed(false);
			setFaceUp(true);
		} else {
			if(isTransformed()) {
				setTransformed(false);
				setFaceUp(false);
			} else {
				setTransformed(true);
			}
		}
	}

	public String getTransformImageUrl() {
		JSONObject info = CardUtil.getSetCardInfo(getTransformName());
		try {
			int id = info.getInt(MtgJsonUtil.multiverseId);
			return ImageUtil.getUrl(id);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return ImageUtil.getUrl(getCard().getMultiverseId());
	}

	public String getTransformImageAlias() {
		return getTransformName();
	}

	@Override
	public String getImageUrl() {
		if(isTransformed()) {
			JSONObject info = CardUtil.getSetCardInfo(getTransformName());
			try {
				int id = info.getInt(MtgJsonUtil.multiverseId);
				return ImageUtil.getUrl(id);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return ImageUtil.getUrl(getCard().getMultiverseId());
	}

	@Override
	public String getImageAlias() {
		if(isTransformed()) {
			return getTransformName();
		}
		return getCard().getName();
	}

	public String getImageHtml() {
		return ImageUtil.getImageHtml(this);
	}

}
