package com.hand.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hand.demo.service.CompanyPageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/companyPages")
public class PublicCompanyPages {
      private final CompanyPageService companyPageService;

  @GetMapping("/{companyId}/{slug}")
    public ResponseEntity<?> getCompanyPage(@PathVariable Long companyId, @PathVariable String slug) {
        try {
            var page = companyPageService.getActivePage(companyId, slug);
            return ResponseEntity.ok().body(page);
        } catch (Exception e) {
            System.out.println(e);
            return ResponseEntity.notFound().build();
            // TODO: handle exception
        }

    }
}
