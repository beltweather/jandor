package util;

public class ManaRipper {
	
	private ManaRipper() {}
	
	public static void main(String[] args) {
		CardUtil.init();
		
		for(String mana : CardUtil.getValues("manaCost")) {
			ImageUtil.save(ImageUtil.readImage(ManaUtil.getSmallUrl(mana), mana), mana);
		}
		String mana = "tap";
		ImageUtil.save(ImageUtil.readImage(ManaUtil.getSmallUrl(mana), mana), mana);
		mana = "untap";
		ImageUtil.save(ImageUtil.readImage(ManaUtil.getSmallUrl(mana), mana), mana);
		ImageUtil.save(ImageUtil.readImage(ManaUtil.getSmallUrl("snow"), "snow"), "S");
	}
	
}
