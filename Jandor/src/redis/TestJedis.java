package redis;

import redis.clients.jedis.Jedis;
import util.JedisUtil;

public class TestJedis {

	public static void main(String[] args) {
		Jedis jedis = new Jedis(JedisUtil.host, JedisUtil.port);
		jedis.auth(JedisUtil.password);
		
		jedis.set("12345", "hello world");
		System.out.println(jedis.get("12345"));
	}
	
}
