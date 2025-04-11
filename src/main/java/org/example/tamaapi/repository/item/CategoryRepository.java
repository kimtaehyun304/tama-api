package org.example.tamaapi.repository.item;

import org.example.tamaapi.domain.item.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("select c from Category c join fetch c.children where c.parent is null")
    List<Category> findAllWithChildrenAllByParentIsNull();

    //자식이 없는 것도 가져와야함 -> left join
    @Query("select c from Category c left join fetch c.children where c.id = :categoryId")
    Optional<Category> findWithChildrenById(Long categoryId);
    Optional<Category> findByName(String name);

    List<Category> findAllByParentIsNull();


}
