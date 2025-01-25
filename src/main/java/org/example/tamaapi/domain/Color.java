package org.example.tamaapi.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minidev.json.annotate.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Color {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "color_id")
    Long id;

    @Column(nullable = false)
    String name;

    //홈페이지 관리자가 추가하면 바로 반영해야해서 필요 
    //색상 예시 렌더링할때 필요
    @Column(nullable = false)
    String hexCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    Color parent;

    @OneToMany(mappedBy = "parent")
    List<Color> children = new ArrayList<>();

    @Builder
    public Color(String name, String hexCode, Color parent) {
        this.name = name;
        this.hexCode = hexCode;
        this.parent = parent;
    }
}
