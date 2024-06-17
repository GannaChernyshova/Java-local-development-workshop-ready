package com.testcontainers.catalog.domain.internal;

import java.util.Optional;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.elasticsearch.annotations.Query;

interface ProductRepository extends ElasticsearchRepository<ProductEntity, String> {
    Optional<ProductEntity> findByCode(String code);

    @Query("""
                    {
                             "script": {
                                 "source": "ctx._source.image = params.image",
                                 "lang": "painless",
                                 "params": {
                                     "image": ":#{#image}"
                                 }
                             },
                             "query": {
                                 "term": {
                                     "code": ":#{#code}"
                                 }
                             }
                         }
            """)
    void updateProductImage(@Param("code") String code, @Param("image") String image);
}
