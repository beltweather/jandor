package redis;

public interface ISubscriber {
	
	public void unsubscribeFromChannel(String channel);

}
