package com.example.bookmarking.controller;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.example.bookmarking.entity.Bookmark;
import com.example.bookmarking.entity.User;
import com.example.bookmarking.repository.BookmarkRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class BookmarkController {

    private final BookmarkRepository bookmarkRepository;

    public BookmarkController(BookmarkRepository bookmarkRepository) {
        this.bookmarkRepository = bookmarkRepository;
    }

    @GetMapping("/bookmarks")
    public String bookmarks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String q,
            HttpSession session,
            Model model) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        Pageable pageable = PageRequest.of(page, 3, Sort.by("createdAt").descending());
        Page<Bookmark> bookmarks;

        if (q != null && !q.isBlank()) {
            bookmarks = bookmarkRepository
                    .findByUserAndTitleContainingIgnoreCaseOrUserAndUrlContainingIgnoreCase(
                            user, q, user, q, pageable);
            model.addAttribute("q", q);
        } else {
            bookmarks = bookmarkRepository.findByUser(user, pageable);
        }

        model.addAttribute("bookmarks", bookmarks);
        return "bookmarks";
    }

    @PostMapping("/bookmarks/add")
    public String addBookmark(
            @RequestParam String title,
            @RequestParam String url,
            HttpSession session,
            RedirectAttributes ra) {

        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        if (bookmarkRepository.countByUser(user) >= 5) {
            ra.addFlashAttribute("limitExceeded", true);
            return "redirect:/bookmarks";
        }

        bookmarkRepository.save(new Bookmark(title, url, user));
        return "redirect:/bookmarks";
    }

    @PostMapping("/bookmarks/delete/{id}")
    public String deleteBookmark(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        bookmarkRepository.deleteById(id);
        return "redirect:/bookmarks";
    }

    @GetMapping("/bookmarks/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, HttpSession session) {
        if (session.getAttribute("loggedInUser") == null)
            return "redirect:/login";

        model.addAttribute("bookmark", bookmarkRepository.findById(id).orElseThrow());
        return "edit-bookmark";
    }

    @PostMapping("/bookmarks/edit")
    public String editBookmark(@RequestParam Long id,
                               @RequestParam String title,
                               @RequestParam String url,
                               HttpSession session) {

        if (session.getAttribute("loggedInUser") == null)
            return "redirect:/login";

        Bookmark b = bookmarkRepository.findById(id).orElseThrow();
        b.setTitle(title);
        b.setUrl(url);
        bookmarkRepository.save(b);

        return "redirect:/bookmarks";
    }
}
