package util;

public enum ShuffleType {

	RANDOM, PLAYER, AZ, ZA, MANA_LH, MANA_HL, RARITY_LH, RARITY_HL;
	
	public String toString() {
		switch(this) {
			case RANDOM: 
				return "Random Shuffle";
			case PLAYER:
				return "Guided Shuffle";
			case AZ:
				return "Name A-Z";
			case ZA:
				return "Name Z-A";
			case MANA_LH:
				return "Mana Low-High";
			case MANA_HL:
				return "Mana High-Low";
			case RARITY_LH:
				return "Rarity Low-High";
			case RARITY_HL:
				return "Rarity High-Low";
		}
		return "";
	}
	
}
