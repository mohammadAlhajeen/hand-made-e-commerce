package com.hand.demo.model.entity.page;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.hand.demo.model.entity.page.content.GridProductsContent;
import com.hand.demo.model.entity.page.content.HeroContent;
import com.hand.demo.model.entity.page.content.PromoContent;
import com.hand.demo.model.entity.page.content.SectionContent;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "page_sections")
@Getter
@Setter
public class PageSection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private CompanyPage page;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private SectionType type;

    @Column(nullable = false)
    private Integer orderIndex = 0;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private SectionContent content;

    @AssertTrue(message = "content.kind must match section type")
    public boolean isTypeConsistent() {
        return content != null && type != null && switch (type) {
            case HERO -> content instanceof HeroContent;
            case GRID_PRODUCTS -> content instanceof GridProductsContent;
            case PROMO -> content instanceof PromoContent;
        };
    }
}
