package redis;

import redis.clients.jedis.JedisPubSub;

public abstract class Subscriber extends JedisPubSub implements ISubscriber {

	@Override
	public void onSubscribe(String channel, int subscribedChannels) {
		
	}

	@Override
	public void onUnsubscribe(String channel, int subscribedChannels) {
		
	}

	@Override
	public void onPUnsubscribe(String pattern, int subscribedChannels) {
		
	}

	@Override
	public void onPSubscribe(String pattern, int subscribedChannels) {
		
	}

	@Override
	public void onPMessage(String pattern, String channel, String message) {
		
	}
	
	@Override
	public void unsubscribeFromChannel(String channel) {
		unsubscribe(channel);
	}

}
