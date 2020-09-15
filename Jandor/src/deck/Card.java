package deck;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;

import canvas.CardRenderer;
import canvas.IRenderable;
import canvas.IRenderer;
import jackson.AllCardsJson.CardJson;
import jackson.AllSetsJson.SetCardJson;
import util.CardUtil;
import util.DebugUtil;
import util.ImageUtil;
import util.ManaUtil;
import util.PriceUtil.PriceJson;

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
	private String multiname = null;

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

	public String getFullName() {
		if(isMultiName()) {
			return getMultiName();
		}
		return getName();
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

	public CardJson getCardInfo() {
		return CardUtil.getCardInfo(this);
	}

	public boolean exists() {
		return CardUtil.exists(this);
	}

	public List<String> getSets() {
		return getCardInfo().printings;
	}

	public SetCardJson getSetCardInfo() {
		return getSetCardInfo(null);
	}

	public SetCardJson getSetCardInfo(String cardSet) {
		SetCardJson setInfo = null;
		if(cardSet != null) {
			set = cardSet;
		}
		if(set == null && setsByCardName.containsKey(getName())) {
			set = setsByCardName.get(getName());
		}

		if(set == null) {
			setInfo = CardUtil.getSetCardInfo(this, isLand());
			if(setInfo != null) {
				set = setInfo.set;
			}
			if(set != null && !isLand()) {
				setsByCardName.put(getName(), set);
			}
		} else {
			setInfo = CardUtil.getSetCardInfo(this, set);
		}
		return setInfo;
	}

	public int getMultiverseId() {
		if(getSetCardInfo() == null) {
			getSetCardInfo();
			System.out.println("Warning: Multiverse ID not found for " + getName() + " because no set info found");
			return 0;
		}
		int multiverseId = getSetCardInfo().multiverseId;
		if(multiverseId == 0) {
			System.out.println("Warning: Multiverse ID not found for " + getName());
		}
		return multiverseId;
	}

	public boolean isLand() {
		List<String> types = getTypes();
		if(types == null) {
			return false;
		}
		for(String type : types) {
			if(type.equals("Land")) {
				return true;
			}
		}
		return false;
	}

	public boolean isBasicLand() {
		return isLand() && CardUtil.isBasicLandName(getName());
	}

	public List<String> getTypes() {
		return getCardInfo().types;
	}

	public String getType() {
		return getCardInfo().type;
	}

	public String getPower() {
		return getCardInfo().power;
	}

	public String getToughness() {
		return getCardInfo().toughness;
	}

	public String getLoyalty() {
		return getCardInfo().loyalty;
	}

	public String getText() {
		return getCardInfo().text;
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

		//sb.append("<br><span><i>Multiverse ID: " + getMultiverseId() + "</i></span>");
		//sb.append("<br><span><i>Image URL: " + getImageUrl() + "</i></span>");

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
		String manaCost = getCardInfo().manaCost;
		return manaCost == null ? null : ManaUtil.jsonToGatherer(manaCost);
	}

	public List<String> getColorIdentity() {
		return getCardInfo().colorIdentity;
	}

	public int getConvertedManaCost() {
		return (int) getCardInfo().convertedManaCost;
	}

	/*public int getTCGPlayerProductId() {
		return getCardInfo().tcgplayerProductId;
	}*/

	public String getPurchaseUrl() {
		if(getPriceInfo().purchaseUrl != null) {
			return getPriceInfo().purchaseUrl;
		}
		return getCardInfo().tcgplayerPurchaseUrl;
	}

	public PriceJson getPriceInfo() {
		return getCardInfo().priceInfo;
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
		SetCardJson setCard = getSetCardInfo(set);
		if(setCard == null) {
			System.out.println("No set info for " + name);
			getSetCardInfo(set);
		}
		if(setCard == null) {
			return null;
		}
		return setCard.rarity;
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
		String layout = getCardInfo().layout;
		return layout != null && layout.equals("double-faced");
	}

	public String getTransformName() {
		List<String> names = getCardInfo().names;
		if(names == null) {
			return name;
		}
		for(String n : names) {
			if(!n.equals(name)) {
				return n;
			}
		}
		return name;
	}

	public boolean isFirstTransform() {
		List<String> names = getCardInfo().names;
		if(names == null || names.size() <= 1) {
			return true;
		}
		return name.equalsIgnoreCase(names.get(0));
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
		SetCardJson info = CardUtil.getSetCardInfo(getTransformName());
		if(info != null) {
			return ImageUtil.getUrl(info.multiverseId);
		}
		return ImageUtil.getUrl(getCard().getMultiverseId());
	}

	public String getTransformImageAlias() {
		return getTransformName();
	}

	@Override
	public String getImageUrl() {
		if(isTransformed()) {
			SetCardJson info = CardUtil.getSetCardInfo(getTransformName());
			return ImageUtil.getUrl(info.multiverseId);
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
		if(ImageUtil.isCustomImage(getImageAlias()) || DebugUtil.IMAGES_OFFLINE_MODE) {
			String html = getToolTipText();
			return html.substring(6, html.length()-7);
		}
		return ImageUtil.getImageHtml(this);
	}

	public boolean isMultiName() {
		return getCardInfo().names != null && getCardInfo().names.size() > 1;
	}

	public String getMultiName() {
		if(multiname != null) {
			return multiname;
		}
		if(!isMultiName()) {
			return getName();
		}
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for(String name : getCardInfo().names) {
			if(first) {
				sb.append(name);
				first = false;
			} else {
				sb.append(" // " + name);
			}
			multiname = sb.toString();
		}
		return multiname;
	}

	public boolean isWeirdName() {
		if(isMultiName()) {
			for(String n : getCardInfo().names) {
				if(CardUtil.isWeirdName(n)) {
					return true;
				}
			}
			return false;
		}
		return CardUtil.isWeirdName(name);
	}

}
