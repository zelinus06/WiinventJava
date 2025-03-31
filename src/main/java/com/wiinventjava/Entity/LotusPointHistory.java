package com.wiinventjava.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity(name = "lotus_point_history")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LotusPointHistory implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne()
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_lotusPointHistory_user"))
    private Users user;

    @Column(nullable = false)
    private Integer points;

    @Column(nullable = false, length = 10)
    private String type;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(length = 255)
    private String note;

}
