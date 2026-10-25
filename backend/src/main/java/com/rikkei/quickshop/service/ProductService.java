package com.rikkei.quickshop.service;

import com.rikkei.quickshop.dto.PageResponse;
import com.rikkei.quickshop.dto.ProductDtos.CategoryResponse;
import com.rikkei.quickshop.dto.ProductDtos.ProductRequest;
import com.rikkei.quickshop.dto.ProductDtos.ProductResponse;
import com.rikkei.quickshop.entity.Category;
import com.rikkei.quickshop.entity.Product;
import com.rikkei.quickshop.exception.ApiException;
import com.rikkei.quickshop.repository.CategoryRepository;
import com.rikkei.quickshop.repository.ProductRepository;
import com.rikkei.quickshop.repository.ProductSpecifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
    private static final int MAX_PAGE_SIZE = 100;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(String keyword, Long categoryId, int page, int size, String sort) {
        Specification<Product> spec = ProductSpecifications.isActive();
        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and(ProductSpecifications.nameContains(keyword));
        }
        if (categoryId != null) {
            spec = spec.and(ProductSpecifications.inCategory(categoryId));
        }
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        var pageable = PageRequest.of(Math.max(page, 0), safeSize, toSort(sort));
        return PageResponse.of(productRepository.findAll(spec, pageable), ProductResponse::from);
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return productRepository.findByIdAndActiveTrue(id)
                .map(ProductResponse::from)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy sản phẩm"));
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> categories() {
        return categoryRepository.findAllByOrderByNameAsc().stream().map(CategoryResponse::from).toList();
    }

    @Transactional
    public ProductResponse create(ProductRequest req) {
        Product p = new Product();
        apply(p, req);
        productRepository.save(p);
        log.info("Admin created product id={} name={}", p.getId(), p.getName());
        return ProductResponse.from(p);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest req) {
        Product p = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy sản phẩm"));
        apply(p, req);
        log.info("Admin updated product id={}", id);
        return ProductResponse.from(p);
    }

    @Transactional
    public void delete(Long id) {
        Product p = productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy sản phẩm"));
        p.setActive(false);
        log.info("Admin deleted (soft) product id={}", id);
    }

    private void apply(Product p, ProductRequest req) {
        p.setName(req.name().trim());
        p.setDescription(req.description());
        p.setPrice(req.price());
        p.setStock(req.stock());
        p.setImageUrl(req.imageUrl());
        Category category = null;
        if (req.categoryId() != null) {
            category = categoryRepository.findById(req.categoryId())
                    .orElseThrow(() -> ApiException.badRequest("Danh mục không tồn tại"));
        }
        p.setCategory(category);
    }

    private Sort toSort(String sort) {
        if (sort == null) {
            return Sort.by(Sort.Direction.DESC, "id");
        }
        return switch (sort) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            case "name_asc" -> Sort.by(Sort.Direction.ASC, "name");
            default -> Sort.by(Sort.Direction.DESC, "id");
        };
    }
}
