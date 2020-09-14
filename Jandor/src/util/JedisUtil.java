package util;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import redis.BinarySubscriber;
import redis.ISubscriber;
import redis.Subscriber;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;
import session.User;
import ui.pwidget.JUtil;

public class JedisUtil {

	private JedisUtil() {}

	private static String host;
	private static int port;
	private static String password;

	private static final int timeout = 0;

	private static boolean warnedAlready = false;

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

	public static Jedis getPoolResource() {
		try {
			return jedisPool.getResource();
		} catch (JedisConnectionException e) {
			if(!warnedAlready) {
				warnedAlready = true;
				JUtil.showWarningDialog(null, "Jedis Error", "Could not connect to multiplayer service. Multiplayer games will not work correctly. Please tell Jon to log into RedisLab, make sure the Redis service is active and that Jandor has the right URL + Port + Password.");
			}
			return null;
		}
	}

	private static void loadProperties() {
		Properties props = FileUtil.getResourceAsProperties(FileUtil.RESOURCE_REDIS_PROPERTIES);
		host = props.getProperty("host");
		port = Integer.valueOf(props.getProperty("port"));
		password = props.getProperty("password");
	}

	public static void init() {
		if(DebugUtil.OFFLINE_MODE) {
			return;
		}

		loadProperties();

		jedisPool = new JedisPool(new JedisPoolConfig(), host, port, timeout);
		jedisWrite = getPoolResource();
		if(jedisWrite != null) {
			jedisWrite.auth(password);
		}
	}

	private static Jedis newJedis() {
		if(jedisWrite == null) {
			return null;
		}
		Jedis jedis = getPoolResource();
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
				Jedis jedis = newJedis();
				if(jedis != null) {
					jedis.subscribe(subscriber, channel);
				}
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
		if(jedisWrite != null) {
			jedisWrite.publish(channel, message);
		}
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
				Jedis jedis = newJedis();
				if(jedis != null) {
					jedis.subscribe(subscriber, toBytes(channel));
				}
			}

		}.start();
	}

	public static void publish(String channel, byte[] message) {
		if(jedisWrite != null) {
			jedisWrite.publish(toBytes(channel), message);
		}
	}

	public static void publish(byte[] channel, byte[] message) {
		if(jedisWrite != null) {
			jedisWrite.publish(channel, message);
		}
	}

}
