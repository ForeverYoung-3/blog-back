package com.blog.back.service;

import com.blog.back.entity.Post;
import com.blog.back.enums.PostStatus;
import com.blog.back.mapper.PostMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 文章 PDF 备份服务
 * <p>
 * 使用 CommonMark 将 Markdown 转为 HTML，再通过 Flying Saucer（OpenPDF）
 * 渲染成 PDF，每篇文章独立输出一个文件。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostBackupService {

    private final PostMapper postMapper;

    @Value("${backup.pdf-dir:/data/blog-backup/pdf}")
    private String pdfBaseDir;

    // CommonMark 解析器（复用实例，线程安全）
    private static final Parser MD_PARSER = Parser.builder()
            .extensions(List.of(TablesExtension.create(), HeadingAnchorExtension.create()))
            .build();

    private static final HtmlRenderer HTML_RENDERER = HtmlRenderer.builder()
            .extensions(List.of(TablesExtension.create(), HeadingAnchorExtension.create()))
            .build();

    /**
     * 备份所有已发布文章，每篇生成一个 PDF 文件。
     * 文件保存在 {@code {pdfBaseDir}/{yyyy-MM-dd}/{postId}-{slug}.pdf}。
     *
     * @return 成功生成的 PDF 数量
     */
    public int backupAllPublishedPosts() {
        List<Post> posts = postMapper.findByStatus(PostStatus.PUBLISHED);
        if (posts == null || posts.isEmpty()) {
            log.info("[PDF备份] 没有已发布的文章，跳过。");
            return 0;
        }

        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        Path dirPath = Paths.get(pdfBaseDir, today);
        try {
            Files.createDirectories(dirPath);
        } catch (IOException e) {
            log.error("[PDF备份] 无法创建备份目录：{}", dirPath, e);
            return 0;
        }

        int successCount = 0;
        for (Post post : posts) {
            try {
                generatePdf(post, dirPath);
                successCount++;
            } catch (Exception e) {
                log.error("[PDF备份] 文章 id={} title={} 生成 PDF 失败", post.getId(), post.getTitle(), e);
            }
        }

        log.info("[PDF备份] 完成，共 {} 篇，成功 {} 篇，目录：{}", posts.size(), successCount, dirPath);
        return successCount;
    }

    // ------------------------------------------------------------------ //

    private void generatePdf(Post post, Path dir) throws Exception {
        String safeSlug = sanitizeFilename(post.getSlug() != null ? post.getSlug() : String.valueOf(post.getId()));
        String filename = post.getId() + "-" + safeSlug + ".pdf";
        Path outputPath = dir.resolve(filename);

        String xhtml = buildXhtml(post);

        try (OutputStream os = new FileOutputStream(outputPath.toFile())) {
            ITextRenderer renderer = new ITextRenderer();
            // 注册系统中文字体（Linux/Docker 部署时请确保安装了字体，例如 fonts-wqy-zenhei）
            registerSystemFonts(renderer);
            renderer.setDocumentFromString(xhtml);
            renderer.layout();
            renderer.createPDF(os);
        }

        log.debug("[PDF备份] 已生成：{}", outputPath);
    }

    /**
     * 将文章组装为合法的 XHTML 字符串，供 Flying Saucer 渲染。
     */
    private String buildXhtml(Post post) {
        String bodyHtml = markdownToHtml(post.getContent() == null ? "" : post.getContent());
        String title = escapeHtml(post.getTitle() == null ? "无标题" : post.getTitle());

        String author = post.getAuthorName() != null ? escapeHtml(post.getAuthorName()) : "未知作者";
        String publishedAt = post.getPublishedAt() != null
                ? post.getPublishedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                : "—";

        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
                    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
                <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="zh" lang="zh">
                <head>
                    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
                    <title>%s</title>
                    <style type="text/css">
                        @page { size: A4; margin: 2cm 2.5cm; }
                        body {
                            font-family: "WenQuanYi Zen Hei", "Noto Sans CJK SC", "SimHei", Arial, sans-serif;
                            font-size: 13px;
                            line-height: 1.8;
                            color: #333;
                        }
                        h1.post-title {
                            font-size: 22px;
                            font-weight: bold;
                            margin-bottom: 4px;
                        }
                        .post-meta {
                            font-size: 11px;
                            color: #888;
                            margin-bottom: 16px;
                            border-bottom: 1px solid #ddd;
                            padding-bottom: 8px;
                        }
                        h2, h3, h4 { margin-top: 1em; }
                        pre {
                            background: #f5f5f5;
                            padding: 10px;
                            font-size: 11px;
                            overflow: hidden;
                            white-space: pre-wrap;
                        }
                        code { font-size: 12px; background: #f5f5f5; padding: 1px 4px; }
                        blockquote {
                            border-left: 3px solid #ccc;
                            margin-left: 0;
                            padding-left: 12px;
                            color: #666;
                        }
                        table { border-collapse: collapse; width: 100%%; }
                        th, td { border: 1px solid #ccc; padding: 4px 8px; }
                        img { max-width: 100%%; }
                        a { color: #1a73e8; text-decoration: none; }
                    </style>
                </head>
                <body>
                    <h1 class="post-title">%s</h1>
                    <div class="post-meta">作者：%s &nbsp;|&nbsp; 发布时间：%s</div>
                    %s
                </body>
                </html>
                """.formatted(title, title, author, publishedAt, bodyHtml);
    }

    /**
     * CommonMark Markdown → HTML。
     */
    private String markdownToHtml(String markdown) {
        Node document = MD_PARSER.parse(markdown);
        return HTML_RENDERER.render(document);
    }

    /**
     * 尝试注册系统字体目录，让 Flying Saucer 能找到中文字体。
     * 常见中文字体路径：Linux(/usr/share/fonts)、macOS(/Library/Fonts, ~/Library/Fonts)。
     */
    private void registerSystemFonts(ITextRenderer renderer) {
        String[] fontDirs = {
                "/usr/share/fonts",
                "/usr/local/share/fonts",
                System.getProperty("user.home") + "/Library/Fonts",
                "/Library/Fonts",
                "/System/Library/Fonts"
        };
        for (String dir : fontDirs) {
            try {
                Path p = Paths.get(dir);
                if (Files.isDirectory(p)) {
                    renderer.getFontResolver().addFontDirectory(dir, true);
                }
            } catch (Exception e) {
                log.debug("[PDF备份] 注册字体目录失败（可忽略）：{}", dir);
            }
        }
    }

    /** 过滤掉文件名中不合法的字符。 */
    private String sanitizeFilename(String name) {
        return name.replaceAll("[^a-zA-Z0-9\\-_]", "_");
    }

    /** 简单转义 HTML 实体，避免 XHTML 解析出错。 */
    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
