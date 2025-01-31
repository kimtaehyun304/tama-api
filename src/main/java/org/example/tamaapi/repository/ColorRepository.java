package org.example.tamaapi.repository;

import org.example.tamaapi.domain.Category;
import org.example.tamaapi.domain.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ColorRepository extends JpaRepository<Color, Long> {

    Optional<Color> findByName(String name);

    List<Color> findAllByParentIsNull();


    //자식이 없는 것도 가져와야함 -> left join
    @Query("select c from Color c left join fetch c.children where c.id in :colorIds")
    List<Color> findWithChildrenByIdIn(List<Long> colorIds);

}
