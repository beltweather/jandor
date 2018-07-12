package redis;

import redis.clients.jedis.BinaryJedisPubSub;
import util.JedisUtil;

public abstract class BinarySubscriber extends BinaryJedisPubSub implements ISubscriber {

	public abstract void handleOnMessage(byte[] channel, byte[] message);
	
	@Override
	public void onMessage(byte[] channel, byte[] message) {
		handleOnMessage(channel, message);
	}
	
	@Override
	public void unsubscribeFromChannel(String channel) {
		unsubscribe(JedisUtil.toBytes(channel));
	}

}
