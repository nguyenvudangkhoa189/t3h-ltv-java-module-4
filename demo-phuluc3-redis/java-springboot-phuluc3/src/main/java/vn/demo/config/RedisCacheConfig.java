package vn.demo.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * CONFIG — TTL theo cache name + serialize JSON (syllabus §6).
 *
 * <p><b>Không</b> gắn {@code @EnableCaching} ở đây — đã có trên
 * {@link vn.demo.DemoPhuluc3RedisApplication} (một chỗ là đủ).</p>
 *
 * <p><b>Kiến thức mới — TTL:</b> Time To Live = thời gian sống tối đa của entry.
 * Hết hạn → lần đọc sau là MISS (đọc lại Repository). TTL là lưới an toàn khi
 * quên evict hoặc có đường ghi ngoài Service.</p>
 *
 * <p><b>Kiến thức mới — {@code GenericJackson2JsonRedisSerializer}:</b>
 * lưu value dạng JSON (dễ đọc bằng {@code redis-cli}) thay vì Java serialization
 * mặc định. Cần {@code JavaTimeModule} để serialize {@link java.time.Instant}.</p>
 */
@Configuration
public class RedisCacheConfig {

	@Value("${app.cache.product-ttl-seconds:60}")
	private long productTtlSeconds;

	@Value("${app.cache.hello-ttl-seconds:30}")
	private long helloTtlSeconds;

	/**
	 * Tinh chỉnh từng cache name: {@code hello}, {@code products}.
	 *
	 * <p>Boot vẫn tạo {@code RedisCacheManager}; customizer gắn TTL riêng
	 * thay vì một TTL chung cho mọi cache.</p>
	 */
	@Bean
	public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
		return builder -> builder
				.withCacheConfiguration("hello",
						cacheConfiguration(Duration.ofSeconds(helloTtlSeconds)))
				.withCacheConfiguration("products",
						cacheConfiguration(Duration.ofSeconds(productTtlSeconds)));
	}

	/**
	 * Cấu hình chung: TTL + JSON serializer + không cache giá trị {@code null}
	 * (tránh cache "không tìm thấy" vô tình).
	 */
	private RedisCacheConfiguration cacheConfiguration(Duration ttl) {
		// 1) ObjectMapper hỗ trợ Instant (JavaTime) — không ghi date dạng timestamp số
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

		// 2) Serializer JSON có type info — deserialize đúng Product / List / String
		RedisSerializer<Object> jsonSerializer = new GenericJackson2JsonRedisSerializer(mapper);

		// 3) Gắn TTL + serializer vào RedisCacheConfiguration
		return RedisCacheConfiguration.defaultCacheConfig()
				.entryTtl(ttl)
				.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
				.disableCachingNullValues();
	}

}
