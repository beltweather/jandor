package util;

import java.util.HashMap;
import java.util.Map;

import redis.BinarySubscriber;
import redis.ISubscriber;
import redis.Subscriber;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import session.User;

public class JedisUtil {

	private JedisUtil() {}

	//public static final String host = "redis-13983.c1.us-central1-2.gce.cloud.redislabs.com";
	//public static final int port = 13983;
	public static final String host = "redis-14460.c124.us-central1-1.gce.cloud.redislabs.com";
	public static final int port = 14460;
	public static final String password = "jandor88";
	public static final int timeout = 0;

	private static JedisPool jedisPool;
	private static Jedis jedisWrite;

	private static Map<String, ISubscriber> subscribersByChannel = new HashMap<String, ISubscriber>();

	public static byte[] toBytes(String channel) {
		return SerializationUtil.toBytes(channel);
	}

	public static String toStreamChannel(User user) {
		return toStreamChannel(user.getGUID());
	}

	public static String toStreamChannel(String guid) {
		return "StreamChannel:" + guid;
	}

	public static String toMessageChannel(User user) {
		return toMessageChannel(user.getGUID());
	}

	public static String toMessageChannel(String guid) {
		return "MessageChannel:" + guid;
	}

	public static void init() {
		if(DebugUtil.OFFLINE_MODE) {
			return;
		}

		jedisPool = new JedisPool(new JedisPoolConfig(), host, port, timeout);
		jedisWrite = jedisPool.getResource();
		jedisWrite.auth(password);
	}

	private static Jedis newJedis() {
		Jedis jedis = jedisPool.getResource();
		jedis.auth(password);
		return jedis;
	}

	public static void subscribe(final String channel, final Subscriber subscriber) {
		if(subscribersByChannel.containsKey(channel)) {
			return;
		}
		subscribersByChannel.put(channel, subscriber);

		new Thread() {

			@Override
			public void run() {
				System.out.println("User " + LoginUtil.getUser().getUsername() + " subscribed to " + channel);
				newJedis().subscribe(subscriber, channel);
			}

		}.start();
	}

	public static void unsubscribe(String channel) {
		if(!subscribersByChannel.containsKey(channel)) {
			return;
		}
		subscribersByChannel.get(channel).unsubscribeFromChannel(channel);
		subscribersByChannel.remove(channel);
		System.out.println("User " + LoginUtil.getUser().getUsername() + " unsubscribed from " + channel);
	}

	public static void publish(String channel, String message) {
		jedisWrite.publish(channel, message);
	}

	public static void subscribe(final String channel, final BinarySubscriber subscriber) {
		if(subscribersByChannel.containsKey(channel)) {
			return;
		}
		subscribersByChannel.put(channel, subscriber);

		new Thread() {

			@Override
			public void run() {
				System.out.println("User " + LoginUtil.getUser().getUsername() + " subscribed to " + channel);
				newJedis().subscribe(subscriber, toBytes(channel));
			}

		}.start();
	}

	public static void publish(String channel, byte[] message) {
		jedisWrite.publish(toBytes(channel), message);
	}

	public static void publish(byte[] channel, byte[] message) {
		jedisWrite.publish(channel, message);
	}

}
