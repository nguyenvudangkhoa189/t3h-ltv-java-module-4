package vn.demo.product.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import vn.demo.product.model.Product;

/**
 * REPOSITORY — đọc/ghi Product trên MongoDB (giống {@code UserRepository} demo-bai4).
 *
 * <p>Spring Data Mongo tự implement CRUD. Không còn in-memory map.</p>
 */
public interface ProductRepository extends MongoRepository<Product, String> {

}
