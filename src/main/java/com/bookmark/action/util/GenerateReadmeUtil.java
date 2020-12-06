package com.bookmark.action.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.bookmark.action.dto.BookmarkNode;
import lombok.extern.slf4j.Slf4j;
import net.steppschuh.markdowngenerator.link.Link;
import net.steppschuh.markdowngenerator.list.UnorderedList;
import net.steppschuh.markdowngenerator.text.heading.Heading;

import java.io.File;
import java.util.List;

/**
 * README文件生成工具类
 *
 * @author leeyom wang
 * @date 2020/12/4 上午11:46
 */
@Slf4j
public class GenerateReadmeUtil {

    private static final String TOP = "1";
    private static final String BR = "\n";
    private static final String DEFAULT_HEADER = "常用";
    private static final String NULL_DIR = "新建文件夹";
    private static final int MAX_NUM = 10;

    /**
     * github action 中取的是相对地址，本地测试要用绝对地址，比如：/Users/leeyom/workspace/github/chrome-bookmarks-sync/bookmark.json
     * README.md 也是同理
     */
    private static final String BOOKMARK_JSON_PATH = "bookmark.json";
    private static final String README_PATH = "README.md";

    /**
     * 解析bookmark.json，生成README.md
     */
    public static void generateReadme() {
        log.info("========================================= 开始生成README.md =========================================");
        File bookmarkJson = new File(BOOKMARK_JSON_PATH);
        File readmeMd = new File(README_PATH);
        String errorMsg;

        if (!FileUtil.exist(readmeMd)) {
            readmeMd = FileUtil.newFile(README_PATH);
        }

        FileWriter readmeMdWriter = new FileWriter(readmeMd);
        if (!FileUtil.exist(bookmarkJson)) {
            errorMsg = "README.md 生成失败，书签数据不存在，请确保您已上传书签数据 bookmark.json 到仓库";
            log.error(errorMsg);
            readmeMdWriter.write(errorMsg);
            return;
        }

        FileReader fileReader = new FileReader(bookmarkJson);
        String bookmarkJsonData = fileReader.readString();
        List<BookmarkNode> bookmarksNodeList;
        try {
            bookmarksNodeList = JSONUtil.toList(JSONUtil.parseArray(bookmarkJsonData), BookmarkNode.class);
            if (CollUtil.isEmpty(bookmarksNodeList)) {
                errorMsg = "README.md 生成失败，书签数据为空";
                log.error(errorMsg);
                readmeMdWriter.write(errorMsg);
                return;
            }
        } catch (Exception e) {
            log.error("README.md 生成失败，书签数据解析失败：", e);
            return;
        }

        // 顶层为：书签栏类目，取该类目的子节点
        List<BookmarkNode> list = bookmarksNodeList.get(0).getChildren();
        if (CollUtil.isEmpty(list)) {
            errorMsg = "README.md 生成失败，书签数据为空";
            log.error(errorMsg);
            readmeMdWriter.write(errorMsg);
            return;
        }

        StrBuilder content = new StrBuilder();
        StrBuilder topHeader = new StrBuilder().append(new Heading(bookmarksNodeList.get(0).getTitle(), 1)).append(BR);
        content.append(topHeader).append(BR);

        StrBuilder defaultHeader = new StrBuilder().append(new Heading(DEFAULT_HEADER, 2)).append(BR);
        List<Link> oftenUseLinkList = CollUtil.newArrayList();
        StrBuilder otherBookmarkData = new StrBuilder();
        list.forEach(bookmarkNode -> {

            // 收集放在书签栏，经常使用的书签
            if (TOP.equals(bookmarkNode.getParentId())
                    && CollUtil.isEmpty(bookmarkNode.getChildren())
                    && StrUtil.isNotBlank(bookmarkNode.getTitle())
                    && StrUtil.isNotBlank(bookmarkNode.getUrl())) {
                oftenUseLinkList.add(new Link(bookmarkNode.getTitle(), bookmarkNode.getUrl()));
                return;
            }
            appendHeader(otherBookmarkData, bookmarkNode, 2);
        });

        if (CollUtil.isNotEmpty(oftenUseLinkList)) {
            if (oftenUseLinkList.size() > MAX_NUM) {
                displayMore(defaultHeader, oftenUseLinkList);
            } else {
                defaultHeader.append(new UnorderedList<>(oftenUseLinkList)).append(BR);
            }
            content.append(defaultHeader).append(BR);
        }

        content.append(otherBookmarkData);
        readmeMdWriter.write(content.toString());
        log.info("========================================= README.md 生成成功！ =========================================");
    }

    private static void appendHeader(StrBuilder otherBookmarkData, BookmarkNode bookmarkNode, int level) {
        log.info("========================================= 努力生成中 =========================================");
        StrBuilder header = new StrBuilder().append(new Heading(StrUtil.blankToDefault(bookmarkNode.getTitle(), NULL_DIR), level)).append(BR);
        if (CollUtil.isNotEmpty(bookmarkNode.getChildren())) {
            List<BookmarkNode> children = bookmarkNode.getChildren();
            List<Link> linkList = CollUtil.newArrayList();
            List<StrBuilder> childDir = CollUtil.newArrayList();
            children.forEach(childNode -> {
                if (CollUtil.isEmpty(childNode.getChildren())
                        && StrUtil.isNotBlank(childNode.getTitle())
                        && StrUtil.isNotBlank(childNode.getUrl())) {
                    linkList.add(new Link(childNode.getTitle(), childNode.getUrl()));
                    return;
                }
                // 递归
                int finalLevel = level + 1;
                StrBuilder stageBookmarkData = new StrBuilder();
                appendHeader(stageBookmarkData, childNode, finalLevel);
                if (StrUtil.isNotBlank(stageBookmarkData)) {
                    childDir.add(stageBookmarkData);
                }
            });

            if (linkList.size() > MAX_NUM) {
                displayMore(header, linkList);
            } else {
                header.append(new UnorderedList<>(linkList)).append(BR);
            }
            childDir.forEach(child -> header.append(BR).append(child));
        }
        otherBookmarkData.append(header).append(BR);
    }

    private static void displayMore(StrBuilder header, List<Link> linkList) {
        header.append(new UnorderedList<>(ListUtil.sub(linkList, 0, MAX_NUM))).append(BR);
        header.append("<details><summary>显示更多</summary>").append(BR).append(BR);
        header.append(new UnorderedList<>(ListUtil.sub(linkList, MAX_NUM, linkList.size()))).append(BR);
        header.append("</details>").append(BR);
    }

}
