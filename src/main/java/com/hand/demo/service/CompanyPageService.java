package com.hand.demo.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hand.demo.model.Dtos.page.GetSectionDTO;
import com.hand.demo.model.Dtos.page.NavigationLinkDTO;
import com.hand.demo.model.Dtos.page.PageUpdateDTO;
import com.hand.demo.model.Dtos.page.PageViewDTO;
import com.hand.demo.model.Dtos.page.SeoDTO;
import com.hand.demo.model.Dtos.page.SocialLinkDTO;
import com.hand.demo.model.Dtos.page.ThemeDTO;
import com.hand.demo.model.Dtos.page.view.GridProductsViewContent;
import com.hand.demo.model.Dtos.page.view.HeroViewContent;
import com.hand.demo.model.Dtos.page.view.ProductCardDTO;
import com.hand.demo.model.Dtos.page.view.PromoViewContent;
import com.hand.demo.model.entity.Company;
import com.hand.demo.model.entity.MediaItem;
import com.hand.demo.model.entity.page.CompanyPage;
import com.hand.demo.model.entity.page.NavigationLink;
import com.hand.demo.model.entity.page.PageSection;
import com.hand.demo.model.entity.page.PageStatus;
import com.hand.demo.model.entity.page.SectionType;
import com.hand.demo.model.entity.page.Seo;
import com.hand.demo.model.entity.page.SocialLink;
import com.hand.demo.model.entity.page.Theme;
import com.hand.demo.model.entity.page.content.GridProductsContent;
import com.hand.demo.model.entity.page.content.HeroContent;
import com.hand.demo.model.entity.page.content.PromoContent;
import com.hand.demo.model.entity.page.content.SectionContent;
import com.hand.demo.repository.CompanyPageRepository;
import com.hand.demo.repository.ProductRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyPageService {

    private final CompanyPageRepository pageRepo;
    private final ProductRepository productRepo;
    private final MediaService mediaService;

    @Transactional(readOnly = true)
    public PageViewDTO get(Long companyId, String slug) {
        var page = pageRepo.findByCompanyIdAndSlug(companyId, slug)
                .orElseThrow(() -> new EntityNotFoundException("Page not found"));
        return toView(page);
    }

    @Transactional(readOnly = true)
    public PageViewDTO getActivePage(Long companyId, String slug) {
        var page = pageRepo.findByCompanyIdAndSlugAndStatus(companyId, slug, PageStatus.PUBLISHED)
                .orElseThrow(() -> new EntityNotFoundException("Page not found"));
        return toView(page);
    }

    @Transactional
    public CompanyPage upsert(Long companyId, PageUpdateDTO dto) {
        var slug = (dto.slug() == null || dto.slug().isBlank()) ? "home" : dto.slug().trim();
        var page = pageRepo.findByCompanyIdAndSlug(companyId, slug)
                .orElseGet(() -> {
                    var p = new CompanyPage();
                    p.setCompany(new Company()); // Will be set properly below
                    p.getCompany().setId(companyId);
                    p.setSlug(slug);
                    return p;
                });

        if (dto.title() != null) {
            page.setTitle(dto.title());
        }
        if (dto.theme() != null) {
            applyTheme(page.getTheme(), dto.theme());
        }
        if (dto.seo() != null) {
            applySeo(page.getSeo(), dto.seo());
        }
        if (dto.navigation() != null) {
            page.getNavigation().clear();
            page.getNavigation().addAll(mapNav(dto.navigation()));
        }
        if (dto.social() != null) {
            page.getSocial().clear();
            page.getSocial().addAll(mapSocial(dto.social()));
        }
        page.setUpdatedAt(Instant.now());
        System.out.println(page.toString());
        return pageSave(page);
    }

    protected CompanyPage pageSave(CompanyPage page) {
        return pageRepo.save(page);
    }

    protected CompanyPage addSection(CompanyPage page, PageUpdateDTO dto, Long companyId) {
        if (page.getSections() != null) {
            page.getSections().clear();
        }
        if (dto.sections() != null) {
            int i = 0;
            for (var sdto : dto.sections()) {
                var sec = new PageSection();
                sec.setPage(page);
                var type = SectionType.valueOf(sdto.type());
                sec.setType(type);
                sec.setOrderIndex(sdto.orderIndex() != null ? sdto.orderIndex() : i++);

                if (!(sdto.content() instanceof SectionContent sectionContent)) {
                    throw new IllegalArgumentException("Invalid content type for section");
                }
                validateOwnership(type, companyId, sdto.content());
                sec.setContent(sdto.content());
                page.getSections().add(sec);
            }
            return pageSave(page);
        }
        return page;
    }

    // ===== Helpers =====
    private List<NavigationLink> mapNav(List<NavigationLinkDTO> src) {
        return src == null
                ? List.of()
                : src.stream().map(n -> {
                    var m = new NavigationLink();
                    m.setLabel(n.label());
                    m.setHref(n.href());
                    m.setOrderIndex(n.orderIndex());
                    return m;
                }).toList();
    }

    private List<SocialLink> mapSocial(List<SocialLinkDTO> src) {
        return src == null ? List.of() : src.stream().map(s -> {
            var m = new SocialLink();
            m.setPlatform(s.platform());
            m.setUrl(s.url());
            m.setShow(Boolean.TRUE.equals(s.show()));
            m.setOrderIndex(s.orderIndex());
            return m;
        }).toList();
    }

    private void applySeo(Seo s, SeoDTO d) {
        if (d == null) {
            return;
        }
        if (d.metaTitle() != null) {
            s.setMetaTitle(d.metaTitle());
        }
        if (d.metaDescription() != null) {
            s.setMetaDescription(d.metaDescription());
        }
        if (d.ogImageUrl() != null) {
            s.setOgImageUrl(d.ogImageUrl());
        }
    }

    private void validateOwnership(SectionType type, Long companyId, SectionContent content) {
        switch (type) {
            case HERO -> {
                var c = (HeroContent) content;
                if (c.imageMediaId() != null) {
                    var media = mediaService.findMediaById(c.imageMediaId());
                    if (media.getUserId() != null && !media.getUserId().equals(companyId)) {
                        throw new IllegalStateException("You don't own this image");
                    }
                }

            }
            case GRID_PRODUCTS -> {
                var c = (GridProductsContent) content;
                var found = productRepo.findAllByIdAndCompanyId(c.productIds(), companyId);
                if (found.size() != c.productIds().size()) {
                    throw new IllegalArgumentException("some products not found");
                }
            }
            case PROMO -> {
                /* لا شيء */ }
        }
    }

    private void ensurePublishable(CompanyPage page) {
        boolean hasHero = page.getSections().stream().anyMatch(s -> s.getType() == SectionType.HERO);
        if (!hasHero) {
            throw new IllegalStateException("At least one HERO section is required");
        }
        if (page.getTitle() == null || page.getTitle().isBlank()) {
            throw new IllegalStateException("Title is required");
        }

    }

    private GetSectionDTO toSectionDTO(PageSection s) {
        return switch (s.getType()) {
            case HERO -> {
                var c = (HeroContent) s.getContent();
                MediaItem media = new MediaItem();
                if (c.imageMediaId() != null) {
                    media = mediaService.findMediaById(c.imageMediaId());
                }
                String imageUrl = (media == null) ? null
                        : (media.getAbsoluteUrl() != null ? media.getAbsoluteUrl() : media.getPublicPath());

                var out = HeroViewContent.of(c.title(), c.subtitle(), imageUrl, c.ctaText(), c.ctaHref());
                yield new GetSectionDTO("HERO", s.getOrderIndex(), out);
            }
            case GRID_PRODUCTS -> {
                var c = (GridProductsContent) s.getContent();
                var list = productRepo.findCardsForCompanyByIdsOrdered(s.getPage().getCompany().getId(),
                        c.productIds());
                var items = list.stream()
                        .map(v -> new ProductCardDTO(v.getId(), v.getName(), v.getPrice(), v.getMainImageUrl()))
                        .toList();
                var out = GridProductsViewContent.of(c.title(), c.columns(), items);
                yield new GetSectionDTO("GRID_PRODUCTS", s.getOrderIndex(), out);
            }
            case PROMO -> {
                var c = (PromoContent) s.getContent();
                var out = PromoViewContent.of(c.title(), c.badge(), c.href());
                yield new GetSectionDTO("PROMO", s.getOrderIndex(), out);
            }
        };
    }

    @Transactional
    public PageViewDTO changePageStatus(Long companyId, String slug, PageStatus status) {
        var page = pageRepo.findByCompanyIdAndSlug(companyId, slug)
                .orElseThrow(() -> new EntityNotFoundException("Page not found"));
        ensurePublishable(page);
        page.setStatus(status);
        if (status == PageStatus.PUBLISHED) {
            page.setPublishedAt(Instant.now());
        }
        page = pageRepo.save(page);
        return toView(page);
    }

    public String listMyPages(Long companyId) {
        // TODO: Implement list pages functionality
        return "Page listing not implemented yet";
    }

    private void applyTheme(Theme t, ThemeDTO d) {
        if (d == null) {
            return;
        }

        if (d.logoUrl() != null) {
            t.setLogoUrl(d.logoUrl());
        }
        if (d.coverUrl() != null) {
            t.setCoverUrl(d.coverUrl());
        }
    }

    protected PageViewDTO toView(CompanyPage p) {
        ThemeDTO theme;
        if (p.getTheme() != null) {
            theme = new ThemeDTO(p.getTheme().getLogoUrl(), p.getTheme().getCoverUrl());
        } else {
            theme = new ThemeDTO("", "");
        }
        SeoDTO seo;
        if (p.getSeo() != null) {
            seo = new SeoDTO(p.getSeo().getMetaTitle(), p.getSeo().getMetaDescription(), p.getSeo().getOgImageUrl());
        } else {
            seo = new SeoDTO("", "", "");
        }
        var nav = p.getNavigation().stream()
                .sorted(Comparator.comparing(n -> Optional.ofNullable(n.getOrderIndex()).orElse(0)))
                .map(n -> new NavigationLinkDTO(n.getLabel(), n.getHref(), n.getOrderIndex()))
                .toList();
        var social = p.getSocial().stream()
                .sorted(Comparator.comparing(s -> Optional.ofNullable(s.getOrderIndex()).orElse(0)))
                .map(s -> new SocialLinkDTO(s.getPlatform(), s.getUrl(), s.getShow(), s.getOrderIndex()))
                .toList();
        var sections = p.getSections().stream()
                .sorted(Comparator.comparing(PageSection::getOrderIndex).thenComparing(PageSection::getId))
                .map(this::toSectionDTO).toList();

        return new PageViewDTO(p.getId(), p.getSlug(), p.getTitle(), p.getStatus().name(),
                p.getUpdatedAt(), p.getPublishedAt(), theme, seo, nav, social, sections);
    }
}
