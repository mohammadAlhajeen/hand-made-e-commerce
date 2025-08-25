package com.hand.demo.model.entity.page;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.hand.demo.model.entity.Company;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "company_pages", uniqueConstraints = @UniqueConstraint(name = "uk_company_slug", columnNames = {
    "company_id", "slug"}))
@Getter
@Setter
@ToString
public class CompanyPage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Company company;

    @Column(nullable = false, length = 64)
    private String slug = "home";

    @Column(nullable = false, length = 120)
    private String title = "Welcome";

    @Embedded
    private Seo seo = new Seo();

    @ElementCollection
    @CollectionTable(name = "company_page_nav", joinColumns = @JoinColumn(name = "page_id"))
    private List<NavigationLink> navigation = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "company_page_social", joinColumns = @JoinColumn(name = "page_id"))
    private List<SocialLink> social = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private PageStatus status = PageStatus.DRAFT;
    @Embedded
    private Theme theme;
    @Version
    private Long version;

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    private Instant publishedAt;

    @OneToMany(mappedBy = "page", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex asc, id asc")
    private List<PageSection> sections = new ArrayList<>();

}
